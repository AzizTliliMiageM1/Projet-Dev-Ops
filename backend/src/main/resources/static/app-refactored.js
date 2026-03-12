/**
 * FRONTEND REFACTORISÉ v3.0
 * Connecté au backend complet avec analytics
 * 
 * Endpoints disponibles:
 * - /api/session (check auth)
 * - /api/abonnements (CRUD)
 * - /api/analytics/* (14 endpoints: forecast, anomalies, optimize, etc.)
 */

// ==================== CONFIGURATION ====================
const API = {
    BASE: '/api',
    endpoints: {
        // Session & Auth
        session: '/api/session',
        
        // Abonnements CRUD
        abonnements: '/api/abonnements',
        abonnement: id => `/api/abonnements/${id}`,
        
        // Import/Export
        importAbonnements: '/api/abonnements/import',
        importCsv: '/api/abonnements/import/csv',
        exportJson: '/api/abonnements/export',
        exportCsv: '/api/abonnements/export/csv',
        
        // Analytics - 14 endpoints!
        stats: '/api/analytics/stats',
        forecast: '/api/analytics/forecast',
        anomalies: '/api/analytics/anomalies',
        optimize: '/api/analytics/optimize',
        duplicates: '/api/analytics/duplicates',
        seasonalPatterns: '/api/analytics/seasonal-patterns',
        portfolioHealth: '/api/analytics/portfolio-health',
        expiring: '/api/analytics/expiring',
        roi: '/api/analytics/roi',
        savings: '/api/analytics/savings',
        spendingTrend: '/api/analytics/spending-trend',
        topPriority: '/api/analytics/top-priority',
        lifecycle: '/api/analytics/lifecycle',
        clustering: '/api/analytics/clustering',
        
        // New Portfolio & Lifecycle Endpoints
        portfolioRebalance: '/api/portfolio/rebalance',
        lifecyclePlan: '/api/portfolio/lifecycle-plan'
    }
};

// ==================== STATE MANAGEMENT ====================
const AppState = {
    // Données principales
    abonnements: [],
    currentUser: null,
    
    // Analytics cache
    analytics: {
        stats: null,
        forecast: null,
        anomalies: null,
        optimize: null,
        health: null,
        expiring: null
    },
    
    // UI state
    isLoading: false,
    error: null,
    filters: {
        category: '',
        search: '',
        sortBy: 'dateDebut'
    },
    
    // Cherche un abonnement par ID
    findById(id) {
        return this.abonnements.find(a => a.id === id);
    },

    // Filtre les abonnements
    getFiltered() {
        let filtered = this.abonnements;
        
        if (this.filters.category) {
            filtered = filtered.filter(a => a.categorie === this.filters.category);
        }
        if (this.filters.search) {
            const search = this.filters.search.toLowerCase();
            filtered = filtered.filter(a => 
                a.nomService.toLowerCase().includes(search) ||
                a.clientName.toLowerCase().includes(search)
            );
        }
        
        // Tri
        filtered.sort((a, b) => {
            switch(this.filters.sortBy) {
                case 'prix': return a.prixMensuel - b.prixMensuel;
                case 'dateDebut': return new Date(a.dateDebut) - new Date(b.dateDebut);
                case 'dateFin': return new Date(a.dateFin) - new Date(b.dateFin);
                default: return a.nomService.localeCompare(b.nomService);
            }
        });
        
        return filtered;
    }
};

// ==================== API HELPER ====================
class APIHelper {
    static async fetch(url, options = {}) {
        try {
            AppState.isLoading = true;
            AppState.error = null;
            
            const response = await fetch(url, {
                ...options,
                headers: {
                    'Content-Type': 'application/json',
                    ...options.headers
                }
            });
            
            if (!response.ok) {
                const error = await response.text();
                throw new Error(`HTTP ${response.status}: ${error}`);
            }
            
            return response.json();
        } catch (err) {
            AppState.error = err.message;
            console.error('API Error:', err);
            UI.showError(err.message);
            throw err;
        } finally {
            AppState.isLoading = false;
        }
    }
    
    static get(url) {
        return this.fetch(url, { method: 'GET' });
    }
    
