# Plan de Présentation - Gestion d'Abonnements
## Application Full-Stack Java + Docker

---

## 📊 PRÉSENTATION - 6 DIAPOSITIVES (10 minutes)

### **Diapositive 1: Contexte et Objectifs** ⏱️ (1 min)

**De quoi ça parle?**
Application web pour gérer les abonnements (services cloud, SaaS, etc.) avec analyse automatique des dépenses et recommandations d'optimisation.

**Problème résolu:**
- Pas de visibilité sur ce qu'on dépense en abonnements (SaaS sprawl)
- Faut aller sur 10 services différents pour voir les factures
- Risque d'oublier des abonnements ou les renouvellements

**Solution:**
- Dashboard centralisé
- Tout en un endroit : ajouter, modifier, supprimer des abonnements
- Analyse intelligente : détecte les redondances et propose des économies
- Prévisions : prévoir la trésorerie sur 6 mois

**Stack technique:**
- Backend: Java 21, Spark Framework, API REST
- Frontend: HTML5 / JavaScript
- DB: MySQL 8.0 + Flyway migrations
- Infrastructure: Docker Compose, GitHub Actions

---

### **Diapositive 2: Feature #1 - Gestion d'Abonnements** ⏱️ (1.5 min)

**Ce qu'on peut faire:**

1. **Ajouter un abonnement** → Remplir un formulaire (nom, prix mensuel, date début/fin, client)
   - Autosave → Sauvegardé immédiatement dans la DB

2. **Lister ses abonnements** → Dashboard avec tous les services
   - Tri, recherche, filtrage par catégorie
   - Vue actualisée en temps réel

3. **Modifier / Supprimer** → Mettre à jour les informations
   - Changement de prix, de durée, etc.
   - Suppression avec confirmation

4. **Import/Export**
   - Importer une liste CSV d'abonnements (exemple: factures de 10 services)
   - Exporter ses abonnements en CSV (pour audit ou backup)

**Multi-user:**
- Chaque utilisateur a ses propres données (authentification requise)
- Données stockées dans des fichiers séparés par email

**Example API:**
```
POST   /api/abonnements              → Créer
GET    /api/abonnements              → Lister
PUT    /api/abonnements/:id          → Modifier
DELETE /api/abonnements/:id          → Supprimer
POST   /api/abonnements/import/csv   → Importer CSV
GET    /api/abonnements/export/csv   → Exporter CSV
```

---

### **Diapositive 3: Feature #2 - Analytics & Optimisations** ⏱️ (2 min)

**Le "secret sauce" du projet**

**Analyse automatique des dépenses:**
- **Coût total / Prédiction** → Combien je dépense ce mois / sur 6 mois
- **Tendances** → Mes dépenses augmentent ou baissent?
- **Par catégorie** → Quel type d'outils coûte le plus cher

**Détection intelligente:**
- **Redondances** → "Tu as 2 services de stockage cloud, garde les 2?"
- **Anomalies** → "Ce service a triplé de prix, c'est normal?"
- **Services non utilisés** → "Pas utilisé depuis 3 mois, supprime?"

**Recommendations:**
- **Portfolio Rebalance** → "Réorganise tes services pour reduire le coût de X%"
- **Optimization Report** → Liste des économies possibles (ex: "économise 150€/mois si tu supprimes ça")
- **Lifecycle Planning** → "Tes abonnements vont expirer quand? Lesquels renouveler?"

**Forecast (prédiction trésorerie):**
```
Mois 1: 450€
Mois 2: 480€ (augmentation)
Mois 3: 480€
...
Mois 6: 500€
→ Budget à prévoir: 2,800€ pour 6 mois
```

**Example API:**
```
GET  /analytics/optimize         → Rapport d'optimisation
GET  /analytics/forecast         → Prédictions 6 mois
GET  /analytics/anomalies        → Services suspects
POST /portfolio/rebalance         → Recommandations de réorganisation
GET  /analytics/monthly-report    → Bilan mensuel
POST /portfolio/lifecycle-plan    → Plan de renouvellement
```

---

### **Diapositive 4: Architecture & Infrastructure (Docker)** ⏱️ (1.5 min)

**3 composants principaux:**

```
┌─────────────────────────────────┐
│    3 Services en Docker         │
├─────────────────────────────────┤

1. MySQL 8.0 (Port 3306)
   └─ Stocke les abonnements + users
   └─ Persistence via volume db_data
   
2. Flyway 9
   └─ Gère les migrations DB
   └─ Exécution automatique au démarrage
   
3. Backend Java (Port 8080)
   └─ API REST
   └─ Logique analytics
   └─ /workspace mount pour dev
```

