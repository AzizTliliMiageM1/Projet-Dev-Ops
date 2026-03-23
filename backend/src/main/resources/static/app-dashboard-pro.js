/* ============================================================
   PROFESSIONAL DASHBOARD - JAVASCRIPT LOGIC
   Pure vanilla JS + API calls
   ============================================================ */

// ========== INITIALIZATION ==========
document.addEventListener('DOMContentLoaded', async () => {
    await checkAuth();
    setupTabNavigation();
    loadDashboard();
});

// ========== AUTH CHECK ==========
async function checkAuth() {
    try {
        const response = await fetch(API_CONFIG.getUrl(API_CONFIG.endpoints.session));
        const data = await response.json();

        if (!data.authenticated) {
            window.location.href = '/login-pro.html';
            return;
        }

        document.getElementById('user-email').textContent = `👤 ${data.email}`;

    } catch (error) {
        console.error('Auth check failed:', error);
        window.location.href = '/login-pro.html';
    }
}

// ========== TAB NAVIGATION ==========
function setupTabNavigation() {
    const tabs = document.querySelectorAll('[data-tab]');
    const tabButtons = document.querySelectorAll('.tab');

    tabButtons.forEach(button => {
        button.addEventListener('click', () => {
            const tabName = button.dataset.tab;

            // Update active button
            tabButtons.forEach(btn => btn.classList.remove('active'));
            button.classList.add('active');

            // Update active pane
            tabs.forEach(tab => tab.classList.remove('active'));
            document.querySelector(`[data-tab="${tabName}"]`).classList.add('active');

            // Load content
            loadTabContent(tabName);
        });
    });
}

async function loadTabContent(tabName) {
    switch (tabName) {
        case 'abonnements':
            loadAbonnements();
            break;
        case 'analytics':
            loadAnalytics();
            break;
        // dashboard, portfolio, lifecycle sono inline nel form
    }
}

// ========== DASHBOARD LOADER ==========
async function loadDashboard() {
    try {
        // Load metrics
        const response = await fetch(API_CONFIG.getUrl(API_CONFIG.endpoints.abonnements));
        const abos = await response.json();

        const totalCost = abos.reduce((sum, abo) => sum + (abo.prixMensuel || 0), 0);
        const activeCount = abos.filter(abo => abo.estActif).length;

        // Update metrics
        document.getElementById('stat-count').textContent = activeCount;
        document.getElementById('stat-total').textContent = `€${totalCost.toFixed(2)}`;

        // Load health score
        const healthResponse = await fetch(API_CONFIG.getUrl(API_CONFIG.endpoints.portfolio_health));
        const healthData = await healthResponse.json();
        document.getElementById('stat-health').textContent = Math.round(healthData.healthScore) + '%';

        // Calculate savings
        const optimizeResponse = await fetch(API_CONFIG.getUrl(API_CONFIG.endpoints.analytics_optimize));
        const optimizeData = await optimizeResponse.json();
        document.getElementById('stat-savings').textContent = 
            `€${(optimizeData.economiesPotentielles || 0).toFixed(2)}`;

        // Load dashboard content
        loadDashboardContent(abos, optimizeData);

    } catch (error) {
        console.error('Dashboard load error:', error);
        document.getElementById('dashboard-content').innerHTML = 
            `<div class="empty-state"><p>Erreur de chargement. Veuillez rafraîchir la page.</p></div>`;
    }
}

async function loadDashboardContent(abos, optimizeData) {
    const container = document.getElementById('dashboard-content');
    let html = '';

    // Top 3 expensive
    const topExpensive = abos
        .sort((a, b) => b.prixMensuel - a.prixMensuel)
        .slice(0, 3);

    html += '<div style="width: 100%;"><h3>Services les plus coûteux</h3>';
    topExpensive.forEach(abo => {
        html += `
            <div class="metric-card" style="margin-bottom: 16px;">
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <div>
                        <strong>${abo.nomService}</strong>
                        <p class="text-muted">Depuis ${abo.dateDebut}</p>
                    </div>
                    <div style="font-size: 1.5rem; font-weight: 700; color: #667eea;">€${abo.prixMensuel.toFixed(2)}</div>
                </div>
            </div>
        `;
    });
    html += '</div>';

    container.innerHTML = html;
}