    static post(url, data) {
        return this.fetch(url, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    }
    
    static put(url, data) {
        return this.fetch(url, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    }
    
    static delete(url) {
        return this.fetch(url, { method: 'DELETE' });
    }
}

// ==================== ABONNEMENTS ====================
class AbonnementManager {
    static async loadAll() {
        try {
            const data = await APIHelper.get(API.endpoints.abonnements);
            AppState.abonnements = data || [];
            UI.renderAbonnements();
            return data;
        } catch (err) {
            console.error('Erreur chargement abonnements:', err);
        }
    }
    
    static async create(ab) {
        try {
            const result = await APIHelper.post(API.endpoints.abonnements, ab);
            AppState.abonnements.push(result);
            UI.renderAbonnements();
            UI.showSuccess('Abonnement créé');
            return result;
        } catch (err) {
            console.error('Erreur création:', err);
        }
    }
    
    static async update(id, ab) {
        try {
            const result = await APIHelper.put(API.endpoints.abonnement(id), ab);
            const idx = AppState.abonnements.findIndex(a => a.id === id);
            if (idx !== -1) AppState.abonnements[idx] = result;
            UI.renderAbonnements();
            UI.showSuccess('Abonnement modifié');
            return result;
        } catch (err) {
            console.error('Erreur update:', err);
        }
    }
    
    static async delete(id) {
        try {
            if (!confirm('Confirmer suppression?')) return;
            await APIHelper.delete(API.endpoints.abonnement(id));
            AppState.abonnements = AppState.abonnements.filter(a => a.id !== id);
            UI.renderAbonnements();
            UI.showSuccess('Abonnement supprimé');
        } catch (err) {
            console.error('Erreur suppression:', err);
        }
    }
}

// ==================== ANALYTICS ====================
class AnalyticsManager {
    static async loadStats() {
        try {
            const data = await APIHelper.get(API.endpoints.stats);
            AppState.analytics.stats = data;
            return data;
        } catch (err) {
            console.error('Erreur stats:', err);
        }
    }
    
    static async loadForecast(months = 6) {
        try {
            const data = await APIHelper.get(`${API.endpoints.forecast}?months=${months}`);
            AppState.analytics.forecast = data;
            return data;
        } catch (err) {
            console.error('Erreur forecast:', err);
        }
    }
    
    static async loadAnomalies() {
        try {
            const data = await APIHelper.get(API.endpoints.anomalies);
            AppState.analytics.anomalies = data;
            return data;
        } catch (err) {
            console.error('Erreur anomalies:', err);
        }
    }
    
    static async loadOptimize() {
        try {
            const data = await APIHelper.get(API.endpoints.optimize);
            AppState.analytics.optimize = data;
            return data;
        } catch (err) {
            console.error('Erreur optimize:', err);
        }
    }
    
    static async loadHealth() {
        try {
            const data = await APIHelper.get(API.endpoints.portfolioHealth);
            AppState.analytics.health = data;
            return data;
        } catch (err) {
            console.error('Erreur health:', err);
        }
    }
    
    static async loadExpiring() {
        try {
            const data = await APIHelper.get(API.endpoints.expiring);
            AppState.analytics.expiring = data;
            return data;
        } catch (err) {
            console.error('Erreur expiring:', err);
        }
    }
    
    static async loadAll() {
        await Promise.all([
            this.loadStats(),
            this.loadForecast(),
            this.loadAnomalies(),
            this.loadOptimize(),
            this.loadHealth(),
            this.loadExpiring()
        ]);
    }
}

// ==================== UI MODULE ====================
const UI = {
    showError(msg) {
        const alert = document.createElement('div');
        alert.className = 'alert alert-danger alert-dismissible fade show';
        alert.innerHTML = `
            ${msg}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        document.body.insertBefore(alert, document.body.firstChild);
        setTimeout(() => alert.remove(), 5000);
    },
    
    showSuccess(msg) {
        const alert = document.createElement('div');
        alert.className = 'alert alert-success alert-dismissible fade show';
        alert.innerHTML = `
            ${msg}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        document.body.insertBefore(alert, document.body.firstChild);
        setTimeout(() => alert.remove(), 3000);
    },
    
    renderAbonnements() {
        const container = document.getElementById('abonnements-list');
        if (!container) return;
        
        const filtered = AppState.getFiltered();
        
        container.innerHTML = filtered.map(ab => `
            <div class="card mb-3">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-start">
                        <div>
                            <h5 class="card-title">${ab.nomService}</h5>
                            <p class="card-text text-muted">
                                <small>${ab.clientName} • ${ab.categorie}</small>
                            </p>
                            <p class="card-text">
                                <strong>€${(ab.prixMensuel || 0).toFixed(2)}/mois</strong>
                            </p>
                            <small class="text-muted">
                                ${new Date(ab.dateDebut).toLocaleDateString()} → 
                                ${new Date(ab.dateFin).toLocaleDateString()}
                            </small>
                        </div>
                        <div>
                            <button class="btn btn-sm btn-primary me-2" onclick="editAbonnement('${ab.id}')">
                                Modifier
                            </button>
                            <button class="btn btn-sm btn-danger" onclick="deleteAbonnement('${ab.id}')">
                                Supprimer
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `).join('');
        
        // Ajouter le résumé
        const total = filtered.reduce((sum, a) => sum + (a.prixMensuel || 0), 0);
        const summary = document.getElementById('summary');
        if (summary) {
            summary.innerHTML = `
                <div class="alert alert-info">
                    <strong>${filtered.length}</strong> abonnement(s)
                    • <strong>€${total.toFixed(2)}</strong>/mois
                </div>
            `;
        }
    },
    
    renderDashboard() {
        const stats = AppState.analytics.stats;
        const optimize = AppState.analytics.optimize;
        const health = AppState.analytics.health;
        
        if (!stats || !optimize) return;
        
        const dashboard = document.getElementById('dashboard');
        if (!dashboard) return;
        
        dashboard.innerHTML = `
            <div class="row">
                <div class="col-md-3">
                    <div class="card text-center">
                        <div class="card-body">
                            <h6 class="card-title">Abonnements</h6>
                            <h2>${AppState.abonnements.length}</h2>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="card text-center">
                        <div class="card-body">
                            <h6 class="card-title">Dépense Mensuelle</h6>
                            <h2>€${(stats.totalPrice || 0).toFixed(0)}</h2>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="card text-center">
                        <div class="card-body">
                            <h6 class="card-title">Santé Portfolio</h6>
                            <h2>${health?.healthScore || 'N/A'}%</h2>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="card text-center">
                        <div class="card-body">
                            <h6 class="card-title">Économies Possibles</h6>
                            <h2>€${(optimize?.suggestions?.[0]?.savingsAmount || 0).toFixed(0)}</h2>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="row mt-4">
                <div class="col-md-6">
                    <div class="card">
                        <div class="card-header">Prédictions (6 mois)</div>
                        <div class="card-body" id="forecast-chart">
                            ${this.renderForecast()}
                        </div>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="card">
                        <div class="card-header">Anomalies Détectées</div>
                        <div class="card-body" id="anomalies-list">
                            ${this.renderAnomalies()}
                        </div>
                    </div>
                </div>
            </div>
        `;
    },
    
    renderForecast() {
        const forecast = AppState.analytics.forecast;
        if (!forecast || !forecast.projection) return '<p>Chargement...</p>';
        
        return forecast.projection.map((m, i) => `
            <div class="mb-2">
                <small>Mois +${i+1}: <strong>€${m.toFixed(2)}</strong></small>
            </div>
        `).join('');
    },
    
    renderAnomalies() {
        const anomalies = AppState.analytics.anomalies;
        if (!anomalies || !anomalies.anomalies || anomalies.anomalies.length === 0) {
            return '<p class="text-success">✅ Aucune anomalie</p>';
        }
        
        return anomalies.anomalies.map(a => `
            <div class="alert alert-warning">
                <strong>${a.service}:</strong> ${a.reason}
            </div>
        `).join('');
    }
};

// ==================== FORM HANDLERS ====================
async function submitAbonnement(e) {
    e.preventDefault();
    
    const form = e.target;
    const id = form.dataset.id;
    
    const data = {
        nomService: form.nomService?.value || '',
        clientName: form.clientName?.value || '',
        dateDebut: form.dateDebut?.value || new Date().toISOString().split('T')[0],
        dateFin: form.dateFin?.value || '',
        prixMensuel: parseFloat(form.prixMensuel?.value || 0),
        categorie: form.categorie?.value || '',
        derniereUtilisation: form.derniereUtilisation?.value || new Date().toISOString().split('T')[0]
    };
    
    if (id) {
        await AbonnementManager.update(id, data);
    } else {
        await AbonnementManager.create(data);
    }
    
    form.reset();
    document.getElementById('form-container')?.classList.add('d-none');
}

async function editAbonnement(id) {
    const ab = AppState.findById(id);
    if (!ab) return;
    
    const form = document.getElementById('abonnement-form');
    if (form) {
        form.dataset.id = id;
        form.nomService.value = ab.nomService;
        form.clientName.value = ab.clientName;
        form.dateDebut.value = ab.dateDebut;
        form.dateFin.value = ab.dateFin;
        form.prixMensuel.value = ab.prixMensuel;
        form.categorie.value = ab.categorie;
        form.derniereUtilisation.value = ab.derniereUtilisation;
        
        document.getElementById('form-container')?.classList.remove('d-none');
    }
}

async function deleteAbonnement(id) {
    await AbonnementManager.delete(id);
}

function showNewForm() {
    const form = document.getElementById('abonnement-form');
    if (form) {
        form.reset();
        delete form.dataset.id;
    }
    document.getElementById('form-container')?.classList.remove('d-none');
}

// ==================== INITIALIZATION ====================
document.addEventListener('DOMContentLoaded', async () => {
    console.log('🚀 App démarrage');
    
    // Charger les données
    await AbonnementManager.loadAll();
    await AnalyticsManager.loadAll();
    
    // Render UI
    UI.renderAbonnements();
    UI.renderDashboard();
    
    // Attacher les event listeners
    const form = document.getElementById('abonnement-form');
    if (form) {
        form.addEventListener('submit', submitAbonnement);
    }
    
    const searchInput = document.getElementById('search-input');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            AppState.filters.search = e.target.value;
            UI.renderAbonnements();
        });
    }
    
