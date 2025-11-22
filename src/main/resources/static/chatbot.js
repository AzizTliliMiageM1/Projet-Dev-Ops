/**
 * Chatbot IA Intelligent pour Gestion d'Abonnements
 * Utilise une IA basée sur des patterns et du NLP simple
 */

class AbonnementChatbot {
    constructor() {
        this.context = {
            lastQuestion: null,
            conversationHistory: [],
            userData: null,
            awaitingResponse: false
        };
        
        this.initializeKnowledgeBase();
        this.initializeNLP();
    }

    initializeKnowledgeBase() {
        this.knowledgeBase = {
            greetings: {
                patterns: ['bonjour', 'salut', 'hello', 'hey', 'coucou', 'bonsoir'],
                responses: [
                    "Bonjour ! 👋 Je suis votre assistant intelligent pour gérer vos abonnements. Comment puis-je vous aider ?",
                    "Salut ! 😊 Je peux vous aider à gérer vos abonnements, analyser vos dépenses ou répondre à vos questions. Que souhaitez-vous faire ?",
                    "Hello ! Je suis là pour optimiser votre gestion d'abonnements. Posez-moi vos questions !"
                ]
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
📊 **Analyse** : "analyse mes dépenses", "montre mes stats"
➕ **Gestion** : "ajoute un abonnement", "supprime Netflix"
💡 **Conseils** : "comment économiser", "optimise mon budget"
🔍 **Recherche** : "cherche Spotify", "mes abonnements actifs"
📈 **Statistiques** : "quel est mon budget", "combien je dépense"
⚠️ **Alertes** : "mes alertes", "abonnements inutilisés"

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
• Alertes d'inactivité (>30 jours)
• Statistiques en temps réel
• Export/Import JSON
• Analytics avec graphiques
• API REST documentée
• Interface moderne et responsive

Quelle fonctionnalité vous intéresse ?`
                ]
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
            number: null
        };

        // Extract prix (15.99€, 15.99, 15€)
        const priceMatch = message.match(/(\d+(?:[.,]\d{1,2})?)\s*(?:€|euros?)?/i);
        if (priceMatch) {
            entities.price = parseFloat(priceMatch[1].replace(',', '.'));
        }

        // Extract nombres simples
        const numberMatch = message.match(/\b(\d+)\b/);
        if (numberMatch) {
            entities.number = parseInt(numberMatch[1]);
        }

        // Extract noms de services communs
        const services = ['netflix', 'spotify', 'disney', 'amazon', 'apple', 'youtube', 'basic fit', 'dropbox', 'google'];
        for (const service of services) {
            if (message.toLowerCase().includes(service)) {
                entities.service = service.charAt(0).toUpperCase() + service.slice(1);
                break;
            }
        }

        return entities;
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

        // Sauvegarde la réponse
        this.context.conversationHistory.push({
            role: 'assistant',
            message: response,
            timestamp: new Date()
        });

        return response;
    }

    // Obtient des suggestions de questions
    getSuggestions() {
        return [
            "💰 Quel est mon budget mensuel ?",
            "⚠️ Mes alertes d'inactivité",
            "💡 Comment économiser ?",
            "📊 Analyse mes dépenses",
            "📋 Liste mes abonnements actifs"
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
