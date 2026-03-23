# 🔄 Refactoring Global - Plan d'Action

## 📋 État Initial (Avant Refactoring)

**Problèmes Identifiés :**
- ❌ Fichiers orphelins supprimés (PortfolioRebalancer, MainTest, BACKEND_CLI_MODE.md)
- ⚠️ UserService dupliquée dans 2 packages (consolidation complexe, reportée)
- ⚠️ ApiServer monolithe (1,099 lignes)
- ✅ Tests présents mais dispersés
- ✅ Architecture Clean présente mais organisation à améliorer

---

## ✅ Tâches Complétées

### Phase 1 : Nettoyage des Fichiers (✅ COMPLETE)
- [x] Supprimer PortfolioRebalancer orphelin (analytics/optimization/)
- [x] Supprimer MainTest.java orphelin
- [x] Supprimer BACKEND_CLI_MODE.md (fichier généré)
- [x] Supprimer PortfolioRebalancerTest.java (importait PortfolioRebalancer orphelin)
- [x] Ajouter package-info.java pour 5 packages majeurs
- [x] Vérifier compilation après suppression : ✅ SUCCESS
- [x] Exécuter test suite : 71 tests (5 défaillances pré-existantes, non causées)

---

## 🚀 Prochaines Étapes

### Phase 2 : Organisation des Tests
- [ ] Centraliser les tests dans src/test/java/com/projet
- [ ] Déplacer le test aberrant com.example vers com.projet
- [ ] Structurer par module (analytics, api, backend, service, repository)
- [ ] Standardiser les noms des tests

### Phase 3 : Amélioration de la Documentation
- [ ] Ajouter javadoc aux classes principales
- [ ] Ajouter commentaires au code complexe
- [ ] Créer package-info.java pour chaque package majeur
- [ ] Documenter les patterns utilisés

### Phase 4 : Consolidations (Délicat, À Faire Avec Soin)
- [ ] **Fusionner UserService** (interface + implémentation unique)
  - Garder : `com.projet.user.UserService` (interface)
  - Augmenter : Ajouter les méthodes de `backend.service.UserService`
  - Supprimer : `com.projet.backend.service.UserService`
  - Mettre à jour : CommandRouter
- [ ] Réduire ApiServer (split en plusieurs controllers)
- [ ] Consolider organizationService

### Phase 5 : Nettoyage Final
- [ ] Supprimer les fichiers markdown générés
- [ ] Supprimer les fichiers de test obsolètes
- [ ] Valider zéro code mort
- [ ] Vérifier tous les tests passent

---

## 📊 Métriques Cibles

| Métrique | Avant | Après | Cible |
|----------|-------|-------|-------|
| **Fichiers Java** | 59 | ? | 50+ |
| **Lignes API Controllers** | 1,099+ | < 800 | < 500 |
| **Tests** | 18 | 18+ | 25+ |
| **Coverage** | ~30% | ? | 70%+ |
| **Packages** | 18 | 15-17 | ✅ |

---

## ✨ Bénéfices Attendus

- ✅ Meilleure lisibilité du code
- ✅ Maintenance facilitée
- ✅ Onboarding plus rapide
- ✅ Soutenance plus professionnelle
- ✅ Pas de breaking changes
- ✅ Build stable à chaque étape

---

## 🛡️ Protocole de Sécurité

1. **Chaque changement = 1 commit**
2. **Compiler après chaque changement**
3. **Tests avant et après**
4. **Revert immédiat en cas de problème**
5. **Pas de suppression sans backup (git)**

---

## ⚠️ Tests Défaillants (Pré-existants, Non Causés par Refactoring)

| Test | Issue | Priorité |
|------|-------|----------|
| SubscriptionUtilityCalculatorTest.calculateUtility_decaysOverMonths | Valeur 0.00 au lieu de décroissance | Medium |
| SubscriptionUtilityCalculatorTest.calculateUtility_recentlyUsedHigherThanDormant | Récent 0.00 = dormant 0.00 | Medium |
| SubscriptionOptimizationServiceImplTest.testCasAbonnementInutile | Retourne OPTIMISER au lieu de RESILIER | High |
| SubscriptionOptimizationServiceImplTest.testPortefeuilleMixte | Retourne RESILIER au lieu de OPTIMISER | High |
| CommandRouterTest.dashboard_withFile_shouldDisplayPortfolioData | Titre absent du résultat | Medium |

**Action** : À investiguer après Phase 2 (tests n'étaient pas causés par nos modifications)

---

## 📝 État du Refactoring

- [x] Phase 1 : Nettoyage des fichiers orphelins
- [ ] Phase 2 : Organisation des tests
- [ ] Phase 3 : Amélioration de la documentation
- [ ] Phase 4 : Consolidations complexes
- [ ] Phase 5 : Nettoyage final

