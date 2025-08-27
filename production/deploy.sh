#!/bin/bash
set -euo pipefail

# Void Shrine Production Deployment Script
# Usage: ./production/deploy.sh

echo "🌀 Initiating Void Shrine deployment..."

# Configuration
PROJECT_DIR="/var/www/void-shrine.dissemblage.art"
SERVICE_NAME="void-shrine"
NGINX_CONFIG="/etc/nginx/sites-available/void-shrine.dissemblage.art"
SYSTEMD_SERVICE="/etc/systemd/system/${SERVICE_NAME}.service"

# Check if running as root for system configuration
if [ "$EUID" -ne 0 ]; then
    echo "⚠️  Some operations require sudo privileges"
    SUDO="sudo"
else
    SUDO=""
fi

# Update system packages
echo "📦 Updating system packages..."
$SUDO apt-get update && $SUDO apt-get upgrade -y

# Install required packages if not present
echo "🔧 Installing required packages..."
$SUDO apt-get install -y openjdk-17-jdk nginx curl wget

# Install Clojure CLI if not present
if ! command -v clj &> /dev/null; then
    echo "🔧 Installing Clojure CLI..."
    curl -O https://download.clojure.org/install/linux-install-1.11.1.1273.sh
    chmod +x linux-install-1.11.1.1273.sh
    $SUDO ./linux-install-1.11.1.1273.sh
    rm linux-install-1.11.1.1273.sh
fi

# Set up project directory permissions
echo "🔐 Setting up project permissions..."
$SUDO chown -R uprootiny:uprootiny "$PROJECT_DIR"
chmod +x "$PROJECT_DIR/production/deploy.sh"

# Install project dependencies
echo "📚 Installing Clojure dependencies..."
cd "$PROJECT_DIR"
clj -P -M:dev:test

# Run tests to ensure everything works
echo "🧪 Running test suite..."
clj -M:test
if [ $? -ne 0 ]; then
    echo "❌ Tests failed! Aborting deployment."
    exit 1
fi
echo "✅ All tests passed!"

# Stop existing service if running
echo "⏹️  Stopping existing service..."
$SUDO systemctl stop "$SERVICE_NAME" 2>/dev/null || true

# Install systemd service
echo "⚙️  Installing systemd service..."
$SUDO cp "$PROJECT_DIR/production/${SERVICE_NAME}.service" "$SYSTEMD_SERVICE"
$SUDO systemctl daemon-reload
$SUDO systemctl enable "$SERVICE_NAME"

# Configure nginx
echo "🌐 Configuring nginx..."
$SUDO cp "$PROJECT_DIR/production/nginx.conf" "$NGINX_CONFIG"
$SUDO ln -sf "$NGINX_CONFIG" /etc/nginx/sites-enabled/
$SUDO nginx -t

# Start services
echo "🚀 Starting services..."
$SUDO systemctl start "$SERVICE_NAME"
$SUDO systemctl reload nginx

# Wait for service to be ready
echo "⏳ Waiting for Void Shrine to initialize..."
sleep 10

# Health check
echo "🏥 Performing health check..."
if curl -f http://localhost:3000/api/state > /dev/null 2>&1; then
    echo "✅ Void Shrine is responding on port 3000"
else
    echo "❌ Health check failed!"
    $SUDO journalctl -u "$SERVICE_NAME" --lines=20
    exit 1
fi

# SSL Certificate setup (commented out - requires domain configuration)
# echo "🔒 Setting up SSL certificate..."
# $SUDO certbot --nginx -d void-shrine.dissemblage.art --non-interactive --agree-tos -m admin@dissemblage.art

# Final status check
echo "📊 Service status:"
$SUDO systemctl status "$SERVICE_NAME" --no-pager -l

echo ""
echo "🎉 Void Shrine deployment complete!"
echo "🌍 Local: http://localhost:3000"
echo "🌍 Production: https://void-shrine.dissemblage.art"
echo ""
echo "📝 Useful commands:"
echo "  View logs: sudo journalctl -u $SERVICE_NAME -f"
echo "  Restart: sudo systemctl restart $SERVICE_NAME"
echo "  Stop: sudo systemctl stop $SERVICE_NAME"
echo ""
echo "🌀 The void awaits your entropy..."