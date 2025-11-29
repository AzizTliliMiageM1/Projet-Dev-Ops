# Fiche fonctionnalité — Import JSON

But
----
Permettre à l'utilisateur d'importer en masse des abonnements depuis un fichier JSON (par exemple export provenant d'un autre système ou d'un tableur converti).

Contexte
-------
L'interface web propose déjà des actions CRUD simples. L'import JSON facilite l'ajout de nombreux abonnements en une seule opération.

Flux
----
1. L'utilisateur clique sur "Importer JSON" et choisit un fichier local.
2. Le front-end lit le fichier et tente de parser un tableau JSON.
3. Chaque objet du tableau est validé de façon basique (champs obligatoires : `clientName`, `nomService`, `dateDebut`, `dateFin`).
4. Les objets valides sont envoyés en POST au endpoint `/api/abonnements` (un par un).
5. Les objets invalides sont ignorés et signalés à l'utilisateur.

Entrées
------
- Fichier JSON contenant un tableau d'objets (ex: `[ {"clientName":"ACME","nomService":"Cloud","dateDebut":"2024-01-01","dateFin":"2024-12-31"}, ... ]`).

Sorties
-------
- Un ensemble d'abonnements créés côté serveur via la même API POST que le formulaire "Ajouter".
- Message résumé côté UI indiquant le nombre d'objets importés et d'objets ignorés.

Validations
-----------
- Le fichier doit être un tableau JSON.
- Chaque objet doit contenir : `clientName`, `nomService`, `dateDebut`, `dateFin`.
- Les dates sont envoyées telles quelles au back-end ; le back-end applique ses validations habituelles.

Gestion des erreurs
-------------------
- Si le fichier ne contient pas un tableau JSON, l'import échoue et un message est affiché.
- Les objets invalides sont listés/comptés (UI affiche le nombre ignoré) et l'import continue pour les autres.

Sécurité & Remarques
--------------------
- L'import envoie plusieurs requêtes POST : en environnement avec quota/limites, prévoir un throttle si nécessaire.
- Pour des imports volumineux il serait préférable d'ajouter un endpoint d'import massif côté serveur (upload + job asynchrone). Cette fiche décrit une implémentation simple côté client.
