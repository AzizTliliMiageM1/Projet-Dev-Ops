# Tests Fonctionnels - Appli Gestion Abonnements

Date : 24/11/2025  
Testeur : Aziz TLILI

---

## 1. Tests Connexion / Inscription

### Test 1.1 : Se connecter avec un compte qui existe

| N° | Action | Résultat attendu | Résultat obtenu | Commentaires |
|---|---|---|---|---|
| 1 | J'ouvre la page login.html | La page de connexion s'affiche | ✅ OK | y'a bien le formulaire |
| 2 | Je tape test@example.com et le mot de passe | Ça écrit dans les champs | ✅ OK | - |
| 3 | Je clique sur "Se connecter" | Ça me redirige vers la page d'accueil | ✅ OK | assez rapide |
| 4 | Je regarde en haut à droite | Mon pseudo s'affiche à la place du bouton connexion | ✅ OK | cool, ça marche |

### Test 1.2 : Se connecter avec mauvais mot de passe

| N° | Action | Résultat attendu | Résultat obtenu | Commentaires |
|---|---|---|---|---|
| 1 | J'ouvre login.html | Page de connexion | ✅ OK | - |
| 2 | Je mets un email correct mais mauvais mdp | Les champs acceptent | ✅ OK | - |
| 3 | Je clique sur "Se connecter" | Message d'erreur qui dit que c'est pas bon | ✅ OK | message rouge |
| 4 | Je vérifie | Je reste sur la page | ✅ OK | normal |

### Test 1.3 : Se connecter sans avoir confirmé l'email

| N° | Action | Résultat attendu | Résultat obtenu | Commentaires |
|---|---|---|---|---|
| 1 | Je crée un compte | Compte créé | ✅ OK | j'ai reçu l'email |
| 2 | J'essaie de me connecter direct | Message qui dit de confirmer l'email | ✅ OK | logique |
| 3 | Je clique sur le lien dans l'email | Compte confirmé | ✅ OK | - |
| 4 | Je retente la connexion | Cette fois ça marche | ✅ OK | - |

### Test 1.4 : Créer un nouveau compte

| N° | Action | Résultat attendu | Résultat obtenu | Commentaires |
|---|---|---|---|---|
| 1 | Je clique sur "S'inscrire" sur la home | Page register.html s'ouvre | ✅ OK | - |
| 2 | Je remplis le pseudo "JeanTest" | Le champ accepte | ✅ OK | - |
| 3 | Je mets mon email jean.test@mail.com | Ça marche | ✅ OK | - |
| 4 | Je tape le mot de passe 2 fois | Les 2 champs sont remplis | ✅ OK | - |
| 5 | Je clique "Créer mon compte" | Message de succès + email envoyé | ✅ OK | nickel |

### Test 1.5 : S'inscrire avec un email déjà pris

| N° | Action | Résultat attendu | Résultat obtenu | Commentaires |
|---|---|---|---|---|
| 1 | J'ouvre register.html | Formulaire d'inscription | ✅ OK | - |
| 2 | Je mets test@example.com (déjà utilisé) | Le champ accepte | ✅ OK | - |
| 3 | Je remplis le reste et je valide | Message d'erreur "Email déjà utilisé" | ✅ OK | ça bloque bien |
| 4 | Je vérifie | Je reste sur la page pour réessayer | ✅ OK | - |

### Test 1.6 : S'inscrire avec mdp différents

| N° | Action | Résultat attendu | Résultat obtenu | Commentaires |
|---|---|---|---|---|
| 1 | Je remplis le formulaire | Tout s'écrit normalement | ✅ OK | - |
| 2 | Je tape "Pass123" dans mot de passe | OK | ✅ OK | - |
| 3 | Je tape "Pass456" dans confirmation | OK | ✅ OK | - |
| 4 | Je clique "Créer mon compte" | Message "Les mots de passe correspondent pas" | ✅ OK | validation faite |

---

## 2. Tests Gestion des Abonnements

### Test 2.1 : Ajouter un abonnement

