Documentation API — style étudiant

Salut ! Voici une petite doc écrite de façon simple et honnête, comme un étudiant qui explique pourquoi il a fait certains choix.

## But
Cette API expose des opérations basiques pour gérer des abonnements (CRUD). L'interface web fournie interagit avec ces endpoints.

## Principes et décisions
- Identifiants stables : j'utilise des UUID pour chaque abonnement (champ `id`). Ça évite les bugs liés aux index numériques qui bougent quand on supprime/insère des éléments.
- Simplicité : j'ai laissé un petit ensemble d'endpoints REST (GET collection, POST création, GET/PUT/DELETE par id). Rien de compliqué.
- Compatibilité client : l'UI (fichier `static/app.js`) communique en JSON au format attendu par les POJO Java (noms de champs en camelCase/français technique : `nomService`, `dateDebut`, ...).
- Validation légère : le serveur accepte les objets JSON et renvoie 201 à la création ou 400 en cas de JSON invalide (côté client on fait une validation basique avant POST aussi).

## Endpoints disponibles

Base : `/api/abonnements`

- `GET /api/abonnements`
  - Retourne la liste complète d'abonnements au format JSON (tableau). Chaque objet contient :
    - `id` (String UUID)
    - `nomService` (String)
    - `dateDebut` (String, yyyy-MM-dd)
    - `dateFin` (String, yyyy-MM-dd)
    - `prixMensuel` (Number)
    - `clientName` (String)
    - `derniereUtilisation` (String yyyy-MM-dd ou vide)
    - `categorie` (String)
  - Pourquoi ? Pour que l'UI puisse afficher tout et calculer les statistiques côté front (actifs/expirés/alertes).

- `POST /api/abonnements`
  - Corps : JSON avec au minimum `nomService`, `dateDebut`, `dateFin`, `prixMensuel`, `clientName` (le champ `categorie` est optionnel).
  - Répond `201` et renvoie l'objet créé (avec `id` généré si absent).
  - Pourquoi ? On a besoin d'un endpoint simple pour créer des abonnements depuis l'UI ou un import JSON.

- `GET /api/abonnements/:id`
  - `:id` est un UUID. Retourne l'objet correspondant ou 404.
  - Pourquoi ? Pour pouvoir afficher / éditer un abonnement précis.

- `PUT /api/abonnements/:id`
  - Remplace les données de l'abonnement identifié par `:id`. Le body contient le nouvel objet JSON (le serveur s'assure que l'`id` reste le même).
  - Pourquoi ? Pour les modifications depuis la modal d'édition dans l'UI.

- `DELETE /api/abonnements/:id`
  - Supprime l'abonnement identifié par `:id`. Répond 204 si OK.
  - Pourquoi ? Pour la suppression simple et la fonctionnalité de suppression en masse côté UI.

## Notes d'implémentation
- Le serveur est un micro-serveur Spark Java. La sérialisation JSON est gérée par Jackson avec `JavaTimeModule` pour les `LocalDate`.
- Les abonnements sont sauvegardés dans un fichier `abonnements.txt` (format CSV id-first) par défaut. Il existe aussi une implémentation H2 (optionnelle) si on démarre avec `REPO=db`.
- J'ai choisi de garder la logique côté client simple : l'UI crée les objets JSON et laisse le serveur générer l'`id` si besoin.

## Comment tester localement (étapes rapides)
1. Compiler et démarrer le serveur (dans le dossier du projet) :

```bash
mvn -DskipTests=true exec:java -Dexec.mainClass=com.projet.api.ApiServer
```

2. Ouvrir l'UI : http://localhost:4567

3. Ajouter un abonnement via le formulaire (ou tester avec curl) :

```bash
curl -X POST http://localhost:4567/api/abonnements \
  -H 'Content-Type: application/json' \
  -d '{"nomService":"MonService","dateDebut":"2025-11-01","dateFin":"2026-11-01","prixMensuel":4.5,"clientName":"Moi","categorie":"perso"}'
```

4. Vérifier la liste :

```bash
curl http://localhost:4567/api/abonnements
```

## Pourquoi chaque API existe (court résumé étudiant)
- GET collection : utile pour afficher la page principale, le dashboard et filtrer côté client.
- POST création : indispensable pour le formulaire et l'import (côté client on envoie directement le JSON).
- GET/PUT/DELETE par UUID : permet d'identifier de façon stable un abonnement même si l'ordre change dans la liste (évite les bugs quand on supprime des éléments et que les index bougent).

---

Si tu veux que je rende tout encore plus robuste, je peux :
- ajouter une validation serveur plus complète (retourner des messages d'erreur précis),
- ajouter un endpoint serveur pour l'import JSON (upload + traitement),
- ou générer un jar exécutable pour lancer facilement l'app.

Dis-moi ce que tu veux et je l'implemente (commit+push avec message étudiant, comme demandé).

Bonne lecture,
Étudiant dév (expliqué simple)
