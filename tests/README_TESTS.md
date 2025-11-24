# Documentation des Tests

## Introduction

Ce dossier contient l'ensemble des tests réalisés pour valider le bon fonctionnement de l'application de gestion d'abonnements. Les tests couvrent plusieurs aspects : les tests unitaires pour les classes métier, les tests d'intégration pour l'API REST, et les tests de performance.

## Organisation des tests

```
tests/
├── README_TESTS.md                    # Ce fichier
├── tests_unitaires/                   # Tests unitaires des classes métier
│   ├── test_abonnement.md            # Tests de la classe Abonnement
│   ├── test_repository.md            # Tests du repository de fichiers
│   └── test_user.md                  # Tests de la gestion utilisateurs
├── tests_integration/                 # Tests d'intégration de l'API
│   ├── test_api_abonnements.md       # Tests des endpoints abonnements
│   ├── test_api_auth.md              # Tests de l'authentification
│   └── test_api_session.md           # Tests de gestion de session
├── tests_performance/                 # Tests de performance
│   └── test_charge.md                # Tests de charge sur l'API
└── scenarios_tests/                   # Scénarios de tests fonctionnels
    ├── scenario_utilisateur_basic.md
    └── scenario_utilisateur_avance.md
```

## Types de tests réalisés

### 1. Tests Unitaires

Les tests unitaires vérifient le comportement de chaque classe individuellement. Ils utilisent JUnit 5 pour garantir que chaque méthode fonctionne correctement de manière isolée.

**Couverture :**
- Classe `Abonnement` : création, validation, méthodes métier
- Classe `FileAbonnementRepository` : CRUD sur fichier
- Classe `User` : gestion des utilisateurs et pseudo
- Classe `FileUserRepository` : persistance des utilisateurs

### 2. Tests d'Intégration

Les tests d'intégration vérifient que les différents composants fonctionnent bien ensemble, notamment l'API REST avec le serveur Spark et les repositories.

**Couverture :**
- Endpoints CRUD (`/api/abonnements`)
- Authentification (`/api/login`, `/api/register`)
- Gestion de session (`/api/session`, `/api/logout`)
- Import/Export JSON

### 3. Tests de Performance

Les tests de performance permettent de vérifier que l'application peut gérer plusieurs requêtes simultanées et de mesurer les temps de réponse.

**Métriques mesurées :**
- Temps de réponse moyen
- Nombre de requêtes par seconde
- Utilisation mémoire
- Comportement sous charge

### 4. Scénarios Fonctionnels

Des scénarios complets simulant le parcours d'un utilisateur réel depuis l'inscription jusqu'à la gestion complète de ses abonnements.

## Comment exécuter les tests

### Tous les tests

```bash
mvn test
```

### Tests unitaires uniquement

```bash
mvn test -Dtest="*Test"
```

### Tests d'intégration uniquement

```bash
mvn test -Dtest="*IntegrationTest"
```

### Un test spécifique

```bash
mvn test -Dtest=AbonnementTest
```

## Résultats attendus

Tous les tests doivent passer (statut SUCCESS). En cas d'échec :
1. Vérifier les logs d'erreur dans `target/surefire-reports/`
2. Consulter la documentation du test concerné
3. Vérifier que les dépendances sont correctement installées

## Outils utilisés

- **JUnit 5** : Framework de tests unitaires
- **Mockito** : Pour les mocks et stubs
- **HttpClient** : Pour tester l'API REST
- **AssertJ** : Assertions fluides (optionnel)

## Couverture de code

Pour générer un rapport de couverture de code :

```bash
mvn clean test jacoco:report
```

Le rapport sera disponible dans `target/site/jacoco/index.html`

## Bonnes pratiques suivies

1. **Nommage clair** : Chaque test a un nom explicite qui décrit ce qu'il teste
2. **Indépendance** : Les tests ne dépendent pas les uns des autres
3. **Répétabilité** : Les tests donnent toujours le même résultat
4. **Rapidité** : Les tests unitaires s'exécutent rapidement
5. **Assertions pertinentes** : Chaque test vérifie un comportement précis

## Améliorations futures

- Ajouter des tests de sécurité (injection SQL, XSS)
- Tests de compatibilité navigateurs pour l'interface web
- Tests de régression automatiques
- Tests de montée en charge plus poussés
- Tests de l'interface utilisateur avec Selenium
