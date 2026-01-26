FROM python:3.9-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# Using port 8085 as requested
EXPOSE 8085
CMD ["python3", "server.py"]
