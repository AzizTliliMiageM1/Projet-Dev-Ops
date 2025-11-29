# 📚 Index de la Documentation

## 📁 Organisation de la Documentation

La documentation est organisée en **4 catégories principales** :

```
docs/
├── INDEX.md (ce fichier)
├── guides/              (guides utilisateur)
├── techniques/          (documentation technique)
├── fonctionnalites/     (fiches détaillées)
├── integration-bancaire/ (module bancaire)
└── archives/            (historique développement)
```

---

## 📖 Guides Utilisateur (`/guides/`)

Guides pratiques pour utiliser l'application.

### Guides disponibles :

- **[QUICKSTART_BANQUE.md](guides/QUICKSTART_BANQUE.md)**  
  Démarrage rapide intégration bancaire (5 minutes)
  
- **[GUIDE_TEST_RAPIDE.md](guides/GUIDE_TEST_RAPIDE.md)**  
  Guide de test rapide de l'application
  
- **[GUIDE_MODULE_DEPENSES.md](guides/GUIDE_MODULE_DEPENSES.md)**  
  Utilisation du module de gestion des dépenses
  
- **[GUIDE_UTILISATION_MAIN.md](guides/GUIDE_UTILISATION_MAIN.md)**  
  Guide d'utilisation principal et complet
  
- **[GUIDE_NOUVELLES_FONCTIONNALITES.md](guides/GUIDE_NOUVELLES_FONCTIONNALITES.md)**  
  Présentation des nouvelles fonctionnalités v2.0

---

## 🔧 Documentation Technique (`/techniques/`)

Documentation pour développeurs et architecture du système.

### Documents disponibles :

- **[ARCHITECTURE_TECHNIQUE.md](techniques/ARCHITECTURE_TECHNIQUE.md)**  
  Architecture complète du projet (frontend/backend/base de données)
  
- **[CAHIER_DES_CHARGES.md](techniques/CAHIER_DES_CHARGES.md)**  
  Spécifications et exigences du projet
  
- **[API_documentation.md](techniques/API_documentation.md)**  
  Documentation de l'API REST (endpoints, exemples)
  
- **[PLAN_INTEGRATION_FRONTEND.md](techniques/PLAN_INTEGRATION_FRONTEND.md)**  
  Plan d'intégration et architecture frontend

---

## ⚙️ Fiches Fonctionnalités (`/fonctionnalites/`)

Documentation détaillée de chaque fonctionnalité avec diagrammes.

### Fonctionnalités CRUD & Core :

- **[Fiche_Fonctionnalite_CRUD.md](fonctionnalites/Fiche_Fonctionnalite_CRUD.md)**  
  Gestion complète des abonnements (Create, Read, Update, Delete)  
  📊 [Diagramme](fonctionnalites/Fiche_Fonctionnalite_CRUD_Diagram.png)
  
- **[Fiche_Fonctionnalite_Alerte_Inactivite.md](fonctionnalites/Fiche_Fonctionnalite_Alerte_Inactivite.md)**  
  Système d'alertes d'inactivité intelligentes  
  📊 [Diagramme](fonctionnalites/Fiche_Fonctionnalite_Alerte_Inactivite_Diagram.png)
  
- **[Fiche_Fonctionnalite_UUID.md](fonctionnalites/Fiche_Fonctionnalite_UUID.md)**  
  Système d'identifiants uniques universels

### Import/Export :

- **[Fiche_Fonctionnalite_Export_JSON.md](fonctionnalites/Fiche_Fonctionnalite_Export_JSON.md)**  
  Exportation des données au format JSON
  
- **[Fiche_Fonctionnalite_Import_JSON.md](fonctionnalites/Fiche_Fonctionnalite_Import_JSON.md)**  
  Importation des données depuis JSON

### Interface :

- **[Fiche_Fonctionnalite_Interface_Console.md](fonctionnalites/Fiche_Fonctionnalite_Interface_Console.md)**  
  Interface en ligne de commande

### Module Dépenses :

- **[FONCTIONNALITE_DEPENSES.md](fonctionnalites/FONCTIONNALITE_DEPENSES.md)**  
  Documentation complète du module de gestion des dépenses

---

