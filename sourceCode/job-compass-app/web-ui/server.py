#!/usr/bin/env python3
"""
Simple HTTP server for JobCompass Web UI
Serves the web-ui directory on port 8000
"""

import http.server
import socketserver
import os
import json

import ssl

PORT = 8085
DIRECTORY = os.path.dirname(os.path.abspath(__file__))

class MyHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIRECTORY, **kwargs)
    
    def do_GET(self):
        # Proxy API requests to backend services
        if self.path.startswith('/api/jobs'):
            self.proxy_to_storage('GET')
        else:
            super().do_GET()
    
    def do_OPTIONS(self):
        # Handle CORS preflight requests
        self.send_response(200)
        self.end_headers()
    
    def do_POST(self):
        # Route to appropriate backend service
        if self.path.startswith('/api/trigger-scrape'):
            self.proxy_trigger_scrape()
        elif self.path.startswith('/api/'):
            self.proxy_to_storage('POST')
        else:
            self.send_error(404, "Not Found")
    
    def proxy_trigger_scrape(self):
        """Proxy scrape trigger to scraper-service"""
        import urllib.request
        import urllib.error
        import json
        from urllib.parse import urlparse, parse_qs
        
        # Get backend service URL from env (Docker service name or localhost)
        scraper_url = os.getenv('SCRAPER_SERVICE_URL', 'http://scraper-service:8082')
        
        # Parse query string if present
        parsed = urlparse(self.path)
        query_params = parse_qs(parsed.query)
        
        # Read request body if present
        content_length = self.headers.get('Content-Length')
        request_body = {}
        if content_length:
            body_data = self.rfile.read(int(content_length)).decode('utf-8')
            if body_data:
                request_body = json.loads(body_data)
        
        # Build scrape request (prefer body over query params)
        skills = request_body.get('skills', 
                                 query_params.get('skills', [''])[0].split(',') if query_params.get('skills') else [])
        
        # Clean up skills list
        skills = [s.strip() for s in skills if s.strip()]
        
        scrape_request = {
            "skills": skills,
            "location": request_body.get('location', query_params.get('location', [None])[0]),
            "maxJobAgeDays": request_body.get('maxJobAgeDays', 1),
            "maxResults": request_body.get('maxResults', 10)
        }
        
        # Target URL (new multi-skill endpoint)
        target_url = f'{scraper_url}/api/scraper/trigger/multi-skill'
        
        try:
            print(f"[PROXY] Scrape request to: {target_url}")
            print(f"[PROXY] Payload: {scrape_request}")
            
            json_data = json.dumps(scrape_request).encode('utf-8')
            req = urllib.request.Request(
                target_url, 
                data=json_data,
                headers={'Content-Type': 'application/json'},
                method='POST'
            )
            
            with urllib.request.urlopen(req) as response:
                self.send_response(response.status)
                for header, value in response.getheaders():
                    self.send_header(header, value)
                self.end_headers()
                self.wfile.write(response.read())
        except urllib.error.HTTPError as e:
            print(f"[PROXY] HTTP Error: {e.code}")
            self.send_response(e.code)
            self.end_headers()
        except Exception as e:
            print(f"[PROXY] Error: {e}")
            self.send_response(500)
            self.end_headers()
    
    def proxy_to_storage(self, method):
        """Proxy job data requests to storage-service"""
        import urllib.request
        import urllib.error
        
        # Get backend service URL from env (Docker service name or localhost)
        storage_url = os.getenv('STORAGE_SERVICE_URL', 'http://storage-service:8081')
        
        target_url = f'{storage_url}{self.path}'
        
        try:
            print(f"[PROXY] {method} request to: {target_url}")
            
            if method == 'GET':
                req = urllib.request.Request(target_url, method='GET')
            else:  # POST
                content_length = self.headers.get('Content-Length')
                body = self.rfile.read(int(content_length)) if content_length else b''
                req = urllib.request.Request(target_url, data=body, method='POST')
                if content_length:
                    req.add_header('Content-Type', self.headers.get('Content-Type', 'application/json'))
            
            with urllib.request.urlopen(req, timeout=30) as response:
                self.send_response(response.status)
                for header, value in response.getheaders():
                    # Skip transfer-encoding to avoid chunking issues
                    if header.lower() != 'transfer-encoding':
                        self.send_header(header, value)
                self.end_headers()
                self.wfile.write(response.read())
        except urllib.error.HTTPError as e:
            print(f"[PROXY] HTTP Error: {e.code}")
            self.send_response(e.code)
            self.send_header('Content-Type', 'application/json')
            self.end_headers()
            error_msg = json.dumps({"error": f"Backend error: {e.code}"}).encode()
            self.wfile.write(error_msg)
        except Exception as e:
            print(f"[PROXY] Error: {e}")
            self.send_response(500)
            self.send_header('Content-Type', 'application/json')
            self.end_headers()
            error_msg = json.dumps({"error": str(e)}).encode()
            self.wfile.write(error_msg)

    def end_headers(self):
        # Add CORS headers to allow API requests
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type')
        super().end_headers()


if __name__ == '__main__':
    # Create SSL context
    context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
    # These files will be generated by the Dockerfile
    context.load_cert_chain(certfile='/app/cert.pem', keyfile='/app/key.pem')

    class ThreadingSimpleServer(socketserver.ThreadingTCPServer):
        pass

    with ThreadingSimpleServer(("", PORT), MyHTTPRequestHandler) as httpd:
        httpd.socket = context.wrap_socket(httpd.socket, server_side=True)
        
        print(f"🚀 JobCompass Web UI Server (HTTPS Enabled)")
        print(f"📡 Server running at: https://localhost:{PORT}")
        print(f"📂 Serving directory: {DIRECTORY}")
        print(f"🌐 Open in browser: https://localhost:{PORT}/index.html")
        print(f"\n💡 Note: You will sec a security warning because we use a self-signed certificate.")
        print(f"   Click 'Advanced' -> 'Proceed' to access the site.\n")
        print("Press Ctrl+C to stop the server\n")
        
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\n\n👋 Server stopped")
