# DevOps & CI/CD Guide

This document summarizes how to build, run and deploy the backend in a reproducible way.

Quick start (local):

1. Build locally (requires Java & Maven):

```bash
./scripts/build.sh
```

2. Run with Docker Compose (starts MySQL + app):

```bash
cp .env.example .env
docker-compose up --build
```

3. Run the shaded JAR directly:

```bash
./scripts/run-jar.sh
```

CI:
- GitHub Actions workflow triggers on push and pull_request to `main` and performs:
  - Maven package
  - Tests
  - Javadoc generation
  - Uploads `target/*-shaded.jar` and `target/site/apidocs` as artifacts

Database:
- `docker-compose.yml` defines a local MySQL database. See `.env.example` for variables.
- For production / remote services (AlwaysData), provide connection details via env variables or your CI/CD secrets.

Migrations (Flyway)
- The repository includes Flyway SQL migrations under `db/migrations` (V1, V2...).
- To apply migrations locally use the helper script which runs the official Flyway image via docker-compose:

```bash
./scripts/flyway-migrate.sh
```

- Or with PowerShell:

```powershell
.\scripts\flyway-migrate.ps1
```

- Alternatively run the flyway service directly with `docker-compose run --rm flyway migrate`.


Notes:
- The Dockerfile builds the project in a Maven builder stage and produces a runtime image with the shaded jar.
- The CI contains an optional commented block to build/push Docker images to registries — add credentials/secrets to enable.
