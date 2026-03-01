# Backend — build et exécution (minimal)

Ce fichier explique rapidement comment construire le JAR et lancer le backend en ligne de commande.

1) Construire le JAR

Ouvre un terminal dans le répertoire du projet et lance :

```bash
mvn -DskipTests package
```

Le JAR exécutable sera généré dans `target/` (ex. `target/gestion-abonnements-1.0-SNAPSHOT-shaded.jar`).

2) Lancer le backend

Exécute :

```bash
java -jar target/gestion-abonnements-1.0-SNAPSHOT-shaded.jar <commande> [key=value ...]
```

3) Exemple d'utilisation

Exemple pour ajouter un abonnement :

```bash
java -jar target/gestion-abonnements-1.0-SNAPSHOT-shaded.jar addSubscription nomService=Netflix user=aziz prixMensuel=9.99 dateDebut=2025-01-01 dateFin=2026-01-01 categorie=Streaming
```

Exemple pour créer un utilisateur :

```bash
java -jar target/gestion-abonnements-1.0-SNAPSHOT-shaded.jar createUser email=aziz@example.com password=Password1 pseudo=aziz
```

Notes :
- Les arguments `key=value` sont parsés par le router et transmis aux services.
- Ce README est volontairement minimal pour un usage étudiant.
