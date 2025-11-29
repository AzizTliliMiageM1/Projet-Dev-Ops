# 📱 Plan Application Mobile - Gestion des Abonnements

## 🎯 Objectif

Créer une application mobile pour gérer les abonnements, synchronisée avec l'API REST existante.

---

## 🛠️ Technologies Recommandées

### Option 1 : React Native (Recommandée)
**Avantages :**
- ✅ Code partagé iOS + Android
- ✅ Utilise React (cohérence avec le web si migration)
- ✅ Grande communauté
- ✅ Performance native

**Stack technique :**
```
- React Native
- React Navigation (navigation)
- Axios (appels API)
- AsyncStorage (stockage local)
- React Native Chart Kit (graphiques)
```

### Option 2 : Flutter
**Avantages :**
- ✅ Très performant
- ✅ UI moderne et fluide
- ✅ Un seul codebase pour iOS + Android

**Stack technique :**
```
- Flutter / Dart
- Provider (state management)
- http package (API)
- shared_preferences (stockage)
- fl_chart (graphiques)
```

### Option 3 : PWA (Progressive Web App)
**Avantages :**
- ✅ Pas de store (app installable depuis le navigateur)
- ✅ Code web existant réutilisable
- ✅ Maintenance simplifiée
- ✅ Notifications push possibles

**Stack technique :**
```
- Service Worker
- Web App Manifest
- Cache API
- IndexedDB (stockage offline)
```

---

## 📐 Architecture Mobile

```
┌─────────────────────────────────────┐
│     APPLICATION MOBILE              │
├─────────────────────────────────────┤
│                                     │
│  ┌───────────────────────────────┐ │
│  │   ÉCRANS / VUES               │ │
│  │  - Liste abonnements          │ │
│  │  - Détails abonnement         │ │
│  │  - Ajouter/Modifier           │ │
│  │  - Statistiques               │ │
│  │  - Profil utilisateur         │ │
│  └───────────────────────────────┘ │
│                ↓                    │
│  ┌───────────────────────────────┐ │
│  │   SERVICES / LOGIQUE          │ │
│  │  - API Service                │ │
│  │  - Auth Service               │ │
│  │  - Storage Service            │ │
│  │  - Notification Service       │ │
│  └───────────────────────────────┘ │
│                ↓                    │
│  ┌───────────────────────────────┐ │
│  │   STOCKAGE LOCAL              │ │
│  │  - Cache abonnements          │ │
│  │  - Token authentification     │ │
│  │  - Préférences utilisateur    │ │
│  └───────────────────────────────┘ │
│                                     │
└──────────────┬──────────────────────┘
               │ HTTP/REST
               ↓
┌─────────────────────────────────────┐
│     API REST (Existante)            │
│     http://localhost:4567/api       │
└─────────────────────────────────────┘
```

---

## 📱 Écrans Principaux

### 1. Écran de Connexion / Inscription
```
┌─────────────────────┐
│  🔐 Connexion       │
│                     │
│  Email: ___________ │
│  Mot de passe: ____ │
│                     │
│  [Se connecter]     │
│  [Créer un compte]  │
└─────────────────────┘
```

### 2. Liste des Abonnements
```
┌─────────────────────────────┐
│ 📊 Mes Abonnements          │
│ Total: 249.99€/mois     [+] │
├─────────────────────────────┤
│ 🎬 Netflix                  │
│ 13.99€/mois  Expire: 12 j   │
├─────────────────────────────┤
│ 🎵 Spotify Premium          │
│ 9.99€/mois   Actif          │
├─────────────────────────────┤
│ 💪 BasicFit                 │
│ 19.99€/mois  ⚠️ Inutilisé   │
└─────────────────────────────┘
```

