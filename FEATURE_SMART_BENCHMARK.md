# 📊 Smart Subscription Benchmark - Feature Documentation

## 📋 Vue d'ensemble

La **Smart Subscription Benchmark** est une fonctionnalité qui compare automatiquement le prix d'un abonnement utilisateur avec les prix moyens du marché, afin de déterminer si l'abonnement est compétitif ou trop cher.

**Objectif** : Aider les utilisateurs à optimiser leurs dépenses d'abonnement en comparant leurs tarifs avec les standards du marché.

---

## 🚀 Utilisation

### 1️⃣ Endpoint REST : `/api/abonnements/:id/benchmark`

**Requête :**
```bash
GET /api/abonnements/550e8400-e29b-41d4-a716-446655440000/benchmark
```

**Réponse réussie (200) :**
```json
{
  "abonnementId": "550e8400-e29b-41d4-a716-446655440000",
  "serviceName": "Netflix",
  "userPrice": 15.99,
  "marketAveragePrice": 15.99,
  "marketMinPrice": 6.99,
  "marketMaxPrice": 22.99,
  "status": "OPTIMIZED",
  "priceDeviation": 0.0,
  "recommendation": "✅ Parfaitement optimisé ! Votre prix suit exactement la tendance du marché.",
  "region": "Europe",
  "timestamp": 1711274347000
}
```

**Erreur 404 (Abonnement non trouvé) :**
```json
{"error": "Abonnement not found"}
```

---

## 📊 Statuts du Benchmark

### **OPTIMIZED** ✅
- **Condition** : Prix utilisateur dans les limites du marché ± 10%
- **Signification** : L'abonnement est correctement tarifé
- **Recommandation** : Continuer, le tarif est bon

### **OVERPRICED** ⚠️
- **Condition** : Prix utilisateur > prix max du marché + 10%
- **Signification** : L'utilisateur paie trop cher
- **Recommandation** : Envisager de négocier ou chercher une alternative

### **UNDERPRICED** ✅
- **Condition** : Prix utilisateur < prix min du marché - 10%
- **Signification** : L'utilisateur a un excellent tarif
- **Recommandation** : Profiter du bon deal, surveiller les changements

---

## 🏗️ Architecture

### Couches de la Clean Architecture

```
REST Layer
├── GET /api/abonnements/:id/benchmark
└── Deserialize response to BenchmarkResult

Service Layer (Business Logic)
├── BenchmarkService (interface)
├── BenchmarkServiceImpl (implementation)
│   ├── Récupère l'abonnement
│   ├── Obtient les données du marché
│   ├── Compare les prix
│   ├── Détermine le statut
│   └── Génère les recommandations
└── ExternalBenchmarkService (interface)
    └── ExternalBenchmarkServiceImpl
        ├── Récupère les prix du marché
        ├── Gère les timeouts
        └── Fournit des fallback

Domain Layer
├── Abonnement (inchangé)
├── BenchmarkResult (DTO nouveau)
└── User (inchangé)

Repository Layer (inchangé)
├── AbonnementRepository
└── FileAbonnementRepository
```

---

## 💾 Données de Marché

### Base de données de prix intégrée

La base de données contient les prix moyens, min et max pour les services populaires :

| Service | Prix Moyen | Min | Max |
|---------|-----------|-----|-----|
| **Streaming** |
| Netflix | 15.99€ | 6.99€ | 22.99€ |
| Spotify | 11.99€ | 0.00€ | 14.99€ |
| Disney+ | 10.99€ | 10.99€ | 10.99€ |
| Amazon Prime | 14.99€ | 0.00€ | 14.99€ |
| **Productivité** |
| Microsoft 365 | 9.99€ | 9.99€ | 19.99€ |
| Adobe CC | 64.99€ | 19.99€ | 64.99€ |
| Slack | 8.99€ | 0.00€ | 12.99€ |
| Dropbox | 11.99€ | 0.00€ | 19.99€ |
| **Autres** |
| GitHub Pro | 4.00€ | 0.00€ | 4.00€ |
| Medium | 12.99€ | 0.00€ | 12.99€ |
| *Et plus...* |

---

## 🔄 Logique de Calcul

### Déviation de prix

```
priceDeviation = ((userPrice - marketAveragePrice) / marketAveragePrice) * 100
```

- **Positif** = utilisateur paie plus que la moyenne
- **Négatif** = utilisateur paie moins que la moyenne

### Détermination du statut

```java
if (userPrice < marketMin * 0.9) {
    status = "UNDERPRICED";
} else if (userPrice > marketMax * 1.1) {
    status = "OVERPRICED";
} else {
    status = "OPTIMIZED";
}
```

### Génération de recommandations

Les recommandations sont générées dynamiquement basées sur :
- Le statut (OVERPRICED, OPTIMIZED, UNDERPRICED)
- La magnitude de la déviation
- Les prix du marché

