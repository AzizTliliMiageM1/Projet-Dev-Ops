# 💱 Intégration API Conversion de Devises

## 📋 Résumé de l'implémentation

Une nouvelle fonctionnalité de **conversion de devises en temps réel** a été intégrée au projet, respectant la Clean Architecture existante.

### Fichiers créés :

```
backend/src/main/java/com/projet/service/
├── ExchangeRateService.java           (interface)
└── ExchangeRateServiceImpl.java        (impl avec HttpURLConnection)

backend/src/main/java/com/projet/backend/domain/
└── CurrencyConversion.java            (DTO pour résultats)

backend/src/test/java/com/projet/service/
└── ExchangeRateServiceTest.java       (8 tests unitaires)

backend/src/main/java/com/projet/api/
└── ApiServer.java                     (endpoint REST ajouté)
```

---

## 🚀 Utilisation

### 1️⃣ Endpoint REST : `/api/abonnements/:id/convert`

**Requête :**
```bash
GET /api/abonnements/550e8400-e29b-41d4-a716-446655440000/convert?currencies=USD,GBP,CHF
```

**Paramètres :**
- `id` (path) - UUID de l'abonnement
- `currencies` (query, optionnel) - Devises séparées par virgules (défaut: `USD,GBP,CHF`)

**Réponse réussie (200) :**
```json
{
  "abonnementId": "550e8400-e29b-41d4-a716-446655440000",
  "nomService": "Netflix",
  "originalPrice": 15.99,
  "baseCurrency": "EUR",
  "convertedPrices": {
    "USD": 17.27,
    "GBP": 13.75,
    "CHF": 15.36
  },
  "exchangeRates": {
    "USD": 1.08,
    "GBP": 0.86,
    "CHF": 0.96
  },
  "timestamp": 1711270184000
}
```

**Erreur 404 :**
```json
{"error": "Abonnement not found"}
```

---

### 2️⃣ Utilisation dans le code Java

**Récupérer les taux :**
```java
ExchangeRateService service = new ExchangeRateServiceImpl();

// Récupérer les taux de EUR vers USD, GBP, CHF
Map<String, Double> rates = service.getExchangeRates("EUR", "USD,GBP,CHF");
// Résultat : {USD=1.08, GBP=0.86, CHF=0.96}
```

**Convertir un montant :**
```java
double prixEUR = 15.99;
double prixUSD = service.convertAmount(prixEUR, "EUR", "USD");
// Résultat : 17.27
```

---

## 🛡️ Architecture

### Service Layer (Clean Architecture)

```
ExchangeRateService (interface)
    ↓
ExchangeRateServiceImpl
    ├─ getExchangeRates(baseCurrency, targetCurrencies)
    └─ convertAmount(amount, from, to)
        ↓
    HttpURLConnection (Java standard)
        ↓
    API ExchangeRate : https://api.exchangerate-api.com/v4/latest/...
```

### Intégration à l'API REST

```
Spark Framework (ApiServer)
    ↓
GET /api/abonnements/:id/convert
    ├─ Récupère l'abonnement depuis le repository
    ├─ Crée une instance ExchangeRateService
    ├─ Récupère les taux pour les devises demandées
    ├─ Convertit les prix
    └─ Retourne CurrencyConversion en JSON
```

---

## 🔄 Gestion des erreurs

### Fallback automatique

Si l'API externe ne répond pas :
- **Timeout** (5s) : Basculer sur les taux par défaut
- **Erreur HTTP** : Utiliser les taux de fallback
- **API indisponible** : Logs d'erreur + fallback activé

### Taux par défaut (fallback) :
```java
EUR (1.0)
USD (1.08)
GBP (0.86)
CHF (0.96)
JPY (157.0)
CAD (1.45)
AUD (1.62)
CNY (7.75)
INR (89.5)
```

---

## ✅ Tests

**Exécuter les tests :**
```bash
mvn test -Dtest=ExchangeRateServiceTest
```

**Résultats :**
```
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0 ✅

✓ Récupération des taux depuis l'API
✓ Conversion correcte des montants
✓ Conversion identique = même montant
✓ Gestion gracieuse des erreurs (fallback)
✓ Cohérence des conversions multiples
✓ Exceptions pour devises nulles
✓ Fallback activé si API indisponible
✓ Insensibilité à la casse des devises
```

---

## 🔧 Dépendances

- **HttpURLConnection** : Java standard (aucune dépendance externe)
- **Jackson** : Déjà présent dans le projet (pour parsing JSON)
- **SLF4J** : Déjà présent (pour logging)

---

## 📌 Notes d'intégration

✅ **Respecte la Clean Architecture**
- Service layer autonome
- Pas de logique dans les controllers
- Injection possible des dépendances
- Facilement testable

✅ **Pas de breaking changes**
- Aucun fichier existant modifié (sauf ApiServer pour ajouter l'endpoint)
- Les abonnements gardent leur structure
- Compatible avec la base de donnéesexistante

✅ **Production-ready**
- Gestion complète des erreurs
- Fallback automatique
- Timeouts configurés
- Logging structuré
- Tests unitaires complets

---

## 🚀 Exemples complets

### Exemple 1 : Convertir tous les abonnements en 3 devises

```bash
curl "http://localhost:4567/api/abonnements" | jq -r '.[] | .id' | while read id; do
  echo "=== Abonnement $id ==="
  curl "http://localhost:4567/api/abonnements/$id/convert?currencies=USD,GBP,JPY"
done
```

### Exemple 2 : Tableau comparatif de prix

```javascript
// Dans le frontend
async function getPriceComparison(subscriptionId) {
  const res = await fetch(`/api/abonnements/${subscriptionId}/convert?currencies=USD,GBP,CHF,JPY`);
  const data = await res.json();
  
  console.table({
    Service: data.nomService,
    'EUR (original)': data.originalPrice,
    'USD': data.convertedPrices.USD.toFixed(2),
    'GBP': data.convertedPrices.GBP.toFixed(2),
    'CHF': data.convertedPrices.CHF.toFixed(2),
    'JPY': data.convertedPrices.JPY.toFixed(0)
  });
}
```

---

## 📚 Références

- **API utilisée** : [ExchangeRate-API](https://exchangerate-api.com)
- **Documentation** : Taux de change en temps réel, gratuit, sans clé API requise
- **Formats supportés** : EUR, USD, GBP, CHF, JPY, CAD, AUD, CNY, INR, et plus

