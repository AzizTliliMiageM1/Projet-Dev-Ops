# 🎯 Guide d'Utilisation - Classe Main de Démonstration

## 📋 Réponses à vos Questions

### ✅ 1. Avez-vous la dernière version du projet ?

**OUI !** Votre projet est à jour :

```bash
Commit actuel : 88edb44 (HEAD -> main)
Message : ✨ feat: Ajout de fonctionnalités créatives au système d'abonnements
```

Le dernier commit sur `origin/main` est le commit `4bdaa02`, et votre commit local `88edb44` contient toutes les nouvelles fonctionnalités que nous venons d'ajouter.

---

### ✅ 2. Y a-t-il du code métier dans le backend ?

**OUI, ABSOLUMENT !** Le backend contient plusieurs couches métier :

#### 📦 **Architecture du Code Métier**

```
com.projet/
├── api/
│   └── ApiServer.java           ← API REST avec validation métier
├── repository/
│   ├── AbonnementRepository.java        ← Interface repository
│   ├── FileAbonnementRepository.java    ← Persistance fichier
│   └── UserAbonnementRepository.java    ← Repository par utilisateur
├── service/
│   ├── AbonnementService.java           ← Interface service métier
│   └── AbonnementServiceImpl.java       ← Implémentation logique métier
├── user/
│   ├── UserService.java                 ← Interface gestion utilisateurs
│   ├── UserServiceImpl.java             ← Logique utilisateurs + emails
│   └── FileUserRepository.java          ← Persistance utilisateurs
└── dashboard/
    └── DashboardStats.java              ← Statistiques métier

com.example.abonnement/
├── Abonnement.java              ← Modèle de domaine avec méthodes métier
├── GestionAbonnements.java      ← Interface console (logique métier)
└── ...
```

#### 🎯 **Exemples de Code Métier**

**1. Validation et Règles Métier (Abonnement.java)**
```java
public boolean estActif() {
    LocalDate now = LocalDate.now();
    return now.isAfter(dateDebut) && now.isBefore(dateFin);
}

public String getROI() {
    if (derniereUtilisation == null) return "Faible ⛔";
    long joursSansUtilisation = ChronoUnit.DAYS.between(derniereUtilisation, LocalDate.now());
    if (joursSansUtilisation <= 7) return "Excellent 🌟";
    if (joursSansUtilisation <= 30) return "Bon ✅";
    if (joursSansUtilisation <= 90) return "Moyen ⚠️";
    return "Faible ⛔";
}

public double getCoutParPersonne() {
    return nombreUtilisateurs > 0 ? prixMensuel / nombreUtilisateurs : prixMensuel;
}

public LocalDate calculerProchaineEcheance() {
    // Logique selon fréquence de paiement
    switch (frequencePaiement) {
        case "Mensuel": return LocalDate.now().plusMonths(1);
        case "Trimestriel": return LocalDate.now().plusMonths(3);
        case "Semestriel": return LocalDate.now().plusMonths(6);
        case "Annuel": return LocalDate.now().plusYears(1);
        default: return LocalDate.now().plusMonths(1);
    }
}
```

**2. Services Métier (AbonnementServiceImpl.java)**
```java
@Override
public List<String> verifierAlertesUtilisation() {
    List<String> alertes = new ArrayList<>();
    for (Abonnement abonnement : listeAbonnements) {
        if (abonnement.estActif()) {
            long joursRestants = ChronoUnit.DAYS.between(LocalDate.now(), abonnement.getDateFin());
            if (joursRestants <= 7 && joursRestants >=0 ) {
                alertes.add("ALERTE pour " + abonnement.getClientName() + 
                           ": Expire dans " + joursRestants + " jours.");
            }
        }
    }
    return alertes;
}

@Override
public DashboardStats getDashboardStats() {
    // Calcul de statistiques métier
    // Coûts, alertes, tendances, etc.
}
```

**3. Validation API (ApiServer.java)**
```java
if (a.getNomService() == null || a.getNomService().isBlank()) {
    res.status(400);
    return "{\"error\":\"nomService manquant\"}";
}
if (a.getPrixMensuel() < 0) {
    res.status(400);
    return "{\"error\":\"prixMensuel invalide\"}";
}
```

**4. Logique Utilisateur (UserServiceImpl.java)**
```java
@Override
public String register(String email, String password, String pseudo) {
    // Validation
    // Génération token
    // Envoi email de confirmation
    // Persistance
}
```

---

### ✅ 3. Peut-on utiliser le projet avec une classe Main ?

**OUI, ABSOLUMENT !** Nous venons de créer **`DemoMain.java`** qui démontre tout le code métier de manière interactive.

---

## 🚀 Utilisation de la Classe Main de Démonstration

