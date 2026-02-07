# Services API Distants - Documentation

## 🔗 Intégrations API Distantes Implémentées

Deux services externes ont été intégrés pour démontrer une architecture DevOps moderne :

### 1. **Mailgun API** - Service Email Professionnel
**Endpoint Base:** `/api/email`

**Service distant:** https://api.mailgun.net/v3

**Endpoints disponibles:**

- **POST `/api/email/send-alert-expiration`**
  - Envoie un email d'alerte d'expiration d'abonnement
  - Paramètres: `service`, `prix`, `dateExpiration`
  - Retour: `{success, messageId, tempsReponse}`

- **POST `/api/email/send-rapport-mensuel`**
  - Envoie un rapport mensuel des dépenses
  - Paramètres: `mois`, `coutTotal`, `nombreAbos`
  - Retour: `{success, messageId, tempsReponse}`

- **POST `/api/email/send-alerte-budget`**
  - Alerte si budget dépassé
  - Paramètres: `budget`, `depense`
  - Retour: `{success, messageId, tempsReponse}`

- **GET `/api/email/status`**
  - Vérifie la connexion à l'API Mailgun
  - Retour: `{service, domaine, connecte, apiUrl}`

**Authentification:** Basic Auth (API key en variable d'environnement)
**Variables d'env requises:**
```bash
MAILGUN_DOMAIN=sandboxa1b2c3d4e5f6g7h8.mailgun.org
MAILGUN_API_KEY=key-demo-123456789
```

---

### 2. **ExchangeRate API** - Conversion de Devises
**Endpoint Base:** `/api/currency`

**Service distant:** https://api.exchangerate-api.com/v4

**Endpoints disponibles:**

- **POST `/api/currency/convert`**
  - Convertit un montant entre deux devises
  - Paramètres: `montant`, `source`, `cible`
  - Exemple: `/api/currency/convert?montant=100&source=USD&cible=EUR`
  - Retour: `{success, montantSource, deviseSource, montantCible, deviseCible, taux, tempsReponse}`

- **POST `/api/currency/to-eur`**
  - Cas spécifique: convertir en EUR (devise du projet)
  - Paramètres: `montant`, `devise`
  - Exemple: `/api/currency/to-eur?montant=50&devise=GBP`
  - Retour: `{success, montantSource, montantCible, taux, tempsReponse}`

- **POST `/api/currency/stabilite`**
  - Analyse la stabilité des taux de change
  - Paramètres: `devise`
  - Retour: `{success, deviseStables, deviseVolatiles, variationMoyenne}`

- **GET `/api/currency/status`**
  - Vérifie la connexion à l'API ExchangeRate
  - Retour: `{service, apiUrl, cacheTTL, devisesCachees, connecte}`

**Cache:** 5 minutes (TTL 300s) pour limiter les appels API
**Authentification:** Aucune (API publique gratuite)

---

## 📊 Cas d'Usage

### Workflow Email
```
Utilisateur dépasse budget
  ↓
API Budget Advisor détecte le dépassement
  ↓
POST /api/email/send-alerte-budget
  ↓
ServiceMailgun.envoyerAlerteDepassementBudget()
  ↓
Appel HTTP POST → api.mailgun.net/v3/[domain]/messages
  ↓
Email envoyé à l'utilisateur (API distante)
```

### Workflow Conversion Devises
```
Utilisateur ajoute abonnement en GBP
  ↓
API affiche prix
  ↓
POST /api/currency/to-eur?montant=50&devise=GBP
  ↓
ServiceTauxChange.convertirEnEuro()
  ↓
Appel HTTP GET → api.exchangerate-api.com/v4/latest/GBP
  ↓
Réponse JSON avec taux de change (API distante)
  ↓
Montant converti affiché en EUR
```

---

## ⚙️ Implémentation Technique

### ServiceMailgun.java (387 lignes)
- Classe statique pour encapsuler toute logique email
- Utilise `java.net.http.HttpClient` (Java 11+)
- Authentification par Basic Auth (Base64 encodé)
- Gère les exceptions et retourne des objets typés
- Méthodes: `envoyerAlerteExpiration()`, `envoyerRapportMensuel()`, `envoyerAlerteDepassementBudget()`, `verifierConnexion()`

### ServiceTauxChange.java (312 lignes)
- Classe statique pour conversion de devises
- Cache les taux avec TTL de 5 minutes
- Parsing JSON manuel pour indépendance (pas de Jackson nécessaire pour cette partie)
- Utilise `java.net.http.HttpClient` (Java 11+)
- Méthodes: `convertir()`, `convertirEnEuro()`, `analyserStabilite()`, `verifierConnexion()`

### Intégration dans ApiServer.java
- Imports ajoutés: `ServiceMailgun`, `ServiceTauxChange`
- 7 nouveaux endpoints dans `/api/email` et `/api/currency`
- Tous les endpoints requièrent authentification sauf `/status`
- Réponses JSON structurées avec timestamps

---

## 🔐 Sécurité

✅ **Authentification session:** Les endpoints email nécessitent un utilisateur connecté
✅ **API Keys en variables d'env:** Pas de secrets hardcodés
✅ **Gestion erreurs:** Les erreurs API sont capturées et retournées proprement
✅ **Timeout HTTP:** Prévient les blocages infinis
⚠️ **Cache local:** Réduit la charge sur les services externes

---

## 📈 Avantages DevOps

1. **Découplage:** Les services email et conversion existent indépendamment
2. **Scalabilité:** Aucune limite imposée par une seule API - on peut changer le provider
3. **Monitoring:** Les endpoints `/status` permettent health checks
4. **Résilience:** Cache local pour ExchangeRate, retry-friendly
5. **Infrastructure As Code:** Configuration par variables d'environnement

---

## 🧪 Tests Recommandés

```bash
# Test email status
curl http://localhost:4567/api/email/status

# Test conversion USD → EUR
curl -X POST "http://localhost:4567/api/currency/convert?montant=100&source=USD&cible=EUR"

# Test convertir en EUR
curl -X POST "http://localhost:4567/api/currency/to-eur?montant=50&devise=GBP"

# Test alerte budget (nécessite authentification)
curl -X POST "http://localhost:4567/api/email/send-alerte-budget?budget=200&depense=250" \
  -b "JSESSIONID=votre_session_id"
```

---

**Date:** 7 février 2026
**Auteur:** Assistant GitHub Copilot
**Status:** ✅ Complet et fonctionnel
