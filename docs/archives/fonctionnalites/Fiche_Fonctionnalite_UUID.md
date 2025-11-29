# Fiche fonctionnalité — Identifiants persistants (UUID)

But
----
Remplacer l'utilisation d'indices 0-based comme identifiants volatils par des identifiants persistants (UUID) pour chaque abonnement. Cela rend l'API robuste (références stables), surtout quand le stockage passe d'un fichier à une base de données.

Contexte
-------
Actuellement, l'API supporte des accès par index (0-based) — fragile si l'ordre change ou si des suppressions/intercalations surviennent. Les UUID permettent d'identifier un abonnement de façon stable.

Conception
----------
- Ajout d'un champ `id` (UUID string) dans le modèle `Abonnement`.
- Le format CSV est étendu pour stocker `id` en première colonne. Les anciens fichiers (sans id) sont supportés : à la lecture, on génère un UUID et on ré-écrit le fichier migré.
- Le repository en base stocke l'id en tant que VARCHAR(36) (H2). Les opérations CRUD peuvent utiliser soit l'index (compatibilité) soit l'UUID (préféré).
- L'API accepte maintenant des identifiants numériques (index) ou des UUID dans les endpoints `/api/abonnements/:id`.

Flux utilisateur
----------------
1. Lors de la création (POST), si le payload ne contient pas d'id, le serveur attribue un UUID.
2. Les opérations GET/PUT/DELETE peuvent utiliser soit un index (`/api/abonnements/0`) soit un UUID (`/api/abonnements/3f5a...`).

Validations & erreurs
----------------------
- Si l'UUID demandé n'existe pas, le serveur renvoie 404.
- Le serveur continue de supporter les anciens fichiers CSV (migration automatique lors du chargement).

Remarques techniques
---------------------
- Cette migration est rétrocompatible: les endpoints indexés continuent de fonctionner.
- Pour une API publique, recommande d'exposer uniquement les UUIDs et d'enlever la compatibilité indexée après une migration coordonnée.