| N° | Action | Résultat attendu | Résultat obtenu | Commentaires |
|---|---|---|---|---|
| 1 | Je me connecte et j'ouvre le dashboard | Le dashboard s'affiche | ✅ OK | formulaire sur la droite |
| 2 | Je mets "Netflix" dans nom du service | Ça s'écrit | ✅ OK | - |
| 3 | Je choisis 01/01/2025 comme date début | Le calendrier s'ouvre et je sélectionne | ✅ OK | pratique |
| 4 | Je mets 01/01/2026 pour la fin | Date acceptée | ✅ OK | - |
| 5 | Je tape 13.99 dans le prix | Ça prend le chiffre | ✅ OK | avec virgule |
| 6 | Je choisis "Streaming" dans la catégorie | Menu déroulant OK | ✅ OK | - |
| 7 | Je clique "Ajouter le service" | Message de succès | ✅ OK | - |
| 8 | Je regarde la liste | La carte Netflix apparait | ✅ OK | badge vert "Actif" |
| 9 | Je vérifie les stats en haut | Total: 1, Actifs: 1, Coût: 13.99€ | ✅ OK | ça se met à jour tout seul |

### Test 2.2 : Essayer d'ajouter sans être connecté

| N° | Action | Résultat attendu | Résultat obtenu | Commentaires |
|---|---|---|---|---|
| 1 | J'ouvre index.html sans me connecter | La page s'affiche | ✅ OK | - |
| 2 | Je remplis le formulaire d'ajout | Les champs acceptent | ✅ OK | - |
| 3 | Je clique "Ajouter" | Message d'erreur que je dois me connecter | ✅ OK | bien protégé |
| 4 | J'attends un peu | Ça me redirige vers login.html | ✅ OK | - |

### Test 2.3 : Modifier un abonnement existant

| N° | Action | Résultat attendu | Résultat obtenu | Commentaires |
|---|---|---|---|---|
| 1 | J'ai déjà un abonnement dans la liste | Liste visible avec icone modifier | ✅ OK | - |
| 2 | Je clique sur l'icône crayon | Une fenêtre s'ouvre avec les infos | ✅ OK | popup de modification |
| 3 | Je change le prix de 13.99 à 15.99 | Le champ accepte | ✅ OK | - |
| 4 | Je clique "Enregistrer" | La fenêtre se ferme + message succès | ✅ OK | - |
| 5 | Je regarde la carte | Le nouveau prix 15.99€ est là | ✅ OK | changement direct |
| 6 | Je check les stats | Le coût total a changé aussi | ✅ OK | recalculé automatiquement |

### Test 2.4 : Supprimer un abonnement

| N° | Action | Résultat attendu | Résultat obtenu | Commentaires |
|---|---|---|---|---|
| 1 | Je clique sur la poubelle d'un abonnement | Popup qui demande si je suis sûr | ✅ OK | pour éviter les erreurs |
| 2 | Je clique "Annuler" | Le popup se ferme, l'abonnement est toujours là | ✅ OK | - |
| 3 | Je reclique sur la poubelle | Le popup revient | ✅ OK | - |
| 4 | Cette fois je clique "Confirmer" | Message que c'est supprimé | ✅ OK | - |
| 5 | Je regarde | La carte a disparu | ✅ OK | - |
| 6 | Je vérifie les stats | Total et coût ont baissé | ✅ OK | - |

### Test 2.5 : Marquer un abonnement comme utilisé

| N° | Action | Résultat attendu | Résultat obtenu | Commentaires |
|---|---|---|---|---|
| 1 | Je clique "Marquer comme utilisé" sur une carte | Le bouton change | ✅ OK | retour visuel |
| 2 | Je regarde la carte | Badge vert "Utilisé aujourd'hui" apparait | ✅ OK | avec la date du jour |
| 3 | Je refresh la page | Le badge est encore là | ✅ OK | c'est sauvegardé |
| 4 | Je simule 30 jours après | Badge alerte "Inactif depuis 30j" | ✅ OK | prévient l'inactivité |

---

## 3. Tests Recherche et Filtres

### Test 3.1 : Rechercher un service par nom

