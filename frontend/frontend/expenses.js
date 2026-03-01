// Gestion des Dépenses - JavaScript
let currentPeriod = 'all';
let monthlyBudget = parseFloat(localStorage.getItem('monthlyBudget')) || 150;
let abonnements = [];
let monthlyChart, categoryChart;

// Catégories avec couleurs et mots-clés de détection
const CATEGORIES = {
    'Streaming': { 
        color: '#667eea', 
        icon: '📺',
        keywords: ['netflix', 'disney', 'prime', 'video', 'hulu', 'hbo', 'apple tv', 'paramount']
    },
    'Gaming': { 
        color: '#f59e0b', 
        icon: '🎮',
        keywords: ['playstation', 'xbox', 'nintendo', 'steam', 'epic', 'game', 'gamer', 'twitch']
    },
    'Productivité': { 
        color: '#10b981', 
        icon: '💼',
        keywords: ['microsoft', 'office', 'google', 'workspace', 'adobe', 'slack', 'zoom', 'notion']
    },
    'Fitness': { 
        color: '#ef4444', 
        icon: '💪',
        keywords: ['gym', 'fitness', 'sport', 'basicfit', 'keepcool', 'yoga', 'training']
    },
    'Éducation': { 
        color: '#8b5cf6', 
        icon: '📚',
        keywords: ['coursera', 'udemy', 'skillshare', 'pluralsight', 'linkedin learning', 'education']
    },
    'Musique': { 
        color: '#ec4899', 
        icon: '🎵',
        keywords: ['spotify', 'apple music', 'deezer', 'youtube music', 'soundcloud', 'tidal']
    },
    'Cloud': { 
        color: '#3b82f6', 
        icon: '☁️',
        keywords: ['dropbox', 'onedrive', 'icloud', 'drive', 'storage', 'backup']
    },
    'Finance': { 
        color: '#14b8a6', 
        icon: '💳',
        keywords: ['bank', 'banque', 'bnp', 'crédit', 'assurance', 'insurance']
    },
    'Autre': { 
        color: '#6b7280', 
        icon: '📦',
        keywords: []
    }
};

// Détection automatique de la catégorie
function detectCategory(abonnement) {
    if (abonnement.categorie && abonnement.categorie !== 'Non classé' && abonnement.categorie !== '') {
        return abonnement.categorie;
    }
    
    const serviceName = (abonnement.nomService || '').toLowerCase();
    
    for (const [category, info] of Object.entries(CATEGORIES)) {
        if (info.keywords.some(keyword => serviceName.includes(keyword))) {
            return category;
        }
    }
    
    return 'Autre';
}

// Vérifier l'authentification
async function checkAuth() {
    try {
        const response = await fetch('/api/session');
        const data = await response.json();
        
        if (!data.authenticated) {
            alert('⚠️ Vous devez être connecté pour accéder aux dépenses !');
            window.location.href = 'login.html';
            return false;
        }
        return true;
    } catch (error) {
        console.error('Erreur authentification:', error);
        window.location.href = 'login.html';
        return false;
    }
}

// Charger les abonnements
async function loadAbonnements() {
    try {
        const response = await fetch('/api/abonnements');
        if (!response.ok) {
            throw new Error('Erreur de chargement');
        }
        abonnements = await response.json();
        
        // Détecter automatiquement les catégories manquantes
        abonnements = abonnements.map(abo => ({
            ...abo,
            categorie: detectCategory(abo)
        }));
        
        // Calculer les métriques
        updateKPIs();
        updateBudgetProgress();
        updateCharts();
        updateTimeline();
        updateCategoriesOverview();
        generateRecommendations();
    } catch (error) {
        console.error('Erreur:', error);
        showError('Impossible de charger les données');
    }
}

// Calculer les métriques
function calculateMetrics() {
    const now = new Date();
    const currentMonth = now.getMonth();
    const currentYear = now.getFullYear();
    
    // Filtrer les abonnements actifs
    const activeAbos = abonnements.filter(abo => {
        const dateFin = new Date(abo.dateFin);
        return dateFin >= now;
    });
    
    // Dépenses totales mensuelles
    const totalExpenses = activeAbos.reduce((sum, abo) => sum + (abo.prixMensuel || 0), 0);
    
    // Économies potentielles (abonnements inutilisés > 30 jours)
    const potentialSavings = activeAbos
        .filter(abo => {
            if (!abo.derniereUtilisation) return false;
            const lastUse = new Date(abo.derniereUtilisation);
            const daysSinceUse = Math.floor((now - lastUse) / (1000 * 60 * 60 * 24));
            return daysSinceUse > 30;
        })
        .reduce((sum, abo) => sum + (abo.prixMensuel || 0), 0);
    
    // Budget restant
    const budgetRemaining = monthlyBudget - totalExpenses;
    
    // Moyenne sur les 6 derniers mois (simulé)
    const avgExpense = totalExpenses * 0.95; // Légère variation
    
    return {
        totalExpenses,
        budgetRemaining,
        avgExpense,
        potentialSavings,
        budgetPercent: (totalExpenses / monthlyBudget) * 100
    };
}