### 3. Détails Abonnement
```
┌─────────────────────────────┐
│ ← Netflix              [✏️] │
├─────────────────────────────┤
│ 💰 13.99€ / mois            │
│ 📅 Prochaine échéance:      │
│    15 décembre              │
│                             │
│ 📊 Statistiques:            │
│ • Coût annuel: 167.88€      │
│ • Membre depuis: 2 ans      │
│ • Dernière utilisation:     │
│   Il y a 2 jours            │
│                             │
│ 🏷️ Tags: Streaming, Famille│
│ 🎯 Priorité: Essentiel      │
│                             │
│ [Enregistrer utilisation]   │
│ [Supprimer]                 │
└─────────────────────────────┘
```

### 4. Ajouter/Modifier Abonnement
```
┌─────────────────────────────┐
│ ← Nouvel Abonnement         │
├─────────────────────────────┤
│ Service: ________________   │
│                             │
│ Prix: ________ € / [Mois ▼] │
│                             │
│ Catégorie: [Streaming   ▼]  │
│                             │
│ Date début: [📅 Choisir]    │
│ Date fin: [📅 Choisir]      │
│                             │
│ Priorité: [Important    ▼]  │
│                             │
│ ☐ Partagé                   │
│ Nombre d'utilisateurs: [1]  │
│                             │
│ Tags: #Famille #Essentiel   │
│                             │
│ Notes: ___________________  │
│        ___________________  │
│                             │
│ [Annuler]      [Enregistrer]│
└─────────────────────────────┘
```

### 5. Statistiques / Dashboard
```
┌─────────────────────────────┐
│ 📊 Statistiques             │
├─────────────────────────────┤
│ 💰 Total mensuel            │
│    249.99 €                 │
│                             │
│ 📈 Évolution (6 mois)       │
│    ┌──┐                     │
│    │  │  ┌──┐              │
│  ┌─┤  ├──┤  │              │
│  │ └──┘  └──┘              │
│  Jan Feb Mar Apr May Jun    │
│                             │
│ 🏆 Top Dépenses             │
│  1. Salle sport  19.99€     │
│  2. Netflix      13.99€     │
│  3. Spotify       9.99€     │
│                             │
│ ⚠️ Alertes (2)              │
│  • BasicFit non utilisé     │
│  • Netflix expire bientôt   │
└─────────────────────────────┘
```

### 6. Profil Utilisateur
```
┌─────────────────────────────┐
│ 👤 Profil                   │
├─────────────────────────────┤
│ Email: user@example.com     │
│                             │
│ 📊 Statistiques perso       │
│ • Abonnements: 12           │
│ • Dépense totale: 2999.88€  │
│ • Économies: 120€           │
│                             │
│ ⚙️ Paramètres               │
│ • Notifications       [ON]  │
│ • Rappels            [ON]  │
│ • Thème              [Auto] │
│                             │
│ 📤 Export                   │
│ • Exporter en JSON          │
│ • Exporter en CSV           │
│                             │
│ [Déconnexion]               │
└─────────────────────────────┘
```

---

## 🔧 Fonctionnalités Mobiles Spécifiques

### 1. Notifications Push
- ⏰ Rappels avant expiration
- 💰 Alerte dépassement budget
- 📱 Nouvelle facture détectée
- ⚠️ Abonnement inutilisé

### 2. Widget Home Screen (iOS/Android)
```
┌─────────────────┐
│ Abonnements     │
│ 249.99€/mois    │
│                 │
│ ⚠️ 2 alertes    │
└─────────────────┘
```

### 3. Scan de Factures (OCR)
- 📸 Scanner une facture
- 🤖 Détection automatique : service, montant, date
- ✅ Création automatique d'abonnement

### 4. Mode Hors Ligne
- 💾 Cache local des données
- 📱 Consultation hors connexion
- 🔄 Synchronisation automatique

### 5. Touch ID / Face ID
- 🔐 Connexion biométrique
- 🛡️ Sécurité renforcée

### 6. Partage
- 📤 Partager un abonnement
- 👥 Gérer les partages famille

---

## 🎨 Design Mobile

