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
        analytics: '/api/analytics/optimize',
        forecast: '/api/analytics/forecast',
        anomalies: '/api/analytics/anomalies',
        duplicates: '/api/analytics/duplicates',
        monthlyReport: '/api/analytics/monthly-report',
        optimization: '/api/analytics/optimization',
        
        // Portfolio
        rebalance: '/api/portfolio/rebalance',
        lifecyclePlan: '/api/portfolio/lifecycle-plan',
        
        // Email & Notifications
        emailStatus: '/api/api/email/status',
        emailAlertExpiration: '/api/api/email/send-alert-expiration',
        emailRapport: '/api/api/email/send-rapport-mensuel',
        emailAlert: '/api/api/email/send-alerte-budget',
        
        // Currency
        currencyStatus: '/api/api/currency/status',
        currencyConvert: '/api/api/currency/convert',
        currencyToEur: '/api/api/currency/to-eur',
        currencyStability: '/api/api/currency/stabilite',

        // International pricing and payment simulation
        userCountry: '/api/user/country',
        convertAbonnements: '/api/convert/abonnements',
        paymentSimulate: '/api/payment/simulate',
        allCurrencies: '/api/get-all-currencies',
        bankImport: '/api/bank/import',
        openBankingImport: '/api/open-banking/import',
        openBankingAddDetected: '/api/open-banking/add-detected',

        // Sub-accounts and Stripe family billing
        subAccounts: '/api/users/subaccounts',
        subAccountPreferences: '/api/users/subaccounts',
        subAccountDashboard: '/api/users/subaccounts',
        stripeSubscription: '/api/stripe/subscription',
        stripePayments: '/api/stripe/payments'
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
