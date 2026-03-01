/**
 * Chatbot IA Intelligent pour Gestion d'Abonnements v2.0
 * Utilise une IA avancée avec : NLP, contexte persistant, apprentissage utilisateur, recommandations intelligentes
 * Features: Dialogue multi-turns, tutoriels interactifs, suggestions contextuelles, historique persistant
 */

class AbonnementChatbot {
    constructor() {
        this.context = {
            lastQuestion: null,
            conversationHistory: [],
            userData: null,
            awaitingResponse: false,
            userProfile: this.loadUserProfile(),
            sessionStartTime: new Date(),
            messageCount: 0,
            lastIntent: null,
            followUpMode: false
        };
        
        this.initializeKnowledgeBase();
        this.initializeNLP();
        this.initializeAdvancedFeatures();
    }

    // Charge le profil utilisateur sauvegardé en localStorage
    loadUserProfile() {
        const saved = localStorage.getItem('chatbot_user_profile');
        return saved ? JSON.parse(saved) : {
            preferences: {
                language: 'fr',
                showEmojis: true,
                detailLevel: 'normal' // 'simple', 'normal', 'detailed'
            },
            interactions: {
                totalMessages: 0,
                frequentQuestions: {},
                lastTopics: []
            },
            preferences_learning: {
                likesTutorials: null,
                prefersShortAnswers: null,
                interestedInAdvancedFeatures: null
            }
        };
    }

    // Sauvegarde le profil utilisateur
    saveUserProfile() {
        localStorage.setItem('chatbot_user_profile', JSON.stringify(this.context.userProfile));
    }

    // Initialise les fonctionnalités avancées
    initializeAdvancedFeatures() {
        this.tutorialMode = false;
        this.tutorialStep = 0;
        this.tutorials = this.initializeTutorials();
        this.contextualSuggestions = [];
        this.responseTemplates = this.initializeResponseTemplates();
    }

    // Initialise les tutoriels interactifs
    initializeTutorials() {
        return {
            basicUsage: [
                { title: "Bienvenue dans le tutoriel", content: "Je vais vous montrer comment utiliser au mieux cette application. Commençons !" },
                { title: "Ajouter un abonnement", content: "Vous pouvez ajouter un abonnement de deux façons:\n1️⃣ Via le formulaire (bouton '➕ Ajouter')\n2️⃣ En tapant : 'Ajoute Netflix pour Jean à 15.99€'" },
                { title: "Gérer vos abonnements", content: "Pour voir tous vos abonnements, tapez 'liste' ou 'mes abonnements'. Vous pouvez aussi les filtrer par catégorie !" },
                { title: "Vérifier votre budget", content: "Demandez 'Quel est mon budget ?' pour une analyse complète de vos dépenses mensuelles." },
                { title: "Recevoir des alertes", content: "Activez les alertes d'inactivité pour être notifié des abonnements non utilisés depuis 30 jours." }
            ],
            advancedFeatures: [
                { title: "Mode Expert", content: "Vous avez accès à des commandes avancées comme l'export/import de données." },
                { title: "Analyse Intelligente", content: "Utilisez 'Analyse mes dépenses' pour obtenir des graphiques et des recommandations d'optimisation." },
                { title: "Recherche Intelligente", content: "Recherchez rapidement : 'Cherche Netflix', 'Trouve Spotify', etc." },
                { title: "Suivi Automatique", content: "L'application suit vos habitudes et vous propose des suggestions personnalisées." }
            ]
        };
    }

    // Initialise les templates de réponse
    initializeResponseTemplates() {
        return {
            confirmation: (action, details) => `✅ **${action}** effectué${action.includes('e') ? 'e' : ''} avec succès !\n\n${details}`,
            error: (errorType, details) => `❌ **${errorType}**\n\n${details}\n\nBesoin d'aide ? Tapez 'aide' !`,
            info: (title, details) => `ℹ️ **${title}**\n\n${details}`,
            suggestion: (suggestion, details) => `💡 **Suggestion :** ${suggestion}\n\n${details}`,
            question: (question, options) => `❓ **${question}**\n\nOptions :\n${options.map((o, i) => `${i + 1}️⃣ ${o}`).join('\n')}`
        };
    }

