# 🚀 Démarrage Rapide - Intégration Bancaire

## ⚡ En 5 Minutes Chrono

### Étape 1 : Démarrer le Serveur (30 secondes)

```bash
cd /workspaces/Projet-Dev-Ops
mvn exec:java -Dexec.mainClass=com.projet.api.ApiServer
```

**Attendez :** `Server started on http://localhost:4567`

### Étape 2 : Ouvrir l'Interface (10 secondes)

**Navigateur :** `http://localhost:4567/bank-integration.html`

Vous devriez voir :
```
🏦 Intégration Bancaire Intelligente
Importez vos transactions, détectez les abonnements cachés et simulez votre solde
```

### Étape 3 : Charger Données de Test (20 secondes)

**Cliquez :** Bouton `[Charger Exemple]`

**Résultat Immédiat :**
- ✅ 24 transactions importées
- ⚠️ 8 abonnements détectés (Netflix, Spotify, etc.)
- 📊 Graphique de solde affiché
- 💰 Impact mensuel calculé

### Étape 4 : Explorer les Détections (2 minutes)

**Scrollez vers :** "Abonnements Non Déclarés Détectés"

**Vous verrez :**
```
⚠️ NON DÉCLARÉ
Netflix
📺 Streaming • 🔄 3 transaction(s) • 13.49€/mois
Dernière: 15/11/2024
[✅ Déclarer]
```

**Action :**
1. Cliquez sur `[Déclarer]`
2. Confirmez l'ajout
3. ✅ Netflix ajouté à vos abonnements
4. 🔄 Transactions re-traitées automatiquement
5. Badge vert "Rapproché" apparaît

### Étape 5 : Visualiser la Timeline (1 minute)

**Scrollez vers :** "Transactions Importées"

**Timeline par Mois :**
```
📍 Novembre 2024
┌────────────────────────────────────────┐
│ ✅ NETFLIX.COM          -13.49€        │
│    15/11/2024 • Streaming              │
│    🟢 Rapproché: Netflix               │
├────────────────────────────────────────┤
│ ⚠️ SPOTIFY AB            -9.99€        │
│    10/11/2024 • Musique                │
│    🟠 Détecté: Spotify   🔄 Récurrent  │
└────────────────────────────────────────┘
```

**Codes Couleur :**
- 🟢 Vert = Transaction rapprochée
- 🟠 Orange = Abonnement détecté
- ⚪ Blanc = Transaction normale

### Étape 6 : Consulter la Simulation (1 minute)

**En haut de page :**
```
┌─────────────────────┬─────────────────────┐
│ 💰 Solde Actuel     │ 🧮 Solde Prévu (30j)│
│   1500.00€          │   1410.56€          │
│   Mis à jour        │   ⬇️ -89.44€        │
└─────────────────────┴─────────────────────┘
```

**Graphique (6 mois) :**
- Courbe descendante si dépenses > revenus
- Tooltip au survol : Détails par mois
- Aide à décider : Garder/Résilier abonnements

## 🎓 Test Avancé : Votre Propre CSV

### Créer un Fichier CSV

**Fichier :** `mon_releve.csv`

```csv
Date,Description,Montant,Categorie
20/11/2024,NETFLIX.COM,-13.49,Streaming
15/11/2024,SPOTIFY PREMIUM,-9.99,Musique
10/11/2024,SFR MOBILE,-35.00,Telecom
05/11/2024,VIR SALAIRE,2500.00,Revenus
```

### Importer

1. **Cliquez** sur zone "Glissez votre fichier CSV ici"
2. **Sélectionnez** `mon_releve.csv`
3. **Attendez** 1 seconde (parsing automatique)

### Résultat

```
📊 Résultats du Rapprochement
┌────────┬────────┬────────┬────────┐
│   4    │   0    │   3    │   3    │
│ Trans. │ Rappr. │ Non D. │ Cachés │
└────────┴────────┴────────┴────────┘
```

