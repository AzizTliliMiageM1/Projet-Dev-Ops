/**
 * Application Dashboard - Version Améliorée v2.0
 * Gestion complète des abonnements avec :
 * - API robuste avec gestion d'erreurs
 * - Cache et optimisations
 * - Validation des données
 * - State management
 * - Logging et debugging
 */

// ==============================
// 1. CONFIGURATION GLOBALE
// ==============================

const API_CONFIG = {
    BASE_URL: '/api/abonnements',
    TIMEOUT: 10000,
    RETRY_ATTEMPTS: 3,
    RETRY_DELAY: 1000
};

const CACHE_CONFIG = {
    SUBSCRIPTIONS_KEY: 'subscriptions_cache',
    THEME_KEY: 'dashboard_theme',
    FAVORITES_KEY: 'abonnements_favoris',
    CACHE_DURATION: 5 * 60 * 1000 // 5 minutes
};

// ==============================
// 2. STATE MANAGEMENT
// ==============================

const AppState = {
    subscriptions: [],
    filteredSubscriptions: [],
    filters: {
        category: null,
        status: null,
        search: '',
        sortBy: 'name'
    },
    loading: false,
    error: null,
    lastUpdated: null,
    isDarkMode: false,
    favorites: [],

    // Sauvegarde l'état
    save() {
        try {
            localStorage.setItem('app_state', JSON.stringify({
                filters: this.filters,
                favorites: this.favorites,
                isDarkMode: this.isDarkMode
            }));
        } catch (e) {
            console.warn('Impossible de sauvegarder l\'état:', e);
        }
    },

    // Charge l'état
    load() {
        try {
            const saved = localStorage.getItem('app_state');
            if (saved) {
                const data = JSON.parse(saved);
                this.filters = { ...this.filters, ...data.filters };
                this.favorites = data.favorites || [];
                this.isDarkMode = data.isDarkMode || false;
            }
        } catch (e) {
            console.warn('Impossible de charger l\'état:', e);
        }
    },

    // Réinitialise l'état
    reset() {
        this.subscriptions = [];
        this.filteredSubscriptions = [];
        this.filters = {
            category: null,
            status: null,
            search: '',
            sortBy: 'name'
        };
        this.error = null;
        this.lastUpdated = null;
    }
};

// ==============================
// 3. API MANAGER
// ==============================

class APIManager {
    static async fetchWithRetry(url, options = {}, retries = API_CONFIG.RETRY_ATTEMPTS) {
        try {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), API_CONFIG.TIMEOUT);

            const response = await fetch(url, {
                ...options,
                signal: controller.signal
            });

            clearTimeout(timeoutId);

            if (!response.ok) {
                if (response.status === 404) {
                    throw new Error('Ressource non trouvée');
                }
                throw new Error(`HTTP ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            if (retries > 0 && error.name !== 'AbortError') {
                console.log(`Nouvelle tentative... (${retries} restantes)`);
                await new Promise(resolve => setTimeout(resolve, API_CONFIG.RETRY_DELAY));
                return this.fetchWithRetry(url, options, retries - 1);
            }
            throw error;
        }
    }

    static async getAll() {
        try {
            AppState.loading = true;
            const data = await this.fetchWithRetry(API_CONFIG.BASE_URL);
            AppState.subscriptions = data || [];
            AppState.lastUpdated = new Date();
            AppState.error = null;
            return data;
        } catch (error) {
            AppState.error = `Impossible de charger les abonnements: ${error.message}`;
            console.error('Erreur API getAll:', error);
            throw error;
        } finally {
            AppState.loading = false;
        }
    }

    static async getById(id) {
        try {
            return await this.fetchWithRetry(`${API_CONFIG.BASE_URL}/${id}`);
        } catch (error) {
            console.error(`Erreur API getById(${id}):`, error);
            throw error;
        }
    }

    static async create(data) {
        try {
            // Validation
            this.validateSubscription(data);

            const response = await this.fetchWithRetry(API_CONFIG.BASE_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });

            AppState.subscriptions.push(response);
            return response;
        } catch (error) {
            console.error('Erreur API create:', error);
            throw error;
        }
    }

    static async update(id, data) {
        try {
            this.validateSubscription(data);

            const response = await this.fetchWithRetry(`${API_CONFIG.BASE_URL}/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });

