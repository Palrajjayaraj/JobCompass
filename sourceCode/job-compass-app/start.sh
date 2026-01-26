#!/bin/bash

# JobCompass - Startup Script
# This script starts all required services for the JobCompass application

# Set paths
export DOCKER_PATH="/Applications/Development/Docker.app/Contents/Resources/bin"
export JAVA_HOME="/opt/homebrew/opt/openjdk@21"
export PATH="$DOCKER_PATH:$JAVA_HOME/bin:$PATH"

echo "🚀 Starting JobCompass LinkedIn Scraper..."
echo ""

# Check if Docker Desktop is running
if ! $DOCKER_PATH/docker info > /dev/null 2>&1; then
    echo "⚠️  Docker Desktop is not running!"
    echo "Please start Docker Desktop and run this script again."
    echo "Opening Docker Desktop..."
    open -a "/Applications/Development/Docker.app"
    echo "Waiting for Docker to start (30 seconds)..."
    sleep 30
fi

# Start Kafka and Zookeeper
echo "📦 Starting Kafka and Zookeeper containers..."
cd "$(dirname "$0")"
$DOCKER_PATH/docker compose up -d

echo ""
echo "⏳ Waiting for Kafka to be ready (15 seconds)..."
sleep 15

# Start the scraper service
echo ""
echo "🕷️  Starting Scraper Service..."
cd scraper-service
mvn spring-boot:run &
SCRAPER_PID=$!

echo ""
echo "✅ Services Starting!"
echo ""
echo "📋 Service URLs:"
echo "   - Scraper Service: http://localhost:8082"
echo "   - Health Check: http://localhost:8082/api/scraper/health"
echo "   - Kafka: localhost:9092"
echo ""
echo "🧪 Test the scraper:"
echo "   curl -X POST \"http://localhost:8082/api/scraper/trigger/linkedin?maxResults=3&skill=Java\""
echo ""
echo "🛑 To stop all services:"
echo "   1. Press Ctrl+C to stop scraper"
echo "   2. Run: docker compose down"
echo ""
echo "📝 Logs will appear below..."
echo "================================================"
echo ""

# Wait for scraper process
wait $SCRAPER_PID
