#!/usr/bin/env python3
"""
Simple HTTP server for JobCompass Web UI
Serves the web-ui directory on port 8000
"""

import http.server
import socketserver
import os

PORT = 8000
DIRECTORY = os.path.dirname(os.path.abspath(__file__))

class MyHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIRECTORY, **kwargs)
    
    def end_headers(self):
        # Add CORS headers to allow API requests
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type')
        super().end_headers()

if __name__ == '__main__':
    with socketserver.TCPServer(("", PORT), MyHTTPRequestHandler) as httpd:
        print(f"🚀 JobCompass Web UI Server")
        print(f"📡 Server running at: http://localhost:{PORT}")
        print(f"📂 Serving directory: {DIRECTORY}")
        print(f"🌐 Open in browser: http://localhost:{PORT}/index.html")
        print(f"\n💡 Make sure storage-service is running on port 8082")
        print(f"   Check: http://localhost:8082/api/jobs\n")
        print("Press Ctrl+C to stop the server\n")
        
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\n\n👋 Server stopped")
