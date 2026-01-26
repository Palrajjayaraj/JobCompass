FROM python:3.9-slim

ENV PYTHONUNBUFFERED=1

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Install openssl for generating self-signed certificates
RUN apt-get update && apt-get install -y openssl && rm -rf /var/lib/apt/lists/*

# Generate self-signed certificate (valid for 10 years)
RUN openssl req -newkey rsa:2048 -new -nodes -x509 -days 3650 \
    -keyout key.pem -out cert.pem \
    -subj "/C=US/ST=State/L=City/O=JobCompass/CN=localhost"

COPY . .

# Using port 8085 as requested
EXPOSE 8085
CMD ["python3", "server.py"]
