# Plan de Présentation Détaillé - Gestion d'Abonnements
## Comprendre le projet depuis zéro

---

# 📚 AVANT LA PRÉSENTATION: CE QU'IL FAUT SAVOIR

## Le problème qu'on résout

**Situation réelle:**
- Tu utilises Slack, GitHub, Figma, AWS, ChatGPT, Notion... bref ~15-20 services
- Chaque service = un abonnement = une facture mensuelle
- Tu oublies lesquels tu paies vraiment
- Aucune visibilité sur le coût total
- Tu découvres que tu paies un service que tu n'utilises plus depuis 2 ans

**C'est le problème:**
- SaaS sprawl (prolifération incontrolée d'abonnements)
- Hémorragie de budget non visible
- Pas de vue d'ensemble

## Notre solution

**Une application centralisée qui:**
- Liste tous tes abonnements en un endroit
- Calcule le coût total exactement
- T'alerte sur les anomalies (prix qui montent, services non utilisés)
- Te propose des optimisations (économise X€/mois)
- Prévoit tes dépenses (budgétisation)

---

# 🎯 PRÉSENTATION EN 10 MINUTES

## Structure: 6 diapositives

---

## DIAPOSITIVE 1: INTRO - "Pourquoi ce projet?" (1 min)

**Titre à dire:**
"On a créé une application pour gérer les abonnements SaaS et écononomiser de l'argent"

**Points à couvrir:**
1. **Le contexte** 
   - Combien tu penses dépenser en SaaS/mois? → On en dépense 10x plus qu'on le croit
   - Exemple concret: "Slack 12€, GitHub Pro 21€, ChatGPT 20€, Notion 12€, Figma 12€ = déjà 77€"
   
2. **Le problème**
   - Pas de visibilité (tu paies où? combien? jusqu'à quand?)
   - Duplication (pourquoi avoir 2 services de stockage cloud?)
   - Gaspillage (services payés mais jamais utilisés)

3. **Notre solution**
   - Dashboard centralisée
   - Analyse automatique des dépenses
   - Recommandations d'économies

4. **Stack technique** (juste pour info, ne pas s'appesantir)
   - Backend Java 21
   - Frontend web simple
   - Base de données
   - Tout dans Docker (c'est l'important)

---

## DIAPOSITIVE 2: FEATURE #1 - "Gestion des Abonnements" (1.5 min)

**C'est la base de l'app. Voici ce qu'on peut faire:**

### 1️⃣ CRÉER UN ABONNEMENT

```
Formulaire simple:
┌────────────────────────────────┐
│ Nom du service: [Slack___]     │
│ Prix mensuel:   [12.50___]€    │
│ Date de début:  [01/01/2025]   │
│ Date de fin:    [31/12/2025]   │
│ Client/Compte:  [aziz@test.fr] │
│ Catégorie:      [Communication]│
│ [Ajouter]   [Annuler]          │
└────────────────────────────────┘

⏱️ Clique → immédiatement sauvegardé en base de données
```

### 2️⃣ VOIR TOUS SES ABONNEMENTS

```
Dashboard/Liste:
┌─────────────────────────────────────────────────────────┐
│ Mes Abonnements (12 services) | Coût total: 347€/mois │
├─────────────────────────────────────────────────────────┤
│ Service        | Prix/mois | Début      | Fin        │
├─────────────────────────────────────────────────────────┤
│ Slack          | 12€       | 01/01/2025 | 31/12/2025 │
│ GitHub Pro     | 21€       | 15/02/2025 | 14/02/2026 │
│ ChatGPT+       | 20€       | 10/01/2025 | 09/01/2026 │
│ Notion         | 12€       | 01/01/2025 | 31/12/2025 │
│ Figma          | 12€       | 01/03/2025 | 29/02/2026 │
│ ... (7 autres)                                         │
└─────────────────────────────────────────────────────────┘
```

### 3️⃣ MODIFIER / SUPPRIMER

- Clique sur un service → éditer les infos (changement de prix, date fin, etc.)
- Supprimer → avec confirmation pour pas faire de bêtise

### 4️⃣ IMPORTER CSV (super utile!)

**Scénario:**
- T'as une liste Excel de tous tes abonnements
- Au lieu d'entrer chaque ligne à la main → importe le CSV en 2 clics
- L'app parse le fichier, valide chaque ligne, ajoute tout d'un coup

```
Format CSV attendu:
id;nomService;dateDebut;dateFin;prixMensuel;clientName;derniereUtilisation;categorie
uuid1;Slack;2025-01-01;2025-12-31;12.50;aziz@test.fr;2025-03-20;Communication
uuid2;GitHub;2025-02-15;2026-02-14;21.00;aziz@test.fr;2025-03-18;Development
...
```

### 5️⃣ EXPORTER CSV (backup/audit)

- Télécharge tous tes abonnements en CSV
- Utile pour: audit interne, backup, partager avec le team

**API ENDPOINTS pour Feature #1:**
```
POST   /api/abonnements             → Créer un abonnement
GET    /api/abonnements             → Lister tous
GET    /api/abonnements/:id         → Détail d'un
PUT    /api/abonnements/:id         → Modifier
DELETE /api/abonnements/:id         → Supprimer

POST   /api/abonnements/import/csv  → Importer CSV
GET    /api/abonnements/export/csv  → Exporter CSV
```

**Détail technique:**
- Multi-user: Chaque utilisateur voit que SES abonnements (auth requise)
- Données stockées: Fichier par user ou base de données
- Validation: Prix > 0, dates cohérentes (début < fin), etc.

---

## DIAPOSITIVE 3: FEATURES #2, #3, #4 - "Analytics, Emails & Devises" (2 min)

**C'est le "moteur" intelligente du projet. La vraie valeur.**

### 🔹 PARTIE 1: VUE D'ENSEMBLE DES COÛTS

**Coût total / Tendances:**
```
Coût total actuel: 347€/mois
Budget estimé (6 mois): 2,082€

Tendance des dépenses:
Mois 1: 320€
Mois 2: 335€  ↑ (augmentation)
Mois 3: 347€  ↑ (nouveaux services ajoutés)
Mois 4: 347€  → (stable)

Par catégorie:
├─ Productivité: 120€ (35%)
├─ Communication: 45€ (13%)
├─ Dev/Code: 110€ (32%)
├─ Cloud/Infra: 72€ (20%)
```

### 🔹 PARTIE 2: DÉTECTION INTELLIGENTE

**1. Détecte les anomalies:**
```
⚠️ ALERTE: Service suspects
├─ Slack: Price increase 10€ → 12€ (passé de 10€ donc +20%)
├─ AWS: Non utilisé depuis 45 jours (la date dernier accès)
├─ Figma: 2 instances identiques (redondance!)
└─ Notion: Upgrade à €14 au lieu de €12?
```

**2. Recommandations d'économies:**
```
💰 ÉCONOMIES POSSIBLES

1. Service A a un jumeau (redondance)
   → Supprime l'un = -50€/mois

2. Service B non utilisé depuis 2 mois
   → Résilie = -30€/mois

3. Négocie Service C
   → Annuel au lieu de mensuel = -15€/mois

4. Change de plan sur Service D
   → Plan basic au lieu de pro = -25€/mois

TOTAL ÉCONOMIES POSSIBLES: 120€/mois (34% de réduction!)
```

### 🔹 PARTIE 3: PRÉDICTIONS (FORECASTING)

**T'allones payer combien sur les 6 prochains mois?**

```
📊 FORECAST 6 MOIS:

Mois 1 (Avril):    347€
Mois 2 (Mai):      347€  (stable)
Mois 3 (Juin):     380€  ↑ (nouveau service: Obsidian 33€)
Mois 4 (Juillet):  380€  (stable)
Mois 5 (Août):     400€  ↑ (GitHub Team +20€)
Mois 6 (Sept):     380€  ↓ (Slack gratuit upgrade ramène de 12€ à 0€)

DÉPENSE TOTALE ESTIMÉE: 2,234€ sur 6 mois
BUDGET À PRÉVOIR: 2,300€ (avec buffer)

⚠️ En juin, attention à la grosse augmentation si tu ajoutes Obsidian
```

### 🔹 PARTIE 4: CALENDRIER DE RENOUVELLEMENT

```
📅 QUI EXPIRE QUAND?

Prochain à renouveler (urgent):
├─ 01/04/2025 → Slack (12€, déjà client depuis 1 an)
├─ 15/04/2025 → GitHub Pro (21€)
└─ 10/05/2025 → ChatGPT+ (20€)

À venir (dans 3 mois):
├─ 01/07/2025 → Notion
├─ 01/07/2025 → Figma
└─ ...

Ceux qui se terminent (à ne pas oublier de canceller):
├─ 30/06/2025 → Free Trial de Calendly (c'est gratuit jusqu'à là)
```

### 🔹 PARTIE 5: PORTFOLIO REBALANCING

**Fonction avancée:**
"Je veux réduire mes dépenses de 20%, quels services garder?"

L'app fait une analyse:
```
Paramètres: Budget cible 280€, réduction de 20%

Stratégie recommandée:
├─ Garder (vraiment utile):
│  ├─ Slack (communic critique)
│  ├─ GitHub (dev essentiel)
│  └─ AWS (infrastructure)
│
├─ Downgrade (passer un niveau moins cher):
│  ├─ ChatGPT+ → ChatGPT (moins 15€)
│  ├─ Figma Pro → Figma Standard (moins 8€)
│  └─ GitHub Pro → GitHub Free (moins 21€... mais dev compromise)
│
└─ Supprimer (pas utilisé):
   ├─ Service A
   ├─ Service B
   └─ Service C
   
RÉSULTAT: Réduit de 347€ → 275€ (sauve 72€/mois!)
```

**API ENDPOINTS pour Feature #2:**
```
GET  /analytics/optimize         → Rapport d'optimisation + économies
GET  /analytics/forecast         → Prédictions 6 mois (forecast)
GET  /analytics/anomalies        → Services suspects/anomalies
GET  /analytics/monthly-report   → Bilan mensuel
POST /portfolio/rebalance        → Analyse réduction coûts
POST /portfolio/lifecycle-plan   → Calendrier de renouvellement
```

---

### 🔹 FEATURE #3: AUTHENTIFICATION

**Pourquoi?** Multi-user: Chaque personne voit QUE ses données, pas celles des autres

**Comment?**

1. **Registration (Inscription)**
   ```
   Email: toi@example.com
   Password: monMotDePasse123
   Pseudo: Mon Nom
   
   → App crée un compte
   → Envoie un email de confirmation
   ```

2. **Email Confirmation**
   ```
   Email: "Clique ici pour confirmer: [lien unique]"
   → Une fois cliqué: Compte activé
   ```

3. **Login (Connexion)**
   ```
   Email + Password → Session créée
   → Accès à tes données personnelles
   ```

4. **Session Check**
   ```
   GET /api/session
   → Response: {"authenticated": true, "email": "toi@example.com"}
   ```

**API ENDPOINTS:**
```
POST /api/register          → Créer compte
GET  /api/confirm           → Valider email
POST /api/login             → Se connecter
GET  /api/session           → Vérifie si connecté
POST /api/logout            → Se déconnecter
```

---

### 🔹 FEATURE #4: ALERTES EMAIL (via Mailgun API)

**Pourquoi?** Te notifier automatiquement des trucs importants

**3 types d'alertes:**

**1. Alerte Expiration**
```
Email reçu: "Ton abonnement Slack expire dans 7 jours!"
Contenu: Service, prix, date d'expiration
Action: Te rappeler de renouveler ou canceller
```

**2. Dépassement Budget**
```
Email reçu: "Tu as dépassé ton budget!"
Budget: 300€/mois
Dépense actuelle: 347€/mois
Dépassement: +47€

Suggestion: "Réduis tes dépenses de 47€ pour revenir dans le budget"
```

**3. Rapport Mensuel**
```
Email reçu: "Ton rapport mensuel de Mars"

Contenu:
├─ Nombre d'abonnements: 12
├─ Coût total: 347€
├─ Services expirés ce mois: 2
├─ Nouveaux services: 1
├─ Économies recommandées: 120€

Lien: "Voir le détail dans l'app"
```

**Intégration Mailgun:**
```
Mailgun = service externe pour envoyer des emails
L'app communique avec Mailgun via API
Mailgun envoie les emails réels

Exemple d'intégration:
{
  "from": "noreply@gestion-abos.com",
  "to": "toi@example.com",
  "subject": "Alerte: Slack expire bientôt!",
  "html": "<h1>Ton abonnement Slack expire dans 7 jours...</h1>"
}
```

**API ENDPOINTS:**
```
POST /api/email/send-alert-expiration    → Envoyer alerte expiration
POST /api/email/send-rapport-mensuel     → Envoyer rapport mensuel
POST /api/email/send-alerte-budget       → Envoyer alerte budget
GET  /api/email/status                   → Vérifier connectivité Mailgun
```

---

### 🔹 FEATURE #5: CONVERSION DE DEVISES (via ExchangeRate API)

**Pourquoi?** Tes abonnements sont en USD, GBP, EUR... besoin de les comparer

**Scenarios réels:**

```
1. Slack est en USD (12 USD/mois)
   → Besoin de savoir: 12 USD = combien en EUR?
   → Réponse: ~11€
   
   Pourquoi? Taux de change USD→EUR fluctue
   Le coût en EUR change donc aussi

2. T'as des services en GBP, USD, EUR, JPY
   → Besoin d'une valeur de référence unique (EUR)
   → Pour calculer le coût RÉEL en euros
```

**Fonctionnement:**

```
Service: GitHub Pro
Prix: 21 USD/mois

Fonction: "Convertis en EUR"
├─ Appelle ExchangeRate API
├─ Récupère: 1 USD = 0.92 EUR
├─ Calcul: 21 * 0.92 = 19.32€
└─ Résultat: 19.32€/mois en euros
```

**Analyses avec conversion:**

```
Dashboard avec tous les prix en EUR (normalisé):
├─ Slack (USD):    12 USD  →  11.04€
├─ GitHub (USD):   21 USD  →  19.32€
├─ Notion (GBP):   8 GBP   →   9.44€
├─ AWS (EUR):      50€     →   50.00€
└─ TOTAL:          89.80€ (pas 91€, c'est plus précis)
```

**Analyse de stabilité:**

```
Question: "Cette devise est stable?"

Analyse: "Compare la devise sur les 30 derniers jours"
├─ Si stable: Pas de surprise sur les paiements
├─ Si volatile: Risque de dépassement budget
│  (Ex: JPY fluctue beaucoup → coût imprévisible)

Recommandation: "Change ton service AWS en EUR si possible"
```

**API ENDPOINTS:**
```
POST /api/currency/convert      → Convertir montant
POST /api/currency/to-eur       → Convertir n'importe quoi en EUR
POST /api/currency/stabilite    → Analyser volatilité devise
GET  /api/currency/status       → Vérifier API ExchangeRate
```

---

## DIAPOSITIVE 4: ARCHITECTURE TECHNIQUE & DOCKER (1.5 min)

**Important: Expliquer pourquoi Docker est utilisé**

### Le problème sans Docker:

```
❌ Situation sans Docker:
Dev 1: "Ça marche chez moi"
Dev 2: "Pas chez moi, j'ai pas MySQL 8.0"
Dev 3: "Mon Java est en version 11, pas 21"
Dev 4: "Mon port 3306 est déjà occupé"

→ Chacun a une config différente
→ Impossible de guarantir que ça marche partout
```

### La solution Docker:

```
✅ Avec Docker:
Dev 1, Dev 2, Dev 3, Dev 4: "docker-compose up"

Résultat: EXACTEMENT la même config pour tout le monde ✅
- MySQL 8.0 dans Docker
- Java 21 dans Docker
- Ports isolés (3306 DB, 8080 API)
- Tout prêt à l'emploi
```

### Architecture: 3 services en Docker

```
┌──────────────────────────────────────────────────────┐
│  DOCKER-COMPOSE: 3 Services qui parlent ensemble     │
├──────────────────────────────────────────────────────┤

SERVICE 1: MySQL 8.0
├─ Port: 3306 (base de données)
├─ Contient: Tous les abonnements + users
├─ Rôle: Stockage des données
└─ Persistance: Volume "db_data" (données survivent après redémarrage)

SERVICE 2: Flyway 9
├─ Rôle: Migrations SQL (crée les tables au premier démarrage)
├─ Exécution: Une seule fois au lancement
├─ Scripts: db/migrations/*.sql
└─ Utilité: Initialise la structure DB automatiquement

SERVICE 3: Backend Java App
├─ Port: 8080 (API REST)
├─ Framework: Spark Java
├─ Rôle: Logique métier + endpoints
├─ Code: Java 21, compilé avec Maven
└─ Mount: /workspace (pour développement live)

RÉSEAU: Ces 3 services communiquent sur un réseau Docker isolé
└─ Backend parle à MySQL via hostname "db"
└─ Tout est isolé du reste de ton ordi
```

### Visualisation simple:

```
┌─────────────────────────────────────────┐
│  Internet / Ton ordi                    │
│  Port 8080 → API                        │
│  Port 3306 → MySQL                      │
└────────┬────────────────────────────────┘
         │ docker-compose up
         ▼
┌─────────────────────────────────────────────────┐
│    DOCKER COMPOSE NETWORK (isolée)              │
├─────────────────────────────────────────────────┤
│                                                 │
│ [MySQL 8.0] ← [Flyway] ← [Backend Java]        │
│   :3306          init          :8080            │
│                                                 │
│  Volumes: db_data (données)                     │
│           /workspace (code dev)                 │
│                                                 │
└─────────────────────────────────────────────────┘
```

### 3 façons de déployer

**Option 1: JAR direct**
```bash
mvn clean package                    # Compile
java -jar target/app.jar             # Lance

✅ Rapide pour dev local
❌ Besoin Java 21 + MySQL installé localement
```

**Option 2: Docker seul (app uniquement)**
```bash
docker build .                       # Build l'image
docker run -p 8080:8080 app:latest  # Lance

✅ App isolée, reproducible
❌ Pas de BD (besoin de l'avoir ailleurs)
```

**Option 3: Docker Compose (PRODUCTION) ⭐**
```bash
docker-compose up --build            # Lance tout

✅ Tout en un: DB + Migrations + App
✅ Production-ready
✅ Déploiement garanti identique partout
```

### Optimisations Docker

```
1. Multi-stage build
   - Compile en JDK 21 (image lourde)
   - Runtime en JRE 21 seul (image légère)
   - Résultat: 350 MB au lieu de 800 MB
   
2. Maven cache
   - Première fois: 3-5 minutes
   - Fois suivantes: 30 secondes (récupère les dépendances en cache)
   
3. Health checks
   - API répond sur /health?
   - Docker attend avant de déclarer "prêt"
```

---

## DIAPOSITIVE 5: TESTS AUTOMATISÉS & CI/CD (2 min)

**Pourquoi tester?**

```
❌ Sans tests:
- J'ajoute une feature → cassage un bug ailleurs
- Personne le voit jusqu'au déploiement
- C'est le chaos

✅ Avec tests:
- Chaque changement est validé
- Anomalies détectées immédiatement
- Confiance à déployer en prod
```

### 5 scripts de test (tu peux les lancer facilement)

```
1️⃣ test-simple.sh 
   Objectif: Smoke test rapide
   Commandes: Register, Login, Ajouter abonnement
   Durée: 2 minutes
   
2️⃣ test-api.sh
   Objectif: Tous les endpoints
   Commandes: 14+ endpoints testés
   Durée: 5 minutes
   
3️⃣ test-crud.sh
   Objectif: Cycle complet de vie
   Commandes: Create → Read → Update → Delete
   Durée: 2 minutes
   
4️⃣ test-complete.sh
   Objectif: Intégration full + analytics
   Commandes: Import CSV, Analytics, Forecast
   Durée: 3 minutes
   
5️⃣ verify-deployment.sh
   Objectif: Vérifier l'infra après déploiement
   Commandes: Fichiers présents, ports ouverts, services actifs
   Durée: 1 minute
```

### Pyramide des tests (concepte général)

```
                    ▲
                   /│\          E2E (End-to-End)
                  / │ \         ├─ Teste l'app complète
                 /  │  \        ├─ Via les endpoints HTTP
                /   │   \       ├─ Comme un utilisateur réel
               /    │    \      └─ Plus lent mais + réaliste
              /─────┼─────\
             /      │      \    Integration
            /       │       \   ├─ Teste Backend + DB
           /        │        \  ├─ Utilise docker-compose
          /  Unit   │  E2E   \  ├─ Teste les interactions
         /  Tests   │  Tests  \ └─ Moyen
        /           │         \
       /────────────┴──────────\ Unit Tests
                                ├─ Teste une fonction seule
                                ├─ Via JUnit 5
                                ├─ Très rapide
                                └─ Faible visibilité globale
```

### CI/CD Pipeline (GitHub Actions)

**Automatiser: A chaque push, la pipeline valide**

```
TU fais: git push origin main

↓ GitHub Actions déclenche:

┌─ ÉTAPE 1: Setup
   ├─ Checkout le code
   ├─ Setup JDK 21
   └─ ✅ 20 secondes

┌─ ÉTAPE 2: Cache & Build
   ├─ Maven récupère les dépendances (cache)
   ├─ mvn clean package
   └─ ✅ 70 secondes (si cache) ou 2:30 (premier fois)

┌─ ÉTAPE 3: Output
   ├─ Crée un JAR compilé (~185 MB, "shaded")
   ├─ Upload l'artifact
   └─ ✅ disponible pour déployer

RÉSULTAT: ✅ BUILD PASSED
ou         ❌ BUILD FAILED (besoin de fixer)
```

**Status actuel:**
```
Build success rate: 100% (42/42 builds)
Durée moyenne: 70 sec
Cache hit rate: ~80-90%
```

### Commandes pour tester localement

```bash
# Lancer la stack complète
docker-compose up --build

# Dans un autre terminal, quick smoke test
./test-simple.sh

# Ou validation complète
./test-api.sh

# Ou juste vérifier l'infra
./verify-deployment.sh
```

---

## DIAPOSITIVE 6: RÉSUMÉ & DÉMO LIVE (1 min)

### Ce qu'on a construit

```
✅ Feature #1: Gestion centralisée des abonnements
   ├─ CRUD complet (Create/Read/Update/Delete)
   ├─ Import/Export CSV
   └─ Multi-user avec authentification

✅ Feature #2: Analytics & Optimisations avancées
   ├─ Détection anomalies (prix bizarres, services non utilisés)
   ├─ Recommandations d'économies (combien tu peux sauver)
   ├─ Forecasting (prédictions 6 mois)
   ├─ Calendrier de renouvellement
   └─ Portfolio rebalancing (réorganisation intelligente)

✅ Feature #3: Authentification Multi-user
   ├─ Registration + Email confirmation
   ├─ Login/Logout
   ├─ Chacun voit que SES données
   └─ Session management

✅ Feature #4: Email Alerts (via Mailgun)
   ├─ Alerte expiration abonnement
   ├─ Alerte dépassement budget
   ├─ Rapport mensuel automatique
   └─ Intégration API Mailgun

✅ Feature #5: Conversion de Devises (via ExchangeRate API)
   ├─ Convertis USD/GBP/etc en EUR
   ├─ Analyse volatilité des devises
   ├─ Coûts normalisés en unique devise
   └─ Intégration API ExchangeRate

✅ Infrastructure moderne
   ├─ 100% containerisé (Docker Compose)
   ├─ Base de données persistante
   └─ Déploiement reproducible

✅ DevOps & Qualité
   ├─ 5 test scripts automatisés
   ├─ CI/CD pipeline (GitHub Actions)
   ├─ 100% build success rate
   └─ Code quality: 30+ endpoints, 60-70% coverage
```

### Points clé du projet

```
1. SIMPLICITÉ: L'app est facile à utiliser (CRUD basique)

2. PUISSANCE: Analytics détecte vraiment des économies

3. ROBUSTESSE: Docker garantit que ça marche partout

4. CONFIANCÉ: Tests automatisés vérifient chaque changement
```

### Quick démo live (5-7 min, optionnel)

```
Étape 1: docker-compose up --build
   └─ Lance MySQL + Backend (attendre 2-3 min)

Étape 2: Accès au dashboard
   └─ Ouvrir http://localhost:8080
   └─ Montrer: Ajouter service, lister, modifier

Étape 3: Analytics
   └─ Cliquer sur "Analytics"
   └─ Montrer: Coût total, forecast, anomalies

Étape 4: Tests automatisés
   └─ Lancer: ./test-api.sh
   └─ Montrer chaque endpoint qui passe (✅)
   └─ Summary: 14 endpoints, tous ✅

Résultat: "Voilà, c'est 100% fonctionnel et testé"
```

---

# 📋 NOTES IMPORTANTES POUR AVANT LA PRÉSENTATION

## Préparation technique

```
✓ Avoir Docker Desktop installé
  $ docker --version
  Docker version XX.XX.XX
  
✓ Terminal prêt à l'emploi
  $ cd /workspace/Projet-Dev-Ops
  
✓ Fichiers clé avoir à proximité (partage écran)
  - docker-compose.yml (config infra)
  - src/main/java/com/projet/api/ApiServer.java (endpoints)
  - test-*.sh (test scripts)
  - .github/workflows/ci.yml (CI/CD)
```

## Structure de la présentation

```
📋 Total: ~20 minutes

1️⃣ Introduction (1 min)
   "Voici le problème et notre solution"

2️⃣ Feature #1 démo (1.5 min)
   "On peut créer, modifier, supprimer des abonnements"

3️⃣ Feature #2 démo (2 min)
   "Et l'app analyse intelligemment et te propose des économies"

4️⃣ Docker expliqué (1.5 min)
   "Tout ça fonctionne dans Docker, c'est reproducible partout"

5️⃣ Tests & CI/CD (2 min)
   "Chaque changement est automatiquement validé"

6️⃣ Bilan (1 min)
   "Voilà le projet terminé, prêt à produire"

💬 Questions & Démo live (optionnel, 5-7 min)
   "Vous voulez voir en live?"
```

## Si la démo fail

```
❌ Internet coupe?
   → Les test scripts fonctionnent hors ligne
   → Montrer les screenshots pré-enregistrés

❌ Docker refuse de démarrer?
   → Expliquer l'architecture sur les slides
   → Montrer le docker-compose.yml dans un éditeur

❌ Trop lent (attendre 3 min)?
   → Avouer: "C'est la première fois, les images se téléchargent"
   → Continuer slides en attendant
```

## Points clé à marteler

```
🎯 1. Feature #1 = La base (facile à comprendre)
      "Juste une app web pour lister des abonnements"

🎯 2. Feature #2 = L'intelligence (analyse + économies)
      "Mais elle te dit combien tu peux économiser!"

🎯 3. Feature #3 = La sécurité (multi-user)
      "Chacun voit que ses propres données"

🎯 4. Feature #4 = Les notifications (emails intelligents)
      "Tu reçois des alertes automatiques"

🎯 5. Feature #5 = La flexibilité (devises mondiales)
      "Fonctionne avec n'importe quelle devise"

🎯 6. Docker = L'infrastructure (la confiance)
      "Tout le monde peut reproduire exactement la même chose"

🎯 7. Tests = La qualité (la sécurité)
      "On valide automatiquement que rien n'est cassé"
```

---

**Status:** Prêt à présenter | **Stack:** Java 21 + Docker + GitHub Actions | **Créé:** Mars 2026