**3 Abonnements Détectés :**
- Netflix (13.49€)
- Spotify (9.99€)
- SFR (35.00€)

**Action :** Déclarez-les en 3 clics !

## 📥 Template CSV Personnalisé

### Télécharger Template

**Cliquez :** `[Télécharger Template CSV]`

**Fichier obtenu :** `template_import_bancaire.csv`

```csv
Date,Description,Montant,Categorie
15/11/2024,NETFLIX.COM,-13.49,Streaming
10/11/2024,SPOTIFY AB,-9.99,Musique
05/11/2024,VIREMENT SALAIRE,2500.00,Revenus
01/11/2024,SFR MOBILE,-35.00,Telecom
```

### Personnaliser

1. **Ouvrez** avec Excel/Google Sheets
2. **Remplacez** les lignes par vos vraies données
3. **Sauvegardez** au format CSV
4. **Importez** via drag & drop

## 🏦 Importer Relevé Bancaire Réel

### Banques Françaises Supportées

**Format OFX :**
- BNP Paribas
- Société Générale
- Crédit Agricole
- LCL
- Banque Postale

**Format CSV :**
- Boursorama
- ING Direct
- N26
- Revolut

### Étapes

1. **Connectez-vous** à votre banque en ligne
2. **Menu :** Téléchargements / Exports
3. **Sélectionnez :** Format OFX ou CSV
4. **Période :** 1-3 mois recommandé
5. **Téléchargez** le fichier
6. **Glissez-déposez** dans l'interface

**⚠️ Attention :**
- Vérifiez que le fichier contient : Date, Description, Montant
- Certaines banques nomment les colonnes différemment
- Utilisez le template pour adapter si besoin

## 🔍 Interpréter les Résultats

### Badge "Rapproché" (Vert) ✅
**Signification :** Transaction liée à un abonnement que vous avez déclaré  
**Action :** Aucune, c'est normal !

### Badge "Détecté" (Orange) ⚠️
**Signification :** Abonnement trouvé mais pas dans votre liste  
**Action :** Cliquez "Déclarer" pour l'ajouter

### Badge "Récurrent" (Bleu) 🔄
**Signification :** Paiement qui revient tous les mois  
**Action :** Vérifiez si c'est un abonnement oublié

### Aucun Badge (Blanc) ⚪
**Signification :** Transaction ponctuelle normale  
**Action :** Rien à faire

## 💡 Cas d'Usage Pratiques

### Cas 1 : "J'ai trop d'abonnements !"

**Objectif :** Identifier les services inutilisés

**Méthode :**
1. Importez 3 mois de relevés
2. Consultez les abonnements détectés
3. Pour chaque service :
   - Utilisé récemment ? → Garder
   - Jamais utilisé ? → Résilier
4. Économie potentielle : 50-200€/an

### Cas 2 : "Mon budget est serré"

**Objectif :** Visualiser l'impact des abonnements

**Méthode :**
1. Importez vos transactions
2. Consultez "Solde Prévu (30j)"
3. Si négatif :
   - Identifiez abonnements les plus chers
   - Décidez lesquels résilier
   - Re-calculez l'impact
4. Graphique 6 mois aide à planifier

### Cas 3 : "Quelqu'un utilise mes comptes"

**Objectif :** Détecter comptes partagés non autorisés

**Méthode :**
1. Importez relevés récents
2. Cherchez doublons :
   - 2× Netflix = Quelqu'un paie avec votre carte
   - 2× Spotify = Compte familial ?
3. Action : Changer mot de passe / Bloquer carte

### Cas 4 : "Je veux optimiser mes finances"

**Objectif :** Analyse complète des dépenses récurrentes

**Méthode :**
1. Importez 6 mois de relevés
2. Notez tous les abonnements détectés
3. Comparez prix avec concurrents
4. Exemple : Netflix 13.49€ → Disney+ 8.99€ = -4.50€/mois
5. Économie annuelle : 54€

## 📊 Dashboard de Contrôle

