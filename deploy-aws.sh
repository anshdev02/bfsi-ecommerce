#!/bin/bash
# =============================================================================
# BFSI E-Commerce API — AWS EC2 Free Tier Deployment Script
# Target: Amazon Linux 2023 / Ubuntu 22.04 on t2.micro (1 vCPU, 1GB RAM)
# Run as: chmod +x deploy-aws.sh && sudo ./deploy-aws.sh
# =============================================================================

set -e  # Exit on any error

APP_NAME="bfsi-ecommerce"
APP_JAR="ecommerce-banking-1.0.0.jar"
APP_DIR="/opt/bfsi"
SERVICE_FILE="/etc/systemd/system/bfsi-app.service"
LOG_DIR="/var/log/bfsi"

echo "========================================"
echo " BFSI E-Commerce API — AWS Deployment"
echo "========================================"

# ── 1. System Update ──────────────────────────────────────────────────────
echo "[1/8] Updating system packages..."
if command -v apt-get &>/dev/null; then
    apt-get update -y && apt-get upgrade -y
else
    yum update -y
fi

# ── 2. Install Java 17 ───────────────────────────────────────────────────
echo "[2/8] Installing Java 17..."
if command -v apt-get &>/dev/null; then
    apt-get install -y openjdk-17-jdk
else
    yum install -y java-17-amazon-corretto
fi
java -version

# ── 3. Install MySQL (or use RDS Free Tier) ──────────────────────────────
echo "[3/8] Installing MySQL..."
# OPTION A: Local MySQL on EC2 (free tier — use for dev only)
if command -v apt-get &>/dev/null; then
    apt-get install -y mysql-server
    systemctl start mysql
    systemctl enable mysql

    # Create DB and user
    mysql -u root -e "
        CREATE DATABASE IF NOT EXISTS bfsi_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
        CREATE USER IF NOT EXISTS 'bfsi_user'@'localhost' IDENTIFIED BY 'BfsiP@ss2024!';
        GRANT ALL PRIVILEGES ON bfsi_db.* TO 'bfsi_user'@'localhost';
        FLUSH PRIVILEGES;
    "
    echo "MySQL configured. DB: bfsi_db, User: bfsi_user"
else
    yum install -y mysql-server
    systemctl start mysqld
    systemctl enable mysqld
    TEMP_PASS=$(grep 'temporary password' /var/log/mysqld.log | tail -1 | awk '{print $NF}')
    echo "MySQL temp root password: $TEMP_PASS"
    echo "Run: mysql_secure_installation   (then repeat the DB creation above)"
fi

# OPTION B: AWS RDS MySQL Free Tier (recommended for production)
# 1. AWS Console → RDS → Create Database → MySQL
# 2. Template: Free tier | Instance: db.t2.micro | Storage: 20 GB
# 3. Set DB name, username, password
# 4. Set DB_HOST env var below to the RDS endpoint

# ── 4. Create app directories ────────────────────────────────────────────
echo "[4/8] Setting up application directories..."
mkdir -p "$APP_DIR" "$LOG_DIR"
useradd -r -s /sbin/nologin bfsi 2>/dev/null || true
chown -R bfsi:bfsi "$APP_DIR" "$LOG_DIR"

# ── 5. Copy application JAR ──────────────────────────────────────────────
echo "[5/8] Deploying application JAR..."
# Build locally first:  mvn clean package -DskipTests
# Then SCP to EC2:      scp target/ecommerce-banking-1.0.0.jar ec2-user@<EC2_IP>:/tmp/
cp /tmp/$APP_JAR "$APP_DIR/"
chown bfsi:bfsi "$APP_DIR/$APP_JAR"

# ── 6. Create environment config ────────────────────────────────────────
echo "[6/8] Writing environment configuration..."
cat > "$APP_DIR/app.env" << 'ENV'
# ── Database ──────────────────────────────────────────────────────────────
# For local MySQL:
DB_HOST=localhost
DB_PORT=3306
DB_NAME=bfsi_db
DB_USER=bfsi_user
DB_PASSWORD=BfsiP@ss2024!

# For AWS RDS (replace with your RDS endpoint):
# DB_HOST=bfsi-db.xxxxxxx.us-east-1.rds.amazonaws.com

# ── JWT ───────────────────────────────────────────────────────────────────
# IMPORTANT: Change this in production — minimum 256 bits (32 chars)
JWT_SECRET=bfsi-aws-prod-secret-key-change-me-min-256-bits-required
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000
ENV
chmod 600 "$APP_DIR/app.env"
chown bfsi:bfsi "$APP_DIR/app.env"

# ── 7. Create systemd service ────────────────────────────────────────────
echo "[7/8] Creating systemd service..."
cat > "$SERVICE_FILE" << SERVICE
[Unit]
Description=BFSI E-Commerce Banking API
After=network.target mysql.service
Wants=mysql.service

[Service]
Type=simple
User=bfsi
Group=bfsi
WorkingDirectory=$APP_DIR
EnvironmentFile=$APP_DIR/app.env
ExecStart=/usr/bin/java \
  -Xms256m \
  -Xmx512m \
  -XX:+UseG1GC \
  -Djava.security.egd=file:/dev/./urandom \
  -jar $APP_DIR/$APP_JAR \
  --spring.profiles.active=prod

StandardOutput=append:$LOG_DIR/app.log
StandardError=append:$LOG_DIR/error.log

Restart=always
RestartSec=10
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
SERVICE

# ── 8. Start service ─────────────────────────────────────────────────────
echo "[8/8] Starting BFSI application service..."
systemctl daemon-reload
systemctl enable bfsi-app
systemctl start bfsi-app

sleep 5
systemctl status bfsi-app --no-pager

echo ""
echo "========================================"
echo " Deployment Complete!"
echo "========================================"
echo " API Base URL : http://$(curl -s ifconfig.me):8080"
echo " Swagger UI   : http://$(curl -s ifconfig.me):8080/swagger-ui.html"
echo " Health Check : http://$(curl -s ifconfig.me):8080/actuator/health"
echo ""
echo " Logs         : tail -f $LOG_DIR/app.log"
echo " Service      : systemctl status bfsi-app"
echo ""
echo " AWS Security Group — open inbound port 8080 (TCP) from your IP"
echo "========================================"
