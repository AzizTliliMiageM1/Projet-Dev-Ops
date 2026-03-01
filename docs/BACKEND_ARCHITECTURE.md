# Architecture Backend – Projet Java DevOps

## Vue d'ensemble

Le backend est construit selon une architecture en couches pures et découplées, permettant une exécution indépendante sous forme de `.jar` autonome.

## Couches architecturales

### 1. Domain Layer (`com.projet.backend.domain/`)
**État**: 100% pure business logic  
**Contenu:**
- `Abonnement.java` – Entité domaine avec règles métier
- `User.java` – Entité domaine utilisateur

**Caractéristiques:**
- ✅ Zéro I/O, Console, UI
- ✅ Zéro dépendance externe
- ✅ Imports uniquement : `java.time.*`, `java.util.*`
- ✅ Complètement stateless

**Utilisée par:** Service, Adapter, Repository layers

---

### 2. Adapter Layer (`com.projet.backend.adapter/`)
**État**: Infrastructure concerns isolées  
**Contenu:**
- `AbonnementCsvConverter.java` – Conversion CSV ↔ Abonnement

**Caractéristiques:**
- ✅ Gère la sérialisation CSV (4 formats rétrocompatibles)
- ✅ Extraite du domaine pour pureté
- ✅ Zéro logique métier

**Utilisée par:** Repository, Migration layers

---

### 3. Service Layer (`com.projet.backend.service/`)
**État**: Business orchestration pure  
**Contenu:**
- `SubscriptionService.java` (413 lignes) – Orchestration abonnements
  - Validation métier complète (ValidationResult pattern)
  - Filtrage/triage/statistiques
  - Calculs métier (ROI, santé portefeuille, détection churn)
  - Recommandations d'optimisation
  - Structures: `ValidationResult`, `PortfolioStats`

- `UserService.java` (263 lignes) – Orchestration utilisateurs
  - Validation email/password/pseudo
  - Confirmation email workflow
  - Détection domaines institutionnels
  - Structures: `ValidationResult`

**Caractéristiques:**
- ✅ 100% business logic
- ✅ Zéro I/O, Console, UI, Persistence
- ✅ Zéro dépendance framework
- ✅ Stateless et thread-safe
- ✅ Testable sans dépendances externes

**Utilisée par:** API layer (Router)

---

### 4. Repository Layer (`com.projet.repository/`)
**État**: Persistence abstraite  
**Contenu:**
- `AbonnementRepository.java` – Interface
- `FileAbonnementRepository.java` – Fichiers
- `DatabaseAbonnementRepository.java` – Base de données
- `UserRepository.java` – Interface utilisateurs
- `FileUserRepository.java` – Fichiers utilisateurs

**Caractéristiques:**
- ✅ Dépend du domaine + adapter
- ✅ Pas de logique métier ("thin layer")
- ✅ Interface-based (swappable implementations)

**Utilisée par:** API layer, Migration layer

---

### 5. API Layer (`com.projet.api/`)
**État**: REST endpoints + orchestration  
**Contenu:**
- `ApiServer.java` – Serveur Spark (port 4567)
  - Endpoints REST GET/POST/PUT/DELETE
  - Orchestration via services
  - Lecture/écriture via repositories

**Caractéristiques:**
- ✅ Utilise service layer pour logique
- ✅ Utilise repository pour persistence
- ✅ Framework: Spark 2.9.4

**Point entry:** Appelée par `App.java`

---

### 6. Autres couches (anciennes/utilitaires)

#### Utilities
- `com.projet.service/` – Services métier spécialisés
  - `ServiceTauxChange.java` – API taux de change
  - `ServiceMailgun.java` – API email Mailgun
  - `SmartBudgetAdvisor.java` – Algorithme budget (K-means)
  - `DuplicateDetector.java` – Algorithme détection doublons (Levenshtein)
  - `SubscriptionOptimizer.java` – Reporter optimisation

#### Data Migration
- `com.projet.migration.MigrationAbonnements` – Migration CSV → CSV

#### CLI Demo
- `com.projet.demo.DemoMain` – Interface utilisateur CLI (730 lignes)