    initializeKnowledgeBase() {
        this.knowledgeBase = {
            greetings: {
                patterns: ['bonjour', 'salut', 'hello', 'hey', 'coucou', 'bonsoir', 'bonjour !', 'yo'],
                responses: this.generateGreetingResponse.bind(this)
            },
            farewell: {
                patterns: ['au revoir', 'bye', 'à plus', 'merci', 'ciao', 'salut'],
                responses: [
                    "Au revoir ! N'hésitez pas à revenir si vous avez d'autres questions. 👋",
                    "À bientôt ! Bon contrôle de vos abonnements ! 😊",
                    "Merci de m'avoir consulté ! À très vite ! 🎉"
                ]
            },
            help: {
                patterns: ['aide', 'help', 'comment', 'que peux-tu faire', 'fonctionnalités', 'commandes'],
                responses: [
                    `Je peux vous aider avec :\n
➕ **Ajouter** : "Ajoute Netflix pour Jean Dupont à 15.99€"
➕ **Ajouter complet** : "Ajoute Basic Fit pour Marie Sport début 13/12/2025 fin 14/12/2025 catégorie sport à 20€"
🗑️ **Supprimer** : "Supprime Netflix" ou "Supprime l'abonnement 2"
📊 **Analyser** : "Analyse mes dépenses", "Mon budget"
📈 **Stats** : "Quel est mon coût mensuel", "Combien je dépense"
📋 **Lister** : "Mes abonnements actifs", "Liste tout"
🔍 **Chercher** : "Cherche Spotify", "Trouve Disney"
⚠️ **Alertes** : "Mes alertes", "Abonnements inutilisés"
💡 **Conseils** : "Comment économiser", "Optimise mon budget"

Que voulez-vous faire ?`
                ]
            },
            addSubscription: {
                patterns: ['ajoute', 'créer', 'nouveau', 'enregistrer', 'add'],
                responses: [
                    "Pour ajouter un abonnement, utilisez le formulaire à droite ou dites-moi : \"Ajoute Netflix pour Jean Dupont à 15.99€\"",
                    "Je peux vous guider ! Quel service voulez-vous ajouter ? (ex: Netflix, Spotify, Basic Fit...)"
                ]
            },
            statistics: {
                patterns: ['statistique', 'stats', 'combien', 'total', 'dépense', 'budget', 'coût'],
                responses: this.getStatisticsResponse.bind(this)
            },
            activeSubscriptions: {
                patterns: ['actif', 'en cours', 'liste', 'mes abonnements', 'show', 'affiche'],
                responses: this.getActiveSubscriptionsResponse.bind(this)
            },
            alerts: {
                patterns: ['alerte', 'inactif', 'inutilisé', 'warning', 'attention'],
                responses: this.getAlertsResponse.bind(this)
            },
            optimization: {
                patterns: ['économiser', 'optimiser', 'réduire', 'conseil', 'suggestion', 'améliorer'],
                responses: this.getOptimizationAdvice.bind(this)
            },
            search: {
                patterns: ['cherche', 'trouve', 'recherche', 'search', 'où est'],
                responses: this.searchSubscription.bind(this)
            },
            analytics: {
                patterns: ['analyse', 'graphique', 'chart', 'visualisation', 'tendance'],
                responses: [
                    "📊 Pour voir vos analytics détaillées, cliquez sur le bouton 'Analytics' en haut de la page. Vous y trouverez des graphiques interactifs !",
                    "Les statistiques avancées sont disponibles dans la page Analytics. Voulez-vous que je vous y emmène ?"
                ]
            },
            export: {
                patterns: ['exporter', 'export', 'télécharger', 'sauvegarder', 'backup'],
                responses: [
                    "Pour exporter vos données, cliquez sur le bouton 'Exporter JSON' en haut de la page. Vous aurez une sauvegarde complète !",
                    "💾 L'export JSON est disponible en un clic ! C'est dans la barre d'outils en haut."
                ]
            },
            import: {
                patterns: ['importer', 'import', 'charger', 'restaurer'],
                responses: [
                    "Pour importer des données, utilisez le bouton 'Importer JSON' et sélectionnez votre fichier de sauvegarde.",
                    "📂 L'import se fait facilement via le bouton d'import en haut de la page !"
                ]
            },
            pricing: {
                patterns: ['prix', 'combien coûte', 'tarif', 'gratuit', 'payant'],
                responses: [
                    "Cette application de gestion d'abonnements est 100% gratuite et open source ! 🎉",
                    "L'application est totalement gratuite. Aucun frais caché ! 😊"
                ]
            },
            features: {
                patterns: ['fonctionnalité', 'feature', 'capacité', 'possibilité'],
                responses: [
                    `🎯 **Fonctionnalités principales :**
• Gestion CRUD complète des abonnements
• Chatbot IA avec commandes naturelles
• Ajout/Suppression en langage naturel
• Alertes d'inactivité (>30 jours)
• Statistiques en temps réel
• Export/Import JSON
• Analytics avec graphiques
• API REST documentée
• Interface moderne et responsive

Quelle fonctionnalité vous intéresse ?`
                ]
            },
            categories: {
                patterns: ['catégorie', 'type', 'classification'],
                responses: this.showCategories.bind(this)
            },
            recordUsage: {
                patterns: ['utilisé', 'utilise', 'j\'ai utilisé', 'marque comme utilisé'],
                responses: this.recordSubscriptionUsage.bind(this)
            }
        };
    }

