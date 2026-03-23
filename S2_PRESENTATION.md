# 📊 Projet Gestion d'Abonnements - Présentation Semestre 2

**Auteur:** Aziz Tlili  
**Date:** Mars 2026  
**Semestre:** 2 (DevOps Focus avec Docker)

---

## 📌 Résumé Exécutif

Le projet **Gestion d'Abonnements** a été **complètement refactorisé** au S2:
- ✅ **Séparation Front/Back** propre
- ✅ **Vraies features métier** (pas de mini-features)
- ✅ **Dockerisation complète** (S2 requirement)
- ✅ **30+ endpoints REST** fonctionnels
- ✅ **CI/CD fonctionnel** (GitHub Actions)

---

## 🔄 Comparaison S1 vs S2

### Semestre 1 : "Fausse bonne idée"
**État:** ❌ Architecture faible, features fragmentées

| Aspect | S1 | 
|--------|-----|
| **Architecture** | Monolithe mal organisé, peu de séparation |
| **Frontend** | HTML basique, peu d'interactivité |
| **Backend** | APIs minimalistes (~5 endpoints) |
| **Features** | Nombreuses mini-features sans cohérence |
| **DevOps** | Aucun (pas de Docker) |
| **Maintenance** | Difficile, code dupliqué |
| **Tests** | Tests unitaires seulement |

### Semestre 2 : "Réfaction complète"
**État:** ✅ Architecture robuste, vraies features métier

| Aspect | S2 |
|--------|-----|
| **Architecture** | Clean 6-layer + Séparation strict Front/Back |
| **Frontend** | SPA moderne (Bootstrap 5.3), 100% fonctionnel |
| **Backend** | 30+ endpoints REST avec Jackson |
| **Features** | 3 features majeures bien intégrées |
| **DevOps** | Docker + Docker Compose + GitHub Actions CI/CD |
| **Maintenance** | Excellent, code réutilisable |
| **Tests** | Unitaires + Intégration + E2E |

---

## 🏗️ Architecture du Projet

### Structure Multi-Couches (6 couches)

```
API Layer (ApiServer.java - 1000+ lignes)
    ↓
Service Layer (Analytics, Optimization, Lifecycle)
    ↓
Domain Layer (Abonnement, User entities)
    ↓
Repository Layer (File/DB persistence)
    ↓
Utils Layer (Converters, Validators)
    ↓
External Services (Mailgun, ExchangeRate API)
```

### Stack Technologique

**Backend:**
- Java 21 + Spark 2.9.4 (REST framework léger)
- Maven 3.9.10 (build & dependencies)
- Jackson (JSON serialization)
- File-based persistence (extensible à BD)

**Frontend:**
- HTML5 + CSS3 + JavaScript vanilla
- Bootstrap 5.3.2 (UI components)
- Font Awesome 6.4 (icons)
- No frameworks (pur JS - performant)

**DevOps:**
- Docker + Docker Compose
- GitHub Actions (CI/CD)
- Ubuntu 24.04 base image
- Maven cache optimization

---

## 🎯 Features Majeures Implémentées

### Feature #1: Portfolio Optimizer 💼
**Objectif:** Optimiser la répartition des abonnements

- Endpoint: `POST /api/portfolio/rebalance`
- Inputs: budgetTarget, valueWeight, riskWeight, comfortWeight
- Outputs: objectiveScore, savingsPotential, recommendations
- Algorithme: Score pondéré multi-critères

### Feature #2: Lifecycle Planner 📅  
**Objectif:** Planifier les cycles de vie

- Endpoint: `POST /api/portfolio/lifecycle-plan`
- Inputs: months, budget
- Outputs: monthlyPlans, totalCost, globalScore
- Features: Forecasting, allocation budgétaire

### Feature #3: Advanced Analytics 📈
**Objectif:** Analyses prédictives & détection anomalies

- 11 endpoints analytics
- Anomaly detection, duplicate detection
- Forecasting, metrics, clustering

---

## 📡 REST API Complète (30+ endpoints)

```
Authentification (5):     login, register, confirm, logout, session
Abonnements (8):         CRUD, import/export CSV/JSON, prediction
Analytics (11):          optimize, forecast, anomalies, duplicates...
Portfolio (2):           rebalance, lifecycle-plan
Services Distants (8):   email alerts, currency conversion
```

---

## 🐳 Docker & Déploiement (S2)

**Dockerfile:** Java 21 + Spark framework  
**Docker Compose:** Backend service + volumes + networks  
**CI/CD:** GitHub Actions avec Maven build  
**Commandes:**
```bash
docker-compose up -d
docker-compose logs -f backend
docker-compose down
```

---

## 🎨 Frontend Moderne (S2 Amélioré)

**5 Onglets Fonctionnels:**
1. Dashboard - Vue d'ensemble
2. Abonnements - CRUD
3. Analytics - 11 analyses
4. Portfolio - Optimiseur
5. Lifecycle - Planificateur

**Stats Widget:** 4 cartes en temps réel  
**Responsive:** Mobile-first Bootstrap 5.3.2  
**Auth:** Login/Register/Session

---

## 📊 Statistiques du Projet

| Métrique | S1 | S2 | Amélioration |
|----------|-----|-----|--------|
| Java files | 8 | 40+ | ✅ +400% |
| Endpoints | 5 | 30+ | ✅ +500% |
| Frontend | Basique | SPA | ✅ Complete |
| DevOps | Aucun | Docker+CI/CD | ✅ Production |

---

## 🎓 Conclusions

**Points Forts S2:**
- Architecture clean & scalable
- Features métier bien implémentées
- Docker ready pour production
- CI/CD automation
- Frontend fully functional
- Documentation complète

**Défis Surmontés:**
- Refactoring complet du S1
- Integration 30+ endpoints
- DevOps docker setup
- GitHub Actions CI/CD

**Compétences Acquises:**
- Architecture microservices
- DevOps (Docker, CI/CD)
- Clean code principles
- Full-stack development

---

**Version:** 2.0.0 | **Status:** ✅ Production Ready
