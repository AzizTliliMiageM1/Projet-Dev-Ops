param(
  [string]$EnvFile = '.env'
)

if (-not (Test-Path -Path $EnvFile)) {
  Write-Error "Please create an .env file from .env.example with MYSQL_ROOT_PASSWORD set."
  exit 2
}

. $EnvFile

if (-not $env:MYSQL_ROOT_PASSWORD) {
  Write-Error "MYSQL_ROOT_PASSWORD not set in environment"
  exit 2
}

Write-Host "Applying schema.sql to running db container..."
docker-compose exec -T db sh -c "mysql -u root -p\"$env:MYSQL_ROOT_PASSWORD\" abonnementsdb" < db/init/schema.sql

Write-Host "Applying sample_data.sql to running db container..."
docker-compose exec -T db sh -c "mysql -u root -p\"$env:MYSQL_ROOT_PASSWORD\" abonnementsdb" < db/init/sample_data.sql

Write-Host "Done."