### Palette de Couleurs
```css
/* Mode Clair */
--primary: #6366f1        /* Indigo */
--secondary: #8b5cf6      /* Violet */
--success: #10b981        /* Vert */
--warning: #f59e0b        /* Orange */
--danger: #ef4444         /* Rouge */
--background: #ffffff     /* Blanc */
--text: #1f2937          /* Gris foncé */

/* Mode Sombre */
--primary: #818cf8
--secondary: #a78bfa
--background: #1f2937
--text: #f9fafb
```

### Composants UI
- **Cards** : Affichage abonnements
- **Bottom Navigation** : Navigation principale
- **Floating Action Button** : Ajouter abonnement
- **Swipe Actions** : Modifier/Supprimer
- **Pull to Refresh** : Actualisation
- **Modal Bottom Sheet** : Filtres, options

---

## 🔌 Intégration API

### Service API (React Native)

```javascript
// services/api.js
import axios from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';

const API_URL = 'http://localhost:4567/api';

class ApiService {
  constructor() {
    this.client = axios.create({
      baseURL: API_URL,
      headers: {
        'Content-Type': 'application/json'
      }
    });

    // Intercepteur pour ajouter le token
    this.client.interceptors.request.use(async (config) => {
      const token = await AsyncStorage.getItem('authToken');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    });
  }

  // Authentification
  async login(email, password) {
    const response = await this.client.post('/login', { email, password });
    await AsyncStorage.setItem('authToken', response.data.token);
    return response.data;
  }

  async register(email, password, pseudo) {
    return await this.client.post('/register', { email, password, pseudo });
  }

  async logout() {
    await AsyncStorage.removeItem('authToken');
  }

  // Abonnements
  async getAbonnements() {
    const response = await this.client.get('/abonnements');
    return response.data;
  }

  async getAbonnement(id) {
    const response = await this.client.get(`/abonnements/${id}`);
    return response.data;
  }

  async createAbonnement(data) {
    const response = await this.client.post('/abonnements', data);
    return response.data;
  }

  async updateAbonnement(id, data) {
    const response = await this.client.put(`/abonnements/${id}`, data);
    return response.data;
  }

  async deleteAbonnement(id) {
    await this.client.delete(`/abonnements/${id}`);
  }

  // Statistiques
  async getStats() {
    const response = await this.client.get('/analytics/stats');
    return response.data;
  }

  async getDepensesMensuelles() {
    const response = await this.client.get('/analytics/depenses-mensuelles');
    return response.data;
  }
}

export default new ApiService();
```

### Gestion du State (React Native + Context)

```javascript
// context/AbonnementsContext.js
import React, { createContext, useState, useEffect } from 'react';
import ApiService from '../services/api';

export const AbonnementsContext = createContext();

export const AbonnementsProvider = ({ children }) => {
  const [abonnements, setAbonnements] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const loadAbonnements = async () => {
    setLoading(true);
    try {
      const data = await ApiService.getAbonnements();
      setAbonnements(data);
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const addAbonnement = async (abonnement) => {
    try {
      const newAbo = await ApiService.createAbonnement(abonnement);
      setAbonnements([...abonnements, newAbo]);
    } catch (err) {
      setError(err.message);
      throw err;
    }
  };

  const updateAbonnement = async (id, data) => {
    try {
      const updated = await ApiService.updateAbonnement(id, data);
      setAbonnements(abonnements.map(a => a.id === id ? updated : a));
    } catch (err) {
      setError(err.message);
      throw err;
    }
  };

  const deleteAbonnement = async (id) => {
    try {
      await ApiService.deleteAbonnement(id);
      setAbonnements(abonnements.filter(a => a.id !== id));
    } catch (err) {
      setError(err.message);
      throw err;
    }
  };

  useEffect(() => {
    loadAbonnements();
  }, []);

  return (
    <AbonnementsContext.Provider
      value={{
        abonnements,
        loading,
        error,
        loadAbonnements,
        addAbonnement,
        updateAbonnement,
        deleteAbonnement
      }}
    >
      {children}
    </AbonnementsContext.Provider>
  );
};
```