    initializeNLP() {
        // Simple tokenizer et normalizer
        this.stopWords = ['le', 'la', 'les', 'un', 'une', 'des', 'de', 'du', 'à', 'est', 'et', 'dans', 'pour', 'mon', 'ma', 'mes'];
    }

    // Normalise et tokenize le texte
    tokenize(text) {
        return text
            .toLowerCase()
            .normalize("NFD").replace(/[\u0300-\u036f]/g, "") // Remove accents
            .replace(/[^\w\s]/g, ' ')
            .split(/\s+/)
            .filter(word => word.length > 2 && !this.stopWords.includes(word));
    }

    // Trouve l'intention de l'utilisateur
    detectIntent(userMessage) {
        const tokens = this.tokenize(userMessage);
        let bestMatch = { category: 'unknown', score: 0 };

        for (const [category, data] of Object.entries(this.knowledgeBase)) {
            let score = 0;
            for (const pattern of data.patterns) {
                if (tokens.some(token => token.includes(pattern) || pattern.includes(token))) {
                    score += 2;
                }
                if (userMessage.toLowerCase().includes(pattern)) {
                    score += 1;
                }
            }

            if (score > bestMatch.score) {
                bestMatch = { category, score };
            }
        }

        return bestMatch.score > 0 ? bestMatch.category : 'unknown';
    }