| N° | Action | Résultat attendu | Résultat obtenu | Commentaires |
|---|---|---|---|---|
| 1 | J'ai Netflix, Spotify et Disney+ | Les 3 sont affichés | ✅ OK | - |
| 2 | Je clique dans la barre de recherche | Le champ devient actif | ✅ OK | - |
| 3 | Je tape "net" | Y'a que Netflix qui reste | ✅ OK | filtre en direct |
| 4 | Je continue "netflix" | Toujours juste Netflix | ✅ OK | majuscule/minuscule pareil |
| 5 | J'efface tout | Les 3 abonnements reviennent | ✅ OK | - |

### Test 3.2 : Chercher quelque chose qui existe pas

| N° | Action | Résultat attendu | Résultat obtenu | Commentaires |
|---|---|---|---|---|
| 1 | Je tape "xyz123" dans la recherche | Aucune carte | ✅ OK | - |
| 2 | Je regarde | Message "Aucun abonnement trouvé" | ✅ OK | - |
| 3 | Je check les stats | Toujours les bonnes valeurs | ✅ OK | pas impacté par la recherche |

---

## 4. Tests Import / Export

### Test 4.1 : Exporter mes abonnements en JSON

| N° | Action | Résultat attendu | Résultat obtenu | Commentaires |
|---|---|---|---|---|
| 1 | J'ai créé plusieurs abonnements | Ils sont tous dans la liste | ✅ OK | - |
| 2 | Je clique "Exporter JSON" | Un fichier se télécharge | ✅ OK | - |
| 3 | J'ouvre le fichier | C'est un fichier .json | ✅ OK | format correct |
| 4 | Je regarde dedans | Tous mes abonnements sont là | ✅ OK | avec toutes les infos |
| 5 | Je vérifie | Format tableau JSON avec des objets | ✅ OK | id, nomService, dates, prix... |

### Test 4.2 : Importer un fichier JSON

| N° | Action | Résultat attendu | Résultat obtenu | Commentaires |
|---|---|---|---|---|
| 1 | Je clique "Importer JSON" | Sélecteur de fichier | ✅ OK | - |
| 2 | Je choisis mon fichier JSON | Le fichier est accepté | ✅ OK | - |
| 3 | J'attends | Y'a un truc qui tourne | ✅ OK | chargement |
| 4 | Résultat | Message "X abonnements importés" | ✅ OK | X = le vrai nombre |
| 5 | Je regarde ma liste | Les nouveaux sont ajoutés | ✅ OK | pas de doublons |
| 6 | Je vérifie stats | Tout est recalculé | ✅ OK | - |

### Test 4.3 : Importer un mauvais fichier

| N° | Action | Résultat attendu | Résultat obtenu | Commentaires |
|---|---|---|---|---|
| 1 | Je clique "Importer JSON" | Sélecteur s'ouvre | ✅ OK | - |
| 2 | Je choisis un fichier .txt | Fichier rejeté | ✅ OK | vérifie l'extension |
| 3 | J'essaie avec un JSON cassé | Message d'erreur "Format invalide" | ✅ OK | bien géré |

---

## 5. Tests Dashboard Statistiques

### Test 5.1 : Voir les graphiques

| N° | Action | Résultat attendu | Résultat obtenu | Commentaires |
|---|---|---|---|---|
| 1 | Je clique sur "Dashboard" en haut | Page stats.html s'ouvre | ✅ OK | assez rapide |
| 2 | Je regarde les stats en haut | 4 cartes : Total, Actifs, Coût, Alertes | ✅ OK | bons chiffres |
| 3 | Je check le graphique rond | Graphique coloré avec les catégories | ✅ OK | joli |
| 4 | Je regarde le graphique en barres | Évolution du coût par mois | ✅ OK | avec les axes |
| 5 | Je passe la souris dessus | Infos détaillées qui s'affichent | ✅ OK | interactif cool |

### Test 5.2 : Dashboard quand j'ai rien

