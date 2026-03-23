# 🖥️ Exécution Backend CLI (Sans Frontend)

**Feature:** Backend exécutable en mode CLI pur (demande S1 du professeur)  
**Date:** Mars 2026

---

## 🎯 Objectif

Permettre l'exécution du backend Java **sans interface web**, uniquement en ligne de commande. Mode idéal pour:
- Traitement batch
- Scripting automatisé
- Tests API purs
- Déploiement headless (serveur)
- Intégration DevOps

---

## 🚀 Démarrage Rapide - Mode CLI

### Option 1: Mode API Server (Par défaut)

**Exécution:**
```bash
java -jar target/gestion-abonnements-1.0-SNAPSHOT.jar api
```

**Résultat:**
```
API server démarré sur http://localhost:4567
```

**Utilisation:**
- Frontend web accède via `http://localhost:4567`
- REST API sur `http://localhost:4567/api/*`
- CORS enabled pour requêtes externes

---

### Option 2: Mode Dashboard CLI (S1 Feature)

**Exécution:**
```bash
java -jar target/gestion-abonnements-1.0-SNAPSHOT.jar dashboard file=data/abonnements.csv
```

**Paramètres:**
- `dashboard` - Lance le mode tableau de bord CLI
- `file=` - Chemin fichier abonnements (CSV)
- Options: `file=`, `format=`, `output=`

**Exemple complet:**
```bash
java -jar target/gestion-abonnements-1.0-SNAPSHOT.jar dashboard \
  file=data/abonnements.csv \
  format=detailed \
  output=console
```

**Résultat:**
```
╔════════════════════════════════════════════╗
║   GESTION D'ABONNEMENTS - Dashboard CLI    ║
╠════════════════════════════════════════════╣
║ Total Abonnements: 12                      ║
║ Dépense Mensuelle: €2,450.00               ║
║ Dépense Annuelle: €29,400.00               ║
║ Prévision 3 mois: €7,350.00                ║
╚════════════════════════════════════════════╝

📋 ABONNEMENTS DÉTAIL:
┌─────────────────────────────────────────────┐
│ Service        │ Client  │ Prix/Mois │ Fin │
├─────────────────────────────────────────────┤
│ Netflix        │ Client A│ €15.99   │ 2025-12-31
│ Office 365     │ Client A│ €12.50   │ 2025-11-15
│ AWS            │ Client B│ €500.00  │ 2025-09-30
│ Slack          │ Client B│ €8.00    │ 2025-07-20
│ ...            │ ...     │ ...      │ ...
└─────────────────────────────────────────────┘
```

---

### Option 3: Mode Batch Processing

**Exécution:**
```bash
java -jar target/gestion-abonnements-1.0-SNAPSHOT.jar batch \
  operation=optimize \
  format=json \
  output=results.json
```

**Opérations disponibles:**
- `optimize` - Lancer optimisation
- `forecast` - Prévisions 6 mois
- `anomalies` - Détecter anomalies
- `duplicates` - Trouver doublons
- `export` - Exporter données

**Résultat:**
```json
{
  "operation": "optimize",
  "status": "success",
  "results": {
    "totalSavings": 245.50,
    "opportunities": 5,
    "recommendations": [...]
  },
  "executionTime": "1234ms"
}
```

---

### Option 4: Mode Help (Affiche toutes options)

**Exécution:**
```bash
java -jar target/gestion-abonnements-1.0-SNAPSHOT.jar help
```

**Résultat:**
```
╔══════════════════════════════════════════════════════════════╗
║        GESTION D'ABONNEMENTS - Help Available               ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║ USAGE:                                                       ║
║   java -jar gestion-abonnements-*.jar [COMMAND] [OPTIONS]   ║
║                                                              ║
║ COMMANDS:                                                    ║
║   api              - Start REST API server (default)         ║
║   dashboard        - Show terminal dashboard                ║
║   batch            - Run batch operations                   ║
║   import           - Import abonnements from file           ║
║   export           - Export abonnements to file             ║
║   help             - Show this help                         ║
║                                                              ║
║ OPTIONS:                                                     ║
║   file=PATH        - Input/output file path                 ║
║   format=FORMAT    - Output format (json|csv|detailed)      ║
║   port=PORT        - API server port (default: 4567)        ║
║   output=FORMAT    - Output destination (console|file)      ║
║                                                              ║
║ EXAMPLES:                                                    ║
║                                                              ║
║   1. Start API server:                                       ║
║      java -jar app.jar api                                  ║
║                                                              ║
║   2. Show dashboard:                                         ║
║      java -jar app.jar dashboard file=data/abos.csv         ║
║                                                              ║
║   3. Run batch optimization:                                ║
║      java -jar app.jar batch operation=optimize             ║
║                                                              ║
║   4. Import data:                                            ║
║      java -jar app.jar import file=data/abos.csv            ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 🛠️ Commandes Complètes (Mode CLI)

### 1️⃣ Dashboard Détaillé
```bash
java -jar backend/target/gestion-abonnements-1.0-SNAPSHOT.jar \
  dashboard \
  file=backend/data/abonnements.txt \
  format=detailed
```

### 2️⃣ Export JSON
```bash
java -jar backend/target/gestion-abonnements-1.0-SNAPSHOT.jar \
  export \
  file=backend/data/abonnements.txt \
  format=json \
  output=export.json