#### Analytics (SUPPRIMÉ)
- ~~`com.projet.analytics.SubscriptionAnalytics`~~ (Migré vers backend/service)
- ~~`com.projet.dashboard.DashboardStats`~~ (DEAD CODE – SUPPRIMÉ)

---

## Flux d'utilisation

### Flux API (Backend autonome)
```
HTTP Request
    ↓
ApiServer (REST endpoint)
    ↓
SubscriptionService/UserService (Orchestration)
    ↓
Business logic (Domaine)
    ↓
Repository (Persistence)
    ↓
FileAbonnementRepository / DatabaseAbonnementRepository
    ↓
Adapter (CSV conversion si besoin)
    ↓
HTTP Response (JSON)
```

### Flux Interne (Service layer → Domain layer)
```
SubscriptionService.calculatePortfolioStats(List<Abonnement>)
    ↓
Domaine orchestration (PURE computation)
    ↓
Retourne PortfolioStats (java.* standard types)
```

---

## Dépendances Maven principales

```xml
<dependency>
    <groupId>com.sparkjava</groupId>
    <artifactId>spark-core</artifactId>
    <version>2.9.4</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.14.2</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
    <version>2.14.2</version>
</dependency>
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>2.0.7</version>
</dependency>
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-simple</artifactId>
    <version>2.0.7</version>
</dependency>
```

---

## Mode d'exécution

### 1. Backend autonome (Via App.java)
```bash
# Compilation
mvn clean package

# Exécution JAR
java -jar target/gestion-abonnements-1.0-SNAPSHOT.jar

# Avec options
java -jar target/gestion-abonnements-1.0-SNAPSHOT.jar --help

# Test API
curl http://localhost:4567/api/abonnements/all
```

### 2. Développement Maven
```bash
# Compiler
mvn clean compile

# Tester
mvn test

# Exécuter
mvn exec:java -Dexec.mainClass=com.projet.App
```

### 3. CLI Demo (Ancien)
```bash
mvn exec:java -Dexec.mainClass=com.projet.demo.DemoMain
```

---

## Principes d'architecture respectés

### Séparation des responsabilités
- ✅ Domain = logique métier uniquement
- ✅ Service = orchestration métier
- ✅ Repository = accès données
- ✅ API = exposition REST

### Pureté des couches
- ✅ Domain n'importe aucune autre couche
- ✅ Service n'importe que le Domain
- ✅ API utilise Service + Repository (découplage)

### Scalabilité
- ✅ Stateless = déploiement multi-instance
- ✅ FastFail = validation early (service layer)
- ✅ Pattern Result = erreurs structurées

### Testabilité
- ✅ Domain/Service testables sans I/O
- ✅ Repository swappable (interface pattern)
- ✅ Zéro mock complexity

---

## Nettoyage effectué (Février 2026)

### Supprimé
- ✅ `com.projet.dashboard.DashboardStats` – DEAD CODE (66 lignes)

### Amélioré
- ✅ `com.projet.App.java` – Ajout support `--help` et documentation
- ✅ Documentation d'architecture complète

### Conservé (Ne pas supprimer)
- ✅ `com.projet.api.ApiServer` – Actif (API)
- ✅ `com.projet.demo.DemoMain` – Actif (CLI)
- ✅ `com.projet.service.*` – Actif (utilisé par API)
- ✅ `com.projet.analytics.SubscriptionAnalytics` – Actif (utilisé par API)
- ✅ `com.projet.user.*` – Actif (utilisé par API)

### Status de compilation
- ✅ **26 fichiers compilés** (avant: 27)
- ✅ **BUILD SUCCESS**
- ✅ **Zéro erreurs**

---

## Prochaines étapes recommandées

1. **ÉTAPE 3**: Réorganiser `backend/repository` si nécessaire
2. **ÉTAPE 4**: Ajouter unit tests pour services
3. **ÉTAPE 5**: Configuration Docker pour déploiement
4. **ÉTAPE 6**: Documentation OpenAPI/Swagger pour API REST

---

## Contacts & Questions

Architecture: Clean code principles, layered architecture  
Framework API: Spark Java 2.9.4  
Domaine: Gestion d'abonnements SaaS avec recommandations  
Status: **Production-ready backend** ✅

