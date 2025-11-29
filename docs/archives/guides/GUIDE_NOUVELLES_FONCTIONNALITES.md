# 📚 Guide d'Utilisation des Nouvelles Fonctionnalités

## Version 2.0 - Janvier 2024

Bienvenue dans ce guide complet des nouvelles fonctionnalités de Gestion Abonnements v2.0 ! Ce document vous aidera à maîtriser les trois grandes nouveautés : les thèmes personnalisables, l'export/import avancé, et les notifications email.

---

## 🎨 1. Système de Thèmes Personnalisables

### Accès
Cliquez sur **"Thèmes"** dans la barre de navigation (icône palette 🎨)

### Thèmes Prédéfinis

Nous proposons **6 thèmes soigneusement conçus** :

1. **Violet Premium** (par défaut)
   - Couleurs : #667eea → #764ba2 → #f093fb
   - Style : Professionnel et moderne

2. **Bleu Océan**
   - Couleurs : #4facfe → #00f2fe → #43e97b
   - Style : Frais et dynamique

3. **Rose Sunset**
   - Couleurs : #f857a6 → #ff5858 → #feca57
   - Style : Chaleureux et moderne

4. **Vert Nature**
   - Couleurs : #11998e → #38ef7d → #7ed6df
   - Style : Apaisant et naturel

5. **Orange Énergie**
   - Couleurs : #f79d00 → #ff6b6b → #ee5a6f
   - Style : Énergique et motivant

6. **Minimaliste**
   - Couleurs : #667eea → #764ba2 → #a8a8a8
   - Style : Sobre et élégant

### Utilisation des Thèmes Prédéfinis

1. Accédez à la page **Thèmes**
2. Parcourez les 6 cartes de thèmes
3. Cliquez sur le thème souhaité
4. Le thème est **appliqué instantanément** sur toutes les pages
5. Votre choix est **sauvegardé automatiquement**

### Personnalisation Avancée

Pour créer votre **thème unique** :

1. Descendez à la section **"Personnalisation Avancée"**
2. Utilisez les **3 color pickers** :
   - **Couleur Primaire** : Couleur principale de l'application
   - **Couleur Secondaire** : Couleur de dégradé
   - **Couleur Accent** : Couleur de mise en avant
3. Observez le **preview en temps réel** avec les KPI cards
4. Cliquez sur **"Appliquer le Thème"** pour sauvegarder
5. Utilisez **"Réinitialiser"** pour revenir au thème Violet Premium

### Aperçu en Temps Réel

La section preview affiche :
- **4 KPI cards** stylisées avec votre thème
- **Barre de gradient** montrant la transition de couleurs
- **Mise à jour instantanée** à chaque changement

### Conseils de Personnalisation

✅ **Bonnes Pratiques** :
- Utilisez des couleurs **contrastées** pour la lisibilité
- Testez votre thème sur **plusieurs pages** (Dashboard, Dépenses, etc.)
- Vérifiez que les textes blancs **restent lisibles**

⚠️ **À Éviter** :
- Couleurs trop claires (mauvaise visibilité sur fond clair)
- Combinaisons peu contrastées
- Couleurs trop saturées (fatigue visuelle)

---

## 📥 2. Export/Import Avancé

### Accès
Cliquez sur **"Export/Import"** dans la barre de navigation (icône téléchargement 📥)

### Exports Disponibles

#### 📄 Export PDF
**Idéal pour** : Rapports professionnels, archivage, partage avec comptable

**Contenu** :
- Header thématisé avec vos couleurs personnalisées
- **KPIs principaux** : Total dépenses, nombre d'abonnements, budget restant
- **Table des abonnements** avec détails (prix, fréquence, expiration)
- **Table des dépenses** (20 dernières transactions)
- Footer avec numérotation des pages

**Utilisation** :
1. Cliquez sur **"Générer PDF"**
2. Le fichier se télécharge automatiquement
3. Nom : `rapport_financier_YYYY-MM-DD.pdf`

#### 📊 Export CSV
**Idéal pour** : Excel, Google Sheets, analyses personnalisées

**Format** :
```csv
Type,Date,Description,Catégorie,Montant,Fréquence,Expiration
Abonnement,2024-01-15,Netflix,Streaming,15.99,Mensuel,2024-12-15
Dépense,2024-01-20,Restaurant,Alimentation,45.50,,
```

**Utilisation** :
1. Cliquez sur **"Générer CSV"**
2. Importez dans Excel ou Google Sheets
3. Utilisez les filtres et tableaux croisés dynamiques

#### 💾 Export JSON
**Idéal pour** : Sauvegarde complète, restauration, migration

