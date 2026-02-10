#!/usr/bin/env bash
set -euo pipefail

if [ -z "${MYSQL_ROOT_PASSWORD:-}" ]; then
  echo "Please set MYSQL_ROOT_PASSWORD environment variable (or provide .env with docker-compose)."
  exit 2
fi

echo "Applying schema.sql to running db container..."
docker-compose exec -T db sh -c 'mysql -u root -p"$MYSQL_ROOT_PASSWORD" abonnementsdb' < db/init/schema.sql

echo "Applying sample_data.sql to running db container..."
docker-compose exec -T db sh -c 'mysql -u root -p"$MYSQL_ROOT_PASSWORD" abonnementsdb' < db/init/sample_data.sql

echo "Done."
