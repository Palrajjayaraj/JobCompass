#!/bin/bash
###############################################################################
# JobCompass Deployment Script
# This script automates the build, test, and deployment process for the
# JobCompass application.
#
# Author: Palraj Jayaraj
###############################################################################

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}"
echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║         JobCompass CI/CD Deployment Pipeline                 ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo -e "${NC}"

# Parse arguments
SKIP_BUILD=false
if [ "$1" == "--skip-build" ]; then
    SKIP_BUILD=true
    echo -e "${YELLOW}⚠️  Skipping Maven build (using existing JARs)${NC}"
fi

###############################################################################
# Pre-flight checks
###############################################################################
echo -e "${YELLOW}[0/4] Running pre-flight checks...${NC}"

# Check Java version for Maven
JAVA_VERSION=$(mvn -version 2>&1 | grep "Java version" | awk '{print $3}' | cut -d',' -f1)
REQUIRED_JAVA_VERSION="21"

if [ -n "$JAVA_VERSION" ] && [ "$JAVA_VERSION" != "$REQUIRED_JAVA_VERSION" ]; then
    echo -e "${YELLOW}⚠️  Warning: Maven is using Java $JAVA_VERSION, but project requires Java $REQUIRED_JAVA_VERSION${NC}"
    echo -e "${YELLOW}   This may cause compilation issues.${NC}"
    echo ""
    echo -e "${BLUE}💡 To fix this:${NC}"
    echo "   1. Install Java 21: brew install openjdk@21"
    echo "   2. Set JAVA_HOME: export JAVA_HOME=\$(/usr/libexec/java_home -v 21)"
    echo "   3. Re-run this script"
    echo ""
    echo -e "${YELLOW}   OR run with --skip-build to use existing JARs:${NC}"
    echo "   ./deploy.sh --skip-build"
    echo ""
    read -p "Continue anyway? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

###############################################################################
# Step 1: Clean and Build with Maven
###############################################################################
if [ "$SKIP_BUILD" = false ]; then
    echo -e "${YELLOW}[1/4] Cleaning and building Maven project...${NC}"
    mvn clean install

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Maven build successful!${NC}"
    else
        echo -e "${RED}❌ Maven build failed!${NC}"
        echo -e "${YELLOW}💡 If you have Java version issues, try:${NC}"
        echo "   ./deploy.sh --skip-build"
        exit 1
    fi
else
    echo -e "${YELLOW}[1/4] Skipping Maven build (--skip-build flag)${NC}"
fi

###############################################################################
# Step 2: Stop and Remove Existing Containers
###############################################################################
echo -e "${YELLOW}[2/4] Stopping and removing existing Docker containers...${NC}"
docker-compose down

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Containers stopped and removed!${NC}"
else
    echo -e "${RED}⚠️  Warning: Failed to stop containers (they may not be running)${NC}"
fi

###############################################################################
# Step 3: Build Docker Images
###############################################################################
echo -e "${YELLOW}[3/4] Building Docker images...${NC}"
docker-compose build

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Docker images built successfully!${NC}"
else
    echo -e "${RED}❌ Docker build failed!${NC}"
    exit 1
fi

###############################################################################
# Step 4: Start Services in Detached Mode
###############################################################################
echo -e "${YELLOW}[4/4] Starting services...${NC}"
docker-compose up -d

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Services started successfully!${NC}"
else
    echo -e "${RED}❌ Failed to start services!${NC}"
    exit 1
fi

###############################################################################
# Deployment Summary
###############################################################################
echo -e "${BLUE}"
echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║                  Deployment Successful! 🚀                    ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo -e "${NC}"

echo -e "${GREEN}📡 Services are now running:${NC}"
echo -e "   • Web UI (HTTP):    http://localhost:8085"
echo -e "   • Web UI (HTTPS):   https://localhost:8443"
echo -e "   • Storage Service:  http://localhost:8081"
echo -e "   • Scraper Service:  http://localhost:8082"
echo ""
echo -e "${YELLOW}💡 Open http://localhost:8085 in any browser (Safari, Chrome, Firefox)${NC}"
echo -e "${YELLOW}   For phone access use https://<your-ip>:8443${NC}"
echo -e "${YELLOW}   For Chrome/Firefox: Either URL works${NC}"
echo ""
echo -e "${BLUE}📋 Useful Commands:${NC}"
echo -e "   • View logs:        docker-compose logs -f"
echo -e "   • View service:     docker-compose logs -f <service-name>"
echo -e "   • Stop services:    docker-compose down"
echo -e "   • Restart service:  docker-compose restart <service-name>"
echo ""

