# Cahier des Charges - Application de Gestion d'Abonnements

**Projet** : Système de gestion d'abonnements en ligne  
**Date** : 3 octobre 2025  
**Équipe** :
- Aziz TLILI - 41006201
- Maissara FERKOUS - 42006149


## 1. Contexte du Projet

### 1.1 Présentation

De nos jours, on s'abonne à plein de services différents : Netflix, Spotify, Amazon Prime, salles de sport, etc. C'est facile de perdre le fil et de pas savoir combien on dépense au total chaque mois. Parfois on oublie même qu'on est abonné à certains trucs qu'on utilise plus.

Notre projet consiste à créer une application web qui permet de gérer tous ses abonnements au même endroit. L'idée c'est de pouvoir voir rapidement combien on dépense, quels abonnements sont encore actifs, et lesquels on utilise plus vraiment.

### 1.2 Objectifs

L'objectif principal est de développer une application simple et pratique pour :
- Enregistrer tous mes abonnements avec leurs infos (prix, dates, catégorie)
- Voir combien je dépense au total par mois
- Être alerté quand j'ai pas utilisé un service depuis longtemps
- Exporter et sauvegarder mes données
- Consulter des statistiques visuelles sur mes dépenses

### 1.3 Public visé

L'application s'adresse à tout le monde, principalement :
- Les étudiants qui veulent gérer leur budget
- Les personnes qui ont beaucoup d'abonnements différents
- Ceux qui veulent économiser en repérant les services inutilisés
- Les familles qui partagent des abonnements

## 2. Besoins Fonctionnels

### 2.1 Gestion des Utilisateurs

#### 2.1.1 Inscription
- L'utilisateur peut créer un compte avec un email, un mot de passe et un pseudo
- Le système envoie un email de confirmation
- L'email doit être unique (pas de doublons)
- Le mot de passe doit être confirmé deux fois pour éviter les erreurs

#### 2.1.2 Connexion
- L'utilisateur se connecte avec son email et mot de passe
- Le compte doit être confirmé par email avant la première connexion
- Une session est créée pour garder l'utilisateur connecté

#### 2.1.3 Déconnexion
- L'utilisateur peut se déconnecter à tout moment
- La session est effacée et on retourne à la page d'accueil

### 2.2 Gestion des Abonnements

#### 2.2.1 Ajouter un abonnement
L'utilisateur peut ajouter un nouvel abonnement avec :
- **Nom du service** : Netflix, Spotify, etc.
- **Date de début** : Quand l'abonnement a commencé
- **Date de fin** : Quand il se termine (optionnel)
- **Prix mensuel** : Combien ça coûte par mois
- **Catégorie** : Type de service (Streaming, Sport, Musique, Logiciels, Autres)
- **Description** : Notes personnelles (optionnel)

Un identifiant unique (UUID) est généré automatiquement pour chaque abonnement.

#### 2.2.2 Modifier un abonnement
- L'utilisateur peut modifier toutes les infos d'un abonnement existant
- Les changements se sauvegardent immédiatement
- Les statistiques se mettent à jour automatiquement

#### 2.2.3 Supprimer un abonnement
- L'utilisateur peut supprimer un abonnement
- Une confirmation est demandée pour éviter les suppressions accidentelles
- L'abonnement disparaît définitivement après confirmation

#### 2.2.4 Marquer comme utilisé
- L'utilisateur peut marquer qu'il a utilisé un service récemment
- La date d'utilisation est enregistrée
- Ça permet de suivre quels services on utilise vraiment

#### 2.2.5 Alertes d'inactivité
- Si un service n'a pas été utilisé depuis 30 jours, une alerte apparaît
- Ça aide à repérer les abonnements inutiles
- Le badge change de couleur (vert → orange/rouge)

### 2.3 Recherche et Filtrage

#### 2.3.1 Recherche textuelle
- Barre de recherche pour trouver un abonnement par son nom
- La recherche se fait en temps réel (pas besoin de cliquer sur un bouton)
- Insensible à la casse (majuscules/minuscules)

#### 2.3.2 Filtres (futur)
- Filtrer par catégorie
- Filtrer par statut (actif/inactif)
- Trier par prix, date, nom

