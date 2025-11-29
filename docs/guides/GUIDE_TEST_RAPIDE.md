# 🧪 Guide de Test Rapide - Version 2.0

## 🚀 Démarrage

### 1. Lancer le serveur
```bash
cd /workspaces/Projet-Dev-Ops
mvn exec:java -Dexec.mainClass="com.projet.api.ApiServer"
```

Attendez le message : `API server démarré sur http://localhost:4567`

### 2. Ouvrir l'application
Ouvrez votre navigateur à : **http://localhost:4567**

---

## ✅ Tests à Effectuer

### 🎨 Test 1 : Système de Thèmes

#### Étapes
1. Cliquez sur **"Thèmes"** dans le menu
2. Sélectionnez un thème prédéfini (ex: "Bleu Océan")
3. Naviguez vers **Dashboard** → Les couleurs doivent changer
4. Naviguez vers **Dépenses** → Les couleurs doivent être cohérentes
5. Naviguez vers **Export/Import** → Les boutons doivent avoir les nouvelles couleurs
6. Retournez à **Thèmes** et créez un thème personnalisé
7. Rafraîchissez la page → Le thème doit persister

#### ✅ Résultat attendu
- Toutes les pages utilisent les mêmes couleurs
- Le thème persiste après rechargement
- Les gradients et boutons suivent le thème

---

### 📥 Test 2 : Import de Données

#### Test 2.1 : Import CSV
1. Allez sur **Export/Import**
2. Cliquez sur **"📁 Choisir un fichier"**
3. Sélectionnez le fichier `/workspaces/Projet-Dev-Ops/test_import.csv`
4. Vérifiez la prévisualisation :
   - Nombre de transactions : 8
   - Abonnements détectés : 5-6 (Netflix, Spotify, Adobe, etc.)
5. Cliquez sur **"✅ Confirmer l'import"**
6. Attendez le message de succès
7. Allez sur **Dépenses** → Vérifiez que les abonnements apparaissent

#### Test 2.2 : Import OFX
1. Sur **Export/Import**, cliquez sur **"📁 Choisir un fichier"**
2. Sélectionnez `/workspaces/Projet-Dev-Ops/test_import.ofx`
3. Vérifiez la prévisualisation :
   - 4 transactions bancaires
   - Montants négatifs convertis en positifs
   - Catégories détectées automatiquement
4. Confirmez l'import
5. Vérifiez sur **Dépenses**

#### ✅ Résultat attendu
- Import réussi avec message de confirmation
- Abonnements visibles dans la page Dépenses
- Catégories correctement détectées
- Aucune erreur dans la console (F12)

---

### 📤 Test 3 : Export de Données

**Important** : Vous devez être connecté pour exporter

#### Test 3.1 : Export PDF
1. Allez sur **Export/Import**
2. Cliquez sur **"📄 Exporter en PDF"**
3. Un fichier PDF doit se télécharger
4. Ouvrez le PDF :
   - Vérifiez le header avec les couleurs du thème
   - Vérifiez la section "Indicateurs Clés"
   - Vérifiez le tableau des abonnements
   - Vérifiez le tableau des dépenses

#### Test 3.2 : Export CSV
1. Cliquez sur **"📊 Exporter en CSV"**
2. Ouvrez le fichier avec Excel ou un éditeur de texte
3. Vérifiez les 2 sections :
   - Abonnements
   - Dépenses

#### Test 3.3 : Export JSON
1. Cliquez sur **"💾 Exporter en JSON"**
2. Ouvrez le fichier JSON
3. Vérifiez la structure :
   ```json
   {
     "version": "1.0",
     "exportDate": "...",
     "expenses": [...],
     "subscriptions": [...],
     "metadata": {...}
   }
   ```

#### Test 3.4 : Historique d'Export
1. Après avoir exporté plusieurs fois, scrollez vers le bas
2. Trouvez la section **"Historique des exports"**
3. Vérifiez que vos exports sont listés
4. Cliquez sur **"🔄 Régénérer"** sur un export
5. Un nouveau fichier doit se télécharger

#### ✅ Résultat attendu
- Fichiers téléchargés correctement
- Contenu complet avec vos données
- Historique mis à jour
- Bouton "Régénérer" fonctionnel

---

### 📧 Test 4 : Notifications Email

#### Configuration SMTP (Optionnel)
1. Allez sur **Notifications**
2. Si vous avez un serveur SMTP, configurez-le :
   - Serveur : smtp.gmail.com (pour Gmail)
   - Port : 587
   - Email : votre.email@gmail.com
   - Mot de passe : votre mot de passe d'application
3. Cliquez sur **"💾 Sauvegarder SMTP"**

#### Test Email
1. Assurez-vous que votre email est configuré
2. Cochez les types de notifications (ex: "Alertes d'expiration")
3. Cliquez sur **"📧 Envoyer Email de Test"**
4. Vérifiez votre boîte mail

#### ✅ Résultat attendu
- Message de confirmation d'envoi
- Email reçu dans votre boîte (si SMTP configuré)
- En mode simulation : message "Mode simulation"
- Historique des notifications mis à jour

