#!/bin/bash

# Clash of Clans Backend - EC2 Deployment Script
# Run this script on your EC2 instance (Ubuntu)

set -e

APP_NAME="coc-backend"
APP_DIR="/opt/$APP_NAME"
JAR_NAME="clash-of-clans-backend-1.0.0.jar"

echo "=== Installing Java 17 and Maven ==="
if ! command -v java &> /dev/null; then
    sudo apt-get update -y
    sudo apt-get install -y openjdk-17-jdk
fi
if ! command -v mvn &> /dev/null; then
    sudo apt-get install -y maven
fi
java -version
mvn -version

echo "=== Building JAR ==="
mvn clean package -DskipTests -q

echo "=== Creating application directory ==="
sudo mkdir -p $APP_DIR
sudo chown $USER:$USER $APP_DIR

echo "=== Copying JAR file ==="
if [ -f "target/$JAR_NAME" ]; then
    cp target/$JAR_NAME $APP_DIR/
elif [ -f "$JAR_NAME" ]; then
    cp $JAR_NAME $APP_DIR/
else
    echo "ERROR: JAR file not found. Build with 'mvn package -DskipTests' first."
    exit 1
fi

echo "=== Creating environment file ==="
if [ ! -f $APP_DIR/.env ]; then
    cat > $APP_DIR/.env << 'EOF'
# Database Configuration
DB_URL=jdbc:mysql://YOUR_RDS_ENDPOINT:3306/personal
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password

# Server Configuration
SERVER_PORT=8081

# Clash of Clans API Token (IP: 15.206.29.16)
COC_API_TOKEN=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiIsImtpZCI6IjI4YTMxOGY3LTAwMDAtYTFlYi03ZmExLTJjNzQzM2M2Y2NhNSJ9.eyJpc3MiOiJzdXBlcmNlbGwiLCJhdWQiOiJzdXBlcmNlbGw6Z2FtZWFwaSIsImp0aSI6IjY0ZjFhZDA4LTM1NTItNDA1OS1iMzI5LTdiNzNhZmM0ZTk4MiIsImlhdCI6MTc3ODQyNDMwNiwic3ViIjoiZGV2ZWxvcGVyLzkzM2UwN2IwLWMzYmItNWY2Zi1iYjRiLWZmZjYzMzI4NzZkNyIsInNjb3BlcyI6WyJjbGFzaCJdLCJsaW1pdHMiOlt7InRpZXIiOiJkZXZlbG9wZXIvc2lsdmVyIiwidHlwZSI6InRocm90dGxpbmcifSx7ImNpZHJzIjpbIjE1LjIwNi4yOS4xNiJdLCJ0eXBlIjoiY2xpZW50In1dfQ.vfWuCH_4rovsG9QJhKCf2_Ad5JC4DDmfHCrXTwbsGuZgKXPIMqVTIOzjdckSpWdZ78VTt5NLUYmKtOZSsBUmIg
EOF
    echo "Created .env file at $APP_DIR/.env - PLEASE UPDATE DB VALUES"
fi

echo "=== Installing systemd service ==="
sudo cp deploy/coc-backend.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable $APP_NAME

echo "=== Starting service ==="
sudo systemctl start $APP_NAME
sleep 3
sudo systemctl status $APP_NAME

echo "=== Deployment complete! ==="
echo "View logs: sudo journalctl -u $APP_NAME -f"