            const index = AppState.subscriptions.findIndex(s => s.id === id);
            if (index !== -1) {
                AppState.subscriptions[index] = response;
            }
            return response;
        } catch (error) {
            console.error(`Erreur API update(${id}):`, error);
            throw error;
        }
    }

    static async delete(id) {
        try {
            await this.fetchWithRetry(`${API_CONFIG.BASE_URL}/${id}`, {
                method: 'DELETE'
            });

            AppState.subscriptions = AppState.subscriptions.filter(s => s.id !== id);
        } catch (error) {
            console.error(`Erreur API delete(${id}):`, error);
            throw error;
        }
    }

    static validateSubscription(data) {
        const required = ['nomService', 'prixMensuel', 'dateDebut', 'dateFin'];
        for (const field of required) {
            if (!data[field]) {
                throw new Error(`Champ manquant: ${field}`);
            }
        }

        if (isNaN(parseFloat(data.prixMensuel)) || data.prixMensuel < 0) {
            throw new Error('Prix invalide');
        }

        const start = new Date(data.dateDebut);
        const end = new Date(data.dateFin);
        if (start >= end) {
            throw new Error('Date de fin doit être après la date de début');
        }
    }
}

// ==============================
// 4. UTILITAIRES
// ==============================

class Utils {
    static escapeHtml(text) {
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#39;'
        };
        return String(text || '').replace(/[&<>"']/g, m => map[m]);
    }

    static formatDate(date) {
        if (!date) return 'N/A';
        const d = new Date(date);
        return d.toLocaleDateString('fr-FR', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    }

    static formatPrice(price) {
        return new Intl.NumberFormat('fr-FR', {
            style: 'currency',
            currency: 'EUR'
        }).format(price);
    }

    static getDaysBetween(date1, date2) {
        const d1 = new Date(date1);
        const d2 = new Date(date2);
        const diff = Math.floor((d2 - d1) / (1000 * 60 * 60 * 24));
        return diff;
    }

    static isActive(subscription) {
        const now = new Date();
        const start = new Date(subscription.dateDebut);
        const end = new Date(subscription.dateFin);
        return now >= start && now <= end;
    }

    static isInactive(subscription) {
        if (!subscription.derniereUtilisation) return true;
        const daysSince = this.getDaysBetween(new Date(subscription.derniereUtilisation), new Date());
        return daysSince > 30;
    }

    static getCategoryEmoji(category) {
        const emojis = {
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
        return emojis[category] || '📦';
    }
}

// ==============================
// 5. GESTION THÈME
// ==============================

class ThemeManager {
    static init() {
        const isDark = localStorage.getItem(CACHE_CONFIG.THEME_KEY) === 'dark' ||
                      (window.matchMedia('(prefers-color-scheme: dark)').matches);
        AppState.isDarkMode = isDark;
        this.apply();

        // Écouter les changements de thème du système
        window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e => {
            AppState.isDarkMode = e.matches;
            this.apply();
        });
    }

    static apply() {
        if (AppState.isDarkMode) {
            document.documentElement.setAttribute('data-theme', 'dark');
            document.body.classList.add('dark-mode');
        } else {
            document.documentElement.removeAttribute('data-theme');
            document.body.classList.remove('dark-mode');
        }

        const btn = document.getElementById('toggleTheme');
        if (btn) {
            btn.innerHTML = AppState.isDarkMode 
                ? '<i class="bi bi-sun"></i>'
                : '<i class="bi bi-moon-stars"></i>';
        }

        localStorage.setItem(CACHE_CONFIG.THEME_KEY, AppState.isDarkMode ? 'dark' : 'light');
    }

    static toggle() {
        AppState.isDarkMode = !AppState.isDarkMode;
        this.apply();
    }
}

// ==============================
// 6. FILTERING & SEARCHING
// ==============================

class FilterManager {
    static applyFilters() {
        let filtered = [...AppState.subscriptions];

        // Filtre par catégorie
        if (AppState.filters.category) {
            filtered = filtered.filter(s => s.categorie === AppState.filters.category);
        }

        // Filtre par statut
        if (AppState.filters.status) {
            if (AppState.filters.status === 'actif') {
                filtered = filtered.filter(s => Utils.isActive(s));
            } else if (AppState.filters.status === 'inactif') {
                filtered = filtered.filter(s => Utils.isInactive(s));
            }
        }

        // Recherche
        if (AppState.filters.search) {
            const query = AppState.filters.search.toLowerCase();
            filtered = filtered.filter(s =>
                s.nomService.toLowerCase().includes(query) ||
                (s.clientName && s.clientName.toLowerCase().includes(query))
            );
        }

        // Tri
        filtered = this.sortSubscriptions(filtered);

        AppState.filteredSubscriptions = filtered;
        return filtered;
    }

    static sortSubscriptions(subscriptions) {
        const sorted = [...subscriptions];
        switch (AppState.filters.sortBy) {
            case 'price_low':
                sorted.sort((a, b) => a.prixMensuel - b.prixMensuel);
                break;
            case 'price_high':
                sorted.sort((a, b) => b.prixMensuel - a.prixMensuel);
                break;
            case 'date_end':
                sorted.sort((a, b) => new Date(a.dateFin) - new Date(b.dateFin));
                break;
            case 'name':
            default:
                sorted.sort((a, b) => a.nomService.localeCompare(b.nomService));
        }
        return sorted;
    }

    static setFilter(filterName, value) {
        AppState.filters[filterName] = value;
        AppState.save();
    }

    static clearFilters() {
        AppState.filters = {
            category: null,
            status: null,
            search: '',
            sortBy: 'name'
        };
        AppState.save();
    }
}

// ==============================
// 7. FAVORIS
// ==============================

class FavoritesManager {
    static toggle(subscriptionId) {
        const index = AppState.favorites.indexOf(subscriptionId);
        if (index > -1) {
            AppState.favorites.splice(index, 1);
        } else {
            AppState.favorites.push(subscriptionId);
        }
        AppState.save();
    }

    static isFavorite(subscriptionId) {
        return AppState.favorites.includes(subscriptionId);
    }

    static getFavorites() {
        return AppState.subscriptions.filter(s =>
            AppState.favorites.includes(s.id)
        );
    }
}

// ==============================
// 8. INITIALISATION
// ==============================

async function initializeApp() {
    try {
        // Charger l'état sauvegardé
        AppState.load();

        // Initialiser le thème
        ThemeManager.init();

        // Charger les données
        await loadSubscriptions();

        // Afficher le dashboard
        renderDashboard();
    } catch (error) {
        console.error('Erreur initialisation:', error);
        showError('Impossible de démarrer l\'application. Vérifiez votre connexion.');
    }
}

async function loadSubscriptions() {
    try {
        await APIManager.getAll();
        FilterManager.applyFilters();
    } catch (error) {
        console.error('Erreur chargement:', error);
        throw error;
    }
}

function renderDashboard() {
    const dash = document.getElementById('dashboard');
    if (!dash) return;

    const total = AppState.subscriptions.length;
    const active = AppState.subscriptions.filter(s => Utils.isActive(s)).length;
    const inactive = AppState.subscriptions.filter(s => Utils.isInactive(s)).length;
    const totalCost = AppState.subscriptions
        .filter(s => Utils.isActive(s))
        .reduce((sum, s) => sum + (parseFloat(s.prixMensuel) || 0), 0);

    dash.innerHTML = `
        <div class="metric-card animate-slide-in">
            <div class="metric-icon"><i class="bi bi-collection"></i></div>
            <div class="metric-value">${total}</div>
            <div class="metric-label">Total Services</div>
        </div>
        <div class="metric-card animate-slide-in" style="animation-delay: 0.1s">
            <div class="metric-icon"><i class="bi bi-check-circle"></i></div>
            <div class="metric-value">${active}</div>
            <div class="metric-label">Actifs</div>
        </div>
        <div class="metric-card animate-slide-in" style="animation-delay: 0.2s">
            <div class="metric-icon"><i class="bi bi-exclamation-circle"></i></div>
            <div class="metric-value">${inactive}</div>
            <div class="metric-label">Inactifs</div>
        </div>
        <div class="metric-card animate-slide-in" style="animation-delay: 0.3s">
            <div class="metric-icon"><i class="bi bi-currency-euro"></i></div>
            <div class="metric-value">${Utils.formatPrice(totalCost)}</div>
            <div class="metric-label">Coût Mensuel</div>
        </div>
    `;
}

function showError(message) {
    const errorDiv = document.createElement('div');
    errorDiv.className = 'alert alert-danger';
    errorDiv.textContent = message;
    document.body.insertAdjacentElement('afterbegin', errorDiv);

    setTimeout(() => errorDiv.remove(), 5000);
}

function showSuccess(message) {
    const successDiv = document.createElement('div');
    successDiv.className = 'alert alert-success';
    successDiv.textContent = message;
    document.body.insertAdjacentElement('afterbegin', successDiv);

    setTimeout(() => successDiv.remove(), 3000);
}

// Initialiser au chargement
document.addEventListener('DOMContentLoaded', initializeApp);

// Rafraîchir les données toutes les 5 minutes
setInterval(loadSubscriptions, 5 * 60 * 1000);
