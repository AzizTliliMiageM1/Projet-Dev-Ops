# 🎯 Gestion d'Abonnements - Application Full-Stack Moderne

![Version](https://img.shields.io/badge/version-2.0.0-blue.svg)
![Java](https://img.shields.io/badge/Java-17-orange.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)

Une application complète de gestion d'abonnements avec interface moderne glassmorphism, analytics avancés, **chatbot IA intelligent**, tutoriels vidéo interactifs, **système de thèmes personnalisables**, **export/import avancé** et **notifications email automatiques**.

## ✨ Nouvelles Fonctionnalités 2.0

### 🎨 Système de Thèmes Personnalisables
- **6 thèmes prédéfinis** : Violet Premium, Bleu Océan, Rose Sunset, Vert Nature, Orange Énergie, Minimaliste
- **Personnalisation avancée** : 3 color pickers (primaire, secondaire, accent)
- **Aperçu en temps réel** avec KPI cards stylisées
- **Persistance automatique** des préférences utilisateur
- **Application globale** sur toutes les pages de l'application
- **CSS Variables dynamiques** pour performance optimale

### 📥 Export/Import Avancé
- **Export PDF professionnel** avec graphiques et statistiques
  - Header thématisé avec couleurs personnalisées
  - Tableaux détaillés des abonnements et dépenses
  - KPIs et résumés financiers
- **Export CSV/Excel** compatible avec Google Sheets et Excel
- **Export JSON** pour sauvegarde complète des données
- **Import intelligent** de formats bancaires :
  - OFX (Open Financial Exchange) - banques françaises
  - CSV avec détection automatique des colonnes
  - QIF (Quicken Interchange Format)
  - JSON (restauration de sauvegardes)
- **Détection automatique** :
  - Catégories (Streaming, Télécom, Transport, etc.)
  - Abonnements récurrents vs dépenses ponctuelles
  - Parsing multi-formats de dates
- **Prévisualisation avant import** avec statistiques
- **Historique des exports** avec métadonnées

### 📧 Notifications Email Automatiques
- **4 types de notifications** :
  - ⚠️ Alertes d'expiration (X jours avant)
  - 💸 Dépassement de budget
  - 📊 Rapports mensuels automatiques
  - 🔔 Dépenses inhabituelles
- **Emails HTML professionnels** avec templates thématisés
- **Configuration flexible** :
  - Fréquence des rappels (quotidien/hebdomadaire/mensuel)
  - Personnalisation des délais d'alerte
  - Activation/désactivation par type
- **Backend JavaMail** avec support SMTP
  - Compatible Gmail, SendGrid, etc.
  - Configuration SMTP avancée (TLS/SSL)
  - Mode simulation pour développement
- **Historique des notifications** envoyées
- **Email de test** pour vérifier la configuration

### 🏦 Intégration Bancaire Intelligente
- **Import relevés bancaires** multi-formats :
  - CSV (Excel, Google Sheets)
  - OFX (Open Financial Exchange - banques françaises)
  - QIF (Quicken Interchange Format)
  - Drag & drop intuitif avec validation
- **Rapprochement automatique** :
  - Matching transactions ↔ abonnements déclarés
  - Algorithme intelligent (nom + prix ±2€)
  - Badges visuels de correspondance
- **Détection abonnements non déclarés** :
  - 15+ patterns prédéfinis (Netflix, Spotify, SFR, etc.)
  - Analyse récurrence automatique (28-32 jours)
  - Alertes "Abonnement caché détecté"
  - Bouton ajout rapide en 1 clic
- **Simulation solde virtuel** :
  - Calcul impact mensuel des abonnements
  - Projection sur 6 mois avec graphique Chart.js
  - Visualisation évolution finances
  - Aide à la décision (garder/résilier)
- **Timeline transactions** organisée par mois
- **Statistiques temps réel** : Rapprochées, Non déclarées, Cachés

## ✨ Fonctionnalités Principales

### 🎨 Interface Utilisateur Premium
- **Dashboard moderne** avec design glassmorphism professionnel
- **4 KPI en temps réel** : Total, Actifs, Coût mensuel, Alertes
- **Design responsive** adapté mobile/tablette/desktop
- **Gradient violet** cohérent (#667eea → #764ba2)
- **Animations fluides** et transitions CSS avancées

### 🤖 **NOUVEAU ! Chatbot IA Intelligent**
- **Assistant conversationnel** avec NLP (Natural Language Processing)
- **Analyse automatique** de vos dépenses et budget
- **Conseils personnalisés** d'optimisation financière
- **Recherche intelligente** d'abonnements en langage naturel
- **Détection d'intentions** : statistiques, alertes, recherche, aide
- **Interface moderne** avec fenêtre flottante élégante
- **Réponses contextuelles** basées sur vos données réelles

**Exemples de questions :**
- "Quel est mon budget mensuel ?"
- "Mes alertes d'inactivité"
- "Comment économiser ?"
- "Cherche Netflix"
- "Liste mes abonnements actifs"

### 📹 Tutoriels Vidéo Interactifs
- **2 tutoriels complets** (13 minutes au total)
  - 🌟 Débutant (5 min) : Premiers pas
  - 🎓 Avancé (8 min) : Fonctionnalités expertes
- **Navigation par chapitres** avec timeline interactive
- **Contrôles vidéo** (Play/Pause, seek bar cliquable)
- **Raccourcis clavier** (Espace, flèches)
- **18 slides animées** au total

## Fonctionnalités Clés Développées

Pour une description détaillée de chaque fonctionnalité, y compris les personas utilisateurs, les diagrammes de flux et les axes d'amélioration, veuillez consulter les fiches de fonctionnalités dédiées :

*   [**Fiche de Fonctionnalité 1 : Gestion Complète des Abonnements (CRUD)**](docs/Fiche_Fonctionnalite_CRUD.md)
    *   [Diagramme de Flux CRUD](docs/Fiche_Fonctionnalite_CRUD_Diagram.png)

*   [**Fiche de Fonctionnalité 2 : Alerte d'Inactivité Intelligente**](docs/Fiche_Fonctionnalite_Alerte_Inactivite.md)
    *   [Diagramme de Flux Alerte d'Inactivité](docs/Fiche_Fonctionnalite_Alerte_Inactivite_Diagram.png)

## Comment ça marche ?

*   **Java** : Tout le code est écrit en Java, en utilisant les bonnes pratiques de la programmation orientée objet.
*   **Maven** : J'ai utilisé Maven pour gérer les dépendances et faciliter la compilation du projet.
*   **Persistance des Données** : Pour que les abonnements ne soient pas perdus à chaque fermeture de l'application, toutes les données sont automatiquement sauvegardées dans un fichier texte (`abonnements.txt`) et rechargées au démarrage. C'est une introduction simple mais efficace à la persistance des données.
*   **Gestion des Dates** : Utilisation des classes modernes `java.time` pour une manipulation précise et facile des dates (début, fin d'abonnement, dernière utilisation).

## API REST (optionnel)

Une petite API REST a été ajoutée pour exposer les abonnements (utile pour tester depuis un navigateur ou un script). Le serveur est minimal et basé sur Spark Java.

Endpoints disponibles :
- GET  /api/abonnements        -> liste des abonnements (JSON)
- GET  /api/abonnements/:id    -> abonnement par index (0-based)
- POST /api/abonnements        -> ajoute un abonnement (JSON)
- PUT  /api/abonnements/:id    -> met à jour un abonnement par index
- DELETE /api/abonnements/:id  -> supprime un abonnement par index

Pour lancer l'API localement :
```bash

# Lancer l'API (par défaut file-backed)
mvn exec:java -Dexec.mainClass=com.projet.api.ApiServer

# Lancer l'API avec H2 (DB embarquée, persistance) :
REPO=db mvn exec:java -Dexec.mainClass=com.projet.api.ApiServer

# Lancer l'API avec H2 en mémoire (utile pour tests rapides) :
REPO=db mvn exec:java -Dexec.mainClass=com.projet.api.ApiServer -DJDBC_URL=jdbc:h2:mem:abos;DB_CLOSE_DELAY=-1
```

Exemples curl :
```bash
curl -s http://localhost:4567/api/abonnements
curl -X POST http://localhost:4567/api/abonnements -H "Content-Type: application/json" -d '@exemple.json'
```
Note sur l'ID : l'API utilise pour l'instant l'index dans la liste (0-based). Pour production, il faut ajouter un champ `id` persistant.

### 🎯 **Interface Révolutionnaire 2024**

> **Interface Web Ultra-Premium avec Glassmorphisme Avancé**

Notre application arbore maintenant un design révolutionnaire qui redéfinit l'expérience utilisateur :

#### ✨ **Caractéristiques Visuelles Ultra-Modernes**

🌟 **Design Glassmorphisme 3.0**
- Effets de transparence et flou avancés (`backdrop-filter: blur(30px)`)
- Gradients animés multi-couches avec transitions fluides
- Effets de particules et animations de lumière
- Micro-interactions sophistiquées avec transformations 3D

🎨 **Système de Couleurs Futuristes**
- Palette tricolore : Indigo → Violet → Rose (`#667eea → #764ba2 → #f093fb`)
- Dégradés animés en temps réel sur tous les éléments
- Ombres douces multi-niveaux pour la profondeur
- Effets de lueur et réflexions lumineuses

⚡ **Animations Ultra-Fluides**
- Transitions `cubic-bezier(0.4, 0, 0.2, 1)` pour un rendu premium
- Effets de sweep lumineux sur hover
- Transformations 3D avec scale et rotation
- Animations de gradient en boucle continue

#### 📊 **Dashboard Principal (`/index.html`)**

🎯 **Métriques KPI Animées**
- Compteurs géants avec gradient animé
- Icônes flottantes avec rotations au hover
- Cartes glass avec effets de profondeur
- Grid responsive ultra-moderne

🔍 **Recherche Intelligente Premium**
- Barre de recherche glassmorphisme avec effets lumineux
- Focus avec scale et glow effects
- Placeholder animé et auto-complétion visuelle
- Filtres temps-réel avec transitions

💎 **Cartes Abonnements Futuristes**
- Design 3D avec élévation au hover
- Status badges avec gradients animés et glow
- Prix avec gradient de texte animé
- Actions avec effets de sweep

#### 📈 **Analytics Avancés (`/stats.html`)**

📊 **Graphiques Chart.js Spectaculaires**
- Fond glassmorphisme avec particules animées
- Couleurs gradient synchronisées avec le thème
- Animations d'entrée fluides et interactions
- Tooltips customisées avec style glass

💡 **Insights Temps Réel**
- KPI cards avec effets de rotation de fond
- Badges métriques avec sweep animations
- Données connectées API en temps réel
- Auto-refresh avec indicateurs visuels

#### 🎪 **Effets Visuels Avancés**

✨ **Micro-Interactions Premium**
- Hover effects avec transformations 3D complexes
- Boutons avec sweep lumineux et scale effects
- Navigation avec indicateurs de position animés
- Loading states avec pulsations élégantes

🌊 **Animations Fluides**
- Gradient shifts en boucle sur tous les éléments
- Particules flottantes en arrière-plan
- Effets de parallaxe sur scroll
- Transitions de page avec fade et slide

#### 📱 **Responsive Ultra-Optimisé**

- Breakpoints intelligents pour tous les devices
- Adaptation automatique des animations sur mobile
- Grid system flexible avec gap adaptatif
- Touch gestures optimisées pour tablet/mobile

#### ⚡ **Performance & Accessibilité**

- CSS optimisé avec variables pour cohérence
- Animations GPU-accelerated uniquement
- **Contraste élevé optimisé** : Fonds semi-transparents pour tous les textes
- **Lisibilité étudiante renforcée** : Ombres portées et backdrop-filters
- **Variables --text-shadow** : Cohérence des effets de profondeur
- **Glass-text-bg containers** : Fonds contrastés pour éléments importants
- Support complet des lecteurs d'écran

#### 🎓 **Optimisations Étudiantes**

📖 **Lisibilité Premium**
- Fonds semi-transparents rgba(0,0,0,0.25) sur textes cruciaux
- Contraste élevé maintenu malgré l'esthétique glassmorphisme
- Tailles de police agrandies pour confort de lecture
- Drop-shadows sur gradients pour distinction visuelle
- Bordures subtiles pour délimitation des zones de contenu

✅ **Balance Design/Fonctionnel**
- Esthétique futuriste préservée intégralement
- Accessibilité web standards respectés
- UX optimisée pour usage académique et professionnel
- Interface adaptée aux longues sessions d'étude

Pour l'ouvrir :
1. Démarre le serveur API :
```bash
mvn exec:java -Dexec.mainClass=com.projet.api.ApiServer
# Ou avec la DB embarquée :
REPO=db mvn exec:java -Dexec.mainClass=com.projet.api.ApiServer
```
2. Ouvre ton navigateur et va sur :
```
http://localhost:4567
```

L'interface propose :
- affichage responsive en cartes (compatible mobile)
- bouton Ajouter / Supprimer
- import/export JSON
- couleurs et styles modernes

Si tu utilises Codespaces ou un conteneur distant, expose le port 4567 dans ta workspace / forward port pour l'ouvrir depuis ton navigateur local.

## Structure du Projet

Le projet suit une structure Maven standard, ce qui le rend facile à comprendre et à étendre :

```
. 
├── pom.xml
├── README.md
├── abonnements.txt
├── docs/
│   ├── Fiche_Fonctionnalite_CRUD.md
│   ├── Fiche_Fonctionnalite_CRUD_Diagram.png
│   ├── Fiche_Fonctionnalite_Alerte_Inactivite.md
│   └── Fiche_Fonctionnalite_Alerte_Inactivite_Diagram.png
└── src
    └── main
        └── java
            └── com
                └── example
                    └── abonnement
                        ├── Abonnement.java         # La classe qui représente un abonnement
                        └── GestionAbonnements.java # La logique principale de l'application
```

*   `Abonnement.java` : Définit la structure d'un abonnement, incluant le nom du service, les dates de début et de fin, le prix mensuel, le nom du client, et la date de la dernière utilisation. Il contient aussi des méthodes pour vérifier si l'abonnement est actif et pour la conversion en/depuis un format CSV pour la sauvegarde.
*   `GestionAbonnements.java` : C'est le cœur de l'application. Il gère la liste des abonnements, interagit avec l'utilisateur via un menu en console, et implémente toutes les fonctionnalités (ajout, affichage, modification, suppression, recherche, enregistrement d'utilisation, et vérification des alertes).
*   `pom.xml` : Le fichier de configuration Maven qui permet de compiler le projet et de créer un fichier JAR exécutable.

## Comment compiler et exécuter

1.  **Prérequis** : Assurez-vous d'avoir le Java Development Kit (JDK) version 11 ou plus récent et Apache Maven installés sur votre machine.
2.  **Compilation** : Ouvrez un terminal, naviguez jusqu'au répertoire racine du projet (là où se trouve le fichier `pom.xml`), et exécutez la commande suivante :
    ```bash
    mvn clean package
    ```
3.  **Exécution** :

- Option A (recommandée pour le développement — fournit le classpath complet) :
```bash
mvn exec:java -Dexec.mainClass=com.example.abonnement.GestionAbonnements
```

- Option B (si vous voulez lancer le JAR directement) :
  - Par défaut, `mvn package` ne produit pas d'uber-jar (les dépendances ne sont pas incluses). Si vous voulez un seul fichier exécutable, il faut ajouter le plugin `maven-shade-plugin` au `pom.xml` . Sinon, lance le JAR produit en t'assurant que le classpath contient les dépendances.

```bash
java -jar target/gestion-abonnements-1.0-SNAPSHOT.jar
```

L'application démarrera et vous présentera un menu interactif en console.

Ce projet est une excellente démonstration des compétences en programmation Java, couvrant la POO, la gestion des collections, les E/S, la persistance et une touche d'intelligence métier avec les alertes d'utilisation. Il est prêt à être présenté et peut servir de base pour des évolutions futures !

## À propos du build GitHub (CI)

En cas d'erreur du type `Tests run: 5, Failures: 1` sur GitHub Actions (échec sur `ApiServerIntegrationTest` avec `expected 201 but was 400`), cela peut être dû à la désérialisation JSON : la classe `Abonnement` nécessite un constructeur sans argument pour que Jackson puisse correctement traiter les requêtes POST.

Pour résoudre ce problème, vérifier que le workflow GitHub a bien été déclenché après les corrections et que le cache n'empêche pas la prise en compte des nouveaux artefacts.