---

## 📦 Structure du Projet React Native

```
mobile-app/
├── App.js
├── package.json
├── android/
├── ios/
└── src/
    ├── screens/
    │   ├── LoginScreen.js
    │   ├── HomeScreen.js
    │   ├── AbonnementDetailsScreen.js
    │   ├── AddAbonnementScreen.js
    │   ├── StatsScreen.js
    │   └── ProfileScreen.js
    ├── components/
    │   ├── AbonnementCard.js
    │   ├── StatCard.js
    │   ├── ChartComponent.js
    │   └── FilterModal.js
    ├── navigation/
    │   └── AppNavigator.js
    ├── services/
    │   ├── api.js
    │   ├── storage.js
    │   └── notifications.js
    ├── context/
    │   ├── AbonnementsContext.js
    │   └── AuthContext.js
    ├── utils/
    │   ├── formatters.js
    │   └── validators.js
    └── assets/
        ├── icons/
        └── images/
```

---

## 🚀 Exemple de Composant

### AbonnementCard.js

```javascript
import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { Swipeable } from 'react-native-gesture-handler';
import Icon from 'react-native-vector-icons/MaterialIcons';

const AbonnementCard = ({ abonnement, onPress, onDelete }) => {
  const renderRightActions = () => (
    <TouchableOpacity 
      style={styles.deleteButton}
      onPress={() => onDelete(abonnement.id)}
    >
      <Icon name="delete" size={24} color="#fff" />
    </TouchableOpacity>
  );

  const getPriorityColor = (priorite) => {
    switch(priorite) {
      case 'Essentiel': return '#ef4444';
      case 'Important': return '#f59e0b';
      case 'Optionnel': return '#eab308';
      case 'Luxe': return '#10b981';
      default: return '#6b7280';
    }
  };

  return (
    <Swipeable renderRightActions={renderRightActions}>
      <TouchableOpacity 
        style={styles.card}
        onPress={() => onPress(abonnement)}
      >
        <View style={styles.header}>
          <Text style={styles.service}>{abonnement.nomService}</Text>
          <View 
            style={[
              styles.priorityBadge, 
              { backgroundColor: getPriorityColor(abonnement.priorite) }
            ]}
          >
            <Text style={styles.priorityText}>{abonnement.priorite}</Text>
          </View>
        </View>

        <View style={styles.body}>
          <Text style={styles.price}>
            {abonnement.prixMensuel.toFixed(2)}€ / {abonnement.frequencePaiement}
          </Text>
          
          {abonnement.partage && (
            <Text style={styles.shared}>
              👥 {abonnement.nombreUtilisateurs} utilisateurs
            </Text>
          )}
        </View>

        <View style={styles.footer}>
          <Text style={styles.category}>📁 {abonnement.categorie}</Text>
          {abonnement.doitEnvoyerRappel() && (
            <Text style={styles.alert}>⚠️ Expire bientôt</Text>
          )}
        </View>
      </TouchableOpacity>
    </Swipeable>
  );
};

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  service: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#1f2937',
  },
  priorityBadge: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
  },
  priorityText: {
    color: '#fff',
    fontSize: 12,
    fontWeight: '600',
  },
  body: {
    marginBottom: 8,
  },
  price: {
    fontSize: 16,
    color: '#6366f1',
    fontWeight: '600',
  },
  shared: {
    fontSize: 14,
    color: '#6b7280',
    marginTop: 4,
  },
  footer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  category: {
    fontSize: 14,
    color: '#6b7280',
  },
  alert: {
    fontSize: 12,
    color: '#ef4444',
    fontWeight: '600',
  },
  deleteButton: {
    backgroundColor: '#ef4444',
    justifyContent: 'center',
    alignItems: 'center',
    width: 80,
    borderRadius: 12,
    marginBottom: 12,
  },
});

export default AbonnementCard;
```