// ========== LOAD ABONNEMENTS ==========
async function loadAbonnements() {
    try {
        const response = await fetch(API_CONFIG.getUrl(API_CONFIG.endpoints.abonnements));
        const abos = await response.json();

        const tbody = document.getElementById('abonnements-list');
        if (abos.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="6" class="text-center py-md text-muted">
                        <i class="fas fa-inbox" style="font-size: 2rem; margin-bottom: 16px;"></i>
                        <p>Aucun abonnement trouvé</p>
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = abos.map(abo => `
            <tr>
                <td><strong>${abo.nomService}</strong></td>
                <td>${abo.clientName}</td>
                <td>${abo.dateDebut}</td>
                <td>${abo.dateFin}</td>
                <td><span style="font-weight: 700; color: #667eea;">€${abo.prixMensuel.toFixed(2)}</span></td>
                <td>
                    <button class="btn btn-sm btn-secondary" onclick="editAbonnement('${abo.id}')">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="deleteAbonnement('${abo.id}')">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            </tr>
        `).join('');

    } catch (error) {
        console.error('Abonnements load error:', error);
        document.getElementById('abonnements-list').innerHTML = 
            `<tr><td colspan="6" class="text-center text-muted">Erreur de chargement</td></tr>`;
    }
}

// ========== LOAD ANALYTICS ==========
async function loadAnalytics() {
    try {
        const [optim, forecast, anomalies] = await Promise.all([
            fetch(API_CONFIG.getUrl(API_CONFIG.endpoints.analytics_optimize)).then(r => r.json()),
            fetch(API_CONFIG.getUrl(API_CONFIG.endpoints.analytics_forecast) + '?months=6').then(r => r.json()),
            fetch(API_CONFIG.getUrl(API_CONFIG.endpoints.analytics_anomalies)).then(r => r.json())
        ]);

        const container = document.getElementById('analytics-content');
        let html = '<div style="width: 100%;">';

        // Optimization insights
        html += '<h3 style="margin-bottom: 16px;">🎯 Recommandations</h3>';
        if (optim.analyses && optim.analyses.length > 0) {
            html += optim.analyses.slice(0, 5).map(analysis => `
                <div class="metric-card" style="margin-bottom: 12px;">
                    <strong>${analysis.abonnement.nomService}</strong>
                    <p style="margin: 8px 0 0 0; font-size: 0.875rem;">${analysis.recommendation}</p>
                </div>
            `).join('');
        }

        // Forecast
        html += '<h3 style="margin: 24px 0 16px 0;">📊 Prévision 6 mois</h3>';
        const forecasts = Object.entries(forecast.projectedCosts || {}).slice(0, 6);
        html += '<div class="grid grid-2" style="gap: 8px;">';
        forecasts.forEach(([period, cost]) => {
            html += `
                <div class="metric-card">
                    <div class="text-small text-muted">${period}</div>
                    <div style="font-weight: 700; color: #667eea;">€${cost.toFixed(2)}</div>
                </div>
            `;
        });
        html += '</div>';

        // Anomalies
        if (anomalies.duplicates && anomalies.duplicates.length > 0) {
            html += '<h3 style="margin: 24px 0 16px 0;">⚠️ Anomalies détectées</h3>';
            html += `<p class="error-message" style="display: block;">
                <i class="fas fa-exclamation-triangle"></i> ${anomalies.duplicates.length} doublons détectés
            </p>`;
        }

        html += '</div>';
        container.innerHTML = html;

    } catch (error) {
        console.error('Analytics load error:', error);
        document.getElementById('analytics-content').innerHTML = 
            `<p class="text-center text-muted">Erreur de chargement</p>`;
    }
}

// ========== PORTFOLIO OPTIMIZATION ==========
async function optimizePortfolio() {
    try {
        const budgetTarget = parseFloat(document.getElementById('budget-target').value);
        const valueWeight = parseFloat(document.getElementById('value-weight').value);
        const riskWeight = parseFloat(document.getElementById('risk-weight').value);
        const comfortWeight = parseFloat(document.getElementById('comfort-weight').value);

        const response = await fetch(API_CONFIG.getUrl(API_CONFIG.endpoints.portfolio_rebalance), {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                budgetTarget,
                valueWeight,
                riskWeight,
                comfortWeight
            })
        });

        const result = await response.json();
        displayPortfolioResults(result);

    } catch (error) {
        console.error('Portfolio optimization error:', error);
        showError('Erreur lors de l\'optimisation', 'portfolio-results');
    }
}

