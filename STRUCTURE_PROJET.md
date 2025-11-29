# 📁 Structure du Projet - Gestion d'Abonnements v2.0

## 🌳 Arborescence Organisée

```
Projet-Dev-Ops/
│
├── 📄 README.md                    # Documentation principale du projet
├── 📄 pom.xml                      # Configuration Maven
├── 📄 server.log                   # Logs du serveur
│
├── 📂 src/                         # Code source Java
│   ├── main/
│   │   ├── java/com/
│   │   │   ├── example/abonnement/ # Logique métier principale
│   │   │   └── projet/api/         # API REST & Services
│   │   │       ├── ApiServer.java
│   │   │       └── EmailService.java
│   │   └── resources/static/       # Interface Web (HTML/CSS/JS)
│   │       ├── index.html          # Dashboard principal
│   │       ├── analytics.html      # Analytics avancés
│   │       ├── expenses.html       # Gestion dépenses
│   │       ├── export-import.html  # Export/Import données
│   │       ├── bank-integration.html # 🆕 Intégration bancaire
│   │       ├── notifications.html  # Système notifications
│   │       ├── themes.html         # Personnalisation thèmes
│   │       ├── *.js                # Scripts JavaScript
│   │       └── *.css               # Feuilles de style
│   └── test/                       # Tests unitaires/intégration
│
├── 📂 data/                        # 🆕 Données de l'application
│   ├── abonnements/                # Abonnements par utilisateur
│   ├── backup/                     # Sauvegardes automatiques
│   ├── examples/                   # Fichiers d'exemple
│   │   ├── exemple_import_bancaire.csv
│   │   ├── test_import.csv
│   │   └── abonnements_test_export.json
│   ├── abonnements.txt             # Base de données abonnements
│   └── users-db.txt                # Base utilisateurs
│
├── �� docs/                        # 🆕 Documentation complète
│   ├── 📂 archives/                # Historique développement
│   │   ├── CORRECTIONS_*.md
│   │   ├── RECAP_*.md
│   │   └── SYNTHESE_*.md
│   ├── INDEX.md                    # 🆕 Navigation documentation
│   ├── QUICKSTART_BANQUE.md        # 🆕 Guide rapide bancaire
│   ├── INTEGRATION_BANCAIRE.md     # 🆕 Doc intégration bancaire
│   ├── ARCHITECTURE_TECHNIQUE.md   # Architecture système
│   ├── CAHIER_DES_CHARGES.md       # Spécifications
│   ├── API_documentation.md        # Documentation API
│   ├── GUIDE_*.md                  # Guides utilisateur
│   └── Fiche_Fonctionnalite_*.md   # Fiches détaillées
│
├── 📂 tests/                       # Tests & Scénarios
│   ├── scenarios_tests/
│   ├── tests_integration/
│   └── tests_unitaires/
│
└── 📂 support/                     # Support utilisateur
    ├── screenshots/
    └── videos/                     # Tutoriels vidéo
```

## 🎯 Points Clés de l'Organisation

### ✅ Ce qui a été fait

1. **Nettoyage Racine**
   - ✅ Seul README.md reste à la racine
   - ✅ Suppression fichiers temporaires
   - ✅ Suppression dependency-reduced-pom.xml

2. **Organisation `/data/`**
   - ✅ Données actives : `abonnements.txt`, `users-db.txt`
   - ✅ Exemples : CSV/JSON de test
   - ✅ Backups : Sauvegardes automatiques

3. **Organisation `/docs/`**
   - ✅ Documentation technique
   - ✅ Guides utilisateur
   - ✅ Archives du développement dans `/archives/`
   - ✅ INDEX.md pour navigation

4. **Code Source `/src/`**
   - ✅ Backend Java organisé
   - ✅ Frontend moderne (12 pages HTML)
   - ✅ API REST fonctionnelle

## 📊 Statistiques du Projet

### Code
- **48 fichiers modifiés** dans le dernier commit
- **+9371 lignes ajoutées**
- **-114 lignes supprimées**

### Frontend
- **12 pages HTML** complètes
- **12 scripts JavaScript**
- **Design responsive** glassmorphisme

### Documentation
- **25+ fichiers** de documentation
- **INDEX.md** avec navigation complète
- **Archives organisées** dans `docs/archives/`

## 🚀 Accès Rapide

### Pour Commencer
1. Lire **README.md** (racine)
2. Consulter **docs/QUICKSTART_BANQUE.md**
3. Explorer **docs/INDEX.md**

### Développeurs
- Code source : `src/main/java/`
- Frontend : `src/main/resources/static/`
- API : `docs/API_documentation.md`

### Documentation
- Navigation : `docs/INDEX.md`
- Architecture : `docs/ARCHITECTURE_TECHNIQUE.md`
- Guides : `docs/GUIDE_*.md`

## 🎓 Structure Pédagogique

**Organisation pensée pour :**
- ✅ Faciliter la compréhension
- ✅ Permettre navigation rapide
- ✅ Séparer dev/prod
- ✅ Archiver l'historique

**Principes appliqués :**
- �� Séparation concerns (data/docs/src)
- 📚 Documentation structurée
- 🗄️ Archives préservées mais isolées
- 🧹 Racine propre et claire

**Dernière réorganisation :** 29 novembre 2024  
**Commit :** b336a69 - "feat: ajout module intégration bancaire + refonte interface v2.0"