---

## 🎯 Plan de Développement

### Phase 1 : Setup & Authentification (1 semaine)
- ✅ Initialiser projet React Native
- ✅ Configurer navigation
- ✅ Écrans login/register
- ✅ Service API de base
- ✅ Gestion authentification

### Phase 2 : CRUD Abonnements (1 semaine)
- ✅ Liste des abonnements
- ✅ Détails abonnement
- ✅ Ajouter/Modifier
- ✅ Supprimer
- ✅ Filtres et recherche

### Phase 3 : Dashboard & Stats (1 semaine)
- ✅ Écran statistiques
- ✅ Graphiques dépenses
- ✅ Top abonnements
- ✅ Alertes et rappels

### Phase 4 : Fonctionnalités Avancées (1 semaine)
- ✅ Notifications push
- ✅ Mode hors ligne
- ✅ Export données
- ✅ Scan factures (OCR)

### Phase 5 : Polish & Tests (1 semaine)
- ✅ Design responsive
- ✅ Animations
- ✅ Tests unitaires
- ✅ Tests E2E
- ✅ Optimisations performances

---

## 📋 Checklist Développement

### Configuration
- [ ] Installer React Native CLI
- [ ] Créer projet : `npx react-native init GestionAbonnements`
- [ ] Installer dépendances :
  ```bash
  npm install @react-navigation/native @react-navigation/bottom-tabs
  npm install axios react-native-chart-kit
  npm install @react-native-async-storage/async-storage
  npm install react-native-gesture-handler
  npm install react-native-vector-icons
  ```

### Développement
- [ ] Configurer ESLint + Prettier
- [ ] Créer structure dossiers
- [ ] Implémenter service API
- [ ] Créer contextes (Auth, Abonnements)
- [ ] Développer composants réutilisables
- [ ] Créer écrans principaux
- [ ] Implémenter navigation
- [ ] Ajouter notifications
- [ ] Gérer mode hors ligne

### Tests
- [ ] Tests unitaires (Jest)
- [ ] Tests composants (React Testing Library)
- [ ] Tests E2E (Detox)
- [ ] Tests performances

### Déploiement
- [ ] Configurer builds Android
- [ ] Configurer builds iOS
- [ ] Créer icônes app (1024x1024)
- [ ] Créer screenshots stores
- [ ] Publier sur Google Play
- [ ] Publier sur App Store

---

## 💡 Recommandations

### Pour Commencer
1. **PWA d'abord** : Plus simple, réutilise le code web
2. **Puis React Native** : Si besoin de fonctionnalités natives

### Bonnes Pratiques
- ✅ Gérer les erreurs réseau
- ✅ Implémenter retry automatique
- ✅ Cache intelligent
- ✅ Optimiser les images
- ✅ Lazy loading
- ✅ Animations fluides (60 FPS)

### Sécurité
- 🔐 Stocker tokens de manière sécurisée
- 🔐 HTTPS obligatoire
- 🔐 Validation côté client + serveur
- 🔐 Timeout sessions
- 🔐 Biométrie pour données sensibles

---

## 🎓 Ressources

### Documentation
- [React Native](https://reactnative.dev/)
- [React Navigation](https://reactnavigation.org/)
- [Flutter](https://flutter.dev/)

### Tutoriels
- [React Native Full Course](https://www.youtube.com/watch?v=0-S5a0eXPoc)
- [Building REST API Apps](https://www.youtube.com/watch?v=qJSHyLB2K48)

### Outils
- [Expo](https://expo.dev/) - Développement React Native simplifié
- [CodePush](https://microsoft.github.io/code-push/) - Updates OTA
- [Fastlane](https://fastlane.tools/) - Automatisation déploiement

---

**L'application mobile sera un excellent complément à votre système de gestion d'abonnements !** 📱✨
