# Tutoriels Vidéo - Gestion d'Abonnements

Ce dossier contient les tutoriels vidéo interactifs pour l'application de gestion d'abonnements.

## 📹 Tutoriels Disponibles

### 1. Premiers pas avec l'application (5 minutes)
**Fichier :** `tutorial-beginner.html`  
**Niveau :** Débutant  
**Public :** Tous les utilisateurs

**Contenu :**
- Introduction à l'interface principale
- Découverte des 4 KPI (Total, Actifs, Coût mensuel, Alertes)
- Ajouter votre premier abonnement
- Comprendre les cartes d'abonnements
- Navigation dans l'application
- Aperçu de la page Analytics

**Durée totale :** ~5 minutes  
**Chapitres :**
1. 0:30 - L'interface principale
2. 1:30 - Ajouter un abonnement
3. 2:30 - Résultat de l'ajout
4. 3:00 - Comprendre les KPI
5. 4:00 - Navigation dans l'app
6. 4:30 - Page Analytics
7. 4:50 - Conclusion

### 2. Gestion avancée (8 minutes)
**Fichier :** `tutorial-advanced.html`  
**Niveau :** Intermédiaire  
**Public :** Utilisateurs ayant suivi le tutoriel débutant

**Contenu :**
- Modifier et supprimer des abonnements
- Recherche instantanée et filtres avancés
- Comprendre les alertes d'inactivité
- Exporter vos données en JSON
- Importer des données depuis un fichier JSON
- Utiliser la page Analytics avancée
- Découvrir la documentation API

**Durée totale :** ~8 minutes  
**Chapitres :**
1. 0:30 - Modifier un abonnement
2. 1:15 - Supprimer en toute sécurité
3. 2:00 - Recherche instantanée
4. 2:45 - Filtres avancés
5. 3:30 - Alertes d'inactivité
6. 4:30 - Exporter en JSON
7. 5:15 - Importer des données
8. 6:00 - Analytics : Vue d'ensemble
9. 6:45 - Graphiques interactifs
10. 7:30 - Documentation API
11. 7:50 - Conclusion

## 🎬 Format des Tutoriels

Les tutoriels sont des présentations interactives HTML/CSS/JavaScript qui simulent une expérience vidéo :

- **Format :** HTML5 + CSS3 + JavaScript Vanilla
- **Résolution :** Responsive (optimisé 16:9)
- **Contrôles :** 
  - Bouton Play/Pause
  - Barre de progression cliquable
  - Navigation par chapitres
  - Raccourcis clavier (Espace, flèches)
- **Durée par slide :** 5 secondes en lecture automatique
- **Navigation manuelle :** Possible via chapitres ou barre de progression

## 🚀 Accès aux Tutoriels

### Via l'interface web
1. Accédez à la page d'aide : `http://localhost:4567/help.html`
2. Scrollez jusqu'à la section "Tutoriels vidéo"
3. Cliquez sur le tutoriel souhaité

### Accès direct
- **Débutant :** `http://localhost:4567/support/videos/tutorial-beginner.html`
- **Avancé :** `http://localhost:4567/support/videos/tutorial-advanced.html`

## 📝 Scripts de Narration

Les scripts détaillés de chaque tutoriel sont disponibles :
- `TUTORIEL_1_DEBUTANT.md` - Script complet du tutoriel débutant
- `TUTORIEL_2_AVANCE.md` - Script complet du tutoriel avancé

Ces scripts contiennent :
- Le texte de narration complet
- Les actions à montrer à l'écran
- Le timing précis de chaque partie
- Les notes de production
- Les recommandations d'accessibilité

## 🎨 Personnalisation

### Couleurs
- **Tutoriel Débutant :** Gradient violet (#667eea → #764ba2)
- **Tutoriel Avancé :** Gradient violet inversé (#764ba2 → #667eea)

### Icônes
Les tutoriels utilisent Bootstrap Icons 1.11.3 :
- `bi-play-circle` - Démarrage
- `bi-mortarboard` - Apprentissage avancé
- `bi-check-circle` - Validation
- Et bien d'autres pour illustrer les concepts

## 🔧 Maintenance

### Mise à jour des tutoriels
1. Modifier les fichiers HTML dans `/support/videos/`
2. Copier les fichiers mis à jour vers `/src/main/resources/static/support/videos/`
3. Recompiler les ressources : `mvn resources:resources`
4. Redémarrer le serveur si nécessaire

### Ajouter un nouveau tutoriel
1. Créer le fichier HTML dans `/support/videos/`
2. Suivre la structure des tutoriels existants
3. Copier vers `/src/main/resources/static/support/videos/`
4. Ajouter le lien dans `help.html`

## 📊 Statistiques

- **Nombre de tutoriels :** 2
- **Durée totale :** 13 minutes
- **Nombre de slides :** 18 (7 débutant + 11 avancé)
- **Chapitres totaux :** 18
- **Technologies :** HTML5, CSS3, Bootstrap 5, Bootstrap Icons

## ✅ Checklist de Qualité

- [x] Responsive design (mobile, tablette, desktop)
- [x] Navigation clavier (Espace, flèches)
- [x] Navigation par chapitres
- [x] Barre de progression interactive
- [x] Indicateurs de temps
- [x] Animations fluides
- [x] Contraste et lisibilité optimisés
- [x] Icônes cohérentes avec l'application
- [x] Liens de retour vers le centre d'aide

## 🌐 Accessibilité

Les tutoriels respectent les bonnes pratiques d'accessibilité :
- Contraste de couleurs suffisant (WCAG AA)
- Textes lisibles avec ombres pour améliorer la visibilité
- Navigation clavier complète
- Taille de police adaptative
- Boutons et liens avec zones de clic suffisantes

## 📞 Support

Pour toute question concernant les tutoriels :
- Consultez la FAQ dans le centre d'aide
- Référez-vous à la documentation API
- Contactez le support technique

**Dernière mise à jour :** Décembre 2024  
**Version :** 1.0  
**Auteur :** Équipe Gestion d'Abonnements
