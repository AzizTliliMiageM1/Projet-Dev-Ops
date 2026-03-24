/**
 * API Configuration Centralisée
 * Gère toutes les URLs de l'API
 */

const API_CONFIG = {
    // Base URL de l'API Spark
    BASE_URL: 'http://localhost:4567',
    
    // Endpoints d'authentification
    endpoints: {
        login: '/api/login',
        register: '/api/register',
        logout: '/api/logout',
        session: '/api/session',
        
        // Abonnements
        abonnements: '/api/abonnements',
        abonnementsImport: '/api/abonnements/import/csv',
        abonnementsExport: '/api/abonnements/export/csv',
        
        // Analytics
        analytics: '/analytics/optimize',
        forecast: '/analytics/forecast',
        anomalies: '/analytics/anomalies',
        duplicates: '/analytics/duplicates',
        monthlyReport: '/analytics/monthly-report',
        optimization: '/analytics/optimization',
        
        // Portfolio
        rebalance: '/portfolio/rebalance',
        lifecyclePlan: '/portfolio/lifecycle-plan',
        
        // Email & Notifications
        emailStatus: '/api/email/status',
        emailAlertExpiration: '/api/email/send-alert-expiration',
        emailRapport: '/api/email/send-rapport-mensuel',
        emailAlert: '/api/email/send-alerte-budget',
        
        // Currency
        currencyStatus: '/api/currency/status',
        currencyConvert: '/api/currency/convert',
        currencyToEur: '/api/currency/to-eur',
        currencyStability: '/api/currency/stabilite',

        // International pricing and payment simulation
        userCountry: '/api/user/country',
        convertAbonnements: '/api/convert/abonnements',
        paymentSimulate: '/api/payment/simulate',
        allCurrencies: '/api/get-all-currencies'
    },
    
    /**
     * Construit l'URL complète pour un endpoint
     * @param {string} endpoint - Le chemin de l'endpoint (ex: '/api/login')
     * @returns {string} - URL complète (ex: 'http://localhost:4567/api/login')
     */
    getUrl(endpoint) {
        return `${this.BASE_URL}${endpoint}`;
    },
    
    /**
     * Effectue un fetch avec gestion d'erreur centralisée
     * @param {string} endpoint - Le chemin de l'endpoint
     * @param {object} options - Options du fetch (method, body, headers, etc.)
     * @returns {Promise<object>} - Réponse JSON
     */
    async fetchJson(endpoint, options = {}) {
        const url = this.getUrl(endpoint);
        const defaultHeaders = {
            'Content-Type': 'application/json'
        };
        
        const config = {
            ...options,
            headers: {
                ...defaultHeaders,
                ...options.headers
            }
        };
        
        try {
            const response = await fetch(url, config);
            
            // Si la réponse n'est pas OK, récupérer l'erreur
            if (!response.ok) {
                const data = await response.json().catch(() => ({}));
                throw new Error(data.error || `HTTP ${response.status}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error(`API Error (${endpoint}):`, error);
            throw error;
        }
    }
};

// Export pour Node.js/CommonJS (si utilisé)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = API_CONFIG;
}