### 2.4 Statistiques et Visualisation

#### 2.4.1 KPI (Indicateurs clés)
Affichage en haut du dashboard de :
- **Nombre total** d'abonnements
- **Nombre d'abonnements actifs** (pas encore terminés)
- **Coût mensuel total** en euros
- **Nombre d'alertes** (services non utilisés depuis 30j)

#### 2.4.2 Graphiques
- **Graphique en donut** : Répartition des dépenses par catégorie
- **Graphique en barres** : Évolution du coût mois par mois
- Graphiques interactifs avec détails au survol de la souris

### 2.5 Import/Export de Données

#### 2.5.1 Export JSON
- Télécharger tous ses abonnements dans un fichier JSON
- Utile pour faire des sauvegardes
- Le fichier contient toutes les infos (nom, prix, dates, catégorie, etc.)

#### 2.5.2 Import JSON
- Charger un fichier JSON pour importer des abonnements
- Vérification du format avant l'import
- Les abonnements sont ajoutés à ceux existants (pas de remplacement)

### 2.6 Chatbot Assistant

#### 2.6.1 Interface conversationnelle
- Icône de chat en bas à droite de la page
- Fenêtre de discussion qui s'ouvre au clic

#### 2.6.2 Fonctionnalités du chatbot
Le chatbot peut répondre à des questions comme :
- "Quel est mon budget ?"
- "Combien j'ai d'abonnements ?"
- "Cherche Netflix"
- "Mes alertes"
- "Mes abonnements actifs"

Il comprend le langage naturel et donne des réponses personnalisées.

### 2.7 Multi-utilisateurs

#### 2.7.1 Séparation des données
- Chaque utilisateur a son propre espace
- Les abonnements d'un utilisateur ne sont pas visibles par les autres
- Chaque utilisateur a son propre fichier de données

#### 2.7.2 Sécurité
- Les routes de modification (ajout, édition, suppression) nécessitent d'être connecté
- La consultation peut se faire sans connexion (mode démo avec données publiques)

## 3. Besoins Non-Fonctionnels

### 3.1 Performance

- Temps de chargement de la page : < 3 secondes
- Temps de réponse des actions (ajout, modification) : < 1 seconde
- Recherche en temps réel : latence < 100ms

### 3.2 Ergonomie et Design

