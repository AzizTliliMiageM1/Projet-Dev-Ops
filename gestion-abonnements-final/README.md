# Gestion d'Abonnements en Java

Ce projet est une application console en Java pour gérer ses abonnements. C'est un projet étudiant qui montre comment utiliser les bases de Java pour créer une petite application pratique.

## Les fonctionnalités que j'ai développées

1.  **Ajouter, modifier, supprimer et lister les abonnements** : C'est la base de l'application. On peut facilement ajouter un nouvel abonnement (comme Netflix, Spotify, etc.), changer ses informations, le supprimer si on ne l'utilise plus, et bien sûr voir la liste de tous les abonnements qu'on a.

2.  **Alerte d'inactivité (la feature "Basic-Fit")** : C'est une fonctionnalité un peu plus avancée. L'idée est venue de mon abonnement à la salle de sport. Je paye tous les mois, mais parfois je n'y vais pas. L'application permet donc d'enregistrer la dernière fois qu'on a utilisé un service (par exemple, la dernière fois qu'on est allé à la salle). Si ça fait longtemps (plus de 30 jours), l'application affiche une alerte pour nous le rappeler. Ça aide à ne pas payer pour rien !

## Comment ça marche ?

*   **Java** : Tout le code est en Java.
*   **Maven** : J'ai utilisé Maven pour que ce soit plus simple de compiler le projet.
*   **Sauvegarde en fichier texte** : Pour ne pas perdre les données à chaque fois qu'on ferme l'application, les abonnements sont sauvegardés dans un fichier `abonnements.txt`. C'est une manière simple de faire de la persistance de données.

## Pour lancer le projet

Il faut avoir Java et Maven d'installés. Ensuite, il suffit d'ouvrir un terminal dans le dossier du projet et de taper :

```bash
mvn clean install
java -jar target/gestion-abonnements-1.0-SNAPSHOT.jar
```

Et voilà, l'application se lance et on peut commencer à gérer ses abonnements !
