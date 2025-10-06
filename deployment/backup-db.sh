#!/bin/bash
# Database backup script for Tasklist API
# Usage: ./backup-db.sh <db-host> <db-user> <db-name> [backup-dir]

set -e

DB_HOST=$1
DB_USER=$2
DB_NAME=$3
BACKUP_DIR=${4:-/opt/tasklist/backup}

if [ -z "$DB_HOST" ] || [ -z "$DB_USER" ] || [ -z "$DB_NAME" ]; then
    echo "Usage: $0 <db-host> <db-user> <db-name> [backup-dir]"
    exit 1
fi

# Ensure backup directory exists
mkdir -p "$BACKUP_DIR"

# Set filename with timestamp
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/${DB_NAME}_backup_${TIMESTAMP}.sql"

# Read password securely
read -s -p "Enter database password for $DB_USER: " DB_PASSWORD
echo ""

echo "📦 Creating database backup of $DB_NAME to $BACKUP_FILE..."

# Create the backup
PGPASSWORD="$DB_PASSWORD" pg_dump -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -F c -f "$BACKUP_FILE"

if [ $? -eq 0 ]; then
    # Set proper permissions
    chmod 600 "$BACKUP_FILE"
    
    # Keep only the last 7 backups
    (cd "$BACKUP_DIR" && ls -tp ${DB_NAME}_backup_*.sql | grep -v '/$' | tail -n +8 | xargs -I {} rm -- {})
    
    echo "✅ Backup completed successfully: $BACKUP_FILE"
    echo "📊 Backup size: $(du -h "$BACKUP_FILE" | cut -f1)"
    echo "💾 Latest backups:"
    ls -lth "$BACKUP_DIR"/${DB_NAME}_backup_*.sql | head -5
else
    echo "❌ Backup failed!"
    exit 1
fi
