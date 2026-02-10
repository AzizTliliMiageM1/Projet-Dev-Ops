# 🧹 PLAN DE NETTOYAGE DES FEATURES BACKEND

## 📊 Audit Résumé

### Features ACTIVES (à conserver) ✅

1. **SubscriptionAnalytics** (547 lignes, 29 méthodes)
   - Utilisé: 15 places dans ApiServer
   - Endpoints: 10+ analytics endpoints
   - Rôle: Analytics avancé (clustering, prédictions, détection anomalies)
   - Statut: **CORE FEATURE - MAJOR**

2. **SubscriptionOptimizer** (323 lignes, 24 méthodes)
   - Utilisé: 1 place dans ApiServer
   - Endpoints: /api/subscribe/optimize
   - Rôle: Optimisation budgétaire
   - Statut: **CORE FEATURE**

3. **SubscriptionService** (backend/service)
   - Orchestration métier
   - Statut: **CORE SERVICE - PRESERVE**

4. **UserService** (backend/service)
   - Gestion utilisateurs
   - Statut: **CORE SERVICE - PRESERVE**

5. **ServiceMailgun** (notifications email)
   - Utilisé: 4 places
   - Endpoints: /api/notifications/*
   - Statut: **MINI-FEATURE**

6. **ServiceTauxChange** (conversion devises)
   - Utilisé: 4 places
   - Endpoints: /api/convert/*
   - Statut: **MINI-FEATURE**

### Features MORTES (à supprimer) ❌

1. **SmartBudgetAdvisor** (516 lignes)
   - Utilisé: 0 places
   - Raison: Redondant avec SubscriptionOptimizer + SubscriptionAnalytics
   - Suppression: OUI

2. **DuplicateDetector** (418 lignes)
   - Utilisé: 0 places
   - Raison: Redondant avec SubscriptionAnalytics.detectDuplicates()
   - Suppression: OUI

3. **EmailService** (N/A)
   - Utilisé: 0 places
   - Raison: Redondant avec ServiceMailgun
   - Suppression: OUI

## 🎯 Actions Proposées

### A. SUPPRIMER (Code mort)
- [ ] `src/main/java/com/projet/service/SmartBudgetAdvisor.java`
- [ ] `src/main/java/com/projet/service/DuplicateDetector.java`
- [ ] `src/main/java/com/projet/service/EmailService.java` (si existe)

### B. RÉSULTAT ATTENDU

Après nettoyage:
- **6 features conservées** (conformément au cahier des charges)
- **100% utilisées** (zéro code mort)
- **Structure claire**:
  - 2 FEATURES PRINCIPALES BIG (Analytics, Optimizer)
  - 2 SERVICES BACKEND CORE (SubscriptionService, UserService)
  - 2 MINI-FEATURES (ServiceMailgun, ServiceTauxChange)

### C. VÉRIFICATIONS APRÈS NETTOYAGE

- [ ] Compilation réussie (mvn clean compile)
- [ ] Pas d'importations orphelines
- [ ] Pas d'arrêt des endpoints API
- [ ] Router/CLI/Tests inchangés
- [ ] Git commit et synchronisation

## 📈 Avant/Après

| Métrique | Avant | Après |
|----------|-------|-------|
| Features totales | 9 | 6 |
| Lignes de code dead | 1352 | 0 |
| Code utilisation | 88% | 100% |
| Compilation | ✅ | ✅ |

---

Date: 10 février 2026
Status: **PRÊT POUR EXÉCUTION**