    // Extrait des entités du message (nombres, noms de services, etc.)
    extractEntities(message) {
        const entities = {
            price: null,
            service: null,
            client: null,
            number: null,
            category: null,
            startDate: null,
            endDate: null
        };

        // Extract prix - cherche n'importe quel nombre suivi de € ou euros
        const priceMatch = message.match(/(\d+(?:[.,]\d{1,2})?)\s*(?:€|euros?)/i);
        if (priceMatch) {
            entities.price = parseFloat(priceMatch[1].replace(',', '.'));
        }

        // Extract dates (format DD/MM/YYYY ou DD-MM-YYYY)
        const dateMatches = message.match(/(\d{1,2})[\/\-](\d{1,2})[\/\-](\d{4})/g);
        if (dateMatches && dateMatches.length >= 1) {
            entities.startDate = this.parseDate(dateMatches[0]);
            if (dateMatches.length >= 2) {
                entities.endDate = this.parseDate(dateMatches[1]);
            }
        }

        // Extract catégorie
        const categories = ['streaming', 'musique', 'sport', 'cloud', 'gaming', 'productivité', 'autre'];
        for (const cat of categories) {
            if (message.toLowerCase().includes('catégorie ' + cat)) {
                entities.category = cat;
                break;
            }
        }

        // Extract nom du client - cherche après "au nom de" ou "pour"
        // On cherche 1 ou 2 mots avec majuscule (prénom + nom)
        const clientMatch = message.match(/(?:au nom de|pour)\s+([A-ZÀ-ÿ][a-zà-ÿ]+(?:\s+[A-ZÀ-ÿ][a-zà-ÿ]+)?)/i);
        if (clientMatch) {
            entities.client = clientMatch[1].trim();
        }

        // Extract nom du service - LOGIQUE UNIVERSELLE
        // On cherche tout ce qui vient après le nom du client jusqu'au prochain mot-clé
        if (entities.client) {
            // Cherche après le nom du client
            const afterClient = message.split(entities.client)[1];
            if (afterClient) {
                // Extrait tout jusqu'au premier mot-clé (début, fin, catégorie, prix, à, pour)
                const serviceMatch = afterClient.match(/^\s+([A-Za-zÀ-ÿ0-9\s]+?)(?=\s+(?:début|fin|catégorie|prix|à|pour un|et|$))/i);
                if (serviceMatch) {
                    entities.service = serviceMatch[1].trim();
                }
            }
        }
        
        // Si pas de service trouvé avec la méthode précédente, essayer après "ajoute"
        if (!entities.service) {
            const serviceMatch = message.match(/(?:ajoute|ajouter|créer?|nouveau)\s+(?:un\s+)?(?:abonnement\s+)?(?:au nom de\s+[A-ZÀ-ÿ][a-zà-ÿ]+(?:\s+[A-ZÀ-ÿ][a-zà-ÿ]+)?\s+)?([A-Za-zÀ-ÿ0-9\s]+?)(?=\s+(?:pour|au nom de|début|à|$))/i);
            if (serviceMatch) {
                entities.service = serviceMatch[1].trim();
            }
        }

        // Extract nombres simples
        const numberMatch = message.match(/\b(\d+)\b/);
        if (numberMatch) {
            entities.number = parseInt(numberMatch[1]);
        }

        return entities;
    }

    parseDate(dateStr) {
        const parts = dateStr.split(/[\/\-]/);
        if (parts.length === 3) {
            const day = parseInt(parts[0]);
            const month = parseInt(parts[1]) - 1;
            const year = parseInt(parts[2]);
            return new Date(year, month, day).toISOString();
        }
        return null;
    }

    // Récupère les statistiques actuelles
    async getStatisticsResponse() {
        try {
            const response = await fetch('/api/abonnements');
            const abonnements = await response.json();
            
            const total = abonnements.length;
            const actifs = abonnements.filter(a => a.statut === 'actif').length;
            const coutTotal = abonnements.reduce((sum, a) => sum + (a.prixMensuel || 0), 0);
            const alertes = abonnements.filter(a => {
                const lastUse = new Date(a.dernierUtilisation);
                const daysSinceUse = (new Date() - lastUse) / (1000 * 60 * 60 * 24);
                return daysSinceUse > 30;
            }).length;

            return `📊 **Voici vos statistiques :**

💳 **Total abonnements :** ${total}
✅ **Actifs :** ${actifs}
💰 **Coût mensuel :** ${coutTotal.toFixed(2)}€
⚠️ **Alertes inactivité :** ${alertes}

${alertes > 0 ? '\n🔔 Vous avez des abonnements inutilisés ! Voulez-vous que je vous aide à optimiser ?' : '\n✨ Excellent ! Tous vos abonnements sont bien utilisés.'}`;
        } catch (error) {
            return "Désolé, je n'ai pas pu récupérer vos statistiques. Assurez-vous que le serveur est bien démarré.";
        }
    }