    const categoryFilter = document.getElementById('category-filter');
    if (categoryFilter) {
        categoryFilter.addEventListener('change', (e) => {
            AppState.filters.category = e.target.value;
            UI.renderAbonnements();
        });
    }
    
    console.log('✅ App prêt');
});

// ==================== PORTFOLIO OPTIMIZATION ====================
async function optimizePortfolio() {
    try {
        AppState.isLoading = true;
        
        // Récupérer les paramètres du formulaire
        const budgetTarget = parseFloat(document.getElementById('budget-target').value);
        const valueWeight = parseFloat(document.getElementById('value-weight').value);
        const riskWeight = parseFloat(document.getElementById('risk-weight').value);
        const comfortWeight = parseFloat(document.getElementById('comfort-weight').value);
        
        // Validation simple
        if (budgetTarget <= 0 || isNaN(budgetTarget)) {
            alert('Budget invalide');
            return;
        }
        
        const weights = valueWeight + riskWeight + comfortWeight;
        if (weights <= 0 || isNaN(weights)) {
            alert('Poids invalides');
            return;
        }
        
        // Appeler API
        const result = await APIHelper.post(API.endpoints.portfolioRebalance, {
            budgetTarget: budgetTarget,
            valueWeight: valueWeight,
            riskWeight: riskWeight,
            comfortWeight: comfortWeight
        });
        
        // Afficher les résultats
        displayPortfolioResults(result);
        
    } catch (error) {
        console.error('❌ Erreur optimisation:', error);
        alert('Erreur lors de l\'optimisation: ' + error.message);
    } finally {
        AppState.isLoading = false;
    }
}

