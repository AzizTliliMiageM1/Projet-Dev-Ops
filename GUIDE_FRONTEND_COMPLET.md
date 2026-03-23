# 🎨 Guide Complet du Frontend - Semestre 2

**Date:** Mars 2026  
**Version:** 2.0.0 Enhanced

---

## 🚀 Démarrage Rapide

### 1. Lancer le serveur
```bash
cd backend
mvn clean package -DskipTests
java -jar target/gestion-abonnements-1.0-SNAPSHOT.jar
```

### 2. Accéder au frontend
Ouvrir navigateur: `http://localhost:4567`

### 3. S'authentifier
- **New User:** Cliquez sur "S'inscrire" → Remplissez le formulaire
- **Existing User:** Entrez email + mot de passe → "Se connecter"

---

## 📖 Guide des Fonctionnalités

### 🔐 Authentification & Profil

#### Inscription
1. Page d'accueil → Cliquez "S'inscrire"
2. Remplissez:
   - Email (unique)
   - Mot de passe (6+ chars recommended)
   - Confirmez le mot de passe
3. Cliquez "S'Inscrire"
4. **Auto-confirmation** en mode démo
5. Redirection vers Dashboard

#### Connexion  
1. Page d'accueil → Remplissez Email + Mot de passe
2. Cliquez "Se Connecter"
3. Dashboard s'affiche
4. Votre email s'affiche dans la navbar

#### Déconnexion
- Navbar (haut à droite) → Bouton "Déconnexion" (rouge)
- Session terminer, retour à login

---

### 📊 Dashboard (Onglet 1)

**Vue:** Aperçu de tous vos abonnements

**Stats en Temps Réel:**
1. **Total Abonnements** - Nombre total
2. **Dépense Mensuelle** - Somme prix/mois
3. **Prévision 3 Mois** - Total × 3
4. **Santé Portfolio** - Score 0-100%

**Contenu:**
- Vue d'ensemble des données
- Top 3 services les plus chers
- Statistiques (moyenne, min, max)

**Actions:**
- Refresh automatique toutes les 30s
- Cliquez autre onglet pour detailing

---

### 📋 Onglet Abonnements (2)

**Vue:** Liste CRUD de tous abonnements

#### 📋 Voir la Liste
- Table avec colonnes: Service, Client, Début, Fin, Prix/Mois, Catégorie
- Tri clickable sur en-têtes
- Couleurs pour prix (vert/rouge gradient)

#### ➕ Ajouter un Abonnement
1. Bouton "Ajouter" (vert, haut table)
2. Formulaire apparaît:
   ```
   - Nom du Service (ex: "Netflix")
   - Client (ex: "Client A")
   - Date Début (picker)
   - Date Fin (picker)
   - Prix Mensuel (€)
   - Catégorie (Software/Cloud/Streaming/Autre)
   ```
3. Cliquez "Enregistrer"
4. ✅ Alerte "Abonnement créé!"
5. Table refresh automatique
6. Stats mise à jour

#### ✏️ Éditer (Future)
- Cliquez icône crayon (⚙️)
- Formulaire s'affiche
- Modifiez champs
- Cliquez "Enregistrer"

#### 🗑️ Supprimer
1. Cliquez icône corbeille (🗑️)
2. Confirmation popup
3. ✅ Alerte "Supprimé!"
4. Table refresh

---

### 📈 Onglet Analytics (3)

**Vue:** 5 analyses majeures en parallèle

#### 1. 📊 Rapport d'Optimisation
- **Affiche:**
  - Total Savings Potentiel (€)
  - Nombre Opportunities détectées
  - Actions recommandées
- **Cas d'usage:** Trouver des économies possibles

#### 2. ⚠️ Anomalies Détectées
- **Affiche:**
  - Nombre total anomalies
  - Liste anomalies top 3
  - Descriptions
- **Cas d'usage:** Détecter prix aberrants, patterns étranges
- **Exemples:** Prix 100x plus cher, peak consumption

#### 3. 👯 Doublons Potentiels
- **Affiche:**
  - Nombre doublons trouvés
  - Services en doublon (paires)
  - Confiance de match
- **Cas d'usage:** Éviter services dupliqués
- **Exemples:** 2 Netflix, 2 Office365

#### 4. 🔮 Prévisions Coûts (6 mois)
- **Affiche:**
  - Coût projeté par mois
  - Coût total 6 mois
  - Tendance (↑/→/↓)
- **Cas d'usage:** Budget forecasting
- **Formule:** Coût actuel × coeff variation

#### 5. 📊 Métriques Avancées
- **Table avec:**
  - Moyenne (€)
  - Médiane (€)
  - Écart-type
  - Coefficient Variation (%)
- **Cas d'usage:** Analyze distribution
- **Expert mode:** Comprendre données

**💡 Tips:**
- Toutes analytics rechargent au refresh tab
- Données basées sur abonnements actuels
- Real-time calculation

---

### 💼 Onglet Portfolio (4)

**Vue:** Optimiseur budgétaire multi-critères

#### 🎯 Objectif
Rééquilibrer vos abonnements selon critères pondérés

#### 📝 Formulaire
```
Budget Cible (€)
  ↳ Montant maximum dépense/mois
  ↳ Exemple: 500

Poids Valeur (0-1)
  ↳ Importance de la valeur service
  ↳ 0.4 = modéré, 1.0 = critique

Poids Risque (0-1)
  ↳ Importance gestion risque
  ↳ 0.3 = faible risque accepté

Poids Confort (0-1)
  ↳ Importance comfort utilisateur
  ↳ 0.3 = confort secondaire
```