---

### 🔐 Test 5 : Isolation Utilisateur

#### Test avec Deux Comptes
1. **Utilisateur 1** :
   - Connectez-vous avec `mdaziz.tlili@gmail.com`
   - Créez 2-3 abonnements
   - Changez le thème en "Rose Sunset"
   - Exportez un PDF

2. **Déconnexion** :
   - Cliquez sur le bouton de déconnexion

3. **Utilisateur 2** :
   - Connectez-vous avec `f.mayssara@gmail.com`
   - Vérifiez que vous ne voyez PAS les abonnements de l'utilisateur 1
   - Le thème est celui par défaut (ou celui de l'utilisateur 2)
   - Créez quelques abonnements différents

4. **Re-connexion Utilisateur 1** :
   - Reconnectez-vous avec `mdaziz.tlili@gmail.com`
   - Vérifiez que vos abonnements sont toujours là
   - Vérifiez que le thème "Rose Sunset" est actif

#### ✅ Résultat attendu
- Chaque utilisateur voit uniquement ses propres données
- Les thèmes sont bien isolés par utilisateur
- Les exports contiennent uniquement les données de l'utilisateur connecté

---

### 🎯 Test 6 : Navigation et Responsive

#### Test Navigation
1. Cliquez sur tous les liens du menu :
   - Dashboard ✅
   - AI Analytics ✅
   - Dépenses ✅
   - Export/Import ✅
   - Notifications ✅
   - Thèmes ✅
   - Support ✅
   - Home ✅

2. Sur chaque page :
   - Vérifiez qu'il n'y a pas d'erreur dans la console (F12)
   - Vérifiez que le thème est appliqué
   - Vérifiez que la navbar reste fonctionnelle

#### Test Responsive
1. Ouvrez les outils développeur (F12)
2. Activez le mode "Device Toolbar" (Ctrl+Shift+M)
3. Testez différentes résolutions :
   - iPhone SE (375px)
   - iPad (768px)
   - Desktop (1920px)

4. Vérifiez :
   - Menu hamburger sur mobile
   - Cartes adaptées à la largeur
   - Boutons accessibles
   - Pas de débordement horizontal

#### ✅ Résultat attendu
- Toutes les pages se chargent sans erreur
- Design responsive sur mobile/tablette
- Aucune erreur JavaScript dans la console

---

## 🐛 Vérification des Bugs Corrigés

### Bug #1 : Thèmes non appliqués
- [x] Dashboard affiche les couleurs du thème
- [x] Page Dépenses affiche les couleurs du thème
- [x] Le gradient de fond change avec le thème

### Bug #2 : Import en localStorage
- [x] Import CSV envoie les données au backend
- [x] Import OFX envoie les données au backend
- [x] Vérification de la session avant import
- [x] Messages d'erreur clairs

### Bug #3 : Export sans authentification
- [x] Export PDF vérifie la session
- [x] Export CSV vérifie la session
- [x] Redirection vers login si non connecté

### Bug #4 : Boutons historique
- [x] Bouton "Régénérer" télécharge un nouveau fichier
- [x] Type de fichier correct (PDF/CSV/JSON/Excel)

---

## 📊 Checklist Complète

### Avant de Tester
- [ ] Serveur démarré sur port 4567
- [ ] Navigateur ouvert sur http://localhost:4567
- [ ] Console développeur ouverte (F12)

### Tests Fonctionnels
- [ ] Connexion utilisateur
- [ ] Changement de thème
- [ ] Import CSV
- [ ] Import OFX
- [ ] Export PDF
- [ ] Export CSV
- [ ] Export JSON
- [ ] Régénération depuis historique
- [ ] Configuration email
- [ ] Test d'email
- [ ] Navigation entre pages
- [ ] Responsive mobile

### Tests de Sécurité
- [ ] Isolation des données entre utilisateurs
- [ ] Redirection si non connecté (export/import)
- [ ] Session persistante

### Tests de Performance
- [ ] Chargement thème < 1s
- [ ] Export PDF < 3s
- [ ] Import < 2s
- [ ] Navigation fluide

---

## 🎉 Résultat Final Attendu

Si tous les tests passent :

✅ **Système de Thèmes** : Fonctionnel sur toutes les pages
✅ **Import/Export** : Données persistées au backend
✅ **Notifications** : Configuration et test opérationnels
✅ **Isolation** : Données séparées par utilisateur
✅ **UI/UX** : Responsive et sans erreurs
✅ **Performance** : Rapide et fluide

---

## 📝 Notes de Debug

Si vous rencontrez un problème :

1. **Ouvrez la console** (F12 → Console)
2. **Notez l'erreur** exacte
3. **Vérifiez** :
   - Le serveur est bien démarré
   - Vous êtes connecté
   - Le fichier d'import est valide
4. **Logs serveur** : Regardez le terminal où le serveur tourne

---

*Guide créé le 29 Novembre 2024*
*Version 2.0 - Corrections Intégrées*
