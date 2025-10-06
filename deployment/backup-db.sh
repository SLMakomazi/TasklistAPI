#!/bin/bash
# Database backup script for Tasklist API
# Usage: ./backup-db.sh <db-host> <db-user> <db-name> [backup-dir]

set -e

DB_HOST=${1:-localhost}
DB_USER=${2:-postgres}
DB_NAME=${3:-tasklist}
BACKUP_DIR=${4:-/opt/tasklist/backups}
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# Create backup directory if it doesn't exist
mkdir -p "$BACKUP_DIR"

# Set PGPASSWORD from environment or prompt
if [ -z "$PGPASSWORD" ]; then
    echo -n "Enter database password: "
    read -s PGPASSWORD
    echo
    export PGPASSWORD
fi

# Create backup
echo "Creating backup of $DB_NAME database..."
pg_dump -h "$DB_HOST" -U "$DB_USER" -F c -b -v -f "$BACKUP_DIR/${DB_NAME}_backup_${TIMESTAMP}.dump" "$DB_NAME"

# Verify backup
if [ $? -eq 0 ]; then
    echo "Backup completed successfully: $BACKUP_DIR/${DB_NAME}_backup_${TIMESTAMP}.dump"
    # Keep last 7 days of backups
    find "$BACKUP_DIR" -name "${DB_NAME}_backup_*.dump" -type f -mtime +7 -delete
else
    echo "Backup failed!"
    exit 1
fi