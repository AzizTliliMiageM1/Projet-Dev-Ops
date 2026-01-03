/**
 * Chatbot IA Intelligent v2.0 - Version Avancée
 * Améliorations majeures :
 * - NLP avancé avec détection contextuelle
 * - Apprentissage utilisateur et profils personnalisés
 * - Tutoriels interactifs multi-étapes
 * - Recommandations intelligentes basées sur le comportement
 * - Gestion multi-turns des conversations
 * - Historique persistant en localStorage
 * - Mode débuggage détaillé
 * - Résilience et gestion d'erreurs robuste
 */

class AdvancedAbonnementChatbot {
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
            followUpMode: false,
            lastMessageTime: new Date(),
            sessionContext: {} // Contexte de la session courante
        };
        
        this.tutorialMode = false;
        this.tutorialStep = 0;
        this.multiTurnState = null; // Gestion conversations multi-tours
        this.messageQueue = []; // Queue pour messages
        
        this.initializeKnowledgeBase();
        this.initializeNLP();
        this.initializeTutorials();
        this.loadConversationHistory();
    }

    // ================================
    // 1. GESTION DU PROFIL UTILISATEUR
    // ================================

    loadUserProfile() {
        const saved = localStorage.getItem('chatbot_profile');
        if (saved) {
            try {
                return JSON.parse(saved);
            } catch (e) {
                console.warn('Profil corrompu, réinitialisation');
            }
        }

        return {
            userId: `user_${Date.now()}`,
            language: 'fr',
            preferences: {
                detailLevel: 'normal', // 'simple', 'normal', 'detailed'
                showEmojis: true,
                showCodeExamples: false,
                notificationFrequency: 'balanced' // 'rarely', 'balanced', 'frequent'
            },
            interactions: {
                totalMessages: 0,
                totalSessions: 1,
                favoriteTopics: {}, // {topic: count}
                lastTopics: [], // [topic1, topic2, ...]
                learnedPatterns: {}
            },
            preferences_learned: {
                likesTutorials: null,
                prefersShortAnswers: null,
                interestedInAdvancedFeatures: null,
                prefers: 'balanced'
            },
            stats: {
                addedSubscriptions: 0,
                deletedSubscriptions: 0,
                searchesPerformed: 0,
                adviceRequested: 0,
                lastActiveDate: new Date().toISOString()
            }
        };
    }

    saveUserProfile() {
        localStorage.setItem('chatbot_profile', JSON.stringify(this.context.userProfile));
    }

    loadConversationHistory() {
        const saved = localStorage.getItem('chatbot_history');
        if (saved) {
            try {
                const history = JSON.parse(saved);
                // Garder seulement les 50 derniers messages
                this.context.conversationHistory = history.slice(-50);
            } catch (e) {
                console.warn('Historique corrompu');
            }
        }
    }

    saveConversationHistory() {
        try {
            localStorage.setItem('chatbot_history', JSON.stringify(this.context.conversationHistory));
        } catch (e) {
            console.warn('Impossible de sauvegarder historique:', e);
        }
    }

    // ================================
    // 2. INITIALISATION NLP AVANCÉE
    // ================================

    initializeNLP() {
        this.stopWords = [
            'le', 'la', 'les', 'un', 'une', 'des', 'de', 'du', 'à', 'est', 'et', 'dans', 'pour',
            'mon', 'ma', 'mes', 'ton', 'ta', 'tes', 'son', 'sa', 'ses', 'notre', 'nos', 'votre', 'vos',
            'ce', 'cet', 'cette', 'ces', 'par', 'avec', 'sans', 'sur', 'sous', 'entre', 'chez', 'vers',
            'depuis', 'pendant', 'que', 'qui', 'ce', 'où', 'comment', 'combien', 'quel', 'quelle'
        ];

        this.synonyms = {
            'budget': ['dépense', 'coût', 'prix', 'tarif', 'montant', 'total'],
            'statistiques': ['stats', 'analyse', 'résumé', 'chiffres', 'données'],
            'supprimer': ['effacer', 'retirer', 'annuler', 'enregistrer', 'invalider'],
            'ajouter': ['créer', 'enregistrer', 'nouveau', 'ajoute', 'crée'],
            'chercher': ['recherche', 'trouve', 'localise', 'scan', 'filtre'],
            'alertes': ['warnings', 'notifications', 'rappels', 'inactivité'],
            'économiser': ['réduire', 'épargner', 'minimiser', 'baisser', 'optimiser']
        };

        this.entityPatterns = {
            price: /(\d+(?:[.,]\d{1,2})?)\s*(?:€|euros?|eur)/gi,
            date: /(\d{1,2})[\/\-](\d{1,2})[\/\-](\d{2,4})/g,
            email: /[\w.-]+@[\w.-]+\.\w+/g,
            percent: /(\d+)\s*%/g,
            number: /\b(\d+)\b/g,
            service: /(?:netflix|spotify|disney|amazon|prime|adobe|microsoft|apple|google|dropbox|notion|slack|zoom|figma|vimeo|twitch|hulu|apple\s+tv|dazn|nord\w*|express\w*)/gi
        };
    }

    // Tokenize avec gestion améliorée
    tokenize(text) {
        return text
            .toLowerCase()
            .normalize("NFD")
            .replace(/[\u0300-\u036f]/g, "")
            .replace(/[^\w\s]/g, ' ')
            .split(/\s+/)
            .filter(word => word.length > 1 && !this.stopWords.includes(word));
    }

    // Détection d'intentions améliorée avec scoring
    detectIntent(userMessage, threshold = 0.3) {
        const tokens = this.tokenize(userMessage);
        const lowerMsg = userMessage.toLowerCase();
        
        let bestMatch = { category: 'unknown', score: 0, confidence: 0 };
        const allMatches = [];

        for (const [category, data] of Object.entries(this.knowledgeBase)) {
            let score = 0;
            let patternMatches = 0;

            // Vérifier les patterns
            for (const pattern of data.patterns || []) {
                if (tokens.some(token => token.includes(pattern) || pattern.includes(token))) {
                    score += 2;
                    patternMatches++;
                }
                if (lowerMsg.includes(pattern)) {
                    score += 1;
                }
            }

            // Vérifier les synonymes
            for (const [primary, syns] of Object.entries(this.synonyms)) {
                if (lowerMsg.includes(primary)) score += 0.5;
                for (const syn of syns) {
                    if (tokens.includes(syn)) score += 0.3;
                }
            }

            const confidence = score / (data.patterns?.length || 1);
            
            if (score > 0) {
                allMatches.push({ category, score, confidence });
            }

            if (score > bestMatch.score) {
                bestMatch = { category, score, confidence };
            }
        }

        // Si confiance faible, chercher contexte de session précédente
        if (bestMatch.confidence < threshold && this.context.lastIntent) {
            if (this.isFollowUpQuestion(userMessage)) {
                bestMatch.category = this.context.lastIntent;
                bestMatch.isFollowUp = true;
            }
        }

        return bestMatch.category !== 'unknown' 
            ? bestMatch 
            : { category: 'unknown', score: 0, confidence: 0, allMatches };
    }

    // Détecte si c'est une question de suivi
    isFollowUpQuestion(message) {
        const followUpKeywords = [
            'oui', 'non', 'ok', 'merci', 'more', 'plus', 'détails', 'example', 'comme',
            'et', 'quoi', 'pourquoi', 'comment', 'lequel', 'laquelle', 'lesquels'
        ];
        return followUpKeywords.some(kw => message.toLowerCase().includes(kw));
    }

    // ================================
    // 3. EXTRACTION ENTITÉS AVANCÉE
    // ================================

    extractEntities(message) {
        const entities = {
            service: null,
            client: null,
            price: null,
            startDate: null,
            endDate: null,
            category: null,
            number: null,
            emails: [],
            percentages: [],
            knownServices: []
        };

        // Services connus
        const serviceMatches = message.match(this.entityPatterns.service);
        if (serviceMatches) {
            entities.knownServices = [...new Set(serviceMatches.map(s => s.toLowerCase()))];
        }

        // Prix
        const priceMatches = message.match(this.entityPatterns.price);
        if (priceMatches) {
            entities.price = parseFloat(priceMatches[0].replace(/[^\d.,]/g, '').replace(',', '.'));
        }

        // Dates
        const dateMatches = message.match(this.entityPatterns.date);
        if (dateMatches && dateMatches.length >= 1) {
            entities.startDate = this.parseDate(dateMatches[0]);
            if (dateMatches.length >= 2) {
                entities.endDate = this.parseDate(dateMatches[1]);
            }
        }

        // Emails
        const emailMatches = message.match(this.entityPatterns.email);
        if (emailMatches) {
            entities.emails = emailMatches;
        }

        // Pourcentages
        const percentMatches = message.match(this.entityPatterns.percent);
        if (percentMatches) {
            entities.percentages = percentMatches.map(p => parseInt(p));
        }

        // Catégorie
        const categories = ['streaming', 'musique', 'sport', 'cloud', 'gaming', 'productivité', 'autre', 'telecom', 'transport'];
        for (const cat of categories) {
            if (message.toLowerCase().includes(cat)) {
                entities.category = cat;
                break;
            }
        }

        // Client (pattern: "au nom de X" ou "pour X")
        const clientMatch = message.match(/(?:au nom de|pour)\s+([A-ZÀ-ÿ][a-zà-ÿ]+(?:\s+[A-ZÀ-ÿ][a-zà-ÿ]+)?)/i);
        if (clientMatch) {
            entities.client = clientMatch[1].trim();
        }

        // Service (après client ou après "ajoute")
        if (entities.client) {
            const afterClient = message.split(entities.client)[1];
            if (afterClient) {
                const serviceMatch = afterClient.match(/^\s+([A-Za-zÀ-ÿ0-9\s]+?)(?=\s+(?:début|fin|catégorie|prix|à|pour|€|euro|$))/i);
                if (serviceMatch) {
                    entities.service = serviceMatch[1].trim();
                }
            }
        }

        if (!entities.service && entities.knownServices.length > 0) {
            entities.service = entities.knownServices[0];
        }

        if (!entities.service) {
            const serviceMatch = message.match(/(?:ajoute|créer?)\s+(?:un\s+)?([A-Za-zÀ-ÿ0-9\s]+?)(?=\s+(?:pour|au nom de|début|à|$))/i);
            if (serviceMatch) {
                entities.service = serviceMatch[1].trim();
            }
        }

        // Nombre
        const numberMatch = message.match(this.entityPatterns.number);
        if (numberMatch) {
            entities.number = parseInt(numberMatch[0]);
        }

        return entities;
    }

    parseDate(dateStr) {
        const parts = dateStr.match(/(\d{1,2})[\/\-](\d{1,2})[\/\-](\d{2,4})/);
        if (parts) {
            let day = parseInt(parts[1]);
            let month = parseInt(parts[2]) - 1;
            let year = parseInt(parts[3]);
            
            // Ajuster année si siècle manquant
            if (year < 100) {
                year += year < 50 ? 2000 : 1900;
            }
            
            const date = new Date(year, month, day);
            return date.toISOString();
        }
        return null;
    }

    // ================================
    // 4. INITIALISATION TUTORIELS
    // ================================

    initializeTutorials() {
        this.tutorials = {
            quick_start: {
                title: "🚀 Démarrage Rapide",
                description: "Apprenez les bases en 2 minutes",
                steps: [
                    {
                        title: "Bienvenue !",
                        content: "Félicitations ! Je vais vous guider à travers les fonctionnalités principales.",
                        action: "Prêt ? Dites 'continuer'"
                    },
                    {
                        title: "Ajouter un abonnement",
                        content: "Deux options :\n1. Cliquez sur '➕ Ajouter'\n2. Tapez : 'Ajoute Netflix pour Jean à 15.99€'",
                        action: "Essayez d'en ajouter un"
                    },
                    {
                        title: "Voir vos abonnements",
                        content: "Tapez 'liste', 'mes abonnements' ou 'affiche tout'",
                        action: "Suivant..."
                    },
                    {
                        title: "Analyser votre budget",
                        content: "Posez-moi des questions comme :\n- Quel est mon budget ?\n- Combien je dépense ?\n- Mes alertes ?",
                        action: "Continuer..."
                    },
                    {
                        title: "🎉 Parfait !",
                        content: "Vous êtes maintenant prêt à utiliser toutes les fonctionnalités. Des questions ?",
                        action: "Terminé"
                    }
                ]
            },
            advanced: {
                title: "🔧 Fonctionnalités Avancées",
                description: "Maîtrisez les outils experts",
                steps: [
                    {
                        title: "Recherche Avancée",
                        content: "Utilisez 'Cherche Netflix' pour trouver rapidement",
                        action: "Suivant"
                    },
                    {
                        title: "Recommandations IA",
                        content: "Tapez 'Optimise mon budget' pour avoir des suggestions personnalisées",
                        action: "Suivant"
                    },
                    {
                        title: "Export/Import",
                        content: "Sauvegardez vos données avec 'Exporte tout' ou restaurez avec 'Importe'",
                        action: "Suivant"
                    },
                    {
                        title: "Filtrage Avancé",
                        content: "Filtrez par catégorie, date, ou prix avec des commandes comme 'Streaming moins de 20€'",
                        action: "Continuer"
                    }
                ]
            }
        };
    }

    // ================================
    // 5. INITIALISATION KNOWLEDGE BASE
    // ================================

    initializeKnowledgeBase() {
        this.knowledgeBase = {
            // ============ GREETINGS ============
            greetings: {
                patterns: ['bonjour', 'salut', 'hello', 'hey', 'coucou', 'bonsoir', 'yo', 'wesh'],
                responses: [
                    this.generateDynamicGreeting.bind(this)
                ]
            },

            // ============ HELP ============
            help: {
                patterns: ['aide', 'help', 'que peux-tu faire', 'commandes', 'fonctionnalités', 'utiliser'],
                responses: [
                    this.generateHelpResponse.bind(this)
                ]
            },

            // ============ TUTORIAL ============
            tutorial: {
                patterns: ['tutoriel', 'tutorial', 'apprendre', 'guide', 'comment utiliser'],
                responses: [
                    this.generateTutorialResponse.bind(this)
                ]
            },

            // ============ STATISTICS ============
            statistics: {
                patterns: ['statistique', 'stats', 'combien', 'total', 'dépense', 'budget', 'coût', 'analyse'],
                responses: [
                    this.getStatisticsResponse.bind(this)
                ]
            },

            // ============ SUBSCRIPTIONS ============
            subscriptions: {
                patterns: ['liste', 'mes abonnements', 'affiche', 'tous', 'voir', 'show'],
                responses: [
                    this.getSubscriptionsResponse.bind(this)
                ]
            },

            // ============ ALERTS ============
            alerts: {
                patterns: ['alerte', 'inactif', 'inutilisé', 'warning', 'danger', 'attention', 'risk'],
                responses: [
                    this.getAlertsResponse.bind(this)
                ]
            },

            // ============ OPTIMIZATION ============
            optimization: {
                patterns: ['économiser', 'optimiser', 'réduire', 'conseil', 'suggestion', 'améliorer', 'sauver'],
                responses: [
                    this.getOptimizationAdvice.bind(this)
                ]
            },

            // ============ SEARCH ============
            search: {
                patterns: ['cherche', 'trouve', 'recherche', 'search', 'localise', 'filtre'],
                responses: [
                    this.searchSubscription.bind(this)
                ]
            },

            // ============ ADD ============
            addSubscription: {
                patterns: ['ajoute', 'créer', 'nouveau', 'enregistre', 'add', 'crée'],
                responses: [
                    "Pour ajouter un abonnement, dites-moi :\n\n'**Ajoute** [service] **pour** [client] **à** [prix]€'\n\nExemple : 'Ajoute Netflix pour Jean à 15.99€'"
                ]
            },

            // ============ DELETE ============
            deleteSubscription: {
                patterns: ['supprime', 'efface', 'retire', 'delete', 'annule', 'enlève'],
                responses: [
                    "Pour supprimer, dites-moi le nom ou le numéro :\n\n'**Supprime** Netflix' ou '**Supprime** l\'abonnement 2'"
                ]
            },

            // ============ CATEGORIES ============
            categories: {
                patterns: ['catégorie', 'type', 'classification', 'genres'],
                responses: [
                    this.showCategories.bind(this)
                ]
            },

            // ============ EXPORT ============
            export: {
                patterns: ['exporte', 'export', 'télécharge', 'sauvegarde', 'backup'],
                responses: [
                    "📥 **Exporter vos données :**\n\nClic sur '💾 Exporter' en haut de la page pour télécharger en JSON"
                ]
            },

            // ============ IMPORT ============
            import: {
                patterns: ['importe', 'import', 'charge', 'restaure', 'upload'],
                responses: [
                    "📤 **Importer vos données :**\n\nClic sur '📂 Importer' en haut de la page et sélectionnez votre fichier JSON"
                ]
            },

            // ============ PRICING ============
            pricing: {
                patterns: ['prix', 'coût', 'tarif', 'gratuit', 'payant', 'frais'],
                responses: [
                    "💰 **Application gratuite !**\n\nCette application est 100% gratuite et open source. Aucun frais caché ! 🎉"
                ]
            },

            // ============ FALLBACK ============
            unknown: {
                patterns: [],
                responses: [
                    this.generateFallbackResponse.bind(this)
                ]
            }
        };
    }

    // ================================
    // 6. GÉNÉRATEURS DE RÉPONSES DYNAMIQUES
    // ================================

    generateDynamicGreeting() {
        const hour = new Date().getHours();
        let timeGreeting = '';
        
        if (hour < 12) timeGreeting = '🌅 Bonne matinée !';
        else if (hour < 18) timeGreeting = '☀️ Bon après-midi !';
        else timeGreeting = '🌙 Bonsoir !';

        const messageCount = this.context.userProfile.interactions.totalMessages;
        let personalGreeting = '';

        if (messageCount === 0) {
            personalGreeting = "Bienvenue pour la première fois ! 👋";
        } else if (messageCount < 5) {
            personalGreeting = "Content de vous revoir ! 😊";
        } else {
            personalGreeting = "Ravi de vous revoir ! Toujours là pour vous aider. 💪";
        }

        return `${timeGreeting} ${personalGreeting}\n\nQue puis-je faire pour vous ?`;
    }

    generateHelpResponse() {
        const detailLevel = this.context.userProfile.preferences.detailLevel;
        
        const shortHelp = `📚 **Commandes rapides :**\n
➕ Ajoute Netflix pour Jean à 15€
🗑️ Supprime Netflix
📋 Liste / Mes abonnements
💰 Budget / Mes dépenses
⚠️ Mes alertes
💡 Optimise mon budget
🔍 Cherche Spotify
📊 Analyse mes dépenses`;

        const detailedHelp = `📚 **Commandes disponibles :**

**📋 Gestion d'abonnements :**
• Ajoute [service] pour [client] à [prix]€
• Ajoute Basic Fit pour Marie à 20€ début 15/12/2024 fin 15/01/2025
• Supprime [service ou numéro]
• Liste / Mes abonnements / Affiche tout
• Cherche [service]

**💰 Finances :**
• Quel est mon budget ?
• Combien je dépense ?
• Mes alertes
• Optimise mon budget
• Comment économiser ?

**📊 Analytics :**
• Analyse mes dépenses
• Graphiques
• Statistiques

**⚙️ Paramètres :**
• Catégories
• Exporte mes données
• Importe mes données

**🎓 Aide :**
• Tutoriel / Guide rapide
• Fonctionnalités avancées
• Comment utiliser?`;

        return detailLevel === 'detailed' ? detailedHelp : shortHelp;
    }

    generateTutorialResponse() {
        const tutorials = Object.values(this.tutorials)
            .map((t, i) => `${i + 1}️⃣ ${t.title} - ${t.description}`)
            .join('\n');

        return `📖 **Tutoriels disponibles :**\n\n${tutorials}\n\nQuel tutoriel intéresse-vous ? (tapez le numéro)`;
    }

    generateFallbackResponse() {
        const message = this.context.conversationHistory[this.context.conversationHistory.length - 2]?.message?.toLowerCase() || '';
        
        // Répondre intelligemment selon le contexte
        if (message.includes('cher') || message.includes('prix') || message.includes('coût')) {
            return `💰 Je peux vous aider avec les tarifs ! Voulez-vous :\n1. Ajouter un abonnement\n2. Voir les coûts mensuels\n3. Chercher une réduction ?`;
        }
        
        if (message.includes('nom') || message.includes('service') || message.includes('quel')) {
            return `📱 Exemples de services : Netflix, Spotify, Adobe, Google Drive, Microsoft 365, Canva, ChatGPT Plus, etc.\n\nLequel vous intéresse ?`;
        }

        if (message.includes('nombre') || message.includes('combien') || message.includes('total')) {
            return `📊 Je peux vous afficher vos statistiques complètes ! Tapez "statistiques" ou "stats"`;
        }

        const suggestions = [
            "Je n'ai pas bien compris 🤔 Pouvez-vous reformuler ?\n\n💡 Essayez :\n- 'Ajoute [service]' pour un abonnement\n- 'Cherche [nom]' pour une recherche\n- 'Aide' pour les commandes",
            "Hmm, ce sujet ne m'est pas familier 📚\n\nVous pouvez me demander :\n- Mes statistiques\n- Ajouter un abonnement\n- Chercher un service\n- Conseils d'optimisation",
            "Désolé ! Je n'ai pas bien saisi 🤔\n\nCommandes disponibles :\n- 'Stats' ou 'Statistiques'\n- 'Ajoute [service]' pour un nouvel abonnement\n- 'Supprime [service]'\n- 'Cherche [service]'"
        ];

        return suggestions[Math.floor(Math.random() * suggestions.length)];
    }

    // ================================
    // 7. FONCTIONS API AVANCÉES
    // ================================

    async getStatisticsResponse() {
        try {
            const response = await fetch('/api/abonnements');
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            
            const abonnements = await response.json();
            
            const total = abonnements.length;
            const actifs = abonnements.filter(a => a.statut === 'actif').length;
            const coutTotal = abonnements.reduce((sum, a) => sum + (a.prixMensuel || 0), 0);
            
            const alertes = abonnements.filter(a => {
                if (!a.dernierUtilisation) return false;
                const lastUse = new Date(a.dernierUtilisation);
                const daysSinceUse = (new Date() - lastUse) / (1000 * 60 * 60 * 24);
                return daysSinceUse > 30;
            }).length;

            const detailLevel = this.context.userProfile.preferences.detailLevel;

            if (detailLevel === 'simple') {
                return `💳 Total: ${total} | ✅ Actifs: ${actifs} | 💰 ${coutTotal.toFixed(2)}€/mois | ⚠️ Alertes: ${alertes}`;
            }

            let response_msg = `📊 **Vos statistiques :**\n\n`;
            response_msg += `💳 **Total abonnements :** ${total}\n`;
            response_msg += `✅ **Actifs :** ${actifs}\n`;
            response_msg += `💰 **Coût mensuel :** ${coutTotal.toFixed(2)}€\n`;
            response_msg += `📈 **Coût annuel :** ${(coutTotal * 12).toFixed(2)}€\n`;
            response_msg += `⚠️ **Alertes inactivité :** ${alertes}\n`;
            
            if (alertes > 0) {
                const economiePotentielle = abonnements
                    .filter(a => {
                        if (!a.dernierUtilisation) return false;
                        const daysSinceUse = (new Date() - new Date(a.dernierUtilisation)) / (1000 * 60 * 60 * 24);
                        return daysSinceUse > 30;
                    })
                    .reduce((sum, a) => sum + (a.prixMensuel || 0), 0);
                
                response_msg += `\n💸 **Économies potentielles :** ${economiePotentielle.toFixed(2)}€/mois`;
            }

            return response_msg;
        } catch (error) {
            console.error('Erreur stats:', error);
            return `❌ Impossible de charger les statistiques. Vérifiez votre connexion.`;
        }
    }

    async getSubscriptionsResponse() {
        try {
            const response = await fetch('/api/abonnements');
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            
            const abonnements = await response.json();
            
            if (abonnements.length === 0) {
                return "📭 Vous n'avez aucun abonnement. Voulez-vous en ajouter un ?\n\nDites : 'Ajoute Netflix pour Jean à 15€'";
            }

            let message = `📋 **Vos ${abonnements.length} abonnements :**\n\n`;
            
            abonnements.forEach((ab, index) => {
                const status = ab.statut === 'actif' ? '✅' : '❌';
                message += `${index + 1}. ${status} **${ab.nomService}**\n`;
                message += `   👤 ${ab.clientName || 'N/A'} | 💰 ${ab.prixMensuel}€/mois\n`;
                message += `   📅 ${new Date(ab.dateDebut).toLocaleDateString()} → ${new Date(ab.dateFin).toLocaleDateString()}\n\n`;
            });

            return message;
        } catch (error) {
            console.error('Erreur abonnements:', error);
            return `❌ Impossible de charger vos abonnements.`;
        }
    }

    async getAlertsResponse() {
        try {
            const response = await fetch('/api/abonnements');
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            
            const abonnements = await response.json();
            
            const alertes = abonnements.filter(a => {
                if (!a.dernierUtilisation) return true;
                const lastUse = new Date(a.dernierUtilisation);
                const daysSinceUse = (new Date() - lastUse) / (1000 * 60 * 60 * 24);
                return daysSinceUse > 30;
            });

            if (alertes.length === 0) {
                return "✅ **Aucune alerte !** Tous vos abonnements sont utilisés régulièrement. Bravo ! 🎉";
            }

            let message = `⚠️ **${alertes.length} alerte(s) d'inactivité :**\n\n`;
            
            alertes.forEach((ab, index) => {
                const lastUse = ab.dernierUtilisation 
                    ? new Date(ab.dernierUtilisation)
                    : new Date(ab.dateDebut);
                const daysSinceUse = Math.floor((new Date() - lastUse) / (1000 * 60 * 60 * 24));
                
                message += `${index + 1}. 🔴 **${ab.nomService}**\n`;
                message += `   ⏰ Non utilisé depuis ${daysSinceUse} jours\n`;
                message += `   💸 Vous payez ${ab.prixMensuel}€/mois\n\n`;
            });

            message += "💡 **Conseil :** Pensez à annuler les abonnements non utilisés pour économiser !";
            return message;
        } catch (error) {
            console.error('Erreur alertes:', error);
            return `❌ Impossible de vérifier les alertes.`;
        }
    }

    async getOptimizationAdvice() {
        try {
            const response = await fetch('/api/abonnements');
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            
            const abonnements = await response.json();
            
            const coutTotal = abonnements.reduce((sum, a) => sum + (a.prixMensuel || 0), 0);
            
            const inactifs = abonnements.filter(a => {
                if (!a.dernierUtilisation) return true;
                const lastUse = new Date(a.dernierUtilisation);
                const daysSinceUse = (new Date() - lastUse) / (1000 * 60 * 60 * 24);
                return daysSinceUse > 30;
            });

            const economiesPotentielles = inactifs.reduce((sum, a) => sum + (a.prixMensuel || 0), 0);

            let advice = `💡 **Analyse personnalisée de vos abonnements :**\n\n`;
            advice += `💰 **Budget mensuel actuel :** ${coutTotal.toFixed(2)}€\n`;
            advice += `📈 **Budget annuel :** ${(coutTotal * 12).toFixed(2)}€\n\n`;

            if (economiesPotentielles > 0) {
                const savingsPercent = ((economiesPotentielles / coutTotal) * 100).toFixed(0);
                advice += `🎯 **Économies potentielles :** ${economiesPotentielles.toFixed(2)}€/mois (${savingsPercent}%)\n`;
                advice += `📊 **Soit :** ${(economiesPotentielles * 12).toFixed(2)}€/an !\n\n`;
                
                advice += `📝 **Recommandations :**\n`;
                advice += `1. ❌ Annulez ${inactifs.length} abonnement(s) non utilisé(s)\n`;
                advice += `2. 💰 Économisez ${economiesPotentielles.toFixed(2)}€/mois\n`;
                advice += `3. 📅 Régularité : Vérifiez chaque mois vos alertes\n`;
            } else {
                advice += `✨ **Excellent !** Tous vos abonnements sont bien utilisés.\n\n`;
                advice += `📝 **Conseils généraux :**\n`;
                advice += `1. Continuez à monitorer régulièrement\n`;
                advice += `2. Comparez les prix avec la concurrence\n`;
                advice += `3. Profitez des offres famille/groupe\n`;
                advice += `4. Utilisez les périodes de test gratuites\n`;
            }

            return advice;
        } catch (error) {
            console.error('Erreur conseils:', error);
            return `❌ Impossible de générer des conseils.`;
        }
    }

    async searchSubscription(message) {
        const entities = this.extractEntities(message);

        try {
            const response = await fetch('/api/abonnements');
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            
            const abonnements = await response.json();
            
            let results = abonnements;

            // Chercher par service
            if (entities.service) {
                results = results.filter(a => 
                    a.nomService.toLowerCase().includes(entities.service.toLowerCase())
                );
            }

            // Chercher par service connu
            if (results.length === 0 && entities.knownServices.length > 0) {
                results = abonnements.filter(a =>
                    entities.knownServices.some(s => 
                        a.nomService.toLowerCase().includes(s.toLowerCase())
                    )
                );
            }

            // Chercher par catégorie
            if (results.length === 0 && entities.category) {
                results = abonnements.filter(a => a.categorie === entities.category);
            }

            if (results.length === 0) {
                return `🔍 **Aucun résultat trouvé**${entities.service ? ` pour "${entities.service}"` : ''}.\n\nVoulez-vous en ajouter un ?`;
            }

            let search_result = `🔍 **J'ai trouvé ${results.length} résultat(s) :**\n\n`;
            results.slice(0, 5).forEach((ab, index) => {
                search_result += `${index + 1}. **${ab.nomService}** (${ab.statut})\n`;
                search_result += `   👤 ${ab.clientName || 'N/A'}\n`;
                search_result += `   💰 ${ab.prixMensuel}€/mois\n`;
                search_result += `   📅 ${new Date(ab.dateDebut).toLocaleDateString()} → ${new Date(ab.dateFin).toLocaleDateString()}\n\n`;
            });

            if (results.length > 5) {
                search_result += `... et ${results.length - 5} autre(s)`;
            }

            return search_result;
        } catch (error) {
            console.error('Erreur recherche:', error);
            return `❌ Erreur lors de la recherche.`;
        }
    }

    async showCategories() {
        try {
            const response = await fetch('/api/abonnements');
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            
            const abonnements = await response.json();
            
            const categoriesCount = {};
            abonnements.forEach(ab => {
                const cat = ab.categorie || 'autre';
                categoriesCount[cat] = (categoriesCount[cat] || 0) + 1;
            });

            let message = "📂 **Catégories :**\n\n";
            
            const categoryEmojis = {
                'streaming': '📺',
                'musique': '🎵',
                'sport': '💪',
                'cloud': '☁️',
                'gaming': '🎮',
                'productivité': '💼',
                'telecom': '📱',
                'transport': '🚗',
                'autre': '📦'
            };

            Object.entries(categoriesCount).forEach(([cat, count]) => {
                const emoji = categoryEmojis[cat] || '📦';
                message += `${emoji} **${cat}** : ${count} abonnement(s)\n`;
            });

            return message;
        } catch (error) {
            console.error('Erreur catégories:', error);
            return `❌ Impossible de charger les catégories.`;
        }
    }

    // ================================
    // 8. GESTION DES COMMANDES CRUD
    // ================================

    async handleAddSubscription(message) {
        // Gestion du dialogue multi-tours
        if (!this.multiTurnState) {
            // Premier appel - initialiser le state
            const entities = this.extractEntities(message);
            
            this.multiTurnState = {
                step: 'collecting',
                collected: {
                    service: entities.service,
                    client: entities.client,
                    price: entities.price,
                    category: entities.category || 'autre'
                },
                nextField: this.getNextMissingField(entities)
            };

            if (this.multiTurnState.nextField) {
                return this.askForField(this.multiTurnState.nextField);
            }
        } else {
            // Suite du dialogue - collecter le champ demandé
            const field = this.multiTurnState.nextField;
            
            if (field === 'service') {
                this.multiTurnState.collected.service = message.trim();
            } else if (field === 'client') {
                this.multiTurnState.collected.client = message.trim();
            } else if (field === 'price') {
                const priceMatch = message.match(/\d+(?:[.,]\d{1,2})?/);
                this.multiTurnState.collected.price = priceMatch ? parseFloat(priceMatch[0].replace(',', '.')) : null;
            } else if (field === 'category') {
                const categories = ['streaming', 'musique', 'sport', 'cloud', 'gaming', 'productivité', 'telecom', 'transport', 'autre'];
                const catMatch = message.toLowerCase();
                const cat = categories.find(c => catMatch.includes(c));
                this.multiTurnState.collected.category = cat || 'autre';
            }

            // Chercher le prochain champ manquant
            this.multiTurnState.nextField = this.getNextMissingField(this.multiTurnState.collected);

            if (this.multiTurnState.nextField) {
                return this.askForField(this.multiTurnState.nextField);
            }
        }

        // Tous les champs collectés - créer l'abonnement
        const entities = this.multiTurnState.collected;
        const multiTurnState = this.multiTurnState;
        this.multiTurnState = null; // Réinitialiser le state

        if (!entities.service || !entities.client || !entities.price) {
            return `❌ **Données invalides.** Veuillez recommencer.`;
        }

        const startDate = new Date().toISOString();
        const endDate = new Date();
        endDate.setMonth(endDate.getMonth() + 1);

        const newAbonnement = {
            nomService: entities.service,
            clientName: entities.client,
            prixMensuel: entities.price,
            dateDebut: startDate,
            dateFin: endDate.toISOString(),
            categorie: entities.category,
            derniereUtilisation: new Date().toISOString(),
            statut: 'actif'
        };

        try {
            const response = await fetch('/api/abonnements', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(newAbonnement)
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`${response.status}: ${errorText}`);
            }

            this.context.userProfile.stats.addedSubscriptions++;
            this.saveUserProfile();

            setTimeout(() => location.reload(), 1500);

            return `✅ **Abonnement ajouté !**\n\n📋 ${entities.service}\n👤 ${entities.client}\n💰 ${entities.price}€/mois\n📅 ${new Date(startDate).toLocaleDateString()} → ${new Date(endDate).toLocaleDateString()}`;
        } catch (error) {
            console.error('Erreur ajout:', error);
            this.multiTurnState = multiTurnState; // Restaurer le state en cas d'erreur
            return `❌ **Erreur lors de l'ajout :** ${error.message}`;
        }
    }

    getNextMissingField(entities) {
        if (!entities.service) return 'service';
        if (!entities.client) return 'client';
        if (!entities.price) return 'price';
        return null;
    }

    askForField(field) {
        const prompts = {
            service: "📱 **Quel service voulez-vous ajouter ?**\n\nExemples : Netflix, Spotify, Google Drive, Adobe Creative Cloud...",
            client: "👤 **Pour quel utilisateur ?**\n\nExemple : Jean, Marie, Papa...",
            price: "💰 **Quel est le prix mensuel ?**\n\nExemple : 15.99, 9.99€...",
            category: "📂 **Quelle catégorie ?**\n\nChoix : streaming, musique, sport, cloud, gaming, productivité, telecom, transport, autre"
        };
        return prompts[field] || "Veuillez compléter les informations.";
    }

    async handleDeleteSubscription(message) {
        const entities = this.extractEntities(message);

        if (!entities.service && !entities.number) {
            return "❌ Précisez quel abonnement supprimer. Ex: 'Supprime Netflix' ou 'Supprime l'abonnement 1'";
        }

        try {
            const response = await fetch('/api/abonnements');
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            
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
                return `❌ Abonnement introuvable.`;
            }

            const deleteResponse = await fetch(`/api/abonnements/${index}`, {
                method: 'DELETE'
            });

            if (!deleteResponse.ok) throw new Error('Deletion failed');

            this.context.userProfile.stats.deletedSubscriptions++;
            this.saveUserProfile();

            setTimeout(() => location.reload(), 1500);
            
            return `✅ **Abonnement supprimé !**\n\n🗑️ ${toDelete.nomService} - ${toDelete.prixMensuel}€/mois`;
        } catch (error) {
            console.error('Erreur suppression:', error);
            return `❌ Erreur lors de la suppression.`;
        }
    }

    // ================================
    // 9. RÉPONSE PRINCIPALE
    // ================================

    async generateResponse(userMessage) {
        if (!userMessage || userMessage.trim().length === 0) {
            return "Vous devez tapez quelque chose ! 😊";
        }

        // Enregistrer le message utilisateur
        this.context.conversationHistory.push({
            role: 'user',
            message: userMessage,
            timestamp: new Date()
        });

        // Update profil
        this.context.userProfile.interactions.totalMessages++;
        this.context.messageCount++;
        this.saveConversationHistory();
        this.saveUserProfile();

        try {
            let response;

            // Si on est en mode dialogue multi-tours pour ajouter un abonnement, continuer ce dialogue
            if (this.multiTurnState && this.multiTurnState.step === 'collecting') {
                response = await this.handleAddSubscription(userMessage);
            } else {
                // Détection d'intentions
                const intentDetection = this.detectIntent(userMessage);
                const intent = intentDetection.category;

                this.context.lastIntent = intent;

                // Traiter les commandes CRUD spéciales en premier
                if (this.isAddCommand(userMessage)) {
                    response = await this.handleAddSubscription(userMessage);
                } else if (this.isDeleteCommand(userMessage)) {
                    response = await this.handleDeleteSubscription(userMessage);
                } else if (intent in this.knowledgeBase) {
                    const intentData = this.knowledgeBase[intent];
                    
                    if (typeof intentData.responses[0] === 'function') {
                        response = await intentData.responses[0](userMessage);
                    } else {
                        response = intentData.responses[Math.floor(Math.random() * intentData.responses.length)];
                    }
                } else {
                    response = await this.generateFallbackResponse();
                }
            }

            // Enregistrer la réponse
            this.context.conversationHistory.push({
                role: 'bot',
                message: response,
                timestamp: new Date()
            });

            this.saveConversationHistory();

            return response;
        } catch (error) {
            console.error('Erreur générale:', error);
            return `❌ **Erreur système :** Je n'ai pas pu traiter votre demande. Réessayez !`;
        }
    }

    // ================================
    // 10. UTILITAIRES
    // ================================

    isAddCommand(message) {
        const keywords = ['ajoute', 'créer', 'nouveau', 'enregistre', 'add', 'crée'];
        return keywords.some(kw => message.toLowerCase().includes(kw));
    }

    isDeleteCommand(message) {
        const keywords = ['supprime', 'efface', 'retire', 'delete', 'annule', 'enlève'];
        return keywords.some(kw => message.toLowerCase().includes(kw));
    }

    reset() {
        this.context = {
            ...this.context,
            conversationHistory: [],
            messageCount: 0
        };
        this.saveConversationHistory();
    }
}

// Export
if (typeof module !== 'undefined' && module.exports) {
    module.exports = AdvancedAbonnementChatbot;
}