| N° | Action | Résultat attendu | Résultat obtenu | Commentaires |
|---|---|---|---|---|
| 1 | Nouveau compte vide | Je peux quand même accéder | ✅ OK | - |
| 2 | Je regarde les stats | Tout à zéro | ✅ OK | 0 abonnements, 0€ |
| 3 | Je check les graphiques | Message "Aucune donnée" | ✅ OK | pas d'erreur |

---

## 6. Tests Chatbot

### Test 6.1 : Utiliser le chatbot

| N° | Action | Résultat attendu | Résultat obtenu | Commentaires |
|---|---|---|---|---|
| 1 | Je clique sur l'icône chatbot en bas à droite | La fenêtre s'ouvre | ✅ OK | animation sympa |
| 2 | Je tape "Bonjour" | Le bot répond | ✅ OK | rapide |
| 3 | Je demande "Quel est mon budget ?" | Il me donne le coût mensuel | ✅ OK | calcul juste |
| 4 | Je tape "Mes abonnements actifs" | Liste des abonnements actifs | ✅ OK | bien présenté |

### Test 6.2 : Chercher via le chatbot

| N° | Action | Résultat attendu | Résultat obtenu | Commentaires |
|---|---|---|---|---|
| 1 | J'ouvre le chatbot | Fenêtre visible | ✅ OK | - |
| 2 | Je tape "Cherche Netflix" | Il me donne les détails de Netflix | ✅ OK | comprend bien |
| 3 | Je demande "Mes alertes" | Liste des trucs inactifs | ✅ OK | - |

---

## 7. Test Déconnexion

### Test 7.1 : Me déconnecter

| N° | Action | Résultat attendu | Résultat obtenu | Commentaires |
|---|---|---|---|---|
| 1 | Je suis connecté, je clique "Déconnexion" | Déconnexion | ✅ OK | direct |
| 2 | Je regarde où je suis | Retour à home.html | ✅ OK | - |
| 3 | Je check la navbar | Boutons "Se connecter" et "S'inscrire" de retour | ✅ OK | pseudo parti |
| 4 | J'essaie d'aller au dashboard | Je vois plus mes trucs persos | ✅ OK | session terminée |

---

## 8. Test Responsive (Mobile)

### Test 8.1 : Ouvrir sur téléphone

| N° | Action | Résultat attendu | Résultat obtenu | Commentaires |
|---|---|---|---|---|
| 1 | J'ouvre l'appli sur mon tel | Tout s'adapte | ✅ OK | menu burger |
| 2 | Je regarde les cartes | Empilées les unes sur les autres | ✅ OK | 1 par ligne |
| 3 | Je check le formulaire | En dessous de la liste | ✅ OK | bien placé |
| 4 | Les graphiques | Bien redimensionnés | ✅ OK | - |

---

## Bilan des Tests

| Partie testée | Nombre tests | Réussis | Remarques |
|---|---|---|---|
| Connexion/Inscription | 6 | 6 | RAS |
| Gestion abonnements | 5 | 5 | tout fonctionne |
| Recherche | 2 | 2 | OK |
| Import/Export | 3 | 3 | OK |
| Dashboard | 2 | 2 | graphiques niquel |
| Chatbot | 2 | 2 | réponses correctes |
| Déconnexion | 1 | 1 | OK |
| Mobile | 1 | 1 | responsive impec |
| **TOTAL** | **22** | **22** | **100%** |

---

## Infos Tests

- **Navigateurs** : Chrome, Firefox
- **OS** : Windows 11, Ubuntu
- **Écrans** : PC 1920x1080, portable 1366x768, mobile
- **Testé le** : 24/11/2025
- **Par** : Moi

---

## Conclusion

J'ai testé toutes les fonctionnalités de l'application et tout marche bien. Chaque utilisateur peut gérer ses propres abonnements après s'être inscrit et connecté. Les stats sont correctes, l'import/export JSON fonctionne, le chatbot répond bien aux questions. 

J'ai aussi vérifié que ça marche sur mobile et c'est bien adapté. Pas de bugs trouvés pendant mes tests.

L'interface est claire et assez simple à utiliser, les messages d'erreur aident quand on fait une erreur. Le système de confirmation par email empêche les faux comptes.

Globalement le projet remplit bien le cahier des charges.