function displayPortfolioResults(result) {
    const container = document.getElementById('portfolio-results');
    if (!container) return;
    
    let html = `
        <div class="alert alert-info mb-3">
            <strong>📊 Score Objectif:</strong> ${result.objectiveScore?.toFixed(1) || 0}%
            <br><small>${result.recommendation || 'Optimisation réalisée'}</small>
        </div>
    `;
    
    // Abonnements conservés
    if (result.kept && result.kept.length > 0) {
        html += `
            <div class="mb-3">
                <h6 class="text-success">✅ Conservés (${result.kept.length})</h6>
                <div class="list-group list-group-sm">
        `;
        result.kept.forEach(sub => {
            html += `
                <div class="list-group-item">
                    <div class="d-flex justify-content-between">
                        <span><strong>${sub.nomService}</strong></span>
                        <span class="badge bg-success">€${(sub.prixMensuel || 0).toFixed(2)}</span>
                    </div>
                </div>
            `;
        });
        html += `</div></div>`;
    }
    
    // Abonnements à optimiser
    if (result.optimized && result.optimized.length > 0) {
        html += `
            <div class="mb-3">
                <h6 class="text-warning">⚠️ À Optimiser (${result.optimized.length})</h6>
                <div class="list-group list-group-sm">
        `;
        result.optimized.forEach(sub => {
            html += `
                <div class="list-group-item">
                    <div class="d-flex justify-content-between">
                        <span><strong>${sub.nomService}</strong></span>
                        <span class="badge bg-warning">€${(sub.prixMensuel || 0).toFixed(2)}</span>
                    </div>
                </div>
            `;
        });
        html += `</div></div>`;
    }
    
    // Abonnements à résilier
    if (result.cancelled && result.cancelled.length > 0) {
        html += `
            <div class="mb-3">
                <h6 class="text-danger">❌ À Résilier (${result.cancelled.length})</h6>
                <div class="list-group list-group-sm">
        `;
        result.cancelled.forEach(sub => {
            html += `
                <div class="list-group-item">
                    <div class="d-flex justify-content-between">
                        <span><strong>${sub.nomService}</strong></span>
                        <span class="badge bg-danger">€${(sub.prixMensuel || 0).toFixed(2)}</span>
                    </div>
                </div>
            `;
        });
        html += `</div></div>`;
    }
    
    // Résumé financier
    if (result.savingsPotential) {
        html += `
            <div class="alert alert-success">
                💰 <strong>Économies Potentielles:</strong> €${result.savingsPotential.toFixed(2)}/mois
            </div>
        `;
    }
    
    container.innerHTML = html;
}

