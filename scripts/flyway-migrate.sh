#!/usr/bin/env bash
set -euo pipefail

echo "Applying Flyway migrations via docker-compose..."
docker-compose run --rm flyway migrate

echo "Migrations applied."