    // Liste les abonnements actifs
    async getActiveSubscriptionsResponse() {
        try {
            const response = await fetch('/api/abonnements');
            const abonnements = await response.json();
            const actifs = abonnements.filter(a => a.statut === 'actif');

            if (actifs.length === 0) {
                return "Vous n'avez aucun abonnement actif pour le moment. Voulez-vous en ajouter un ?";
            }

            let message = `📋 **Vos ${actifs.length} abonnements actifs :**\n\n`;
            actifs.forEach((ab, index) => {
                message += `${index + 1}. **${ab.nomService}** - ${ab.clientName || 'N/A'}\n   💰 ${ab.prixMensuel}€/mois\n   📅 Jusqu'au ${new Date(ab.dateFin).toLocaleDateString()}\n\n`;
            });

            return message;
        } catch (error) {
            return "Impossible de récupérer la liste. Vérifiez votre connexion.";
        }
    }

    // Récupère les alertes
    async getAlertsResponse() {
        try {
            const response = await fetch('/api/abonnements');
            const abonnements = await response.json();
            
            const alertes = abonnements.filter(a => {
                const lastUse = new Date(a.dernierUtilisation);
                const daysSinceUse = (new Date() - lastUse) / (1000 * 60 * 60 * 24);
                return daysSinceUse > 30;
            });

            if (alertes.length === 0) {
                return "✅ Aucune alerte ! Tous vos abonnements sont utilisés régulièrement. Bravo ! 🎉";
            }

            let message = `⚠️ **${alertes.length} alerte(s) d'inactivité :**\n\n`;
            alertes.forEach((ab, index) => {
                const lastUse = new Date(ab.dernierUtilisation);
                const daysSinceUse = Math.floor((new Date() - lastUse) / (1000 * 60 * 60 * 24));
                message += `${index + 1}. **${ab.nomService}** - Non utilisé depuis ${daysSinceUse} jours\n   💸 Vous payez ${ab.prixMensuel}€/mois\n\n`;
            });

            message += "\n💡 **Conseil :** Pensez à annuler les abonnements non utilisés pour économiser !";
            return message;
        } catch (error) {
            return "Impossible de vérifier les alertes.";
        }
    }

    // Conseils d'optimisation
    async getOptimizationAdvice() {
        try {
            const response = await fetch('/api/abonnements');
            const abonnements = await response.json();
            
            const coutTotal = abonnements.reduce((sum, a) => sum + (a.prixMensuel || 0), 0);
            const alertes = abonnements.filter(a => {
                const lastUse = new Date(a.dernierUtilisation);
                const daysSinceUse = (new Date() - lastUse) / (1000 * 60 * 60 * 24);
                return daysSinceUse > 30;
            });

            const economiesPotentielles = alertes.reduce((sum, a) => sum + (a.prixMensuel || 0), 0);

            let advice = `💡 **Analyse de vos abonnements :**\n\n`;
            advice += `💰 Budget mensuel actuel : **${coutTotal.toFixed(2)}€**\n`;
            
            if (economiesPotentielles > 0) {
                advice += `💸 Économies potentielles : **${economiesPotentielles.toFixed(2)}€/mois** (${((economiesPotentielles / coutTotal) * 100).toFixed(0)}%)\n\n`;
                advice += `📝 **Mes recommandations :**\n`;
                advice += `1. Annulez ${alertes.length} abonnement(s) non utilisé(s)\n`;
                advice += `2. Économisez ${economiesPotentielles.toFixed(2)}€/mois = ${(economiesPotentielles * 12).toFixed(2)}€/an ! 🎯\n\n`;
            } else {
                advice += `\n✨ **Excellent !** Tous vos abonnements sont bien utilisés.\n\n`;
                advice += `📝 **Conseils généraux :**\n`;
                advice += `1. Vérifiez régulièrement vos alertes d'inactivité\n`;
                advice += `2. Comparez les prix avec la concurrence\n`;
                advice += `3. Profitez des offres famille/groupe quand possible\n`;
            }

            return advice;
        } catch (error) {
            return "Impossible de générer des conseils pour le moment.";
        }
    }