function displayPortfolioResults(result) {
    const container = document.getElementById('portfolio-results');
    let html = '';

    html += `
        <div class="card" style="background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1)); border-color: #667eea;">
            <div class="card-body">
                <h3>Résultats de l'optimisation</h3>
                <p class="text-muted">Score d'optimisation: <strong style="color: #667eea;">${(result.objectiveScore * 100).toFixed(1)}%</strong></p>
                <p class="text-muted">Coût final mensuel: <strong style="color: #667eea;">€${result.currentCost.toFixed(2)}</strong></p>
            </div>
        </div>
    `;

    if (result.kept && result.kept.length > 0) {
        html += '<h4 style="margin-top: 24px; margin-bottom: 12px;">✅ À conserver</h4>';
        html += '<div class="grid grid-2">';
        result.kept.forEach(abo => {
            html += `
                <div class="metric-card" style="border-color: #10b981; background: rgba(16, 185, 129, 0.05);">
                    <strong>${abo.nomService}</strong>
                    <p class="text-muted" style="margin: 8px 0; font-size: 0.875rem;">€${abo.prixMensuel.toFixed(2)}/mois</p>
                </div>
            `;
        });
        html += '</div>';
    }

    if (result.optimized && result.optimized.length > 0) {
        html += '<h4 style="margin-top: 24px; margin-bottom: 12px;">⚠️ À optimiser</h4>';
        html += '<div class="grid grid-2">';
        result.optimized.forEach(abo => {
            html += `
                <div class="metric-card" style="border-color: #f59e0b; background: rgba(245, 158, 11, 0.05);">
                    <strong>${abo.nomService}</strong>
                    <p class="text-muted" style="margin: 8px 0; font-size: 0.875rem;">€${abo.prixMensuel.toFixed(2)}/mois</p>
                </div>
            `;
        });
        html += '</div>';
    }

    if (result.cancelled && result.cancelled.length > 0) {
        html += '<h4 style="margin-top: 24px; margin-bottom: 12px;">❌ À supprimer</h4>';
        html += '<div class="grid grid-2">';
        result.cancelled.forEach(abo => {
            html += `
                <div class="metric-card" style="border-color: #ef4444; background: rgba(239, 68, 68, 0.05);">
                    <strong>${abo.nomService}</strong>
                    <p class="text-muted" style="margin: 8px 0; font-size: 0.875rem;">€${abo.prixMensuel.toFixed(2)}/mois</p>
                </div>
            `;
        });
        html += '</div>';
    }

    container.innerHTML = html;
}

// ========== LIFECYCLE PLANNING ==========
async function generateLifecycle() {
    try {
        const months = parseInt(document.getElementById('plan-months').value);
        const budget = parseFloat(document.getElementById('plan-budget').value);

        const response = await fetch(API_CONFIG.getUrl(API_CONFIG.endpoints.portfolio_lifecycle), {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ months, budget })
        });

        const result = await response.json();
        displayLifecycleResults(result);

    } catch (error) {
        console.error('Lifecycle planning error:', error);
        showError('Erreur lors de la planification', 'lifecycle-results');
    }
}

function displayLifecycleResults(result) {
    const container = document.getElementById('lifecycle-results');
    let html = '';

    if (result.success && result.plan) {
        html += `
            <div class="card" style="background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1)); border-color: #667eea;">
                <div class="card-body">
                    <h3>Plan généré avec succès</h3>
                    <p class="text-muted">Score global: <strong style="color: #667eea;">${(result.plan.globalScore * 100).toFixed(1)}%</strong></p>
                    <p class="text-muted">Coût total ${result.plan.months} mois: <strong style="color: #667eea;">€${result.plan.totalCost.toFixed(2)}</strong></p>
                </div>
            </div>
        `;

        if (result.plan.monthlyPlans && result.plan.monthlyPlans.length > 0) {
            html += '<h4 style="margin-top: 24px; margin-bottom: 12px;">Répartition mensuelle</h4>';
            html += '<div class="grid grid-3" style="gap: 8px;">';
            result.plan.monthlyPlans.forEach((plan, idx) => {
                const date = new Date();
                date.setMonth(date.getMonth() + idx);
                const monthName = date.toLocaleString('fr-FR', { month: 'short', year: '2-digit' });

                html += `
                    <div class="metric-card">
                        <div class="text-small text-muted">${monthName}</div>
                        <div style="font-weight: 700; color: #667eea;">€${plan.cost.toFixed(2)}</div>
                        <div class="text-small text-muted">${plan.subscriptions?.length || 0} abos</div>
                    </div>
                `;
            });
            html += '</div>';
        }
    } else {
        html = `<p class="error-message" style="display: block;">Erreur: ${result.message || 'Impossible de générer le plan'}</p>`;
    }

    container.innerHTML = html;
}

// ========== LOGOUT ==========
async function logout() {
    try {
        await fetch(API_CONFIG.getUrl(API_CONFIG.endpoints.logout), {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });
        window.location.href = '/login-pro.html';
    } catch (error) {
        console.error('Logout error:', error);
        window.location.href = '/login-pro.html';
    }
}

// ========== HELPERS ==========
function showError(message, containerId) {
    const container = document.getElementById(containerId);
    container.innerHTML = `
        <p class="error-message" style="display: block;">
            <i class="fas fa-exclamation-circle"></i> ${message}
        </p>
    `;
}

function editAbonnement(id) {
    alert('Edit not implemented in this demo: ' + id);
}

function deleteAbonnement(id) {
    if (confirm('Êtes-vous sûr de vouloir supprimer cet abonnement?')) {
        alert('Delete not implemented in this demo: ' + id);
    }
}

function showNewForm() {
    alert('Add form not implemented in this demo');
}
