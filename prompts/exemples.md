# Exemples de prompts utilisés pendant la session d'examen


## Prompt 1 — Modèle de données

> "Génère une classe Java simple pour représenter un abonnement mensuel avec les champs id (UUID), nom, prixMensuel, categorie et actif. Ajoute un constructeur vide pour Jackson."

---

## Prompt 2 — Calcul de budget par catégorie

> "Comment grouper une List<Abonnement> par catégorie et sommer les prixMensuel avec les Streams Java ? Je veux un Map<String, Double>."

---

## Prompt 3 — Appel HTTP vers une API externe en Java

> "Comment faire un appel GET en Java avec HttpClient vers https://api.frankfurter.app/latest?from=EUR et parser la réponse JSON avec Jackson pour lire le champ rates.USD ?"

---

## Prompt 4 — Routes Spark Framework

> "Comment créer un endpoint GET /api/budget/convertir avec Spark Java qui lit un query param 'devise', appelle un service, et retourne du JSON ? Montre aussi comment gérer les erreurs 400 et 500."

---

## Prompt 5 — Tests JUnit 5

> "Écris des tests unitaires JUnit 5 pour tester que le total mensuel est correct avec des données de demo, et que supprimer un id inexistant retourne false."

---

## Prompt 6 — CORS Spark

> "Comment activer les headers CORS dans Spark Framework pour pouvoir appeler l'API depuis un navigateur sur un autre port ?"