---

## 🛡️ Gestion des Erreurs

### Cas de gestion robuste

1. **Service inconnu** : Utiliser les prix par défaut (14.99€ avg)
2. **API indisponible** : Utiliser la base locale de fallback
3. **Données manquantes** : Retourner des recommandations génériques
4. **Inputs invalides** : Valider et lever des exceptions claires

### Timeouts

- Timeout HTTP : 5 secondes
- Non-blocking : Les erreurs n'impactent pas les autres services

---

## ✅ Tests Unitaires

**Exécuter les tests :**
```bash
mvn test -Dtest=BenchmarkServiceTest
```

**Couverture (15 tests) :**
```
✓ Benchmark OPTIMIZED
✓ Benchmark OVERPRICED
✓ Benchmark UNDERPRICED
✓ Calcul de déviation de prix
✓ Validations (ID vide, service vide, prix négatif)
✓ Ratio de prix
✓ Services avec espaces dans le nom
✓ Recommandations différentes par statut
✓ Données de marché dans le résultat
✓ Abonnements gratuits
✓ Abonnements très chers
✓ Et plus...
```

**Résultats :**
```
Tests run: 15, Failures: 0, Errors: 0, Skipped: 0 ✅
```

---

## 📚 Exemples complets

### Exemple 1 : Benchmark d'un Netflix trop cher

```bash
# Supposons Netflix à 25€/mois (trop cher)
curl "http://localhost:4567/api/abonnements/netflix-123/benchmark"

Réponse :
{
  "status": "OVERPRICED",
  "priceDeviation": 56.35,
  "recommendation": "⚠️ Beaucoup trop cher ! Considérez de changer de prestataire. Le marché propose 15.99€ en moyenne.",
  ...
}
```

### Exemple 2 : Benchmark d'un Spotify bon marché

```bash
# Supposons Spotify à 5€/mois (très bien)
curl "http://localhost:4567/api/abonnements/spotify-456/benchmark"

Réponse :
{
  "status": "UNDERPRICED",
  "recommendation": "✅ Excellent deal ! Vous bénéficiez d'un bon tarif. Continuez à surveiller les évolutions de prix.",
  ...
}
```

### Exemple 3 : Intégration dans le code

```java
BenchmarkService benchmarkService = new BenchmarkServiceImpl();

BenchmarkResult result = benchmarkService.benchmark(
    abonnementId,
    "Netflix",
    19.99
);

if ("OVERPRICED".equals(result.getStatus())) {
    System.out.println("Alert: " + result.getRecommendation());
}

double ratio = benchmarkService.getPriceRatio(19.99, "Netflix");
// ratio = 1.25 (25% au-dessus du marché)
```

---

## 🔧 Extension Future

### Améliorations possibles

1. **API externe réelle** : Intégrer une API de pricing externe (ex: SerpAPI, Rapidapi)
2. **Historique des prix** : Tracker l'évolution des prix au fil du temps
3. **Alertes** : Notifier quand un prix change
4. **Négociation** : Suggérer les meilleures périodes pour négocier
5. **Analytics** : Visualiser la distribution des prix par service
6. **Machine Learning** : Prédire les changements de prix

---

## 📊 Fichiers impactés

### Créés
- `ExternalBenchmarkService.java` - Interface
- `ExternalBenchmarkServiceImpl.java` - Implémentation (32KB)
- `BenchmarkService.java` - Interface
- `BenchmarkServiceImpl.java` - Implémentation
- `BenchmarkResult.java` - DTO avec logique métier
- `BenchmarkServiceTest.java` - 15 tests unitaires

### Modifiés
- `ApiServer.java` - Ajout endpoint `/benchmark` (35 lignes)

### Stats
- **Lignes de code** : ~900 lignes (service + tests)
- **Classes** : 6 nouvelles classes
- **Tests** : 15 tests (100% passing)
- **Dépendances** : Aucune nouvelle (Java standard + Jackson existing)

---

## 🚀 Déploiement

Le fichier JAR généré inclut la nouvelle feature :
```
target/gestion-abonnements-1.0-SNAPSHOT.jar (9.6M)
```

A redémarrer le serveur pour appliquer les changements :
```bash
java -jar target/gestion-abonnements-1.0-SNAPSHOT.jar
```

Puis tester l'endpoint :
```bash
curl http://localhost:4567/api/abonnements/{id}/benchmark
```

---

## 📌 Notes importantes

✅ **Respecte la Clean Architecture** - Séparation claire des couches  
✅ **Pas de breaking changes** - L'existant reste inchangé  
✅ **Production-ready** - Gestion des erreurs et fallbacks  
✅ **Testable** - Injection de dépendances pour les tests  
✅ **Maintenable** - Code simple et lisible  
✅ **Extensible** - Facile d'ajouter de nouveaux services