    // Recherche un abonnement
    async searchSubscription(message) {
        const entities = this.extractEntities(message);
        
        try {
            const response = await fetch('/api/abonnements');
            const abonnements = await response.json();
            
            let results = abonnements;
            
            if (entities.service) {
                results = results.filter(a => 
                    a.nomService.toLowerCase().includes(entities.service.toLowerCase())
                );
            }

            if (results.length === 0) {
                return `Je n'ai pas trouvé d'abonnement${entities.service ? ` pour "${entities.service}"` : ''}. Voulez-vous en ajouter un ?`;
            }

            let message = `🔍 **J'ai trouvé ${results.length} résultat(s) :**\n\n`;
            results.slice(0, 5).forEach((ab, index) => {
                message += `${index + 1}. **${ab.nomService}** (${ab.statut})\n`;
                message += `   👤 ${ab.clientName || 'N/A'}\n`;
                message += `   💰 ${ab.prixMensuel}€/mois\n`;
                message += `   📅 ${new Date(ab.dateDebut).toLocaleDateString()} → ${new Date(ab.dateFin).toLocaleDateString()}\n\n`;
            });

            return message;
        } catch (error) {
            return "Erreur lors de la recherche.";
        }
    }

    // Génère une réponse
    async generateResponse(userMessage) {
        // Sauvegarde dans l'historique
        this.context.conversationHistory.push({
            role: 'user',
            message: userMessage,
            timestamp: new Date()
        });

        // Détection d'actions CRUD avant l'intent général
        if (this.isAddCommand(userMessage)) {
            const response = await this.handleAddSubscription(userMessage);
            this.saveResponse(response);
            return response;
        }

        if (this.isDeleteCommand(userMessage)) {
            const response = await this.handleDeleteSubscription(userMessage);
            this.saveResponse(response);
            return response;
        }

        if (this.isUpdateCommand(userMessage)) {
            const response = await this.handleUpdateSubscription(userMessage);
            this.saveResponse(response);
            return response;
        }

        const intent = this.detectIntent(userMessage);
        const intentData = this.knowledgeBase[intent];

        let response;

        if (intentData) {
            if (typeof intentData.responses === 'function') {
                response = await intentData.responses(userMessage);
            } else {
                response = intentData.responses[Math.floor(Math.random() * intentData.responses.length)];
            }
        } else {
            // Réponses par défaut pour messages non compris
            const defaultResponses = [
                "Je ne suis pas sûr de comprendre. Pouvez-vous reformuler ? Tapez 'aide' pour voir ce que je peux faire.",
                "Hmm, je n'ai pas bien saisi. Essayez 'aide' pour découvrir mes fonctionnalités !",
                "Désolé, je ne comprends pas encore cette demande. Tapez 'aide' pour voir comment je peux vous aider."
            ];
            response = defaultResponses[Math.floor(Math.random() * defaultResponses.length)];
        }

        this.saveResponse(response);
        return response;
    }

    saveResponse(response) {
        this.context.conversationHistory.push({
            role: 'bot',
            message: response,
            timestamp: new Date()
        });
    }

    // Détecte si c'est une commande d'ajout
    isAddCommand(message) {
        const addKeywords = ['ajoute', 'créer', 'nouveau', 'enregistre', 'add', 'crée'];
        return addKeywords.some(keyword => message.toLowerCase().includes(keyword));
    }

    // Détecte si c'est une commande de suppression
    isDeleteCommand(message) {
        const deleteKeywords = ['supprime', 'efface', 'retire', 'delete', 'annule'];
        return deleteKeywords.some(keyword => message.toLowerCase().includes(keyword));
    }