// ==================== LIFECYCLE PLANNING ====================
async function generateLifecyclePlan() {
    try {
        AppState.isLoading = true;
        
        // Récupérer les paramètres
        const months = parseInt(document.getElementById('plan-months').value);
        const budget = parseFloat(document.getElementById('plan-budget').value);
        
        // Validation
        if (months <= 0 || isNaN(months)) {
            alert('Nombre de mois invalide');
            return;
        }
        if (budget <= 0 || isNaN(budget)) {
            alert('Budget invalide');
            return;
        }
        
        // Appeler API
        const result = await APIHelper.post(API.endpoints.lifecyclePlan, {
            months: months,
            budget: budget
        });
        
        // Afficher le plan
        displayLifecyclePlan(result);
        
    } catch (error) {
        console.error('❌ Erreur planification:', error);
        alert('Erreur lors de la planification: ' + error.message);
    } finally {
        AppState.isLoading = false;
    }
}

function displayLifecyclePlan(result) {
    const container = document.getElementById('lifecycle-results');
    if (!container) return;
    
    let html = '';
    
    // Info globale
    if (result.globalScore !== undefined) {
        html += `
            <div class="alert alert-info mb-3">
                <strong>🎯 Score Global:</strong> ${result.globalScore?.toFixed(1) || 0}%
            </div>
        `;
    }
    
    // Plan mensuel
    if (result.monthlyPlans && result.monthlyPlans.length > 0) {
        html += `<h6>📅 Plan Mensuel</h6><div class="mb-3">`;
        
        result.monthlyPlans.forEach((month, idx) => {
            const monthNum = idx + 1;
            const totalCost = month.totalCost?.toFixed(2) || '0.00';
            const monthScore = month.score?.toFixed(1) || '0';
            
            html += `
                <div class="card mb-2">
                    <div class="card-body p-2">
                        <div class="d-flex justify-content-between align-items-center">
                            <span><strong>Mois ${monthNum}</strong></span>
                            <div>
                                <span class="badge bg-primary">€${totalCost}</span>
                                <span class="badge bg-info">${monthScore}%</span>
                            </div>
                        </div>
                        ${month.decisions && month.decisions.length > 0 ? `
                            <small class="text-muted d-block mt-1">
                                ${month.decisions.length} décision(s)
                            </small>
                        ` : ''}
                    </div>
                </div>
            `;
        });
        
        html += `</div>`;
    }
    
    // Coût total
    if (result.totalCost !== undefined) {
        html += `
            <div class="alert alert-success">
                💰 <strong>Coût Total:</strong> €${result.totalCost.toFixed(2)}
            </div>
        `;
    }
    
    container.innerHTML = html;
}

// Export pour global
window.submitAbonnement = submitAbonnement;
window.editAbonnement = editAbonnement;
window.deleteAbonnement = deleteAbonnement;
window.showNewForm = showNewForm;
