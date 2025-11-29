# 💰 Guide Rapide - Module Dépenses

## 🚀 Comment accéder ?

### Option 1 : Via la Navbar
1. Ouvrez votre navigateur : `http://localhost:4567`
2. Connectez-vous à votre compte
3. Cliquez sur **"Dépenses"** 💰 dans la barre de navigation
4. Le tableau de bord se charge automatiquement !

### Option 2 : URL Directe
Accédez directement à : `http://localhost:4567/expenses.html`

⚠️ **Attention** : Vous devez être connecté pour accéder à cette page.

---

## 🎯 Ce que vous verrez

### 1️⃣ **Indicateurs Clés (en haut)**
- 💰 **Dépenses Totales** : Somme de tous vos abonnements actifs
- 🎯 **Budget Restant** : Combien il vous reste ce mois
- 📊 **Moyenne Mensuelle** : Votre dépense moyenne
- 💡 **Économies Potentielles** : Ce que vous pourriez économiser

### 2️⃣ **Barre de Budget**
Une barre colorée qui montre votre progression :
- 🟢 **Vert** : Vous êtes en dessous de 70% → Tout va bien !
- 🟠 **Orange** : Entre 70% et 90% → Faites attention
- 🔴 **Rouge** : Plus de 90% → Budget dépassé !

### 3️⃣ **Graphiques**
- 📈 **Évolution Mensuelle** : Vos dépenses sur 6 mois
- 🍩 **Répartition par Catégorie** : Où va votre argent ?

### 4️⃣ **Recommandations**
Des conseils personnalisés pour économiser :
- ❌ Abonnements inutilisés à résilier
- ⚠️ Alertes de dépassement de budget
- 📊 Catégories à optimiser

### 5️⃣ **Timeline**
L'historique de vos 10 derniers abonnements avec dates et prix.

### 6️⃣ **Vue par Catégorie**
Chaque catégorie (Streaming, Gaming, Fitness...) avec :
- Nombre d'abonnements
- Montant total
- Pourcentage du budget

---

## ⚙️ Fonctionnalités Interactives

### 💵 Définir votre Budget
1. Cliquez sur le bouton **"Définir le Budget"** (en haut à droite)
2. Entrez votre budget mensuel (ex: 150€)
3. Cliquez OK

✅ Le budget est sauvegardé dans votre navigateur !

### 🔍 Filtrer les Données
Cliquez sur les ongles pour filtrer par période :
- **Tout** : Tous vos abonnements
- **Mois** : Les 30 derniers jours
- **Trimestre** : Les 90 derniers jours
- **Année** : Les 365 derniers jours

### 📊 Interagir avec les Graphiques
- **Survolez** les graphiques pour voir les détails
- **Cliquez** sur les légendes pour afficher/masquer des données

---

## 🏷️ Catégories Automatiques

Le système détecte automatiquement la catégorie de vos abonnements :

| Icône | Catégorie | Exemples |
|-------|-----------|----------|
| 📺 | **Streaming** | Netflix, Disney+, Prime Video |
| 🎮 | **Gaming** | PlayStation Plus, Xbox Game Pass |
| 💼 | **Productivité** | Microsoft 365, Adobe, Slack |
| 💪 | **Fitness** | BasicFit, Gym, Sport |
| 📚 | **Éducation** | Coursera, Udemy, Skillshare |
| 🎵 | **Musique** | Spotify, Apple Music, Deezer |
| ☁️ | **Cloud** | Dropbox, OneDrive, iCloud |
| 💳 | **Finance** | Banque, Assurance, Crédit |
| 📦 | **Autre** | Tout le reste |

**Pas besoin de définir manuellement** : Le système le fait pour vous !

---

## 💡 Conseils d'Utilisation

### Pour Économiser
1. Regardez les **Économies Potentielles** 💡
2. Consultez les **Recommandations** en bas de page
3. Résiliez les abonnements inutilisés (> 30 jours)

### Pour Suivre votre Budget
1. Définissez un **budget réaliste** chaque mois
2. Surveillez la **barre de progression**
3. Consultez la **répartition par catégorie**

### Pour Analyser
1. Regardez l'**évolution mensuelle** (graphique ligne)
2. Identifiez la **catégorie la plus coûteuse** (graphique donut)
3. Consultez la **timeline** pour voir l'historique

---

## 🔄 Rafraîchissement

Les données sont **automatiquement rafraîchies toutes les 30 secondes**.

Vous pouvez aussi **recharger la page** manuellement (F5) pour forcer le rafraîchissement.

---

## 🆘 En cas de Problème

### "Impossible de charger les données"
✅ **Solutions** :
- Vérifiez que vous êtes bien connecté
- Rechargez la page (F5)
- Vérifiez que le serveur tourne (http://localhost:4567)

### "Vous devez être connecté"
✅ **Solutions** :
- Cliquez sur "Se connecter" dans la navbar
- Entrez vos identifiants
- Revenez sur /expenses.html

### Les graphiques ne s'affichent pas
✅ **Solutions** :
- Vérifiez que vous avez des abonnements
- Rechargez la page (F5)
- Videz le cache du navigateur (Ctrl+Shift+Del)

---

## 📱 Compatible avec

✅ Chrome, Firefox, Edge, Safari  
✅ Desktop, Tablette, Mobile  
✅ Toutes les résolutions d'écran

---

## 🎨 Interface

L'interface utilise le même **design moderne** que le reste de l'application :
- Effet **verre/glassmorphism**
- Couleurs **violet/bleu dégradé**
- Animations **fluides**
- Icônes **Bootstrap Icons**

---

## 📊 Exemple Concret

### Scénario
Vous avez 5 abonnements :
- Netflix : 12€/mois (Streaming) 📺
- Spotify : 10€/mois (Musique) 🎵
- BasicFit : 20€/mois (Fitness) 💪
- Office 365 : 7€/mois (Productivité) 💼
- Un vieux abonnement gaming : 15€/mois (non utilisé depuis 60 jours) 🎮

### Ce que vous verrez

**KPIs** :
- Dépenses Totales : **64€**
- Budget Restant : **86€** (si budget = 150€)
- Moyenne : ~**60€**
- Économies Potentielles : **15€** (l'abonnement gaming)

**Recommandations** :
- ❌ **Résilier Gaming** : Non utilisé depuis 60 jours → Économie : 15€/mois
- 📊 **Optimiser Fitness** : Représente 31% des dépenses

**Graphique Donut** :
- Fitness : 31% (rouge)
- Gaming : 23% (orange)
- Streaming : 19% (violet)
- Musique : 16% (rose)
- Productivité : 11% (vert)

---

## 🚀 Prochaines Étapes

Après avoir exploré le module Dépenses :
1. Définissez votre budget mensuel
2. Consultez les recommandations
3. Résiliez les abonnements inutilisés
4. Suivez votre progression chaque mois !

---

## 📞 Besoin d'Aide ?

- 💬 **Chatbot** : Cliquez sur l'icône en bas à droite
- 📧 **Email** : support@abonnements.com
- 📖 **Documentation** : [/help.html](/help.html)

---

**Bon suivi de vos dépenses ! 💰📊**
