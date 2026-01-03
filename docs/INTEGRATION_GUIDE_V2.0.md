# 🔧 Guide d'Intégration - Chatbot v2.0 et App Améliorée

## 📋 Table des Matières
1. [Installation Rapide](#installation-rapide)
2. [Migration depuis l'ancienne version](#migration)
3. [Configuration Avancée](#configuration)
4. [Exemples d'Utilisation](#exemples)
5. [Troubleshooting](#troubleshooting)

---

## 🚀 Installation Rapide

### Option 1: Remplacer les anciens fichiers (Recommandé)

```bash
# Sauvegarder les anciens fichiers
cp src/main/resources/static/chatbot.js src/main/resources/static/chatbot.js.backup
cp src/main/resources/static/chatbot-init.js src/main/resources/static/chatbot-init.js.backup
cp src/main/resources/static/app.js src/main/resources/static/app.js.backup

# Renommer les nouveaux fichiers
mv src/main/resources/static/chatbot-advanced.js src/main/resources/static/chatbot.js
mv src/main/resources/static/chatbot-enhanced-init.js src/main/resources/static/chatbot-init.js
mv src/main/resources/static/app-enhanced.js src/main/resources/static/app.js
```

### Option 2: Garder les deux versions en parallèle

```html
<!-- Dans votre HTML, choisissez l'une ou l'autre -->

<!-- Nouvelle version (recommandée) -->
<script src="chatbot-advanced.js"></script>
<script src="chatbot-enhanced-init.js"></script>
<script src="app-enhanced.js"></script>

<!-- OU ancienne version -->
<!-- <script src="chatbot.js"></script> -->
<!-- <script src="chatbot-init.js"></script> -->
<!-- <script src="app.js"></script> -->
```

---

## 🔄 Migration

### Étape 1: Vérifier la Compatibilité

Les nouveaux fichiers sont **100% compatible backward** avec les anciens. Vous pouvez switcher à tout moment.

```javascript
// Vérifier quelle version est active
console.log(window.chatbotEngine.constructor.name);
// Output: "AdvancedAbonnementChatbot" (nouvelle) ou "AbonnementChatbot" (ancienne)
```

### Étape 2: Mettre à Jour le HTML

Si vous avez du HTML personnalisé, vérifiez ces points :

```html
<!-- Les nouveaux fichiers utilisent les mêmes IDs -->
<div id="chatbotTrigger"></div>
<div id="chatbotWindow"></div>
<div id="chatbotMessages"></div>
<div id="chatbotInput"></div>
<div id="chatbotTyping"></div>

<!-- Aucun changement nécessaire! -->
```

### Étape 3: Vérifier la Console

Après chargement, ouvrez la console (F12) :

```javascript
// Vérifier que le chatbot est initialisé
console.log(window.chatbotEngine);
// Output: AdvancedAbonnementChatbot { context: {...}, ... }

// Vérifier le profil utilisateur
console.log(window.chatbotEngine.context.userProfile);
// Output: { userId: "user_...", language: "fr", ... }

// Vérifier l'historique
console.log(window.chatbotEngine.context.conversationHistory);
// Output: Array(n) [ {...}, {...}, ... ]
```

### Étape 4: Tester les Fonctionnalités

```javascript
// Test 1: Générer une réponse simple
const response = await window.chatbotEngine.generateResponse("Bonjour");
console.log(response);
// Output: "Bonjour ! ☀️ Bon après-midi ! ..."

// Test 2: Déterminer l'intention
const intent = window.chatbotEngine.detectIntent("Quel est mon budget ?");
console.log(intent);
// Output: { category: "statistics", score: 3, confidence: 1 }

// Test 3: Extraire des entités
const entities = window.chatbotEngine.extractEntities("Ajoute Netflix pour Jean à 15€");
console.log(entities);
// Output: { service: "Netflix", client: "Jean", price: 15, ... }
```

---

## ⚙️ Configuration Avancée

### 1. Personnaliser le Comportement du Chatbot

```javascript
// Modifier le profil utilisateur
chatbotEngine.context.userProfile.preferences = {
    language: 'en',           // Changer la langue (futur: support multilingue)
    detailLevel: 'detailed',  // 'simple', 'normal', 'detailed'
    showEmojis: false,        // Désactiver les emojis
    notificationFrequency: 'rarely'  // 'rarely', 'balanced', 'frequent'
};

// Sauvegarder
chatbotEngine.saveUserProfile();
```

### 2. Ajouter des Intentions Personnalisées

```javascript
// Ajouter une nouvelle catégorie d'intention
chatbotEngine.knowledgeBase.custom_action = {
    patterns: ['mon action personnalisée', 'custom'],
    responses: [
        async (message) => {
            // Logique personnalisée
            return "Réponse personnalisée à votre action";
        }
    ]
};
```

### 3. Configurer les Tutoriels

```javascript
// Ajouter un tutoriel personnalisé
chatbotEngine.tutorials.custom_tutorial = {
    title: "Mon Tutoriel",
    description: "Description courte",
    steps: [
        {
            title: "Étape 1",
            content: "Contenu de l'étape",
            action: "Continuer"
        },
        // ... plus d'étapes
    ]
};
```

### 4. Configurer l'API Manager

```javascript
// Modifier la configuration API
API_CONFIG.TIMEOUT = 15000;           // 15 secondes
API_CONFIG.RETRY_ATTEMPTS = 5;        // 5 tentatives
API_CONFIG.RETRY_DELAY = 2000;        // 2 secondes entre tentatives

// Ou au niveau global
APIManager.TIMEOUT = 20000;
```

### 5. Ajouter des Filtres Personnalisés

```javascript
// Ajouter un filtre personnalisé
AppState.filters.customFilter = 'value';

// Appliquer les filtres
FilterManager.applyFilters();
```

---

## 💡 Exemples d'Utilisation

### Exemple 1: Afficher les Statistiques Complètes

```javascript
// Obtenir les statistiques
const response = await chatbotEngine.getStatisticsResponse();
console.log(response);

/* Output:
📊 **Vos statistiques :**

💳 **Total abonnements :** 12
✅ **Actifs :** 10
💰 **Coût mensuel :** 127.50€
📈 **Coût annuel :** 1530€
⚠️ **Alertes inactivité :** 2
*/
```

### Exemple 2: Rechercher un Abonnement

```javascript
// Rechercher via le chatbot
const response = await chatbotEngine.searchSubscription("Cherche Netflix");
console.log(response);

// Ou directement avec FilterManager
FilterManager.setFilter('search', 'Netflix');
const results = FilterManager.applyFilters();
console.log(results);
```

### Exemple 3: Ajouter un Abonnement par Langage Naturel

```javascript
// Via le chatbot
const response = await chatbotEngine.handleAddSubscription(
    "Ajoute Spotify pour Marie à 9.99€"
);
console.log(response);

// Ou directement avec l'API
const newSub = {
    nomService: 'Spotify',
    clientName: 'Marie',
    prixMensuel: 9.99,
    dateDebut: new Date().toISOString(),
    dateFin: new Date(Date.now() + 30*24*60*60*1000).toISOString(),
    categorie: 'musique',
    derniereUtilisation: new Date().toISOString()
};
await APIManager.create(newSub);
```

### Exemple 4: Analyser les Préférences Utilisateur

```javascript
// Voir les topics favoris de l'utilisateur
console.log(chatbotEngine.context.userProfile.interactions.favoriteTopics);
// Output: { statistics: 5, alerts: 3, optimization: 2 }

// Voir les sujets récents
console.log(chatbotEngine.context.userProfile.interactions.lastTopics);
// Output: ['statistics', 'alerts', 'optimization']

// Voir les statistiques d'interaction
console.log(chatbotEngine.context.userProfile.stats);
// Output: { 
//   addedSubscriptions: 3,
//   deletedSubscriptions: 1,
//   searchesPerformed: 12,
//   adviceRequested: 5,
//   lastActiveDate: "2026-01-03T..."
// }
```

### Exemple 5: Implémenter le Thème Automatique

```javascript
// Initialiser le thème au chargement
ThemeManager.init();

// Écouter les changements
window.addEventListener('themechange', (event) => {
    if (event.matches) {
        ThemeManager.apply();
    }
});

// Ou toggler manuellement
document.getElementById('toggleTheme').addEventListener('click', () => {
    ThemeManager.toggle();
});
```

### Exemple 6: Historique Persistent

```javascript
// Voir l'historique complet
const history = chatbotEngine.context.conversationHistory;
console.log(history);
/* Output:
[
    { role: 'user', message: '...', timestamp: Date },
    { role: 'bot', message: '...', timestamp: Date },
    ...
]
*/

// Exporter l'historique
const json = JSON.stringify(history, null, 2);
const blob = new Blob([json], { type: 'application/json' });
const url = URL.createObjectURL(blob);
// Télécharger...

// Importer un historique
const imported = JSON.parse(jsonString);
chatbotEngine.context.conversationHistory = imported;
chatbotEngine.saveConversationHistory();
```

### Exemple 7: Batch Operations

```javascript
// Créer plusieurs abonnements
const subs = [
    { nomService: 'Netflix', clientName: 'Jean', prixMensuel: 15.99, ... },
    { nomService: 'Spotify', clientName: 'Marie', prixMensuel: 9.99, ... },
    { nomService: 'Disney+', clientName: 'Paul', prixMensuel: 8.99, ... }
];

for (const sub of subs) {
    try {
        await APIManager.create(sub);
        console.log(`✅ ${sub.nomService} créé`);
    } catch (error) {
        console.error(`❌ Erreur pour ${sub.nomService}:`, error);
    }
}
```

### Exemple 8: Rapport d'Analyse

```javascript
// Générer un rapport complet
async function generateReport() {
    const stats = await chatbotEngine.getStatisticsResponse();
    const alerts = await chatbotEngine.getAlertsResponse();
    const advice = await chatbotEngine.getOptimizationAdvice();
    
    const report = `
# 📊 Rapport d'Analyse
## ${new Date().toLocaleDateString('fr-FR')}

${stats}

---

${alerts}

---

${advice}
    `;
    
    console.log(report);
    return report;
}

// Utiliser
const report = await generateReport();
```

---

## 🆘 Troubleshooting

### Problème: Chatbot ne répond pas

**Solution 1:** Vérifier la console
```javascript
console.log(window.chatbotEngine);
// Si undefined, les scripts ne sont pas chargés
```

**Solution 2:** Vérifier le chargement des scripts
```html
<!-- Vérifier que les scripts sont chargés dans le bon ordre -->
<script src="chatbot-advanced.js"></script>
<script src="chatbot-enhanced-init.js"></script>
<script src="app-enhanced.js"></script>
```

**Solution 3:** Forcer l'initialisation manuelle
```javascript
// Dans la console
if (!window.chatbotEngine) {
    window.chatbotEngine = new AdvancedAbonnementChatbot();
}
```

### Problème: LocalStorage plein

**Solution:**
```javascript
// Nettoyer l'historique ancien
localStorage.removeItem('chatbot_history');

// Ou limiter à 30 messages
const history = JSON.parse(localStorage.getItem('chatbot_history') || '[]');
const recent = history.slice(-30);
localStorage.setItem('chatbot_history', JSON.stringify(recent));
```

### Problème: API timeout

**Solution 1:** Augmenter le timeout
```javascript
API_CONFIG.TIMEOUT = 20000; // 20 secondes
```

**Solution 2:** Vérifier la connexion serveur
```javascript
fetch('/api/abonnements')
    .then(r => console.log('Serveur OK:', r.status))
    .catch(e => console.error('Serveur inaccessible:', e));
```

### Problème: Thème ne se sauvegarde pas

**Solution:**
```javascript
// Vérifier que localStorage fonctionne
try {
    localStorage.setItem('test', 'test');
    localStorage.removeItem('test');
    console.log('LocalStorage OK');
} catch (e) {
    console.error('LocalStorage non disponible:', e);
}
```

### Problème: Profil utilisateur perdu après rechargement

**Solution 1:** Vérifier la sauvegarde
```javascript
// Forcer la sauvegarde
chatbotEngine.saveUserProfile();

// Vérifier
console.log(localStorage.getItem('chatbot_profile'));
```

**Solution 2:** Réinitialiser le profil
```javascript
// Supprimer et recharger
localStorage.removeItem('chatbot_profile');
location.reload();
```

### Problème: Performance lente

**Solution 1:** Vérifier l'historique trop volumineux
```javascript
// Nettoyer l'historique
localStorage.removeItem('chatbot_history');
```

**Solution 2:** Réduire la fréquence de rafraîchissement
```javascript
// Désactiver le refresh auto (toutes les 5 minutes)
// et le faire manuellement au besoin
clearInterval(autoRefreshId);

// Ou augmenter l'intervalle
setInterval(loadSubscriptions, 15 * 60 * 1000); // 15 minutes
```

---

## 📚 Resources Supplémentaires

- [Documentation Complète](./IMPROVEMENTS_V2.0.md)
- [API Reference](#api-reference)
- [Architecture Design](#architecture)
- [Performance Tips](#performance)

---

**Version:** 2.0 | **Dernière mise à jour:** Jan 2026
