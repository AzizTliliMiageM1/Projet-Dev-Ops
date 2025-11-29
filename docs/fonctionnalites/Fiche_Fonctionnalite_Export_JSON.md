# Fiche de fonctionnalité — Export / Import JSON

Résumé
-------
Cette fiche détaille la fonctionnalité d'export et d'import des abonnements au format JSON.

But
---
Permettre à l'utilisateur de sauvegarder ses abonnements dans un format lisible par machine et d'importer des listes d'abonnements depuis des fichiers JSON.

Détails fonctionnels
--------------------
- Export : sérialise la liste courante d'abonnements vers un fichier JSON (chemin fourni ou `abonnements.json`).
- Import : lit un fichier JSON contenant un tableau d'abonnements et les ajoute à la liste existante.

Implementation
--------------
- Utilisation de Jackson (`jackson-databind` + `jackson-datatype-jsr310`) pour gérer `LocalDate` proprement.
- Méthodes : `exporterJson()` et `importerJson()` dans `GestionAbonnements`.

Cas d'utilisation
------------------
1. Utilisateur choisit "Exporter" et fournit un nom de fichier.
2. Application écrit le JSON et confirme.
3. Utilisateur choisit "Importer" et fournit un chemin vers un fichier JSON valide.

Fichiers impactés
-----------------
- `pom.xml` (dépendances Jackson)
- `src/main/java/com/example/abonnement/GestionAbonnements.java`

Tests
-----
- Tests manuels : exporter puis importer et vérifier que les éléments sont bien présents.
- Tests automatisés : prévus pour les évolutions futures.
