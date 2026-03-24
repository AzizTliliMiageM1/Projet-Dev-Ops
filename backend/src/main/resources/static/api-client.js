/**
 * 🎯 API CLIENT UNIFIÉ - V2
 * Gère toutes les interactions avec le backend
 * Centralise les appels et interconnecte les APIs externes
 */

const APIClient = {
    // ==================== CONFIGURATION ====================
    BASE_URL: 'http://localhost:4567',
    timeout: 5000,
    cache: new Map(),
    cacheExpiry: 5 * 60 * 1000, // 5 min
    
    // ==================== STATE GLOBAL ====================
    user: null,
    isAuthenticated: false,
    lastError: null,
    
    // ==================== INITIALISATION ====================
    async init() {
        try {
            const session = await this.session.check();
            if (session.authenticated) {
                this.user = session;
                this.isAuthenticated = true;
                console.log('✅ APIClient initialisé - Utilisateur:', session.email);
            }
        } catch (e) {
            console.warn('ℹ️ APIClient - Mode anonyme');
            this.isAuthenticated = false;
        }
    },
    
    // ==================== AUTHENTIFICATION ====================
    session: {
        async check() {
            return APIClient.get('/api/session');
        },
        async login(email, password) {
            return APIClient.post('/api/login', { email, password });
        },
        async logout() {
            return APIClient.post('/api/logout', {});
        },
        async register(email, password, pseudo) {
            return APIClient.post('/api/register', { email, password, pseudo });
        }
    },
    
    // ==================== ABONNEMENTS (CRUD) ====================
    abonnements: {
        async getAll() {
            return APIClient.get('/api/abonnements', { cache: true, ttl: 3 * 60 * 1000 });
        },
        async getOne(id) {
            return APIClient.get(`/api/abonnements/${id}`);
        },
        async create(data) {
            const result = await APIClient.post('/api/abonnements', data);
            APIClient.cache.delete('/api/abonnements');
            return result;
        },
        async update(id, data) {
            const result = await APIClient.put(`/api/abonnements/${id}`, data);
            APIClient.cache.delete('/api/abonnements');
            return result;
        },
        async delete(id) {
            const result = await APIClient.delete(`/api/abonnements/${id}`);
            APIClient.cache.delete('/api/abonnements');
            return result;
        },
        async importCSV(file) {
            const formData = new FormData();
            formData.append('file', file);
            return APIClient.postFormData('/api/abonnements/import/csv', formData);
        },
        async exportCSV() {
            return APIClient.get('/api/abonnements/export/csv', { responseType: 'blob' });
        },
        async exportJSON() {
            return APIClient.get('/api/abonnements/export/json', { responseType: 'json' });
        }
    },
    
    // ==================== ANALYTICS - STATISTIQUES ====================
    analytics: {
        async getStats() {
            return APIClient.get('/api/analytics/stats', { cache: true, ttl: 2 * 60 * 1000 });
        },
        async getForecast(months = 6) {
            return APIClient.get(`/api/analytics/forecast?months=${months}`, { cache: true });
        },
        async getAnomalies() {
            return APIClient.get('/api/analytics/anomalies', { cache: true });
        },
        async getOptimization() {
            return APIClient.get('/api/analytics/optimize');
        },
        async getDuplicates() {
            return APIClient.get('/api/analytics/duplicates');
        },
        async getSeasonalPatterns() {
            return APIClient.get('/api/analytics/seasonal-patterns', { cache: true });
        },
        async getPortfolioHealth() {
            return APIClient.get('/api/analytics/portfolio-health', { cache: true });
        },
        async getExpiringSubscriptions() {
            return APIClient.get('/api/analytics/expiring', { cache: true });
        },
        async getROI() {
            return APIClient.get('/api/analytics/roi', { cache: true });
        },
        async getSavingsOpportunities() {
            return APIClient.get('/api/analytics/savings', { cache: true });
        },
        async getSpendingTrend(period = 'monthly') {
            return APIClient.get(`/api/analytics/spending-trend?period=${period}`, { cache: true });
        },
        async getTopPriorities() {
            return APIClient.get('/api/analytics/top-priority');
        },
        async getLifecycleAnalysis() {
            return APIClient.get('/api/analytics/lifecycle', { cache: true });
        },
        async getClustering() {
            return APIClient.get('/api/analytics/clustering', { cache: true });
        },
        async getMonthlyReport() {
            return APIClient.get('/api/analytics/monthly-report', { cache: true });
        }
    },
    
    // ==================== PORTFOLIO & LIFECYCLE ====================
    portfolio: {
        async rebalance() {
            return APIClient.post('/api/portfolio/rebalance', {});
        },
        async getLifecyclePlan() {
            return APIClient.get('/api/portfolio/lifecycle-plan', { cache: true });
        }
    },
    
    // ==================== EMAIL & NOTIFICATIONS ====================
    notifications: {
        async sendTest(email) {
            return APIClient.post('/api/notifications/test', { email });
        },
        async sendExpirationAlert(subscriptionId, email) {
            return APIClient.post('/api/email/send-alert-expiration', { subscriptionId, email });
        },
        async sendMonthlyReport(email) {
            return APIClient.post('/api/email/send-rapport-mensuel', { email });
        },
        async sendBudgetAlert(email, budgetExceeded) {
            return APIClient.post('/api/email/send-alerte-budget', { email, budgetExceeded });
        },
        async checkEmailStatus() {
            return APIClient.get('/api/email/status');
        }
    },
    
    // ==================== DEVISE (CURRENCY) ====================
    currency: {
        async checkStatus() {
            return APIClient.get('/api/currency/status', { cache: true, ttl: 60 * 60 * 1000 }); // 1h
        },
        async convert(amount, fromCurrency, toCurrency = 'EUR') {
            return APIClient.get('/api/currency/convert', {
                params: { amount, from: fromCurrency, to: toCurrency },
                cache: true
            });
        },
        async toEUR(amount, currency = 'USD') {
            return APIClient.get('/api/currency/to-eur', {
                params: { amount, currency },
                cache: true
            });
        },
        async getStability() {
            return APIClient.get('/api/currency/stabilite', { cache: true, ttl: 60 * 60 * 1000 });
        }
    },

    // ==================== OPEN BANKING - DÉTECTION ABONNEMENTS ====================
    openBanking: {
        /**
         * Importer et analyser un fichier CSV bancaire
         * Détecte automatiquement tous les abonnements récurrents
         */
        async importBankStatement(csvContent) {
            try {
                const response = await fetch(`${APIClient.BASE_URL}/api/open-banking/import`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'text/plain' },
                    body: csvContent,
                    credentials: 'include'
                });
                if (!response.ok) throw new Error('Import failed');
                return await response.json();
            } catch (error) {
                console.error('Erreur importBankStatement:', error);
                throw error;
            }
        },

        /**
         * Enregistrer un abonnement détecté depuis le banking
         */
        async addDetectedSubscription(subscriptionData) {
            try {
                return await APIClient.post('/api/open-banking/add-detected', subscriptionData);
            } catch (error) {
                console.error('Erreur addDetectedSubscription:', error);
                throw error;
            }
        },

        /**
         * Obtenir l'historique des imports bancaires
         */
        async getImportHistory() {
            return APIClient.get('/api/open-banking/history', { cache: false });
        },

        /**
         * Analyser les dépenses récurrentes depuis les transactions
         */
        async analyzeRecurringTransactions(dateFrom, dateTo) {
            return APIClient.get('/api/open-banking/analyze', {
                params: { from: dateFrom, to: dateTo }
            });
        }
    },

    // ==================== BANK IMPORT - CSV/PDF ====================
    bank: {
        /**
         * Importer un relevé bancaire (CSV ou PDF) via un endpoint unique.
         */
        async importFile(file) {
            if (!(file instanceof File)) {
                throw new Error('Fichier invalide');
            }

            const headers = {
                'Content-Type': file.type || 'application/octet-stream',
                'X-File-Name': file.name || 'statement'
            };

            return APIClient._fetch(`${APIClient.BASE_URL}/api/bank/import`, {
                method: 'POST',
                headers,
                body: file,
                credentials: 'include'
            });
        }
    },
    
    // ==================== MÉTHODES HTTP DE BASE ====================
    async get(endpoint, options = {}) {
        // Vérifier le cache
        if (options.cache) {
            const cached = this.getFromCache(endpoint);
            if (cached) return cached;
        }
        
        const url = new URL(this.BASE_URL + endpoint);
        if (options.params) {
            Object.entries(options.params).forEach(([key, value]) => {
                url.searchParams.append(key, value);
            });
        }
        
        const response = await this._fetch(url.toString(), {
            method: 'GET',
            headers: this._getHeaders(options)
        });
        
        if (options.cache) {
            this.setCache(endpoint, response, options.ttl);
        }
        
        return response;
    },
    
    async post(endpoint, data, options = {}) {
        return this._fetch(this.BASE_URL + endpoint, {
            method: 'POST',
            headers: this._getHeaders(options),
            body: JSON.stringify(data)
        });
    },
    
    async put(endpoint, data, options = {}) {
        return this._fetch(this.BASE_URL + endpoint, {
            method: 'PUT',
            headers: this._getHeaders(options),
            body: JSON.stringify(data)
        });
    },
    
    async delete(endpoint, options = {}) {
        return this._fetch(this.BASE_URL + endpoint, {
            method: 'DELETE',
            headers: this._getHeaders(options)
        });
    },
    
    async postFormData(endpoint, formData, options = {}) {
        return this._fetch(this.BASE_URL + endpoint, {
            method: 'POST',
            body: formData,
            headers: this._getHeaders(options, false)
        });
    },
    
    // ==================== HELPERS INTERNES ====================
    _getHeaders(options = {}, includeJson = true) {
        const headers = {
            ...(includeJson && { 'Content-Type': 'application/json' })
        };
        
        // Ajouter headers supplémentaires si fournis
        if (options.headers) {
            Object.assign(headers, options.headers);
        }
        
        return headers;
    },
    
    async _fetch(url, config) {
        try {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), this.timeout);
            
            const response = await fetch(url, {
                ...config,
                signal: controller.signal,
                credentials: 'include'
            });
            
            clearTimeout(timeoutId);
            
            // Gestion des erreurs HTTP
            if (!response.ok) {
                const error = await response.text();
                throw new Error(`HTTP ${response.status}: ${error}`);
            }
            
            // Déterminer le type de réponse
            const contentType = response.headers.get('content-type');
            if (contentType?.includes('application/json')) {
                return await response.json();
            } else if (contentType?.includes('text/csv')) {
                return await response.blob();
            } else {
                return await response.text();
            }
        } catch (error) {
            this.lastError = error.message;
            console.error('❌ APIClient Error:', error);
            throw error;
        }
    },
    
    // ==================== GESTION DU CACHE ====================
    getFromCache(key) {
        const cached = this.cache.get(key);
        if (!cached) return null;
        
        if (Date.now() > cached.expiry) {
            this.cache.delete(key);
            return null;
        }
        
        return cached.data;
    },
    
    setCache(key, data, ttl = 5 * 60 * 1000) {
        this.cache.set(key, {
            data,
            expiry: Date.now() + ttl
        });
    },
    
    clearCache() {
        this.cache.clear();
    },
    
    // ==================== INTERCONNEXION DES APIs ====================
    /**
     * Récupère tous les insights du tableau de bord
     * Combine analytics, portfolio, notifications
     */
    async getDashboardInsights() {
        try {
            const [stats, abos, forecast, health, savings] = await Promise.all([
                this.analytics.getStats(),
                this.abonnements.getAll(),
                this.analytics.getForecast(12),
                this.analytics.getPortfolioHealth(),
                this.analytics.getSavingsOpportunities()
            ]);
            
            return {
                stats,
                abonnements: abos,
                forecast,
                health,
                savings,
                timestamp: new Date()
            };
        } catch (error) {
            console.error('Erreur getDashboardInsights:', error);
            throw error;
        }
    },
    
    /**
     * Génère un rapport complet d'optimisation
     * Utilise analytics + portfolio + email
     */
    async generateOptimizationReport(email) {
        try {
            const [optimization, anomalies, duplicates, expiring] = await Promise.all([
                this.analytics.getOptimization(),
                this.analytics.getAnomalies(),
                this.analytics.getDuplicates(),
                this.analytics.getExpiringSubscriptions()
            ]);
            
            const report = {
                optimization,
                anomalies,
                duplicates,
                expiring,
                generatedAt: new Date()
            };
            
            // Envoyer le rapport par email
            if (email) {
                await this.notifications.sendMonthlyReport(email);
            }
            
            return report;
        } catch (error) {
            console.error('Erreur generateOptimizationReport:', error);
            throw error;
        }
    },
    
    /**
     * Lance un rebalancement complet du portefeuille
     * Rebalance + lifecycle + notifications
     */
    async rebalancePortfolio(email) {
        try {
            const [rebalance, lifecycle] = await Promise.all([
                this.portfolio.rebalance(),
                this.portfolio.getLifecyclePlan()
            ]);
            
            const result = {
                rebalance,
                lifecycle,
                executed: true,
                timestamp: new Date()
            };
            
            // Notifier l'utilisateur
            if (email) {
                await this.notifications.sendTest(email);
            }
            
            return result;
        } catch (error) {
            console.error('Erreur rebalancePortfolio:', error);
            throw error;
        }
    },
    
    /**
     * Analyse complète du budget et devise
     * Analytics + currency conversion
     */
    async analyzeBudgetMultiCurrency(targetCurrency = 'EUR') {
        try {
            const stats = await this.analytics.getStats();
            const spending = await this.analytics.getSpendingTrend();
            const currencyStatus = await this.currency.checkStatus();
            
            // Si conversions nécessaires
            const converted = {};
            for (const [curr, amount] of Object.entries(stats.byCurrency || {})) {
                if (curr !== targetCurrency) {
                    converted[curr] = await this.currency.convert(amount, curr, targetCurrency);
                }
            }
            
            return {
                original: stats,
                spending,
                currencyStatus,
                converted,
                targetCurrency
            };
        } catch (error) {
            console.error('Erreur analyzeBudgetMultiCurrency:', error);
            throw error;
        }
    }
};

// Initialiser le client au chargement
document.addEventListener('DOMContentLoaded', () => {
    APIClient.init();
});

// Exporter pour utilisation
if (typeof window !== 'undefined') {
    window.APIClient = APIClient;
}
if (typeof module !== 'undefined' && module.exports) {
    module.exports = APIClient;
}