## 🏦 Intégration Bancaire (`/integration-bancaire/`)

Documentation du module d'intégration bancaire intelligente.

- **[INTEGRATION_BANCAIRE.md](integration-bancaire/INTEGRATION_BANCAIRE.md)**  
  Documentation complète :
  - Import relevés CSV/OFX/QIF
  - Détection automatique abonnements cachés
  - Rapprochement intelligent
  - Simulation solde virtuel
  - Guides d'utilisation et exemples

---

## 🗄️ Archives (`/archives/`)

Historique du développement et documents de travail.

- **[INDEX_ARCHIVES.md](archives/INDEX_ARCHIVES.md)**  
  Index complet des archives de développement
  - `/corrections/` - Corrections de bugs
  - `/recapitulatifs/` - Synthèses fonctionnalités
  - `/historique/` - Journal de développement
  - `/listes/` - Inventaires fichiers

---

## 🔍 Navigation Rapide

### Par Type d'Utilisateur

**👤 Nouvel Utilisateur**
1. Lire `/README.md` (racine du projet)
2. Suivre `guides/QUICKSTART_BANQUE.md`
3. Consulter `guides/GUIDE_NOUVELLES_FONCTIONNALITES.md`

**👨‍💻 Développeur**
1. Étudier `techniques/ARCHITECTURE_TECHNIQUE.md`
2. Consulter `techniques/API_documentation.md`
3. Lire les fiches dans `fonctionnalites/`

**👨‍💼 Chef de Projet**
1. Lire `techniques/CAHIER_DES_CHARGES.md`
2. Consulter `techniques/ARCHITECTURE_TECHNIQUE.md`
3. Vérifier `archives/recapitulatifs/`

**🧪 Testeur / QA**
1. Suivre `guides/GUIDE_TEST_RAPIDE.md`
2. Consulter les fiches fonctionnalités
3. Vérifier `archives/corrections/`

### Par Fonctionnalité

**🏦 Intégration Bancaire**
→ `integration-bancaire/INTEGRATION_BANCAIRE.md`  
→ `guides/QUICKSTART_BANQUE.md`

**💰 Gestion des Dépenses**
→ `fonctionnalites/FONCTIONNALITE_DEPENSES.md`  
→ `guides/GUIDE_MODULE_DEPENSES.md`

**📊 CRUD & Gestion**
→ `fonctionnalites/Fiche_Fonctionnalite_CRUD.md`  
→ `fonctionnalites/Fiche_Fonctionnalite_Alerte_Inactivite.md`

**🔄 Import/Export**
→ `fonctionnalites/Fiche_Fonctionnalite_Export_JSON.md`  
→ `fonctionnalites/Fiche_Fonctionnalite_Import_JSON.md`

**🎨 Architecture & API**
→ `techniques/ARCHITECTURE_TECHNIQUE.md`  
→ `techniques/API_documentation.md`

---

## 📦 Fichiers Complémentaires

- **[projet_description.txt](projet_description.txt)**  
  Description initiale du projet

---

## 📈 Statistiques Documentation

- **21 fichiers** de documentation active
- **5 catégories** thématiques
- **16 fichiers** archivés (développement)
- **2 diagrammes** de flux inclus

---

## 🎓 Parcours Recommandés

### Découverte Rapide (30 min)
1. `/README.md` (racine)
2. `guides/GUIDE_NOUVELLES_FONCTIONNALITES.md`
3. `guides/QUICKSTART_BANQUE.md`

### Formation Complète (2h)
1. `techniques/ARCHITECTURE_TECHNIQUE.md`
2. `guides/GUIDE_UTILISATION_MAIN.md`
3. Toutes les fiches dans `fonctionnalites/`
4. `integration-bancaire/INTEGRATION_BANCAIRE.md`

### Développement (1h)
1. `techniques/ARCHITECTURE_TECHNIQUE.md`
2. `techniques/API_documentation.md`
3. `techniques/PLAN_INTEGRATION_FRONTEND.md`
4. `fonctionnalites/` (fiches détaillées)

---

**Dernière mise à jour :** 29 novembre 2024  
**Version de la documentation :** 2.0  
**Organisation :** Structurée en sous-dossiers thématiques
