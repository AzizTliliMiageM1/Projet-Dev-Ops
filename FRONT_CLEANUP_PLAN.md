# 🧹 FRONTEND CLEANUP PLAN

**Date:** February 10, 2026  
**Branche:** feature/frontend-clean  
**Objectif:** Purger complètement le frontend fictif et recréer minimal + fonctionnel

---

## 📋 FICHIERS À SUPPRIMER - LISTE COMPLÈTE

### 🗂️ HTML Pages à Supprimer (25 fichiers)

```
❌ Pages Fictives / Marketing:
  - home.html (page d'accueil marketing)
  - home_old.html (vieux version)
  - index_old.html (ancienne version)
  - index.html.backup
  - index_backup.html
  - status.html (page de statut déco)

❌ Modules Fictifs:
  - bank-integration.html (API bancaire SIMULÉE)
  - chatbot-widget.html (IA fictive)
  - notifications.html (envoi email SIMULATION)
  - themes.html (sélection thèmes DÉCO)
  - analytics.html (doublons avec stats.html)
  - api.html (documentation statique)
  - api-test.html (copie temporaire)
  - dashboard-modern.html (test temporaire)

❌ Gestion Compte Fictive:
  - account.html (profil utilisateur non-connecté)
  - personal-info.html (modif infos SIMULÉE)
  - email-settings.html (configuration email FAKE)
  - password.html (reset password SIMULÉ)
  - upgrade.html (upgrade fictif)

❌ Pages Statiques:
  - contact.html
  - help.html
  - login.html (à refaire si backend auth existe)
  - register.html (à refaire si backend auth existe)
  - confirm.html (confirmation email FAKE)
```

**Total HTML:** 25 fichiers à supprimer

---

### 📜 JavaScript Fictifs à Supprimer (10 fichiers)

```
❌ Modules Fictifs:
  - chatbot.js (IA FICTIVE)
  - chatbot-advanced.js (IA FICTIVE)
  - chatbot-init.js (IA FICTIVE)
  - chatbot-enhanced-init.js (IA FICTIVE)
  - bank-integration.js (API BANCAIRE SIMULÉE)
  - notifications.js (SIMULATION envoi email)
  - themes.js (thèmes déco FAKE)
  - email-settings.js (configuration email SIMULÉE)
  - navbar-auth.js (doublons)

❌ Anciens JS:
  - app-enhanced.js (doublons, à fusionner dans app.js)
```

**Total JS:** 10 fichiers à supprimer

---

### 🎨 CSS à Supprimer / Nettoyer (4 fichiers)

```
❌ CSS Fictifs:
  - chatbot-styles.css (styles IA FAKE)
  - register.css (page fictive)
  - home.css (page marketing)

⚠️ À Simplifier:
  - styles.css (peut avoir du CSS mort)
  - dashboard.css (peut avoir du CSS mort)
```

**Total CSS:** 3 à supprimer, 2 à nettoyer

---

## 📊 Résumé Suppression

- **HTML Pages:** 25
- **JavaScript:** 10
- **CSS:** 3
- **TOTAL:** 38 fichiers à supprimer

---

## ✅ Fichiers à GARDER / Refactoriser

```
✅ HTML CORE:
  - index.html (to refactor as Dashboard)
  - stats.html (keep, clean)
  - export-import.html (keep, simplify)

✅ JavaScript CORE:
  - app.js (to refactor, remove mock data)
  - navbar-standard.js (rename to navbar.js)

✅ CSS CORE:
  - theme-variables.css (keep)
  - dashboard.css (simplify)
  - styles.css (simplify)
```

---

## 🗑️ Commandes de Suppression

### Phase 1 - Suppression HTML
```bash
rm -f src/main/resources/static/{home,home_old,index_old,status}.html
rm -f src/main/resources/static/{index.html.backup,index_backup.html}
rm -f src/main/resources/static/{bank-integration,chatbot-widget,notifications,themes,analytics,api,api-test,dashboard-modern}.html
rm -f src/main/resources/static/{account,personal-info,email-settings,password,upgrade,contact,help,login,register,confirm}.html
```

### Phase 2 - Suppression JavaScript
```bash
rm -f src/main/resources/static/{chatbot,chatbot-advanced,chatbot-init,chatbot-enhanced-init,bank-integration,notifications,themes,email-settings,navbar-auth,app-enhanced}.js
rm -f src/main/resources/static/{chatbot-styles,register,home}.css
```

---

## 📝 Next Steps After Cleanup

1. **Refactor existing files:**
   - Update `index.html` → Dashboard minimal
   - Update `app.js` → Remove all mock data
   - Rename `navbar-standard.js` → `navbar.js`

2. **Create new files in Phase 2:**
   - `subscriptions.html` (CRUD)
   - `api.js` (centralized fetch)
   - `ui.js` (DOM rendering)

3. **Connect to Real Backend:**
   - Test all endpoints on `/api/...`
   - Remove all localStorage "truth"
   - Handle API errors properly

4. **Documentation:**
   - Create `README_FRONTEND.md`
   - Document all endpoints used
   - Add launch instructions

---

## ✨ Quality Checklist

After Phase 1 cleanup:
- ❌ No import errors in console
- ❌ No broken links in remaining pages
- ❌ No dead CSS
- ❌ No mock data left in app.js
- ❌ All navigation links point to real pages