// Mettre à jour les KPI
function updateKPIs() {
    const metrics = calculateMetrics();
    
    // Animations de compteur
    animateValue('total-expenses', 0, metrics.totalExpenses, 1000, '€');
    animateValue('budget-remaining', 0, metrics.budgetRemaining, 1000, '€');
    animateValue('avg-expense', 0, metrics.avgExpense, 1000, '€');
    animateValue('potential-savings', 0, metrics.potentialSavings, 1000, '€');
}

// Animation des valeurs
function animateValue(id, start, end, duration, suffix = '') {
    const element = document.getElementById(id);
    if (!element) return;
    
    const range = end - start;
    const increment = range / (duration / 16);
    let current = start;
    
    const timer = setInterval(() => {
        current += increment;
        if ((increment > 0 && current >= end) || (increment < 0 && current <= end)) {
            current = end;
            clearInterval(timer);
        }
        element.textContent = current.toFixed(2) + suffix;
    }, 16);
}

// Mettre à jour la progression du budget
function updateBudgetProgress() {
    const metrics = calculateMetrics();
    const bar = document.getElementById('budget-bar');
    const label = document.getElementById('budget-label');
    const percent = document.getElementById('budget-percent');
    
    const percentage = Math.min(metrics.budgetPercent, 100);
    
    // Couleur selon le pourcentage
    let gradient;
    if (percentage < 70) {
        gradient = 'linear-gradient(135deg, #10b981 0%, #059669 100%)';
    } else if (percentage < 90) {
        gradient = 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)';
    } else {
        gradient = 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)';
    }
    
    bar.style.width = percentage + '%';
    bar.style.background = gradient;
    label.textContent = `${metrics.totalExpenses.toFixed(2)}€ / ${monthlyBudget}€`;
    percent.textContent = percentage.toFixed(1) + '%';
}

// Graphique évolution mensuelle
function updateCharts() {
    updateMonthlyChart();
    updateCategoryChart();
}

function updateMonthlyChart() {
    const ctx = document.getElementById('monthlyChart');
    if (!ctx) return;
    
    // Générer données des 6 derniers mois
    const months = [];
    const data = [];
    const now = new Date();
    
    for (let i = 5; i >= 0; i--) {
        const date = new Date(now.getFullYear(), now.getMonth() - i, 1);
        months.push(date.toLocaleDateString('fr-FR', { month: 'short' }));
        
        // Simuler variation (dans un vrai cas, il faudrait l'historique)
        const baseExpense = calculateMetrics().totalExpenses;
        const variation = (Math.random() - 0.5) * 20;
        data.push(Math.max(0, baseExpense + variation));
    }
    
    if (monthlyChart) {
        monthlyChart.destroy();
    }
    
    monthlyChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: months,
            datasets: [{
                label: 'Dépenses Mensuelles',
                data: data,
                borderColor: '#667eea',
                backgroundColor: 'rgba(102, 126, 234, 0.1)',
                borderWidth: 3,
                fill: true,
                tension: 0.4,
                pointRadius: 6,
                pointHoverRadius: 8,
                pointBackgroundColor: '#667eea',
                pointBorderColor: '#fff',
                pointBorderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    labels: { color: 'white', font: { size: 14 } }
                },
                tooltip: {
                    backgroundColor: 'rgba(0, 0, 0, 0.8)',
                    padding: 12,
                    titleFont: { size: 14 },
                    bodyFont: { size: 13 },
                    callbacks: {
                        label: (context) => `Dépenses: ${context.parsed.y.toFixed(2)}€`
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: { color: 'rgba(255, 255, 255, 0.1)' },
                    ticks: {
                        color: 'white',
                        callback: (value) => value + '€'
                    }
                },
                x: {
                    grid: { display: false },
                    ticks: { color: 'white' }
                }
            }
        }
    });
}