- Interface moderne et épurée
- Design responsive (s'adapte au mobile, tablette, PC)
- Utilisation de Bootstrap pour un rendu professionnel
- Couleurs cohérentes et agréables
- Messages d'erreur clairs et compréhensibles

### 3.3 Compatibilité

- Navigateurs : Chrome, Firefox, Safari, Edge (versions récentes)
- Appareils : PC, Mac, smartphones Android/iOS, tablettes
- Résolutions : de 375px (mobile) à 1920px (grand écran)

### 3.4 Accessibilité

- Contrastes suffisants pour la lisibilité
- Tailles de texte adaptées
- Formulaires avec labels clairs
- Messages de feedback visuels

### 3.5 Sécurité

- Stockage sécurisé des mots de passe (recommandation : hash)
- Sessions sécurisées
- Validation des données côté serveur
- Protection contre les injections (validation des inputs)

### 3.6 Fiabilité

- Sauvegarde automatique après chaque action
- Gestion des erreurs (messages clairs, pas de crash)
- Validation des données avant enregistrement

## 4. Architecture Technique

### 4.1 Technologies Utilisées

#### 4.1.1 Backend
- **Langage** : Java 17
- **Framework** : Spark Framework (micro-framework web)
- **Build** : Maven
- **Serveur** : Intégré Jetty (port 4567)

#### 4.1.2 Frontend
- **HTML5** : Structure des pages
- **CSS3** : Mise en forme et styles
- **JavaScript** : Interactivité
- **Bootstrap 5.3.2** : Framework CSS pour le design
- **Chart.js** : Bibliothèque pour les graphiques

#### 4.1.3 Stockage
- **Fichiers texte** : Format CSV pour simplicité
- **Fichiers par utilisateur** : `data/abonnements/abonnements_{email}.txt`
- **Fichier utilisateurs** : `users-db.txt`

### 4.2 Structure du Projet

```
Projet-Dev-Ops/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── abonnement/
│   │   │               ├── Main.java (serveur)
│   │   │               ├── Abonnement.java (modèle)
│   │   │               ├── User.java (utilisateur)
│   │   │               ├── FileAbonnementRepository.java
│   │   │               └── FileUserRepository.java
│   │   └── resources/
│   │       └── static/
│   │           ├── index.html (dashboard)
│   │           ├── login.html
│   │           ├── register.html
│   │           ├── stats.html
│   │           ├── home.html
│   │           └── *.js, *.css
│   └── test/
│       └── java/ (tests unitaires et intégration)
├── data/
│   └── abonnements/ (fichiers utilisateurs)
├── docs/ (documentation)
├── tests/ (documentation tests)
└── pom.xml (configuration Maven)
```

### 4.3 Routes API

| Méthode | Route | Description | Auth requise |
|---------|-------|-------------|--------------|
| GET | `/api/abonnements` | Liste des abonnements | Non* |
| POST | `/api/abonnements` | Créer un abonnement | Oui |
| PUT | `/api/abonnements/:id` | Modifier un abonnement | Oui |
| DELETE | `/api/abonnements/:id` | Supprimer un abonnement | Oui |
| POST | `/api/register` | Créer un compte | Non |
| POST | `/api/login` | Se connecter | Non |
| POST | `/api/logout` | Se déconnecter | Non |
| GET | `/api/confirm` | Confirmer email | Non |

*Sans authentification, retourne les données partagées publiques.

## 5. Cas d'Usage

### 5.1 Cas d'usage 1 : Inscription et première utilisation

**Acteur** : Nouvel utilisateur

**Scénario** :
1. L'utilisateur arrive sur la page d'accueil
2. Il clique sur "S'inscrire"
3. Il remplit le formulaire (pseudo, email, mot de passe)
4. Il reçoit un email de confirmation
5. Il clique sur le lien dans l'email
6. Il se connecte avec son email et mot de passe
7. Il arrive sur le dashboard vide
8. Il ajoute son premier abonnement (Netflix)
9. L'abonnement apparaît dans la liste

### 5.2 Cas d'usage 2 : Ajout et suivi d'abonnements

**Acteur** : Utilisateur connecté

**Scénario** :
1. L'utilisateur se connecte
2. Il clique sur "Ajouter un service"
3. Il remplit les infos (nom, dates, prix, catégorie)
4. Il valide
5. L'abonnement apparaît dans la liste
6. Les statistiques se mettent à jour
7. Il marque l'abonnement comme "utilisé récemment"
8. Un badge vert apparaît avec la date

### 5.3 Cas d'usage 3 : Détection d'abonnements inutilisés

**Acteur** : Utilisateur régulier

**Scénario** :
1. L'utilisateur n'a pas utilisé un service depuis 30 jours
2. Un badge orange/rouge "Inactif depuis 30j" apparaît
3. Le compteur d'alertes en haut augmente
4. L'utilisateur voit l'alerte et décide de supprimer cet abonnement
5. Il clique sur la poubelle
6. Il confirme la suppression
7. L'abonnement disparaît
8. Les statistiques sont recalculées

### 5.4 Cas d'usage 4 : Visualisation des statistiques

**Acteur** : Utilisateur qui veut analyser ses dépenses

**Scénario** :
1. L'utilisateur clique sur "Dashboard" dans la navbar
2. Il voit les 4 KPI (total, actifs, coût, alertes)
3. Il consulte le graphique en donut
4. Il voit que 60% de ses dépenses sont dans le Streaming
5. Il regarde le graphique en barres
6. Il constate que ses dépenses augmentent depuis 3 mois
7. Il décide de supprimer certains abonnements

### 5.5 Cas d'usage 5 : Sauvegarde et restauration

**Acteur** : Utilisateur prudent

**Scénario** :
1. L'utilisateur veut sauvegarder ses données
2. Il clique sur "Exporter JSON"
3. Un fichier se télécharge sur son ordinateur
4. Plus tard, depuis un autre appareil
5. Il clique sur "Importer JSON"
6. Il sélectionne son fichier de sauvegarde
7. Tous ses abonnements sont importés

## 6. Contraintes

### 6.1 Contraintes Techniques

- Développement en Java 17 minimum
- Utilisation obligatoire de Maven
- Backend avec Spark Framework
- Pas de base de données (stockage fichier)
- Compatibilité navigateurs modernes

### 6.2 Contraintes de Temps

- Projet à réaliser en 6 semaines
- Livraison finale : décembre 2025
- Présentations intermédiaires chaque semaine

### 6.3 Contraintes de Ressources

- Équipe de 3 personnes
- Pas de budget (utilisation d'outils gratuits)
- Hébergement local (localhost)

## 7. Évolutions Futures Possibles

### 7.1 Court terme
- Ajout de notifications par email pour les renouvellements
- Filtres avancés (par prix, par date)
- Calendrier visuel des échéances

### 7.2 Moyen terme
- Application mobile native (Android/iOS)
- Connexion avec les APIs des services (récupération auto des infos)
- Partage de listes entre utilisateurs (famille)
- Mode sombre

### 7.3 Long terme
- Intelligence artificielle pour suggérer des économies
- Comparateur de prix entre services similaires
- Intégration bancaire pour tracking automatique
- Rappels automatiques avant renouvellement

## 8. Critères de Validation

### 8.1 Tests Fonctionnels

Pour chaque fonctionnalité majeure, on doit vérifier que :
- L'action fonctionne correctement
- Les données sont bien sauvegardées
- Les messages d'erreur apparaissent si besoin
- L'interface répond comme attendu

### 8.2 Tests de Performance

- Temps de chargement respectés
- Pas de lag lors de la saisie
- Recherche instantanée

### 8.3 Tests de Compatibilité

- Tests sur Chrome, Firefox, Safari
- Tests sur mobile (responsive)
- Tests sur différentes résolutions

### 8.4 Validation Utilisateur

- Interface intuitive (pas besoin de mode d'emploi)
- Messages clairs et en français
- Workflow logique

## 9. Livrables Attendus

### 9.1 Code Source
- Repository GitHub avec tout le code
- Documentation technique dans le README
- Commentaires dans le code

### 9.2 Documentation
- Cahier des charges (ce document)
- Documentation API
- Documentation utilisateur
- Fiches fonctionnalités

### 9.3 Tests
- Tests unitaires (JUnit)
- Tests d'intégration
- Documentation des tests
- Rapport de tests fonctionnels

### 9.4 Démo
- Application fonctionnelle en local
- Présentation PowerPoint
- Vidéo de démonstration (optionnel)

## 10. Organisation du Travail

### 10.1 Répartition des Tâches

**Sprint 1 (Semaines 1-2)** : Base du projet
- Setup du projet Maven
- Modèle de données (Abonnement, User)
- Système de fichiers
- Pages HTML de base

**Sprint 2 (Semaines 3-4)** : Fonctionnalités principales
- CRUD complet des abonnements
- Système d'authentification
- Interface utilisateur
- Import/Export JSON

**Sprint 3 (Semaines 5-6)** : Finitions
- Dashboard et graphiques
- Chatbot
- Tests complets
- Documentation

### 10.2 Outils de Collaboration

- **GitHub** : Versioning et collaboration
- **VS Code** : Éditeur de code
- **Discord/WhatsApp** : Communication équipe
- **Trello** : Gestion des tâches (optionnel)

### 10.3 Méthodologie

- Approche Agile (sprints courts)
- Réunions d'équipe régulières
- Commits fréquents sur GitHub
- Tests continus

## Conclusion

Ce cahier des charges définit les grandes lignes de notre application de gestion d'abonnements. L'objectif est de créer un outil simple, pratique et efficace pour aider les utilisateurs à mieux gérer leurs abonnements et leurs dépenses mensuelles.

Le projet utilise des technologies modernes (Java, Spark, Bootstrap, Chart.js) tout en restant simple dans son architecture (pas de base de données complexe). Ça nous permet de nous concentrer sur les fonctionnalités et l'expérience utilisateur.

L'application répond à un vrai besoin : de plus en plus de gens ont des dizaines d'abonnements différents et perdent le contrôle de leurs dépenses. Notre solution apporte une réponse claire et visuelle à ce problème.