### KPI à Surveiller

**Total Transactions :**
- Importées ce mois : 24
- Moyenne mensuelle : 20-30

**Rapprochées :**
- Objectif : > 80%
- Si < 50% : Beaucoup d'abonnements non déclarés

**Non Déclarées :**
- Si > 10 : Vérifiez les détections
- Action : Déclarer ou ignorer

**Abonnements Cachés :**
- Si > 0 : ⚠️ ALERTE
- Action immédiate : Déclarer ou résilier

### Simulation Solde

**Solde Actuel :** Votre banque réelle (à configurer)  
**Solde Prévu (30j) :** Projection avec abonnements

**Interprétation :**
- 🟢 Solde prévu > 1000€ : Sain
- 🟠 Solde prévu 200-1000€ : Attention
- 🔴 Solde prévu < 200€ : Risque découvert

## 🛠️ Configuration Avancée

### Modifier Solde Initial

**Fichier :** `bank-integration.js` ligne 11

```javascript
let currentBalance = 1500.00; // ← CHANGER ICI
```

**Exemple :** Si votre solde réel est 3200€ :
```javascript
let currentBalance = 3200.00;
```

### Ajouter Pattern Personnalisé

**Fichier :** `bank-integration.js` ligne 14

```javascript
SUBSCRIPTION_PATTERNS.push({
    name: 'Mon Service',
    keywords: ['monservice', 'mon-service'],
    category: 'Categorie',
    avgPrice: 19.99
});
```

**Exemple :** Votre salle de sport locale :
```javascript
SUBSCRIPTION_PATTERNS.push({
    name: 'Fitness Club Local',
    keywords: ['fitness club', 'fcl'],
    category: 'Sport',
    avgPrice: 25.00
});
```

### Modifier Tolérance Prix

**Rapprochement (±2€) :**
```javascript
// Ligne 240
if (Math.abs(transAmount - subPrice) <= 2) { // ← 2€
```

**Détection (±5€) :**
```javascript
// Ligne 264
if (Math.abs(transAmount - pattern.avgPrice) <= 5) { // ← 5€
```

## 🎯 Bonnes Pratiques

### Fréquence d'Import

✅ **Mensuel** : Import chaque début de mois  
✅ **Trimestriel** : Si peu d'abonnements  
❌ **Annuel** : Trop long, perte de détails  

### Nettoyage Données

**Avant Import :**
1. Vérifiez encodage UTF-8
2. Supprimez lignes vides
3. Uniformisez format dates

**Après Import :**
1. Déclarez abonnements détectés
2. Ignorez transactions ponctuelles
3. Notez économies possibles

### Sécurité

🔒 **Ne jamais partager** vos fichiers CSV/OFX  
🔒 **Supprimer** après import  
🔒 **Utiliser données exemple** pour tests  

## ❓ FAQ Express

**Q : Mes transactions n'apparaissent pas**  
R : Vérifiez colonnes Date, Description, Montant

**Q : Aucun abonnement détecté**  
R : Normal si noms différent des patterns (ajoutez manuellement)

**Q : Graphique vide**  
R : Importez au moins 1 transaction

**Q : Doublons dans la liste**  
R : Si même date + montant = Normal (plusieurs transactions/jour)

**Q : Solde négatif prévu**  
R : Vos abonnements > revenus, résiliez services inutiles

## 🎉 Félicitations !

Vous maîtrisez maintenant l'**Intégration Bancaire Intelligente** !

**Prochaines Étapes :**
1. Importez vos vrais relevés
2. Déclarez les abonnements cachés
3. Consultez le graphique de projection
4. Décidez quels services garder/résilier
5. **Économisez de l'argent ! 💰**

**Besoin d'aide ?**  
📖 Lisez `/docs/INTEGRATION_BANCAIRE.md` (documentation complète)

**Temps total : 5 minutes ⏱️**  
**Économies potentielles : 50-200€/an 💰**  
**Satisfaction : 100% 😊**