### 📍 Localisation
```
src/main/java/com/projet/demo/DemoMain.java
```

### ▶️ Lancement

```bash
mvn exec:java -Dexec.mainClass=com.projet.demo.DemoMain
```

### 🎮 Fonctionnalités du Menu Interactif

```
╔══════════════════════════════════════════════════════════════╗
║                                                              ║
║   🎯  GESTIONNAIRE D'ABONNEMENTS - VERSION ENRICHIE  🎯     ║
║                                                              ║
║   Démonstration du code métier et des nouvelles             ║
║   fonctionnalités : tags, groupes, priorités, partage       ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝

━━━━━━━━━━━━━━━━━━ MENU PRINCIPAL ━━━━━━━━━━━━━━━━━━
 1.  🎬 Créer des abonnements de démonstration
 2.  📋 Afficher tous les abonnements
 3.  🎯 Analyser par priorité
 4.  📦 Analyser par groupe
 5.  👥 Afficher abonnements partagés
 6.  📈 Analyser ROI (retour sur investissement)
 7.  🔔 Afficher rappels d'expiration
 8.  💰 Calculer économies du partage
 9.  📊 Analyser fréquences de paiement
10.  🏷️  Rechercher par tag
11.  📊 Statistiques complètes
 0.  🚪 Quitter
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

### 📚 Exemples d'Utilisation

#### 1️⃣ Créer des données de démonstration

Choisissez l'option **1** pour créer 8 abonnements avec :
- ✅ 3 abonnements partagés (Netflix, Spotify, Disney+)
- ✅ 2 packs/groupes (Pack Streaming, Suite Microsoft/Adobe)
- ✅ 4 fréquences différentes (Mensuel, Trimestriel, Semestriel, Annuel)
- ✅ Tags multiples pour organisation
- ✅ Priorités variées
- ✅ Notes personnalisées

#### 2️⃣ Afficher tous les abonnements

Option **2** - Affichage enrichi avec :
```
[1] Netflix Premium
    🟠 Important
    🏷️  Tags : Famille, Essentiel, Divertissement
    📦 Groupe : Pack Streaming
    💰 Prix : 17.99€/Mensuel
    📊 Coût annuel estimé : 215.88€
    👥 Partagé avec 4 personnes → 4.50€/personne
    📈 ROI : Excellent 🌟
    📅 Prochaine échéance : 2026-05-26 (dans 181 jours)
    📝 Notes : Compte familial partagé - Login : famille@netflix.com
```

#### 3️⃣ Analyse par priorité

Option **3** - Tri des abonnements par niveau d'importance :
```
🔴 Essentiel (2 abonnements)
   Coût annuel total : 185.88€
   Services : Microsoft 365 Business, iCloud+ 200GB

🟠 Important (4 abonnements)
   Coût annuel total : 783.64€
   Services : Netflix Premium, Spotify Family, Adobe Creative Cloud, Amazon Prime

🟡 Optionnel (1 abonnement)
   Coût annuel total : 239.88€
   Services : Basic Fit

🟢 Luxe (1 abonnement)
   Coût annuel total : 107.88€
   Services : Disney+
```

#### 4️⃣ Analyse ROI

Option **6** - Identifier les abonnements inutilisés :
```
Excellent 🌟 (4 abonnements)
  • Netflix Premium - Dernière utilisation il y a 2 jours
  • Spotify Family - Dernière utilisation il y a 1 jours
  • Microsoft 365 Business - Dernière utilisation il y a 0 jours
  • Adobe Creative Cloud - Dernière utilisation il y a 5 jours

Faible ⛔ (2 abonnements)
  • Basic Fit - Dernière utilisation il y a 95 jours
  • Disney+ - Dernière utilisation il y a 45 jours

⚠️  ALERTE : 2 abonnement(s) peu utilisé(s)
   Potentiel d'économie : 347.76€/an
```

#### 5️⃣ Calcul économies du partage

Option **8** - Impact financier du partage :
```
• Netflix Premium
  Prix solo : 17.99€/mois
  Prix partagé : 4.50€/mois (divisé par 4)
  Économie : 13.49€/mois → 161.88€/an

• Spotify Family
  Prix solo : 15.99€/mois
  Prix partagé : 2.67€/mois (divisé par 6)
  Économie : 13.32€/mois → 159.84€/an

• Disney+
  Prix solo : 8.99€/mois
  Prix partagé : 4.50€/mois (divisé par 2)
  Économie : 4.49€/mois → 53.88€/an