```

### 3️⃣ Analyse Optimisation
```bash
java -jar backend/target/gestion-abonnements-1.0-SNAPSHOT.jar \
  batch \
  operation=optimize \
  file=backend/data/abonnements.txt \
  output=console
```

### 4️⃣ Prévisions Coûts
```bash
java -jar backend/target/gestion-abonnements-1.0-SNAPSHOT.jar \
  batch \
  operation=forecast \
  months=12 \
  format=json \
  output=forecast.json
```

### 5️⃣ Détection Anomalies
```bash
java -jar backend/target/gestion-abonnements-1.0-SNAPSHOT.jar \
  batch \
  operation=anomalies \
  file=backend/data/abonnements.txt \
  output=anomalies.json
```

---

## 📊 Comparison: API vs CLI Mode

| Aspect | API Mode | CLI Mode |
|--------|----------|----------|
| **Frontend** | ✅ Web UI | ❌ None |
| **Port 4567** | ✅ Listening | ❌ N/A |
| **HTTP Requests** | ✅ Yes | ❌ No |
| **Terminal Output** | ❌ No | ✅ Yes |
| **Batch Processing** | ✅ Via API | ✅ Direct |
| **Automation** | ✅ Scripts | ✅ Scripts |
| **Use Case** | Web app | Server/Batch |

---

## 🔄 Scripts Automatisés (CLI)

### Script: Optimisation Automatique
```bash
#!/bin/bash
# auto-optimize.sh

echo "🚀 Lancement optimisation automatique..."

java -jar backend/target/gestion-abonnements-1.0-SNAPSHOT.jar \
  batch \
  operation=optimize \
  file=backend/data/abonnements.txt \
  output=optimization_$(date +%Y%m%d_%H%M%S).json

echo "✅ Optimisation terminée!"
echo "📁 Résultats dans: optimization_*.json"
```

**Exécution:**
```bash
bash auto-optimize.sh
```

### Script: Rapport Mensuel
```bash
#!/bin/bash
# monthly-report.sh

MONTH=$(date +%B_%Y)

java -jar backend/target/gestion-abonnements-1.0-SNAPSHOT.jar \
  batch \
  operation=export \
  file=backend/data/abonnements.txt \
  format=detailed \
  output=report_${MONTH}.txt

echo "📊 Rapport généré: report_${MONTH}.txt"
```

---

## 🐳 Docker CLI Mode

**Exécuter backend CLI dans Docker:**
```bash
docker run -v ./data:/app/data gestion-abonnements:latest \
  java -jar app.jar dashboard file=/app/data/abonnements.txt
```

**Résultat:** Dashboard CLI dans container Docker

---

## ✅ Features Implémentées (S1 Requirement)

**Original S1 Request:** "Exécuter backend seul sans frontend"

**Implémentation:**
- ✅ Mode API standalone
- ✅ Mode Dashboard CLI
- ✅ Mode Batch processing
- ✅ Mode Import/Export
- ✅ Help command
- ✅ Configuration via arguments

**Validation:**
```bash
# Vérifier que mode CLI fonctionne
java -jar backend/target/gestion-abonnements-1.0-SNAPSHOT.jar help

# Doit afficher: Help Available ✅
```

---

## 🎯 Cas d'Usage Réels

### 1. Analyse Rapport Mensuel (Cron Job)
```bash
# Ajouter dans crontab
0 1 * * * cd /app && java -jar app.jar batch operation=export output=/reports/$(date +\%Y\%m\%d).json
```
Génère rapport chaque jour à 1h du matin

### 2. Pipeline CI/CD (GitHub Actions)
```yaml
- name: Run CLI Optimization
  run: java -jar app.jar batch operation=optimize output=results.json
  
- name: Upload Results
  uses: actions/upload-artifact@v2
  with:
    name: optimization-results
    path: results.json
```

### 3. Healthcheck Docker
```bash
docker run --name app gestion-abonnements
healthcheck:
  test: java -jar app.jar dashboard > /dev/null 2>&1
  interval: 30s
  timeout: 10s
```

---

## 🔍 Debugging CLI

### Verbose Output
```bash
java -jar app.jar dashboard \
  file=data/abonnements.txt \
  --verbose \
  --debug
```

### Log to File
```bash
java -jar app.jar api \
  --log-file=app.log \
  --log-level=DEBUG
```

### Output Redirection
```bash
# Save output to file
java -jar app.jar dashboard file=data/abos.txt > output.txt 2>&1

# Pipe to another command
java -jar app.jar export file=data/abos.txt | jq '.data'
```

---

## 📝 Notes

- **Default:** Sans arguments → mode API (port 4567)
- **CLI Feature:** Implémenté pour S1, maintenu en S2
- **Headless:** Parfait pour serveurs sans GUI
- **Automation:** Idéal pour scripts batch & cron jobs
- **Docker:** Fonctionne aussi en containers

---

## 🎓 Résumé

**CLI Mode permet:**
✅ Exécution backend sans frontend  
✅ Batch processing automatisé  
✅ Dashboard terminal  
✅ Export/Import données  
✅ Serverless deployment  
✅ CI/CD automation  

**Commande principale:**
```bash
java -jar target/gestion-abonnements-1.0-SNAPSHOT.jar [COMMAND]
```

---

**Status:** ✅ Implémenté & Fonctionnel  
**S1 Requirement:** ✅ Satisfait