// Grouper par catégorie
function updateCategoryChart() {
    const ctx = document.getElementById('categoryChart');
    if (!ctx) return;
    
    // Grouper par catégorie
    const categoryExpenses = {};
    abonnements.forEach(abo => {
        const cat = detectCategory(abo);
        categoryExpenses[cat] = (categoryExpenses[cat] || 0) + (abo.prixMensuel || 0);
    });
    
    const labels = Object.keys(categoryExpenses);
    const data = Object.values(categoryExpenses);
    const colors = labels.map(cat => CATEGORIES[cat]?.color || '#6b7280');
    
    if (categoryChart) {
        categoryChart.destroy();
    }
    
    categoryChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: colors,
                borderWidth: 3,
                borderColor: 'rgba(255, 255, 255, 0.2)',
                hoverBorderWidth: 4,
                hoverBorderColor: '#fff'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        color: 'white',
                        font: { size: 12 },
                        padding: 15,
                        generateLabels: (chart) => {
                            const data = chart.data;
                            return data.labels.map((label, i) => ({
                                text: `${CATEGORIES[label]?.icon || '📦'} ${label}: ${data.datasets[0].data[i].toFixed(2)}€`,
                                fillStyle: data.datasets[0].backgroundColor[i],
                                hidden: false,
                                index: i
                            }));
                        }
                    }
                },
                tooltip: {
                    backgroundColor: 'rgba(0, 0, 0, 0.8)',
                    padding: 12,
                    callbacks: {
                        label: (context) => {
                            const label = context.label;
                            const value = context.parsed;
                            const total = context.dataset.data.reduce((a, b) => a + b, 0);
                            const percent = ((value / total) * 100).toFixed(1);
                            return `${label}: ${value.toFixed(2)}€ (${percent}%)`;
                        }
                    }
                }
            }
        }
    });
}

// Timeline des dépenses
function updateTimeline() {
    const timeline = document.getElementById('expense-timeline');
    if (!timeline) return;
    
    // Trier par date de début (plus récent en premier)
    const sorted = [...abonnements].sort((a, b) => {
        return new Date(b.dateDebut) - new Date(a.dateDebut);
    });
    
    const html = sorted.slice(0, 10).map(abo => {
        const dateDebut = new Date(abo.dateDebut).toLocaleDateString('fr-FR');
        const dateFin = new Date(abo.dateFin).toLocaleDateString('fr-FR');
        const category = detectCategory(abo);
        const icon = CATEGORIES[category]?.icon || '📦';
        const isActive = new Date(abo.dateFin) >= new Date();
        
        return `
            <div class="timeline-item">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h6 class="text-white mb-1">
                            ${icon} ${abo.nomService}
                            <span class="badge ${isActive ? 'bg-success' : 'bg-secondary'} ms-2">
                                ${isActive ? 'Actif' : 'Expiré'}
                            </span>
                        </h6>
                        <small class="text-white-50">
                            ${dateDebut} → ${dateFin}
                        </small>
                    </div>
                    <div class="text-end">
                        <div class="text-white fw-bold">${abo.prixMensuel?.toFixed(2) || 0}€/mois</div>
                        <small class="text-white-50">${category}</small>
                    </div>
                </div>
            </div>
        `;
    }).join('');
    
    timeline.innerHTML = html || '<p class="text-white-50 text-center">Aucune dépense trouvée</p>';
}

// Vue d'ensemble des catégories
function updateCategoriesOverview() {
    const container = document.getElementById('categories-overview');
    if (!container) return;
    
    const categoryExpenses = {};
    const categoryCount = {};
    
    abonnements.forEach(abo => {
        const cat = detectCategory(abo);
        categoryExpenses[cat] = (categoryExpenses[cat] || 0) + (abo.prixMensuel || 0);
        categoryCount[cat] = (categoryCount[cat] || 0) + 1;
    });
    
    const html = Object.keys(categoryExpenses)
        .sort((a, b) => categoryExpenses[b] - categoryExpenses[a])
        .map(cat => {
            const expense = categoryExpenses[cat];
            const count = categoryCount[cat];
            const icon = CATEGORIES[cat]?.icon || '📦';
            const color = CATEGORIES[cat]?.color || '#6b7280';
            const percent = (expense / calculateMetrics().totalExpenses * 100).toFixed(1);
            
            return `
                <div class="mb-3">
                    <div class="d-flex justify-content-between align-items-center mb-2">
                        <span class="text-white">
                            <span style="font-size: 1.5rem">${icon}</span>
                            <strong>${cat}</strong>
                            <span class="text-white-50 ms-2">(${count} abonnement${count > 1 ? 's' : ''})</span>
                        </span>
                        <span class="text-white fw-bold">${expense.toFixed(2)}€ (${percent}%)</span>
                    </div>
                    <div class="budget-progress" style="height: 10px;">
                        <div class="budget-fill" style="width: ${percent}%; background: ${color};"></div>
                    </div>
                </div>
            `;
        }).join('');
    
    container.innerHTML = html;
}