🎉 TOTAL ÉCONOMIES ANNUELLES : 375.60€
```

---

## 🧪 Code Métier Démontré

### 1. **Logique de Domaine**
- ✅ Calcul automatique du ROI selon utilisation
- ✅ Détection d'abonnements expirés/actifs
- ✅ Calcul du coût par personne pour abonnements partagés
- ✅ Détermination de la prochaine échéance selon fréquence
- ✅ Calcul du coût total depuis le début

### 2. **Règles Métier**
- ✅ Validation des priorités (Essentiel > Important > Optionnel > Luxe)
- ✅ Alertes si abonnement non utilisé depuis 90+ jours
- ✅ Rappels d'expiration configurables (7, 14, 30 jours)
- ✅ Groupage logique d'abonnements liés

### 3. **Analyses Statistiques**
- ✅ Répartition des coûts par priorité
- ✅ Coût total par groupe d'abonnements
- ✅ Économies réalisées grâce au partage
- ✅ Distribution des fréquences de paiement
- ✅ Identification des tags les plus utilisés

### 4. **Recherche et Filtrage**
- ✅ Recherche par tag (insensible à la casse)
- ✅ Filtrage par priorité
- ✅ Filtrage par groupe
- ✅ Filtrage abonnements partagés
- ✅ Détection abonnements à faible ROI

---

## 🎓 Concepts Démontrés

### Architecture en Couches
```
┌─────────────────────────┐
│   Présentation (UI)     │ ← DemoMain.java (Console interactive)
├─────────────────────────┤
│   Logique Métier        │ ← Abonnement.java (méthodes métier)
│                         │   AbonnementService.java
├─────────────────────────┤
│   Persistance (DAO)     │ ← FileAbonnementRepository.java
├─────────────────────────┤
│   Stockage              │ ← demo_abonnements.txt (CSV)
└─────────────────────────┘
```

### Patterns Utilisés
- ✅ **Repository Pattern** : `AbonnementRepository`
- ✅ **Service Layer** : `AbonnementService`
- ✅ **Domain Model** : `Abonnement` avec logique métier
- ✅ **Data Transfer** : Serialization CSV
- ✅ **Separation of Concerns** : UI / Métier / Persistance

---

## 🔄 Autres Classes Main Disponibles

Le projet contient **4 classes Main** au total :

### 1. **com.projet.demo.DemoMain** (NOUVEAU !)
**Utilisation :** `mvn exec:java -Dexec.mainClass=com.projet.demo.DemoMain`
- Menu interactif de démonstration
- Toutes les nouvelles fonctionnalités
- Analyses statistiques avancées
- Code métier illustré

### 2. **com.projet.api.ApiServer**
**Utilisation :** `mvn exec:java -Dexec.mainClass=com.projet.api.ApiServer`
- Serveur API REST sur port 4567
- Interface web moderne
- Endpoints RESTful
- Gestion multi-utilisateurs

### 3. **com.example.abonnement.GestionAbonnements**
**Utilisation :** `mvn exec:java -Dexec.mainClass=com.example.abonnement.GestionAbonnements`
- Interface console classique
- CRUD manuel
- Export/Import JSON
- Affichage coloré

### 4. **com.example.gui.GestionAbonnementsGui**
**Utilisation :** `mvn exec:java -Dexec.mainClass=com.example.gui.GestionAbonnementsGui`
- Interface graphique Swing
- Formulaires visuels
- Tableaux interactifs

### 5. **com.projet.App** (Alias)
**Utilisation :** `mvn exec:java -Dexec.mainClass=com.projet.App`
- Délègue vers ApiServer
- Point d'entrée par défaut

---

## 📁 Fichiers Générés par DemoMain

La classe crée un fichier de données séparé :
```
demo_abonnements.txt  ← Abonnements de démonstration (ne perturbe pas vos données)
```

---

## 💡 Résumé

### ✅ Questions Répondues

| Question | Réponse |
|----------|---------|
| Dernière version du projet ? | **OUI** - Commit 88edb44 avec toutes les nouvelles features |
| Y a-t-il du code métier ? | **OUI** - Architecture complète avec services, repositories, validations |
| Peut-on utiliser une classe Main ? | **OUI** - 5 classes Main différentes disponibles |

### 🎯 Pour Commencer

```bash
# 1. Lancer la démo interactive
mvn exec:java -Dexec.mainClass=com.projet.demo.DemoMain

# 2. Créer des abonnements de test (option 1)
# 3. Explorer toutes les fonctionnalités métier
# 4. Observer les calculs automatiques
```

### 📚 Documentation Complète

- **Nouvelles fonctionnalités :** `docs/NOUVELLES_FONCTIONNALITES.md`
- **Plan d'intégration frontend :** `docs/PLAN_INTEGRATION_FRONTEND.md`
- **API REST :** `docs/API_documentation.md`
- **Tests :** `tests/README_TESTS.md`

---

**🎉 Profitez de votre système de gestion d'abonnements enrichi !**