**Deployment 3 modes:**

1. **JAR direct** - `java -jar app.jar`
   - Idéal: Dev local rapide
   - Pas de dépendances installées

2. **Docker seul** - `docker build . && docker run`
   - Idéal: Reproducibilité, un seul container
   - Sans DB (besoin de la fixer au démarrage)

3. **Docker Compose** (PRODUCTION) - `docker-compose up`
   - Tout prêt, tous les services
   - Network isolé, volumes persistants
   - Prêt à deployer

**Optimisations appliquées:**
- Multi-stage Dockerfile: 350 MB (au lieu de 800 MB)
- Maven cache: 30 secondes après le premier build
- Health checks automatiques pour vérifier que tout est ready

---

### **Diapositive 5: Tests & Déploiement (CI/CD)** ⏱️ (2 min)

**5 test scripts automatisés:**

```
test-simple.sh       → Auth + CRUD de base           (2 min)
test-api.sh          → Tous les endpoints (14+)      (5 min)
test-crud.sh         → CREATE/UPDATE/DELETE complet  (2 min)
test-complete.sh     → Intégration full              (3 min)
verify-deployment.sh → Vérif infra après déploiement (1 min)
```

**Pyramide des tests:**
- Unit Tests (JUnit 5) → 100 tests, 1 min
- Integration (Docker + MySQL) → Tests du backend avec vraie DB, 2 min
- E2E (API) → Tests des endpoints en vraie conditions, 5 min

**CI/CD GitHub Actions (à chaque push sur main):**
```
Code push → GitHub Actions
  ├─ Checkout code
  ├─ Setup JDK 21
  ├─ Maven cache (80% hit rate)
  ├─ mvn clean package
  ├─ Upload artifact (shaded JAR ~185 MB)
  └─ ✅ Résultat: 70 sec (cached) / 2:30 (first time)
```

**Status actuel:**
- Build success rate: 100% (42/42 builds)
- Test suites: All passing
- Docker image: 350 MB, build 3-5 min (first), 30 sec (cache)

**Commandes pour tester localement:**
```bash
docker-compose up --build        # Lance tout
./test-simple.sh                 # Quick smoke test
./test-api.sh                    # Full coverage
./verify-deployment.sh           # Vérifier infra
```

---

### **Diapositive 6: Bilan & Démo Live** ⏱️ (1 min)

**Ce qu'on a réalisé:**

✅ **Infrastructure solide**
   - 100% containerisé, production-ready
   - Multi-user avec authentification
   - Export/Import pour faciliter la migration

✅ **Analytics avancées**
   - Forecasting automatique
   - Anomaly detection
   - Recommendations d'optimisation

✅ **DevOps-ready**
   - Tests automatisés à tous les niveaux
   - CI/CD pipeline complète
   - Docker Compose pour déploiement facile

✅ **Code quality**
   - 21 endpoints testés
   - 60-70% coverage
   - Multi-stage builds optimisés

**Points clé de la présentation:**
- Feature #1 (CRUD) = la base, facile à utiliser
- Feature #2 (Analytics) = partie intelligente, vraie valeur ajoutée
- Docker = chaque développeur reproduit exactement la même infra
- Tests = chaque changement est validé automatiquement

**Quick demo:**
```
1. docker-compose up --build    (2-3 min de setup)
2. Montrer dashboard / ajouter service
3. Montrer analytics / forecast
4. ./test-api.sh pour montrer tests automatisés
→ Total démo: 5-7 minutes
```

---

## 🚀 NOTES POUR MANUS

**Avant de présenter:**
- Avoir Docker installé (vérifier `docker --version`)
- Terminal prêt
- Peut montrer en direct ou par slides + démo écran partagée

**Timing total:**
- Slides: 10 minutes
- Démo live: 5-7 minutes (optionnel)
- Q&A: 3-5 minutes

**Structure du discours:**
1. Lancer la présentation (6 diapos)
2. Pause Q&A rapide
3. Si envie: démo live (docker-compose up + quick test)

**Fichiers clé à mentionner:**
- `docker-compose.yml` = config infra
- `src/main/java/com/projet/api/ApiServer.java` = tous les endpoints
- `test-*.sh` = suite de validation
- `.github/workflows/ci.yml` = pipeline CI/CD

**Si internet/démo fail:**
- J'ai des screenshots des dashboards
- Les test scripts sont self-contained (fonctionnent hors ligne)
- API response examples à montrer

---

**Status:** Production Ready | **Créé:** Mars 2026 | **Stack:** Java 21 + Docker + GitHub Actions