    // Détecte si c'est une commande de mise à jour
    isUpdateCommand(message) {
        const updateKeywords = ['modifie', 'change', 'update', 'met à jour', 'édite'];
        return updateKeywords.some(keyword => message.toLowerCase().includes(keyword));
    }

    // Gère l'ajout d'un abonnement
    async handleAddSubscription(message) {
        const entities = this.extractEntities(message);

        // DEBUG: Afficher ce qui a été extrait
        console.log("🔍 Extraction:", entities);

        // Validation des données minimales
        if (!entities.service) {
            return `❌ Je n'ai pas compris le nom du service.\n\n🐛 Debug: service="${entities.service}", client="${entities.client}", prix="${entities.price}"`;
        }

        if (!entities.client) {
            return `❌ Je n'ai pas identifié le nom du client.\n\n🐛 Debug: service="${entities.service}", client="${entities.client}"`;
        }

        if (!entities.price) {
            return `❌ Je n'ai pas trouvé le prix.\n\n🐛 Debug: Trouvé prix="${entities.price}"`;
        }

        if (!entities.startDate) {
            // Date par défaut : aujourd'hui
            entities.startDate = new Date().toISOString();
        }

        if (!entities.endDate) {
            // Date de fin par défaut : +1 mois
            const endDate = new Date();
            endDate.setMonth(endDate.getMonth() + 1);
            entities.endDate = endDate.toISOString();
        }

        if (!entities.category) {
            entities.category = 'autre';
        }

        // Créer l'objet abonnement
        const newAbonnement = {
            nomService: entities.service,
            clientName: entities.client,
            prixMensuel: entities.price,
            dateDebut: entities.startDate,
            dateFin: entities.endDate,
            categorie: entities.category,
            derniereUtilisation: new Date().toISOString()
        };

        try {
            console.log("📤 Envoi:", newAbonnement);
            
            const response = await fetch('/api/abonnements', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(newAbonnement)
            });

            console.log("📥 Response status:", response.status);
            
            if (response.ok) {
                // Recharger la page pour voir le nouvel abonnement
                setTimeout(() => location.reload(), 1500);
                
                return `✅ **Abonnement ajouté avec succès !**

📋 **Service :** ${entities.service}
👤 **Client :** ${entities.client}
💰 **Prix :** ${entities.price}€/mois
📅 **Début :** ${new Date(entities.startDate).toLocaleDateString()}
📅 **Fin :** ${new Date(entities.endDate).toLocaleDateString()}
🏷️ **Catégorie :** ${entities.category}

La page va se rafraîchir dans un instant...`;
            } else {
                const errorText = await response.text();
                console.error("❌ Erreur serveur:", errorText);
                return `❌ Erreur ${response.status}: ${errorText}\n\n🐛 Données envoyées: ${JSON.stringify(newAbonnement, null, 2)}`;
            }
        } catch (error) {
            console.error("❌ Exception:", error);
            return `❌ Impossible de contacter le serveur.\n\n🐛 Erreur: ${error.message}`;
        }
    }

    // Gère la suppression d'un abonnement
    async handleDeleteSubscription(message) {
        const entities = this.extractEntities(message);

        if (!entities.service && !entities.number) {
            return "❌ Précisez quel abonnement supprimer. Ex: \"Supprime Netflix\" ou \"Supprime l'abonnement 1\"";
        }

        try {
            const response = await fetch('/api/abonnements');
            const abonnements = await response.json();

            let toDelete = null;
            let index = -1;

            if (entities.number !== null && entities.number > 0 && entities.number <= abonnements.length) {
                toDelete = abonnements[entities.number - 1];
                index = entities.number - 1;
            } else if (entities.service) {
                const found = abonnements.findIndex(a => 
                    a.nomService.toLowerCase().includes(entities.service.toLowerCase())
                );
                if (found !== -1) {
                    toDelete = abonnements[found];
                    index = found;
                }
            }

            if (!toDelete) {
                return `❌ Abonnement introuvable. Tapez "liste" pour voir vos abonnements.`;
            }

            const deleteResponse = await fetch(`/api/abonnements/${index}`, {
                method: 'DELETE'
            });

            if (deleteResponse.ok) {
                setTimeout(() => location.reload(), 1500);
                return `✅ **Abonnement supprimé !**\n\n🗑️ ${toDelete.nomService} (${toDelete.clientName}) - ${toDelete.prixMensuel}€/mois\n\nLa page va se rafraîchir...`;
            } else {
                return "❌ Erreur lors de la suppression.";
            }
        } catch (error) {
            return "❌ Impossible de supprimer l'abonnement.";
        }
    }

    // Gère la modification d'un abonnement
    async handleUpdateSubscription(message) {
        return "🔧 La modification est en cours de développement. Pour l'instant, supprimez et recréez l'abonnement.";
    }

    // Affiche les catégories disponibles
    async showCategories() {
        try {
            const response = await fetch('/api/abonnements');
            const abonnements = await response.json();
            
            const categoriesCount = {};
            abonnements.forEach(ab => {
                const cat = ab.categorie || 'autre';
                categoriesCount[cat] = (categoriesCount[cat] || 0) + 1;
            });

            let message = "📂 **Catégories disponibles :**\n\n";
            Object.entries(categoriesCount).forEach(([cat, count]) => {
                const emoji = this.getCategoryEmoji(cat);
                message += `${emoji} **${cat}** : ${count} abonnement(s)\n`;
            });

            message += "\n💡 Utilisez ces catégories lors de l'ajout : streaming, musique, sport, cloud, gaming, productivité";
            return message;
        } catch (error) {
            return "❌ Impossible de récupérer les catégories.";
        }
    }

    getCategoryEmoji(category) {
        const emojis = {
            'streaming': '📺',
            'musique': '🎵',
            'sport': '💪',
            'cloud': '☁️',
            'gaming': '🎮',
            'productivité': '💼',
            'autre': '📦'
        };
        return emojis[category.toLowerCase()] || '📦';
    }

    // Enregistre l'utilisation d'un abonnement
    async recordSubscriptionUsage(message) {
        const entities = this.extractEntities(message);

        if (!entities.service) {
            return "❌ Précisez quel abonnement vous avez utilisé. Ex: \"J'ai utilisé Netflix\"";
        }

        try {
            const response = await fetch('/api/abonnements');
            const abonnements = await response.json();

            const found = abonnements.findIndex(a => 
                a.nomService.toLowerCase().includes(entities.service.toLowerCase())
            );

            if (found === -1) {
                return `❌ Abonnement "${entities.service}" introuvable.`;
            }

            const abonnement = abonnements[found];
            abonnement.dernierUtilisation = new Date().toISOString();

            const updateResponse = await fetch(`/api/abonnements/${found}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(abonnement)
            });

            if (updateResponse.ok) {
                return `✅ **Utilisation enregistrée !**\n\n📝 ${abonnement.nomService}\n🕐 Dernière utilisation : ${new Date().toLocaleString()}\n\nVotre alerte d'inactivité a été réinitialisée.`;
            } else {
                return "❌ Erreur lors de l'enregistrement.";
            }
        } catch (error) {
            return "❌ Impossible d'enregistrer l'utilisation.";
        }
    }

    // Obtient des suggestions de questions
    getSuggestions() {
        return [
            "💰 Quel est mon budget mensuel ?",
            "⚠️ Mes alertes d'inactivité",
            "💡 Comment économiser ?",
            "📊 Analyse mes dépenses",
            "📋 Liste mes abonnements actifs",
            "➕ Ajoute Netflix pour Jean à 15€",
            "🗑️ Supprime Spotify"
        ];
    }

    // Réinitialise la conversation
    reset() {
        this.context = {
            lastQuestion: null,
            conversationHistory: [],
            userData: null,
            awaitingResponse: false
        };
    }
}

// Export pour utilisation
if (typeof module !== 'undefined' && module.exports) {
    module.exports = AbonnementChatbot;
}