#### ▶️ Résultats
Après clic "Optimiser":
```
✅ Score Objectif: 87.5%
   (Satisfaction objectif optimization)

💰 Économies Potentielles: €245.50
   (Montant à économiser)

📋 Recommendations:
   - Consolidate streaming services
   - Premium plan analysis
   - Annual payment options
```

#### 💡 Cas d'usage
- Réduire budget (100€ → 80€)
- Améliorer value (garder budget, meilleur service)
- Optimiser pour balance parfait

---

### 📅 Onglet Lifecycle (5)

**Vue:** Planificateur de cycle de vie

#### 🎯 Objectif
Planifier dépenses abonnements sur durée

#### 📝 Formulaire
```
Nombre de Mois
  ↳ Durée plan (1-24 mois)
  ↳ Exemple: 12 mois

Budget Total (€)
  ↳ Budget mensuel total
  ↳ Exemple: 1200€
```

#### ▶️ Résultats
Après clic "Générer Plan":
```
✅ Coût Total: €1150.00
   (Coût total 12 mois)

📊 Score Global: 82.3%
   (Qualité du plan)

📅 Mois Planifiés: 12
   Détail par mois:
   - Mois 1: Budget 95.83€, Cost 92.50€
   - Mois 2: Budget 95.83€, Cost 95.00€
   - ...
```

#### 💡 Cas d'usage
- Pré-plan budget 1 an
- Forecast cash flow
- Identify budget peaks

---

## ⚙️ Interface Éléments

### Navigation (Navbar)
- **Logo:** Cliquez pour refresh
- **Titre:** "Gestion d'Abonnements"
- **Email:** Affiche current user
- **Logout:** Bouton rouge déconne

### Tabs Navigation
- **Active Tab:** Fond dégradé bleu-violet
- **Inactive Tab:** Gris, hover active
- **Icons:** Font Awesome 6.4

### Boutons
- **Primary (Bleu):** Actions principales
- **Success (Vert):** Add/Create
- **Danger (Rouge):** Delete
- **Secondary (Gris):** Cancel

### Alerts  
- **Success (Vert):** Opération réussie
- **Danger (Rouge):** Erreur
- **Info (Bleu):** Information
- **Auto-close:** 5 secondes

### Cards & Panels
- **Stats Cards:** 4 colonnes auto-responsive
- **Table:** Scrollable, hover effects
- **Forms:** Bootstrap validated

---

## 🎨 Design & UX

### Couleurs
```
Primary:   #667eea (Bleu indigo)
Secondary: #764ba2 (Violet)
Accent:    #f093fb (Rose)
Success:   #4facfe (Bleu ciel)
Danger:    #fa709a (Rose foncé)
```

### Typography
- **Font:** Segoe UI, Tahoma
- **H1-H6:** 700 weight (bold)
- **Body:** 400 weight (normal)
- **Small:** 12px, #999 (gris)

### Responsive
- **Desktop:** Full width
- **Tablet:** Adjusted grid
- **Mobile:** Single column, full width
- **Breakpoint:** 768px (Bootstrap standard)

### Animations
- **Slide Up:** Page entry (0.5s)
- **Fade In:** Content load (0.5s)
- **Slide In:** Alerts (0.3s)
- **Hover:** Cards elevate (-5px Y)

---

## 🔧 Troubleshooting

### ❌ Problem: "Cannot connect to server"
**Solution:**
1. Vérifier serveur lancé: `http://localhost:4567/api/session`
2. Vérifier port 4567 libre: `lsof -i :4567`
3. Relancer serveur: `java -jar gestion-abonnements-*.jar`

### ❌ Problem: "Page blanche après login"
**Solution:**
1. Hard refresh: Ctrl+Shift+R (Windows) ou Cmd+Shift+R (Mac)
2. Clear cache browser
3. Réouvrir tab
4. Relancer navigateur

### ❌ Problem: "401 Non autorisé"
**Solution:**
1. Session expirée (30 min)
2. Logout + Re-login
3. Vérifier email/password

### ❌ Problem: "Table vide, pas d'abonnements"
**Solution:**
1. Normal si nouveau user
2. Cliquez "Ajouter" pour créer first
3. List refresh automatique après création

### ❌ Problem: "Analytics pas chargé"
**Solution:**
1. Attendre 2-3 secondes
2. Cliquez autre tab puis revenir
3. Hard refresh (Ctrl+Shift+R)
4. Check console (F12 → Console)

---

## 📊 Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| `Ctrl+Shift+R` | Hard refresh (clear cache) |
| `F12` | Open DevTools (debug) |
| `Tab` | Navigate form fields |
| `Enter` | Submit form |
| `Esc` | Close dialog/form |

---

## 📱 Mobile Usage

### Responsive Features
- Navbar collapses on mobile
- Tabs scroll horizontally
- Stats cards stack vertically
- Tables scroll horizontally
- Forms full width

### Recommended
- Use portrait orientation
- Landscape for tables
- Use Chrome/Safari latest
- Min screen: 320px

---

## 🆘 Support & Help

### Debug Mode
1. Open DevTools: F12
2. Go to Console tab
3. Watch for errors
4. Copy/paste errors in issue

### Check Status
```bash
# Test backend
curl http://localhost:4567/api/session

# Test frontend load
curl http://localhost:4567/ | head -50
```

### Report Issues
1. Describe scenario
2. Screenshot if visual
3. Browser + OS
4. Error message from console

---

## 📚 Additional Resources

- **Backend Docs:** `/docs/techniques/`
- **API Endpoints:** See S2_PRESENTATION.md
- **Source Code:** `/backend/src/main/java/com/projet/`
- **Tests:** `/tests/`
- **Docker:** `docker-compose.yml`

---

**Happy Using! 🚀**

Last Updated: March 23, 2026
