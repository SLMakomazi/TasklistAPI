# Tasklist API Deployment Guide

This document provides step-by-step instructions for deploying the Tasklist API to a production environment.

## Prerequisites

- Linux VM (Ubuntu 22.04 recommended)
- SSH access to the VM with sudo privileges
- PostgreSQL database (can be on the same VM or separate)
- Java 17 JDK
- Git
- Maven
- Docker (optional, for containerized deployment)

## 1. Initial Server Setup

### 1.1 Update System Packages

```bash
sudo apt update && sudo apt upgrade -y
```

### 1.2 Install Required Packages

```bash
sudo apt install -y openjdk-17-jdk postgresql-client maven git
```

### 1.3 Create Application User

```bash
sudo useradd -m -d /opt/tasklist -s /bin/bash tasklist
sudo mkdir -p /opt/tasklist/{app,logs,config,backup}
sudo chown -R tasklist:tasklist /opt/tasklist
```

## 2. Database Setup

### 2.1 On the Database Server

```bash
# If installing PostgreSQL locally
sudo apt install -y postgresql postgresql-contrib

# Create database and user
sudo -u postgres psql -c "CREATE DATABASE tasklist;"
sudo -u postgres psql -c "CREATE USER tasklist WITH ENCRYPTED PASSWORD 'your-secure-password';"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE tasklist TO tasklist;"

# Configure PostgreSQL to accept connections
# Edit /etc/postgresql/*/main/postgresql.conf and set:
# listen_addresses = '*'

# Edit /etc/postgresql/*/main/pg_hba.conf and add:
# host    tasklist    tasklist    0.0.0.0/0    scram-sha-256

sudo systemctl restart postgresql
```

## 3. Application Deployment

### 3.1 Manual Deployment

1. Build the application:
   ```bash
   mvn clean package -DskipTests
   ```

2. Copy files to the server:
   ```bash
   scp target/tasklist-api-0.0.1-SNAPSHOT.jar tasklist@your-server-ip:/opt/tasklist/app/tasklist-api.jar
   scp src/main/resources/application-prod.properties tasklist@your-server-ip:/opt/tasklist/config/
   ```

3. On the server, set up the service:
   ```bash
   # Copy service file
   sudo cp deployment/tasklist.service /etc/systemd/system/
   
   # Set environment variables in /opt/tasklist/config/application-prod.properties
   # Update database connection details and other settings
   
   # Reload systemd and start the service
   sudo systemctl daemon-reload
   sudo systemctl enable tasklist
   sudo systemctl start tasklist
   ```

### 3.2 Automated Deployment with GitHub Actions

1. Add the following secrets to your GitHub repository:
   - `SSH_PRIVATE_KEY`: Private key for deployment user
   - `VM_HOST`: Your server IP or hostname
   - `DB_HOST`: Database host
   - `DB_USERNAME`: Database username
   - `DB_PASSWORD`: Database password
   - `KNOWN_HOSTS`: Output of `ssh-keyscan your-server-ip`

2. Push to the main branch to trigger the deployment.

## 4. Post-Deployment

### 4.1 Verify Installation

```bash
# Check service status
sudo systemctl status tasklist

# View logs
sudo journalctl -u tasklist -f

# Test API
curl http://localhost:8080/api/tasks
```

### 4.2 Set Up Nginx (Recommended for Production)

```bash
sudo apt install -y nginx

# Create a new Nginx site configuration
sudo nano /etc/nginx/sites-available/tasklist
```

Add the following configuration (adjust domain as needed):

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Enable the site and restart Nginx:

```bash
sudo ln -s /etc/nginx/sites-available/tasklist /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

### 4.3 Set Up SSL with Let's Encrypt (Recommended)

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d your-domain.com
```

## 5. Maintenance

### 5.1 Database Backups

```bash
# Manual backup
./deployment/backup-db.sh your-db-host your-db-user your-db-name

# Set up automated backups with cron
crontab -e
```

Add the following line to run daily at 2 AM:

```
0 2 * * * /path/to/tasklist-api/deployment/backup-db.sh your-db-host your-db-user your-db-name >> /var/log/db-backup.log 2>&1
```

### 5.2 Log Rotation

Log rotation is automatically configured during deployment. Logs are stored in `/opt/tasklist/logs/` and rotated daily, keeping 14 days of logs.

## 6. Troubleshooting

### Common Issues

1. **Service fails to start**
   - Check logs: `journalctl -u tasklist -n 50 --no-pager`
   - Verify database connection details in `/opt/tasklist/config/application-prod.properties`
   - Ensure the application user has write permissions to `/opt/tasklist/logs/`

2. **Database connection issues**
   - Verify PostgreSQL is running: `sudo systemctl status postgresql`
   - Check PostgreSQL logs: `sudo tail -f /var/log/postgresql/*.log`
   - Test connection: `psql -h your-db-host -U your-db-user -d your-db-name`

3. **Port already in use**
   - Check what's using the port: `sudo lsof -i :8080`
   - Update the port in `application-prod.properties` and restart the service

## 7. Security Considerations

1. **Firewall**
   ```bash
   sudo ufw allow OpenSSH
   sudo ufw allow 'Nginx Full'
   sudo ufw enable
   ```

2. **Application Security**
   - Change default credentials in `application-prod.properties`
   - Use environment variables for sensitive data
   - Keep the system and dependencies updated
   - Regularly review application logs

3. **Database Security**
   - Use strong, unique passwords
   - Limit database user privileges
   - Enable SSL for database connections
   - Regularly backup the database

## 8. Updating the Application

1. Pull the latest changes
2. Rebuild the application: `mvn clean package`
3. Follow the deployment steps in section 3
4. Restart the service: `sudo systemctl restart tasklist`

## 9. Monitoring

### 9.1 Basic Monitoring

```bash
# Check service status
sudo systemctl status tasklist

# View logs
sudo journalctl -u tasklist -f

# Monitor resource usage
top
htop  # If installed
```

### 9.2 Application Metrics

The application exposes metrics at `/actuator/metrics` when running with the `prod` profile.

## 10. Support

For issues and support, please contact your system administrator or open an issue in the project repository.