// Générer des recommandations d'économies
function generateRecommendations() {
    const recommendations = [];
    const now = new Date();
    
    // Abonnements inutilisés
    abonnements.forEach(abo => {
        if (abo.derniereUtilisation) {
            const lastUse = new Date(abo.derniereUtilisation);
            const daysSinceUse = Math.floor((now - lastUse) / (1000 * 60 * 60 * 24));
            
            if (daysSinceUse > 30) {
                recommendations.push({
                    type: 'unused',
                    title: `❌ Résilier ${abo.nomService}`,
                    description: `Non utilisé depuis ${daysSinceUse} jours`,
                    savings: abo.prixMensuel
                });
            }
        }
    });
    
    // Budget dépassé
    const metrics = calculateMetrics();
    if (metrics.budgetPercent > 100) {
        const excess = metrics.totalExpenses - monthlyBudget;
        recommendations.push({
            type: 'budget',
            title: '⚠️ Budget Dépassé',
            description: `Réduisez ${excess.toFixed(2)}€ de dépenses`,
            savings: excess
        });
    }
    
    // Catégorie la plus coûteuse
    const categoryExpenses = {};
    abonnements.forEach(abo => {
        const cat = detectCategory(abo);
        categoryExpenses[cat] = (categoryExpenses[cat] || 0) + (abo.prixMensuel || 0);
    });
    
    const maxCategory = Object.keys(categoryExpenses).reduce((a, b) => 
        categoryExpenses[a] > categoryExpenses[b] ? a : b, 'Autre');
    
    if (categoryExpenses[maxCategory] > monthlyBudget * 0.4) {
        recommendations.push({
            type: 'category',
            title: `📊 Optimiser ${maxCategory}`,
            description: `Représente ${((categoryExpenses[maxCategory] / metrics.totalExpenses) * 100).toFixed(1)}% des dépenses`,
            savings: categoryExpenses[maxCategory] * 0.2
        });
    }
    
    // Afficher les recommandations
    const section = document.getElementById('recommendations-section');
    const list = document.getElementById('recommendations-list');
    
    if (recommendations.length > 0) {
        section.style.display = 'block';
        list.innerHTML = recommendations.map(rec => `
            <div class="recommendation-item">
                <div class="flex-grow-1">
                    <strong class="text-white d-block">${rec.title}</strong>
                    <small class="text-white-50">${rec.description}</small>
                </div>
                <div class="savings-badge">
                    💰 ${rec.savings.toFixed(2)}€/mois
                </div>
            </div>
        `).join('');
    } else {
        section.style.display = 'none';
    }
}

// Filtrer par période
function filterByPeriod(period) {
    currentPeriod = period;
    
    // Mettre à jour l'état actif des tabs
    document.querySelectorAll('.filter-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    event.target.classList.add('active');
    
    // Filtrer les données (à implémenter selon vos besoins)
    loadAbonnements();
}

// Définir le budget
function setBudget() {
    const newBudget = prompt('Définir le budget mensuel (€):', monthlyBudget);
    if (newBudget && !isNaN(newBudget) && parseFloat(newBudget) > 0) {
        monthlyBudget = parseFloat(newBudget);
        localStorage.setItem('monthlyBudget', monthlyBudget);
        updateBudgetProgress();
        updateKPIs();
        generateRecommendations();
    }
}

// Afficher une erreur
function showError(message) {
    const timeline = document.getElementById('expense-timeline');
    if (timeline) {
        timeline.innerHTML = `<p class="text-danger text-center">${message}</p>`;
    }
}

// Initialisation
document.addEventListener('DOMContentLoaded', async () => {
    const isAuth = await checkAuth();
    if (isAuth) {
        await loadAbonnements();
        
        // Rafraîchir toutes les 30 secondes
        setInterval(loadAbonnements, 30000);
    }
});
