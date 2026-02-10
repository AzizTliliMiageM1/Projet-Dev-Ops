# Gestion d'Abonnements - Application Full-Stack

![Version](https://img.shields.io/badge/version-2.0-blue.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Build](https://img.shields.io/badge/build-passing-green.svg)
![Docker](https://img.shields.io/badge/Docker-ready-blue.svg)

Application de gestion d'abonnements avec interface web moderne, backend Java robuste, analytics avancé et déploiement dockerisé.

## 🚀 Démarrage Rapide

### Option 1 : Exécution Directe (JAR)

**Prérequis**: Java 21+

```bash
# Build
mvn clean package -Pprod -q

# Exécuter l'API
java -jar target/gestion-abonnements-1.0-SNAPSHOT.jar api

# Ou utiliser le CLI
java -jar target/gestion-abonnements-1.0-SNAPSHOT.jar dashboard file=data/abonnements.csv

# Aide
java -jar target/gestion-abonnements-1.0-SNAPSHOT.jar help
```

L'API démarre sur **`http://localhost:4567`**

### Option 2 : Docker (Recommandé)

**Prérequis**: Docker + Docker Compose

```bash
# Build et lancer
docker-compose up --build

# Accéder à l'API
curl http://localhost:4567/health

# Arrêter
docker-compose down
```

### Option 3 : Build avec Profils Maven

```bash
# DEV (avec tests)
mvn clean verify -Pdev

# PROD (skip tests, optimisé)
mvn clean package -Pprod -DskipTests
```

## 📖 Documentation

| Ressource | Description |
|-----------|-------------|
| **[BACKEND_ARCHITECTURE.md](./docs/techniques/ARCHITECTURE_TECHNIQUE.md)** | Architecture clean (6 couches) |
| **[GUIDE_EXECUTION_JAR.md](./docs/guides/GUIDE_UTILISATION_MAIN.md)** | Guide d'exécution détaillé |
| **[STRUCTURE_PROJET.md](./STRUCTURE_PROJET.md)** | Arborescence complète |
| **[tests/README_TESTS.md](./tests/README_TESTS.md)** | Tests et validation |
| **[AUDIT_ETAT_PROJET.md](./AUDIT_ETAT_PROJET.md)** | État actuel du projet |

## 🐳 Déploiement Docker

### Architecture Docker

```yaml
Backend Service (Java API + CLI)
├── Port: 4567 (API)
├── Volumes: ./data (persistence)
├── Network: app-network
└── Health Check: Dashboard CLI test
```

### Commandes Utiles

```bash
# Build l'image
docker build -t gestion-abonnements:latest .

# Lancer le conteneur
docker run -p 4567:4567 -v ./data:/app/data gestion-abonnements:latest api

# Lancer avec docker-compose
docker-compose up -d

# Consulter les logs
docker-compose logs -f backend

# Accéder au CLI dans le conteneur
docker-compose exec backend java -jar app.jar dashboard file=/app/data/abonnements.csv
```

### Configuration Environment

```env
# docker-compose.yml variables
JAVA_OPTS=-Xmx512m -Xms128m
LOG_LEVEL=INFO
```

## 🔄 Pipeline CI/CD

### GitHub Actions

Le projet inclut un pipeline CI automatisé (`.github/workflows/ci.yml`):

```
✅ Maven Build (DEV + PROD)
✅ Unit Tests (avec couverture)
✅ JAR Validation
✅ Docker Build
✅ Container Test
```

**Déclenche automatiquement sur:**
- Push vers `main` ou `develop`
- Pull requests

### Exécuter Localement

```bash
# Vérifier compilation
mvn clean compile

# Exécuter les tests
mvn clean test

# Build JAR
mvn clean package -Pprod

# Vérifier JAR exécutable
java -jar target/gestion-abonnements-1.0-SNAPSHOT.jar help
```

## ✨ Fonctionnalités Principales

- ✅ Gestion complète des abonnements (CRUD)
- ✅ Dashboard CLI avec analytics temps réel
- ✅ Consolidation algorithmique (source unique)
- ✅ Export/Import (JSON, CSV, OFX)
- ✅ Notifications email automatiques
- ✅ API REST pour intégrations
- ✅ Scores de valeur et risque churn
- ✅ Recommandations d'économies

## 🏗️ Architecture Technique

| Couche | Technologies |
|--------|------------|
| **Frontend** | HTML5, CSS3, JavaScript vanilla, Chart.js |
| **API** | Spark Framework 2.9.4 (Java 21) |
| **Backend Services** | Jackson JSON, SLF4J logging |
| **Persistance** | Fichiers CSV, Format JSON |
| **Déploiement** | Docker (Alpine Linux, JAR 9.6MB) |
| **CI/CD** | GitHub Actions |

## 📊 Dashboard CLI

Commande principale pour visualiser le portefeuille :

```bash
java -jar app.jar dashboard file=data/abonnements.csv
```

Affiche:
- Résumé financier (dépenses, projection annuelle)
- Score de santé du portefeuille
- Composition par catégorie
- Abonnements à haut risque churn
- Top 5 priorités à conserver
- Opportunités d'économies
- Expirations proches

## 👥 Auteurs

- **Aziz TLILI** (41006201)
- **Maissara FERKOUS** (42006149)

## 📋 Projet

- **Contexte** : Projet DevOps & Architecture Backend (MIAGE M1)
- **Dates** : Octobre 2025 - Février 2026
- **Objectif** : Application robuste avec architecture clean et déploiement automatisé

---

📅 Dernière mise à jour : 10 février 2026

**Status**: ✅ Backend Core Complete | 🐳 Docker Ready | 🔄 CI/CD Active
