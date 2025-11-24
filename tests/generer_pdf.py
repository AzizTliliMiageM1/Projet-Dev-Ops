#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script pour générer un PDF des tests fonctionnels
"""

from reportlab.lib.pagesizes import A4
from reportlab.lib import colors
from reportlab.lib.units import cm
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph, Spacer, PageBreak
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.enums import TA_CENTER, TA_LEFT

def create_pdf():
    # Création du document PDF
    pdf_file = "/workspaces/Projet-Dev-Ops/tests/TESTS_FONCTIONNELS.pdf"
    doc = SimpleDocTemplate(pdf_file, pagesize=A4, 
                           leftMargin=1.5*cm, rightMargin=1.5*cm,
                           topMargin=2*cm, bottomMargin=2*cm)
    
    # Styles
    styles = getSampleStyleSheet()
    title_style = ParagraphStyle(
        'CustomTitle',
        parent=styles['Heading1'],
        fontSize=24,
        textColor=colors.HexColor('#2c3e50'),
        spaceAfter=30,
        alignment=TA_CENTER
    )
    
    heading1_style = ParagraphStyle(
        'CustomHeading1',
        parent=styles['Heading1'],
        fontSize=16,
        textColor=colors.HexColor('#34495e'),
        spaceAfter=12,
        spaceBefore=20
    )
    
    heading2_style = ParagraphStyle(
        'CustomHeading2',
        parent=styles['Heading2'],
        fontSize=13,
        textColor=colors.HexColor('#7f8c8d'),
        spaceAfter=8,
        spaceBefore=12
    )
    
    normal_style = ParagraphStyle(
        'CustomNormal',
        parent=styles['Normal'],
        fontSize=9,
        leading=12
    )
    
    # Contenu du document
    story = []
    
    # Page de titre
    story.append(Paragraph("Tests Fonctionnels", title_style))
    story.append(Paragraph("Application de Gestion d'Abonnements", heading1_style))
    story.append(Spacer(1, 1*cm))
    story.append(Paragraph("Date : 24/11/2025", normal_style))
    story.append(Paragraph("Testeurs :", normal_style))
    story.append(Paragraph("• Aziz TLILI - 41006201", normal_style))
    story.append(Paragraph("• Maissara FERKOUS - 42006149", normal_style))
    story.append(Paragraph("• Doan Thi Mai Chi - 40016084", normal_style))
    story.append(Spacer(1, 2*cm))
    
    # Section 1 : Tests Connexion / Inscription
    story.append(Paragraph("1. Tests Connexion / Inscription", heading1_style))
    story.append(Spacer(1, 0.3*cm))
    
    intro_text = """Cette section teste le système d'authentification de l'application. 
    L'authentification permet aux utilisateurs de créer un compte personnel (inscription) et 
    de se connecter pour accéder à leurs données. Chaque utilisateur a son propre espace où il peut 
    gérer ses abonnements en toute sécurité. Les champs testés incluent l'email (adresse mail unique), 
    le mot de passe (code secret pour protéger le compte), et le pseudo (nom d'affichage). 
    Le système envoie aussi un email de confirmation pour vérifier que l'adresse mail est valide."""
    
    story.append(Paragraph(intro_text, normal_style))
    story.append(Spacer(1, 0.5*cm))
    
    # Test 1.1
    story.append(Paragraph("Test 1.1 : Se connecter avec un compte qui existe", heading2_style))
    data = [
        ['N°', 'Action', 'Résultat attendu', 'Résultat\nobtenu', 'Commentaires'],
        ['1', "J'ouvre la page\nlogin.html", "La page de connexion\ns'affiche", '✅ OK', "y'a bien le\nformulaire"],
        ['2', 'Je tape test@example.com\net le mot de passe', 'Ça écrit dans\nles champs', '✅ OK', '-'],
        ['3', 'Je clique sur\n"Se connecter"', "Ça me redirige vers\nla page d'accueil", '✅ OK', 'assez rapide'],
        ['4', 'Je regarde en haut\nà droite', "Mon pseudo s'affiche à la\nplace du bouton connexion", '✅ OK', 'cool, ça marche']
    ]
    create_table(story, data)
    
    # Test 1.2
    story.append(Paragraph("Test 1.2 : Se connecter avec mauvais mot de passe", heading2_style))
    data = [
        ['N°', 'Action', 'Résultat attendu', 'Résultat\nobtenu', 'Commentaires'],
        ['1', "J'ouvre login.html", 'Page de connexion', '✅ OK', '-'],
        ['2', 'Je mets un email correct\nmais mauvais mdp', 'Les champs acceptent', '✅ OK', '-'],
        ['3', 'Je clique sur\n"Se connecter"', "Message d'erreur qui dit\nque c'est pas bon", '✅ OK', 'message rouge'],
        ['4', 'Je vérifie', 'Je reste sur la page', '✅ OK', 'normal']
    ]
    create_table(story, data)
    
    # Test 1.3
    story.append(Paragraph("Test 1.3 : Se connecter sans avoir confirmé l'email", heading2_style))
    data = [
        ['N°', 'Action', 'Résultat attendu', 'Résultat\nobtenu', 'Commentaires'],
        ['1', 'Je crée un compte', 'Compte créé', '✅ OK', "j'ai reçu l'email"],
        ['2', "J'essaie de me\nconnecter direct", "Message qui dit de\nconfirmer l'email", '✅ OK', 'logique'],
        ['3', "Je clique sur le lien\ndans l'email", 'Compte confirmé', '✅ OK', '-'],
        ['4', 'Je retente la connexion', 'Cette fois ça marche', '✅ OK', '-']
    ]
    create_table(story, data)
    
    # Test 1.4
    story.append(Paragraph("Test 1.4 : Créer un nouveau compte", heading2_style))
    data = [
        ['N°', 'Action', 'Résultat attendu', 'Résultat\nobtenu', 'Commentaires'],
        ['1', 'Je clique sur "S\'inscrire"\nsur la home', 'Page register.html\ns\'ouvre', '✅ OK', '-'],
        ['2', 'Je remplis le pseudo\n"JeanTest"', 'Le champ accepte', '✅ OK', '-'],
        ['3', 'Je mets mon email\njean.test@mail.com', 'Ça marche', '✅ OK', '-'],
        ['4', 'Je tape le mot de\npasse 2 fois', 'Les 2 champs sont\nremplis', '✅ OK', '-'],
        ['5', 'Je clique "Créer\nmon compte"', 'Message de succès +\nemail envoyé', '✅ OK', 'nickel']
    ]
    create_table(story, data)
    
    # Test 1.5
    story.append(Paragraph("Test 1.5 : S'inscrire avec un email déjà pris", heading2_style))
    data = [
        ['N°', 'Action', 'Résultat attendu', 'Résultat\nobtenu', 'Commentaires'],
        ['1', "J'ouvre register.html", "Formulaire d'inscription", '✅ OK', '-'],
        ['2', 'Je mets test@example.com\n(déjà utilisé)', 'Le champ accepte', '✅ OK', '-'],
        ['3', 'Je remplis le reste\net je valide', 'Message d\'erreur\n"Email déjà utilisé"', '✅ OK', 'ça bloque bien'],
        ['4', 'Je vérifie', 'Je reste sur la page\npour réessayer', '✅ OK', '-']
    ]
    create_table(story, data)
    
    # Test 1.6
    story.append(Paragraph("Test 1.6 : S'inscrire avec mdp différents", heading2_style))
    data = [
        ['N°', 'Action', 'Résultat attendu', 'Résultat\nobtenu', 'Commentaires'],
        ['1', 'Je remplis le formulaire', "Tout s'écrit normalement", '✅ OK', '-'],
        ['2', 'Je tape "Pass123"\ndans mot de passe', 'OK', '✅ OK', '-'],
        ['3', 'Je tape "Pass456"\ndans confirmation', 'OK', '✅ OK', '-'],
        ['4', 'Je clique "Créer\nmon compte"', 'Message "Les mots de passe\ncorrespondent pas"', '✅ OK', 'validation faite']
    ]
    create_table(story, data)
    
    story.append(PageBreak())
    
    # Section 2 : Gestion des Abonnements
    story.append(Paragraph("2. Tests Gestion des Abonnements", heading1_style))
    story.append(Spacer(1, 0.3*cm))
    
    intro_text = """Cette section teste les fonctionnalités principales de l'application : la gestion des abonnements. 
    Un abonnement représente un service payant auquel on est inscrit (comme Netflix, Spotify, etc.). 
    Les champs incluent : le nom du service (ex: Netflix), la date de début (quand on a commencé l'abonnement), 
    la date de fin (quand il se termine), le prix mensuel (combien ça coûte par mois), et la catégorie 
    (type de service : Streaming, Sport, etc.). L'application permet d'ajouter de nouveaux abonnements, 
    de modifier les informations existantes, de supprimer ceux qu'on n'utilise plus, et de marquer 
    quand on les a utilisés récemment. Les statistiques en haut (Total, Actifs, Coût) se mettent à jour 
    automatiquement à chaque modification."""
    
    story.append(Paragraph(intro_text, normal_style))
    story.append(Spacer(1, 0.5*cm))
    
    # Test 2.1
    story.append(Paragraph("Test 2.1 : Ajouter un abonnement", heading2_style))
    data = [
        ['N°', 'Action', 'Résultat attendu', 'Résultat\nobtenu', 'Commentaires'],
        ['1', "Je me connecte et j'ouvre\nle dashboard", "Le dashboard s'affiche", '✅ OK', 'formulaire sur\nla droite'],
        ['2', 'Je mets "Netflix" dans\nnom du service', "Ça s'écrit", '✅ OK', '-'],
        ['3', 'Je choisis 01/01/2025\ncomme date début', "Le calendrier s'ouvre\net je sélectionne", '✅ OK', 'pratique'],
        ['4', 'Je mets 01/01/2026\npour la fin', 'Date acceptée', '✅ OK', '-'],
        ['5', 'Je tape 13.99\ndans le prix', 'Ça prend le chiffre', '✅ OK', 'avec virgule'],
        ['6', 'Je choisis "Streaming"\ndans la catégorie', 'Menu déroulant OK', '✅ OK', '-'],
        ['7', 'Je clique "Ajouter\nle service"', 'Message de succès', '✅ OK', '-'],
        ['8', 'Je regarde la liste', 'La carte Netflix\napparait', '✅ OK', 'badge vert\n"Actif"'],
        ['9', 'Je vérifie les stats\nen haut', 'Total: 1, Actifs: 1,\nCoût: 13.99€', '✅ OK', 'ça se met à\njour tout seul']
    ]
    create_table(story, data)
    
    # Test 2.2
    story.append(Paragraph("Test 2.2 : Essayer d'ajouter sans être connecté", heading2_style))
    data = [
        ['N°', 'Action', 'Résultat attendu', 'Résultat\nobtenu', 'Commentaires'],
        ['1', "J'ouvre index.html sans\nme connecter", "La page s'affiche", '✅ OK', '-'],
        ['2', "Je remplis le formulaire\nd'ajout", 'Les champs acceptent', '✅ OK', '-'],
        ['3', 'Je clique "Ajouter"', "Message d'erreur que je\ndois me connecter", '✅ OK', 'bien protégé'],
        ['4', "J'attends un peu", 'Ça me redirige vers\nlogin.html', '✅ OK', '-']
    ]
    create_table(story, data)
    
    # Test 2.3
    story.append(Paragraph("Test 2.3 : Modifier un abonnement existant", heading2_style))
    data = [
        ['N°', 'Action', 'Résultat attendu', 'Résultat\nobtenu', 'Commentaires'],
        ['1', "J'ai déjà un abonnement\ndans la liste", 'Liste visible avec\nicone modifier', '✅ OK', '-'],
        ['2', "Je clique sur l'icône\ncrayon", "Une fenêtre s'ouvre\navec les infos", '✅ OK', 'popup de\nmodification'],
        ['3', 'Je change le prix de\n13.99 à 15.99', 'Le champ accepte', '✅ OK', '-'],
        ['4', 'Je clique "Enregistrer"', 'La fenêtre se ferme +\nmessage succès', '✅ OK', '-'],
        ['5', 'Je regarde la carte', 'Le nouveau prix\n15.99€ est là', '✅ OK', 'changement\ndirect'],
        ['6', 'Je check les stats', 'Le coût total a\nchangé aussi', '✅ OK', 'recalculé\nautomatiquement']
    ]
    create_table(story, data)
    
    # Test 2.4
    story.append(Paragraph("Test 2.4 : Supprimer un abonnement", heading2_style))
    data = [
        ['N°', 'Action', 'Résultat attendu', 'Résultat\nobtenu', 'Commentaires'],
        ['1', 'Je clique sur la poubelle\nd\'un abonnement', 'Popup qui demande si\nje suis sûr', '✅ OK', 'pour éviter\nles erreurs'],
        ['2', 'Je clique "Annuler"', "Le popup se ferme,\nl'abonnement est toujours là", '✅ OK', '-'],
        ['3', 'Je reclique sur\nla poubelle', 'Le popup revient', '✅ OK', '-'],
        ['4', 'Cette fois je clique\n"Confirmer"', "Message que c'est\nsupprimé", '✅ OK', '-'],
        ['5', 'Je regarde', 'La carte a disparu', '✅ OK', '-'],
        ['6', 'Je vérifie les stats', 'Total et coût\nont baissé', '✅ OK', '-']
    ]
    create_table(story, data)
    
    # Test 2.5
    story.append(Paragraph("Test 2.5 : Marquer un abonnement comme utilisé", heading2_style))
    data = [
        ['N°', 'Action', 'Résultat attendu', 'Résultat\nobtenu', 'Commentaires'],
        ['1', 'Je clique "Marquer comme\nutilisé" sur une carte', 'Le bouton change', '✅ OK', 'retour visuel'],
        ['2', 'Je regarde la carte', 'Badge vert "Utilisé\naujourd\'hui" apparait', '✅ OK', 'avec la date\ndu jour'],
        ['3', 'Je refresh la page', 'Le badge est\nencore là', '✅ OK', "c'est sauvegardé"],
        ['4', 'Je simule 30 jours après', 'Badge alerte "Inactif\ndepuis 30j"', '✅ OK', "prévient\nl'inactivité"]
    ]
    create_table(story, data)
    
    story.append(PageBreak())
    
    # Section 3 : Recherche et Filtres
    story.append(Paragraph("3. Tests Recherche et Filtres", heading1_style))
    story.append(Spacer(1, 0.3*cm))
    
    intro_text = """Cette section teste la fonction de recherche qui permet de trouver rapidement un abonnement 
    parmi tous ceux qu'on a enregistrés. La barre de recherche est un champ de texte où on peut taper 
    le nom d'un service (ou une partie du nom) et l'application affiche uniquement les abonnements 
    correspondants en temps réel. Par exemple, si on tape "net", seuls les services contenant "net" 
    dans leur nom s'affichent. C'est très pratique quand on a beaucoup d'abonnements et qu'on cherche 
    un service précis."""
    
    story.append(Paragraph(intro_text, normal_style))
    story.append(Spacer(1, 0.5*cm))
    
    # Test 3.1
    story.append(Paragraph("Test 3.1 : Rechercher un service par nom", heading2_style))
    data = [
        ['N°', 'Action', 'Résultat attendu', 'Résultat\nobtenu', 'Commentaires'],
        ['1', "J'ai Netflix, Spotify\net Disney+", 'Les 3 sont affichés', '✅ OK', '-'],
        ['2', 'Je clique dans la barre\nde recherche', 'Le champ devient actif', '✅ OK', '-'],
        ['3', 'Je tape "net"', "Y'a que Netflix\nqui reste", '✅ OK', 'filtre en direct'],
        ['4', 'Je continue "netflix"', 'Toujours juste Netflix', '✅ OK', 'majuscule/minuscule\npareil'],
        ['5', "J'efface tout", 'Les 3 abonnements\nreviennent', '✅ OK', '-']
    ]
    create_table(story, data)
    
    # Test 3.2
    story.append(Paragraph("Test 3.2 : Chercher quelque chose qui existe pas", heading2_style))
    data = [
        ['N°', 'Action', 'Résultat attendu', 'Résultat\nobtenu', 'Commentaires'],
        ['1', 'Je tape "xyz123" dans\nla recherche', 'Aucune carte', '✅ OK', '-'],
        ['2', 'Je regarde', 'Message "Aucun abonnement\ntrouvé"', '✅ OK', '-'],
        ['3', 'Je check les stats', 'Toujours les bonnes\nvaleurs', '✅ OK', 'pas impacté par\nla recherche']
    ]
    create_table(story, data)
    
    # Section 4 : Import / Export
    story.append(Paragraph("4. Tests Import / Export", heading1_style))
    story.append(Spacer(1, 0.3*cm))
    
    intro_text = """Cette section teste les fonctionnalités d'import et export de données. L'export permet 
    de sauvegarder tous ses abonnements dans un fichier JSON (format texte structuré lisible par ordinateur) 
    qu'on peut télécharger sur son ordinateur. C'est utile pour faire une sauvegarde de ses données. 
    L'import fait l'inverse : il permet de charger un fichier JSON contenant des abonnements pour les 
    ajouter dans l'application. Ces fonctions sont pratiques pour transférer ses données d'un ordinateur 
    à un autre, ou pour restaurer une sauvegarde. Le système vérifie que le fichier est au bon format 
    avant de l'importer."""
    
    story.append(Paragraph(intro_text, normal_style))
    story.append(Spacer(1, 0.5*cm))
    
    # Test 4.1
    story.append(Paragraph("Test 4.1 : Exporter mes abonnements en JSON", heading2_style))
    data = [
        ['N°', 'Action', 'Résultat attendu', 'Résultat\nobtenu', 'Commentaires'],
        ['1', "J'ai créé plusieurs\nabonnements", 'Ils sont tous dans\nla liste', '✅ OK', '-'],
        ['2', 'Je clique "Exporter JSON"', 'Un fichier se télécharge', '✅ OK', '-'],
        ['3', "J'ouvre le fichier", "C'est un fichier .json", '✅ OK', 'format correct'],
        ['4', 'Je regarde dedans', 'Tous mes abonnements\nsont là', '✅ OK', 'avec toutes\nles infos'],
        ['5', 'Je vérifie', 'Format tableau JSON\navec des objets', '✅ OK', 'id, nomService,\ndates, prix...']
    ]
    create_table(story, data)
    
    # Test 4.2
    story.append(Paragraph("Test 4.2 : Importer un fichier JSON", heading2_style))
    data = [
        ['N°', 'Action', 'Résultat attendu', 'Résultat\nobtenu', 'Commentaires'],
        ['1', 'Je clique "Importer JSON"', 'Sélecteur de fichier', '✅ OK', '-'],
        ['2', 'Je choisis mon\nfichier JSON', 'Le fichier est accepté', '✅ OK', '-'],
        ["J'attends", "Y'a un truc qui tourne", '✅ OK', 'chargement'],
        ['4', 'Résultat', 'Message "X abonnements\nimportés"', '✅ OK', 'X = le vrai\nnombre'],
        ['5', 'Je regarde ma liste', 'Les nouveaux sont\najoutés', '✅ OK', 'pas de doublons'],
        ['6', 'Je vérifie stats', 'Tout est recalculé', '✅ OK', '-']
    ]
    create_table(story, data)
    
    # Test 4.3
    story.append(Paragraph("Test 4.3 : Importer un mauvais fichier", heading2_style))
    data = [
        ['N°', 'Action', 'Résultat attendu', 'Résultat\nobtenu', 'Commentaires'],
        ['1', 'Je clique "Importer JSON"', "Sélecteur s'ouvre", '✅ OK', '-'],
        ['2', 'Je choisis un fichier .txt', 'Fichier rejeté', '✅ OK', "vérifie\nl'extension"],
        ['3', "J'essaie avec un\nJSON cassé", 'Message d\'erreur\n"Format invalide"', '✅ OK', 'bien géré']
    ]
    create_table(story, data)
    
    story.append(PageBreak())
    
    # Section 5 : Dashboard Statistiques
    story.append(Paragraph("5. Tests Dashboard Statistiques", heading1_style))
    story.append(Spacer(1, 0.3*cm))
    
    intro_text = """Cette section teste le tableau de bord (dashboard) qui affiche des statistiques visuelles 
    sur nos abonnements. En haut, 4 cartes montrent : le nombre total d'abonnements, combien sont actifs 
    actuellement, le coût mensuel total, et le nombre d'alertes (abonnements non utilisés depuis longtemps). 
    Les graphiques en dessous visualisent ces données : un graphique rond (donut) montre la répartition 
    par catégorie (combien on a d'abonnements Streaming, Sport, etc.), et un graphique en barres montre 
    l'évolution du coût mois par mois. Ces graphiques sont interactifs : quand on passe la souris dessus, 
    on voit plus de détails."""
    
    story.append(Paragraph(intro_text, normal_style))
    story.append(Spacer(1, 0.5*cm))
    
    # Test 5.1
    story.append(Paragraph("Test 5.1 : Voir les graphiques", heading2_style))
    data = [
        ['N°', 'Action', 'Résultat attendu', 'Résultat\nobtenu', 'Commentaires'],
        ['1', 'Je clique sur "Dashboard"\nen haut', "Page stats.html s'ouvre", '✅ OK', 'assez rapide'],
        ['2', 'Je regarde les stats\nen haut', '4 cartes : Total, Actifs,\nCoût, Alertes', '✅ OK', 'bons chiffres'],
        ['3', 'Je check le graphique\nrond', 'Graphique coloré avec\nles catégories', '✅ OK', 'joli'],
        ['4', 'Je regarde le graphique\nen barres', 'Évolution du coût\npar mois', '✅ OK', 'avec les axes'],
        ['5', 'Je passe la souris\ndessus', "Infos détaillées qui\ns'affichent", '✅ OK', 'interactif cool']
    ]
    create_table(story, data)
    
    # Test 5.2
    story.append(Paragraph("Test 5.2 : Dashboard quand j'ai rien", heading2_style))
    data = [
        ['N°', 'Action', 'Résultat attendu', 'Résultat\nobtenu', 'Commentaires'],
        ['1', 'Nouveau compte vide', 'Je peux quand même\naccéder', '✅ OK', '-'],
        ['2', 'Je regarde les stats', 'Tout à zéro', '✅ OK', '0 abonnements, 0€'],
        ['3', 'Je check les graphiques', 'Message "Aucune donnée"', '✅ OK', "pas d'erreur"]
    ]
    create_table(story, data)
    
    # Section 6 : Chatbot
    story.append(Paragraph("6. Tests Chatbot", heading1_style))
    story.append(Spacer(1, 0.3*cm))
    
    intro_text = """Cette section teste le chatbot, un assistant virtuel qui répond aux questions en langage 
    naturel. Le chatbot s'ouvre en cliquant sur une icône en bas à droite de la page. On peut lui poser 
    des questions comme "Quel est mon budget ?" ou "Cherche Netflix" et il répond avec les informations 
    correspondantes. C'est une façon plus naturelle et conversationnelle d'interagir avec l'application, 
    sans avoir à naviguer dans les menus. Le chatbot comprend le français et peut fournir des informations 
    sur nos abonnements, notre budget, ou nos alertes."""
    
    story.append(Paragraph(intro_text, normal_style))
    story.append(Spacer(1, 0.5*cm))
    
    # Test 6.1
    story.append(Paragraph("Test 6.1 : Utiliser le chatbot", heading2_style))
    data = [
        ['N°', 'Action', 'Résultat attendu', 'Résultat\nobtenu', 'Commentaires'],
        ['1', "Je clique sur l'icône chatbot\nen bas à droite", "La fenêtre s'ouvre", '✅ OK', 'animation sympa'],
        ['2', 'Je tape "Bonjour"', 'Le bot répond', '✅ OK', 'rapide'],
        ['3', 'Je demande "Quel est\nmon budget ?"', 'Il me donne le\ncoût mensuel', '✅ OK', 'calcul juste'],
        ['4', 'Je tape "Mes abonnements\nactifs"', 'Liste des abonnements\nactifs', '✅ OK', 'bien présenté']
    ]
    create_table(story, data)
    
    # Test 6.2
    story.append(Paragraph("Test 6.2 : Chercher via le chatbot", heading2_style))
    data = [
        ['N°', 'Action', 'Résultat attendu', 'Résultat\nobtenu', 'Commentaires'],
        ['1', "J'ouvre le chatbot", 'Fenêtre visible', '✅ OK', '-'],
        ['2', 'Je tape "Cherche Netflix"', 'Il me donne les détails\nde Netflix', '✅ OK', 'comprend bien'],
        ['3', 'Je demande "Mes alertes"', 'Liste des trucs\ninactifs', '✅ OK', '-']
    ]
    create_table(story, data)
    
    # Section 7 : Déconnexion
    story.append(Paragraph("7. Test Déconnexion", heading1_style))
    story.append(Spacer(1, 0.3*cm))
    
    intro_text = """Cette section teste la fonction de déconnexion qui permet de terminer sa session et 
    de fermer l'accès à son compte. Quand on se déconnecte, on retourne à la page d'accueil publique 
    et notre pseudo (nom d'affichage) disparaît de la barre de navigation. C'est important pour la sécurité, 
    surtout si on utilise un ordinateur partagé : personne d'autre ne peut accéder à nos données personnelles 
    après qu'on se soit déconnecté. Il faut se reconnecter avec son email et mot de passe pour retrouver 
    l'accès à ses abonnements."""
    
    story.append(Paragraph(intro_text, normal_style))
    story.append(Spacer(1, 0.5*cm))
    
    # Test 7.1
    story.append(Paragraph("Test 7.1 : Me déconnecter", heading2_style))
    data = [
        ['N°', 'Action', 'Résultat attendu', 'Résultat\nobtenu', 'Commentaires'],
        ['1', 'Je suis connecté, je clique\n"Déconnexion"', 'Déconnexion', '✅ OK', 'direct'],
        ['2', 'Je regarde où je suis', 'Retour à home.html', '✅ OK', '-'],
        ['3', 'Je check la navbar', 'Boutons "Se connecter" et\n"S\'inscrire" de retour', '✅ OK', 'pseudo parti'],
        ['4', "J'essaie d'aller au\ndashboard", 'Je vois plus mes\ntrucs persos', '✅ OK', 'session terminée']
    ]
    create_table(story, data)
    
    # Section 8 : Responsive
    story.append(Paragraph("8. Test Responsive (Mobile)", heading1_style))
    story.append(Spacer(1, 0.3*cm))
    
    intro_text = """Cette section teste l'adaptabilité de l'application sur différents appareils, notamment 
    les smartphones et tablettes. Le terme "responsive" signifie que l'interface s'adapte automatiquement 
    à la taille de l'écran. Sur mobile, le menu se transforme en menu "burger" (trois lignes horizontales), 
    les cartes d'abonnements s'empilent verticalement (une par ligne au lieu de plusieurs côte à côte), 
    et les formulaires se repositionnent pour être facilement utilisables avec le doigt. Les graphiques 
    se redimensionnent aussi pour rester lisibles sur petit écran. Cela permet d'utiliser l'application 
    confortablement depuis n'importe quel appareil."""
    
    story.append(Paragraph(intro_text, normal_style))
    story.append(Spacer(1, 0.5*cm))
    
    # Test 8.1
    story.append(Paragraph("Test 8.1 : Ouvrir sur téléphone", heading2_style))
    data = [
        ['N°', 'Action', 'Résultat attendu', 'Résultat\nobtenu', 'Commentaires'],
        ['1', "J'ouvre l'appli sur\nmon tel", "Tout s'adapte", '✅ OK', 'menu burger'],
        ['2', 'Je regarde les cartes', 'Empilées les unes sur\nles autres', '✅ OK', '1 par ligne'],
        ['3', 'Je check le formulaire', 'En dessous de la liste', '✅ OK', 'bien placé'],
        ['4', 'Les graphiques', 'Bien redimensionnés', '✅ OK', '-']
    ]
    create_table(story, data)
    
    story.append(PageBreak())
    
    # Bilan
    story.append(Paragraph("Bilan des Tests", heading1_style))
    story.append(Spacer(1, 0.5*cm))
    
    bilan_data = [
        ['Partie testée', 'Nombre tests', 'Réussis', 'Remarques'],
        ['Connexion/Inscription', '6', '6', 'RAS'],
        ['Gestion abonnements', '5', '5', 'tout fonctionne'],
        ['Recherche', '2', '2', 'OK'],
        ['Import/Export', '3', '3', 'OK'],
        ['Dashboard', '2', '2', 'graphiques niquel'],
        ['Chatbot', '2', '2', 'réponses correctes'],
        ['Déconnexion', '1', '1', 'OK'],
        ['Mobile', '1', '1', 'responsive impec'],
        ['TOTAL', '22', '22', '100%']
    ]
    
    bilan_table = Table(bilan_data, colWidths=[5*cm, 3*cm, 2.5*cm, 4*cm])
    bilan_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#3498db')),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
        ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
        ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
        ('FONTSIZE', (0, 0), (-1, 0), 11),
        ('BOTTOMPADDING', (0, 0), (-1, 0), 12),
        ('BACKGROUND', (0, -1), (-1, -1), colors.HexColor('#ecf0f1')),
        ('FONTNAME', (0, -1), (-1, -1), 'Helvetica-Bold'),
        ('GRID', (0, 0), (-1, -1), 1, colors.grey),
        ('FONTSIZE', (0, 1), (-1, -1), 10),
        ('ROWBACKGROUNDS', (0, 1), (-1, -2), [colors.white, colors.HexColor('#f8f9fa')])
    ]))
    story.append(bilan_table)
    
    story.append(Spacer(1, 1*cm))
    
    # Infos tests
    story.append(Paragraph("Infos Tests", heading1_style))
    story.append(Paragraph("• <b>Navigateurs</b> : Chrome, Firefox", normal_style))
    story.append(Paragraph("• <b>OS</b> : Windows 11, Ubuntu", normal_style))
    story.append(Paragraph("• <b>Écrans</b> : PC 1920x1080, portable 1366x768, mobile", normal_style))
    story.append(Paragraph("• <b>Testé le</b> : 24/11/2025", normal_style))
    story.append(Paragraph("• <b>Par</b> : Aziz TLILI, Maissara FERKOUS, Doan Thi Mai Chi", normal_style))
    
    story.append(Spacer(1, 1*cm))
    
    # Conclusion
    story.append(Paragraph("Conclusion", heading1_style))
    conclusion_text = """J'ai testé toutes les fonctionnalités de l'application et tout marche bien. 
    Chaque utilisateur peut gérer ses propres abonnements après s'être inscrit et connecté. 
    Les stats sont correctes, l'import/export JSON fonctionne, le chatbot répond bien aux questions.<br/><br/>
    J'ai aussi vérifié que ça marche sur mobile et c'est bien adapté. Pas de bugs trouvés pendant mes tests.<br/><br/>
    L'interface est claire et assez simple à utiliser, les messages d'erreur aident quand on fait une erreur. 
    Le système de confirmation par email empêche les faux comptes.<br/><br/>
    Globalement le projet remplit bien le cahier des charges."""
    
    story.append(Paragraph(conclusion_text, normal_style))
    
    # Génération du PDF
    doc.build(story)
    print(f"PDF généré avec succès : {pdf_file}")

