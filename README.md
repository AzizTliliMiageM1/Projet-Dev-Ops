# Gestion d'Abonnements en Java

Ce projet est une application console en Java pour gérer ses abonnements. C'est un projet étudiant qui montre comment utiliser les bases de Java pour créer une petite application pratique et utile.

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
    mvn clean install
    ```
3.  **Exécution** : Une fois la compilation réussie, un fichier JAR exécutable sera généré dans le dossier `target/`. Vous pouvez lancer l'application avec :
    ```bash
    java -jar target/gestion-abonnements-1.0-SNAPSHOT.jar
    ```

L'application démarrera et vous présentera un menu interactif en console. Vous pourrez alors tester toutes les fonctionnalités.

Ce projet est une excellente démonstration des compétences en programmation Java, couvrant la POO, la gestion des collections, les E/S, la persistance et une touche d'intelligence métier avec les alertes d'utilisation. Il est prêt à être présenté et peut servir de base pour des évolutions futures !