**Contenu** :
- Tous vos **abonnements**
- Toutes vos **dépenses**
- Paramètres de **budget**
- Préférences de **thème**
- **Métadonnées** (version, date d'export, email utilisateur)

**Utilisation** :
1. Cliquez sur **"Générer JSON"**
2. Conservez ce fichier comme **backup**
3. Utilisez-le pour restaurer vos données

### Imports Supportés

#### 🏦 Import OFX (Open Financial Exchange)
**Source** : Relevés bancaires français (BNP, Société Générale, etc.)

**Fonctionnalités** :
- **Parsing automatique** des transactions
- **Détection des catégories** (Streaming, Transport, Alimentation...)
- **Identification des abonnements** récurrents
- **Conversion des montants** (gestion des débits/crédits)

**Étapes** :
1. Téléchargez votre relevé OFX depuis votre banque
2. Glissez-déposez le fichier dans la zone prévue
3. Consultez l'**aperçu des transactions**
4. Vérifiez les **statistiques** (total, période, abonnements détectés)
5. Cliquez sur **"Confirmer l'import"**

#### 📑 Import CSV
**Source** : Relevés bancaires CSV, exports d'autres applications

**Détection Automatique** :
L'application détecte intelligemment :
- Les colonnes **Date** (multiples formats supportés)
- Les colonnes **Montant** (avec ou sans symbole €)
- Les colonnes **Description/Libellé**
- Les colonnes **Catégorie** (si présente)

**Catégorisation Automatique** :
Si aucune catégorie n'est fournie, le système détecte :
- **Streaming** : Netflix, Spotify, Amazon Prime, Disney+
- **Télécom** : Orange, SFR, Bouygues, Free
- **Transport** : SNCF, Uber, Essence
- **Alimentation** : Restaurants, Uber Eats, Deliveroo
- **Énergie** : EDF, Gaz, Eau
- **Autres** : Par défaut

#### 🔄 Import JSON
**Source** : Sauvegardes de l'application

**Utilisation** :
1. Sélectionnez votre fichier de backup JSON
2. L'application détecte automatiquement le format
3. Les données sont restaurées :
   - Abonnements
   - Dépenses
   - Budget
   - Thème personnalisé

### Aperçu Avant Import

Pour chaque import, vous voyez :

**Statistiques** :
- 📊 Nombre total de transactions
- 🔁 Abonnements récurrents détectés
- 💰 Montant total
- 📅 Période couverte

**Tableau de prévisualisation** :
- 50 premières transactions
- Colonnes : Date, Description, Catégorie, Montant, Type
- Possibilité de **valider ou annuler**

### Historique des Exports

Consultez vos **10 derniers exports** :
- Date et heure
- Type de fichier (PDF, CSV, JSON)
- Taille approximative
- Option de re-téléchargement (à venir)

---

## 📧 3. Notifications Email Automatiques

### Accès
Cliquez sur **"Notifications"** dans la barre de navigation (icône enveloppe 📧)

### Configuration Initiale

#### 1️⃣ Paramètres Email

**Adresse email** :
- Entrez l'email où recevoir les notifications
- Par défaut, utilise l'email de votre compte

**Fréquence des rappels** :
- **Quotidien** : Récapitulatif journalier
- **Hebdomadaire** : Résumé chaque semaine
- **Mensuel** : Rapport mensuel uniquement

**Jours avant expiration** :
- Définissez le délai d'alerte (1-30 jours)
- Recommandé : **7 jours** pour anticiper les renouvellements

#### 2️⃣ Types de Notifications

**⚠️ Alertes d'Expiration**
- Email envoyé X jours avant l'expiration
- Contenu :
  - Nom de l'abonnement
  - Prix mensuel
  - Date d'expiration
  - Lien vers le dashboard

**💸 Dépassement de Budget**
- Alerte automatique si vous dépassez votre budget mensuel
- Contenu :
  - Budget défini
  - Montant dépensé
  - Montant du dépassement
  - Pourcentage de dépassement

**📊 Résumé Mensuel**
- Envoyé le 1er de chaque mois à 9h
- Contenu :
  - Total dépensé
  - Nombre d'abonnements actifs
  - Coût total des abonnements
  - Nombre de transactions
  - Top 5 des catégories
  - Comparaison avec le mois précédent

**🔔 Nouveaux Abonnements**
- Confirmation à chaque ajout d'abonnement
- Résumé des détails

**📈 Dépenses Inhabituelles**
- Détection des dépenses anormalement élevées
- Comparaison avec vos habitudes

#### 3️⃣ Test de Configuration

Avant d'activer les notifications, **testez votre configuration** :

1. Remplissez votre adresse email
2. Cliquez sur **"Envoyer un email de test"**
3. Vérifiez votre boîte de réception
4. L'email de test contient :
   - Confirmation que la configuration fonctionne
   - Liste des notifications activées
   - Date et heure du test
   - Lien vers les paramètres

### Aperçu des Emails

Visualisez les **3 types d'emails principaux** :

**🔔 Email d'Expiration** :
- Design orange/jaune (alerte)
- Encadré avec compte à rebours
- Détails de l'abonnement
- Bouton "Gérer mes abonnements"

**💸 Email de Budget** :
- Design rouge (danger)
- 3 KPI boxes (Budget / Dépensé / Dépassement)
- Pourcentage de dépassement
- Bouton "Voir mes dépenses"

**📊 Email Mensuel** :
- Design violet (professionnel)
- 4 KPI principales
- Liste récapitulative :
  - Abonnements renouvelés
  - Expirations à venir
  - Économies réalisées
- Bouton "Voir le rapport complet"

### Configuration SMTP Avancée

Pour les utilisateurs avancés, configurez votre **propre serveur SMTP** :

**Gmail** :
1. Serveur : `smtp.gmail.com`
2. Port : `587`
3. Sécurité : `TLS`
4. **Important** : Utilisez un [Mot de passe d'application](https://support.google.com/accounts/answer/185833)

**Autres Fournisseurs** :
- **Outlook** : `smtp.office365.com:587`
- **Yahoo** : `smtp.mail.yahoo.com:587`
- **SendGrid** : `smtp.sendgrid.net:587`

### Historique des Notifications

Consultez les **20 dernières notifications** :
- Date et heure d'envoi
- Type de notification
- Statut (Envoyé / Simulé / Erreur)

### Vérifications Automatiques

Le système effectue des **vérifications périodiques** :

- **Expirations** : Vérification quotidienne
- **Budget** : Vérification toutes les heures
- **Rapport mensuel** : Automatique le 1er de chaque mois

### Mode Simulation

En environnement de développement (backend non configuré) :
- Les emails sont **simulés**
- Vous voyez les notifications dans la console
- L'historique indique "Simulé" au lieu de "Envoyé"

---

## 🎯 Conseils d'Utilisation

### Workflow Recommandé

1. **Configuration Initiale** :
   - Choisissez votre thème préféré
   - Configurez vos notifications email
   - Testez l'envoi d'email

2. **Import de Données** :
   - Importez vos relevés bancaires (OFX/CSV)
   - Vérifiez la catégorisation automatique
   - Ajustez les catégories si nécessaire

3. **Utilisation Quotidienne** :
   - Consultez votre dashboard
   - Ajoutez les nouvelles dépenses
   - Surveillez les alertes email

4. **Exports Réguliers** :
   - Export JSON mensuel pour backup
   - Export PDF pour votre comptable
   - Export CSV pour analyses Excel

### Bonnes Pratiques

✅ **Recommandations** :
- Activez les **alertes d'expiration** (7 jours avant)
- Faites un **backup JSON mensuel**
- Vérifiez le **résumé mensuel** reçu par email
- Utilisez un **thème confortable** pour vos yeux
- Importez vos relevés bancaires **chaque mois**

⚠️ **Précautions** :
- Conservez vos **backups JSON** en lieu sûr
- Utilisez un **mot de passe d'application** pour Gmail
- Vérifiez vos **spams** lors du premier email
- Ne partagez pas vos exports (données personnelles)

---

## 🆘 Dépannage

### Les thèmes ne s'appliquent pas
- Videz le cache du navigateur (Ctrl+Shift+R)
- Vérifiez que JavaScript est activé
- Consultez la console développeur (F12)

### Les emails ne sont pas reçus
- Vérifiez votre dossier spam
- Testez avec "Envoyer un email de test"
- Vérifiez la configuration SMTP
- Utilisez un mot de passe d'application (Gmail)

### L'import échoue
- Vérifiez le format du fichier (OFX/CSV/JSON)
- Assurez-vous que le fichier n'est pas vide
- Consultez l'aperçu pour identifier les erreurs
- Essayez avec un fichier d'exemple

### Le PDF ne se génère pas
- Autorisez les pop-ups dans votre navigateur
- Vérifiez que jsPDF est chargé (console F12)
- Essayez avec un navigateur récent (Chrome, Firefox)

---

## 📞 Support

Pour toute question ou problème :
- Consultez la **documentation complète** dans `/docs`
- Regardez les **tutoriels vidéo** dans l'application
- Utilisez le **chatbot IA** pour de l'aide contextuelle
- Contactez le support : support@gestion-abonnements.fr

---

**Version du guide** : 2.0 - Janvier 2024  
**Dernière mise à jour** : Ajout des fonctionnalités Thèmes, Export/Import, Notifications Email
