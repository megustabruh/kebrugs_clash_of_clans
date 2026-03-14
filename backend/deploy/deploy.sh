#!/bin/bash

# Clash of Clans Backend - EC2 Deployment Script
# Run this script on your EC2 instance

set -e

APP_NAME="coc-backend"
APP_DIR="/opt/$APP_NAME"
JAR_NAME="clash-of-clans-backend-1.0.0.jar"

echo "=== Installing dependencies ==="
sudo yum update -y || sudo apt-get update -y
sudo yum install -y java-17-amazon-corretto || sudo apt-get install -y openjdk-17-jdk

echo "=== Creating application directory ==="
sudo mkdir -p $APP_DIR
sudo chown $USER:$USER $APP_DIR

echo "=== Copying JAR file ==="
cp target/$JAR_NAME $APP_DIR/

echo "=== Creating environment file ==="
if [ ! -f $APP_DIR/.env ]; then
    cat > $APP_DIR/.env << 'EOF'
# Database Configuration
DB_URL=jdbc:mysql://YOUR_RDS_ENDPOINT:3306/personal
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password

# Server Configuration
SERVER_PORT=8081

# Clash of Clans API Token (get from https://developer.clashofclans.com/)
# IMPORTANT: Generate a new token with your EC2's public IP
COC_API_TOKEN=your_coc_api_token_here
EOF
    echo "Created .env file at $APP_DIR/.env - PLEASE UPDATE WITH YOUR VALUES"
fi

echo "=== Installing systemd service ==="
sudo cp deploy/coc-backend.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable $APP_NAME

echo "=== Deployment complete ==="
echo ""
echo "Next steps:"
echo "1. Edit $APP_DIR/.env with your database and API credentials"
echo "2. Start the service: sudo systemctl start $APP_NAME"
echo "3. Check status: sudo systemctl status $APP_NAME"
echo "4. View logs: sudo journalctl -u $APP_NAME -f"
