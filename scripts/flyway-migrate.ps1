#!/usr/bin/env pwsh
Set-StrictMode -Version Latest

Write-Host "Applying Flyway migrations via docker-compose..."
docker-compose run --rm flyway migrate

Write-Host "Migrations applied."