def create_table(story, data):
    """Crée un tableau formaté"""
    # Calcul des largeurs de colonnes
    col_widths = [1*cm, 4.5*cm, 4.5*cm, 2*cm, 3*cm]
    
    table = Table(data, colWidths=col_widths)
    table.setStyle(TableStyle([
        # En-tête
        ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#3498db')),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
        ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
        ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
        ('FONTSIZE', (0, 0), (-1, 0), 9),
        ('BOTTOMPADDING', (0, 0), (-1, 0), 8),
        ('TOPPADDING', (0, 0), (-1, 0), 8),
        
        # Corps du tableau
        ('FONTSIZE', (0, 1), (-1, -1), 8),
        ('FONTNAME', (0, 1), (-1, -1), 'Helvetica'),
        ('GRID', (0, 0), (-1, -1), 0.5, colors.grey),
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.HexColor('#f8f9fa')]),
        ('LEFTPADDING', (0, 0), (-1, -1), 4),
        ('RIGHTPADDING', (0, 0), (-1, -1), 4),
        ('TOPPADDING', (0, 1), (-1, -1), 6),
        ('BOTTOMPADDING', (0, 1), (-1, -1), 6),
    ]))
    
    story.append(table)
    story.append(Spacer(1, 0.5*cm))

if __name__ == "__main__":
    create_pdf()
