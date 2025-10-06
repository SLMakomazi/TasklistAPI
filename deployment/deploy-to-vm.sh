#!/bin/bash
# Usage: ./deploy-to-vm.sh <vm-ip> <ssh-key-path> [commit-sha]

set -e

VM_IP=$1
SSH_KEY=$2
COMMIT_SHA=${3:-latest}
APP_NAME="tasklist-api"
REMOTE_USER="tasklist"
REMOTE_DIR="/opt/tasklist"

if [ -z "$VM_IP" ] || [ -z "$SSH_KEY" ]; then
    echo "Usage: $0 <vm-ip> <ssh-key-path> [commit-sha]"
    exit 1
fi

# Check if the SSH key exists
if [ ! -f "$SSH_KEY" ]; then
    echo "Error: SSH key not found at $SSH_KEY"
    exit 1
fi

# Set strict permissions for the key
chmod 600 "$SSH_KEY"

echo "🚀 Starting deployment to $VM_IP..."

# Create necessary directories on remote server
echo "📁 Setting up remote directories..."
ssh -i "$SSH_KEY" -o StrictHostKeyChecking=no "$REMOTE_USER@$VM_IP" "
    sudo mkdir -p $REMOTE_DIR/{app,logs,config,backup} \
    && sudo chown -R $REMOTE_USER:$REMOTE_USER $REMOTE_DIR \
    && sudo chmod 755 $REMOTE_DIR
"

# Stop the service if running
echo "🛑 Stopping existing service if running..."
ssh -i "$SSH_KEY" "$REMOTE_USER@$VM_IP" "
    if systemctl is-active --quiet $APP_NAME; then
        sudo systemctl stop $APP_NAME
    fi
" || echo "Service not running, continuing..."

# Backup existing JAR if it exists
echo "💾 Creating backup of existing application..."
ssh -i "$SSH_KEY" "$REMOTE_USER@$VM_IP" "
    if [ -f "$REMOTE_DIR/app/$APP_NAME.jar" ]; then
        BACKUP_FILE="$REMOTE_DIR/backup/${APP_NAME}_$(date +%Y%m%d_%H%M%S).jar"
        cp "$REMOTE_DIR/app/$APP_NAME.jar" "$BACKUP_FILE"
        echo "✅ Created backup at $BACKUP_FILE"
    fi
"

# Copy new JAR
echo "📤 Uploading new application JAR..."
scp -i "$SSH_KEY" "target/$APP_NAME-0.0.1-SNAPSHOT.jar" "$REMOTE_USER@$VM_IP:$REMOTE_DIR/app/$APP_NAME.jar"

# Copy configuration files
echo "⚙️  Uploading configuration..."
scp -i "$SSH_KEY" "src/main/resources/application-prod.properties" "$REMOTE_USER@$VM_IP:$REMOTE_DIR/config/"

# Copy systemd service file
echo "🔧 Setting up systemd service..."
scp -i "$SSH_KEY" "deployment/tasklist.service" "$REMOTE_USER@$VM_IP:/tmp/"
ssh -i "$SSH_KEY" "$REMOTE_USER@$VM_IP" "
    sudo mv /tmp/tasklist.service /etc/systemd/system/
    sudo chmod 644 /etc/systemd/system/tasklist.service
    sudo systemctl daemon-reload
"

# Set up log rotation
echo "📝 Configuring log rotation..."
scp -i "$SSH_KEY" "deployment/logrotate-tasklist" "$REMOTE_USER@$VM_IP:/tmp/"
ssh -i "$SSH_KEY" "$REMOTE_USER@$VM_IP" "
    sudo mv /tmp/logrotate-tasklist /etc/logrotate.d/$APP_NAME
    sudo chmod 644 /etc/logrotate.d/$APP_NAME
"

# Set permissions
echo "🔒 Setting file permissions..."
ssh -i "$SSH_KEY" "$REMOTE_USER@$VM_IP" "
    sudo chown -R $REMOTE_USER:$REMOTE_USER $REMOTE_DIR
    sudo chmod 750 $REMOTE_DIR
    sudo chmod 640 $REMOTE_DIR/config/*
    sudo chmod 750 $REMOTE_DIR/logs
"

# Start the service
echo "🚀 Starting application..."
ssh -i "$SSH_KEY" "$REMOTE_USER@$VM_IP" "
    sudo systemctl enable $APP_NAME
    if ! sudo systemctl restart $APP_NAME; then
        echo "❌ Failed to start $APP_NAME. Check the logs with: journalctl -u $APP_NAME -n 50 --no-pager"
        exit 1
    fi
"

# Verify service is running
echo "🔍 Verifying service status..."
ssh -i "$SSH_KEY" "$REMOTE_USER@$VM_IP" "
    if systemctl is-active --quiet $APP_NAME; then
        echo "✅ $APP_NAME is running successfully!"
        echo "🌐 Application should be available at: http://$VM_IP:8080"
        echo "📋 Service status:"
        sudo systemctl status $APP_NAME --no-pager
    else
        echo "❌ $APP_NAME failed to start. Check the logs with: journalctl -u $APP_NAME -n 50 --no-pager"
        exit 1
    fi
"

echo "🎉 Deployment completed successfully!"
echo "🔗 API Documentation: http://$VM_IP:8080/swagger-ui.html"
echo "📊 Actuator Health: http://$VM_IP:8080/actuator/health"
