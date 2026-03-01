// ========================================
// GESTION DES PARAMÈTRES EMAIL
// ========================================

// Charger les paramètres au démarrage
document.addEventListener('DOMContentLoaded', () => {
    loadEmailSettings();
    loadNotificationSettings();
});

// ========================================
// SAUVEGARDER LES PARAMÈTRES EMAIL
// ========================================
function saveEmailSettings() {
    const settings = {
        email: document.getElementById('emailAddress').value,
        frequency: document.getElementById('reminderFrequency').value,
        daysBefore: parseInt(document.getElementById('daysBefore').value),
        updatedAt: new Date().toISOString()
    };
    
    // Valider l'email
    if (!validateEmail(settings.email)) {
        showNotification('❌ Veuillez entrer une adresse email valide', 'error');
        return;
    }
    
    localStorage.setItem('emailSettings', JSON.stringify(settings));
    showNotification('✅ Paramètres email sauvegardés !', 'success');
    
    // Envoyer au backend (simulation)
    sendToBackend('/api/notifications/settings', settings);
}

// ========================================
// CHARGER LES PARAMÈTRES EMAIL
// ========================================
function loadEmailSettings() {
    const settings = JSON.parse(localStorage.getItem('emailSettings') || '{}');
    
    if (settings.email) {
        document.getElementById('emailAddress').value = settings.email;
    } else {
        // Utiliser l'email de session si disponible
        const userEmail = sessionStorage.getItem('user_email');
        if (userEmail) {
            document.getElementById('emailAddress').value = userEmail;
        }
    }
    
    if (settings.frequency) {
        document.getElementById('reminderFrequency').value = settings.frequency;
    }
    
    if (settings.daysBefore) {
        document.getElementById('daysBefore').value = settings.daysBefore;
    }
}

// ========================================
// SAUVEGARDER LES PARAMÈTRES DE NOTIFICATION
// ========================================
function saveNotificationSettings() {
    const settings = {
        expirationAlerts: document.getElementById('expirationAlerts').checked,
        budgetAlerts: document.getElementById('budgetAlerts').checked,
        monthlyReport: document.getElementById('monthlyReport').checked,
        newSubAlerts: document.getElementById('newSubAlerts').checked,
        unusualExpenses: document.getElementById('unusualExpenses').checked,
        updatedAt: new Date().toISOString()
    };
    
    localStorage.setItem('notificationSettings', JSON.stringify(settings));
    
    // Envoyer au backend
    sendToBackend('/api/notifications/preferences', settings);
}

// ========================================
// CHARGER LES PARAMÈTRES DE NOTIFICATION
// ========================================
function loadNotificationSettings() {
    const settings = JSON.parse(localStorage.getItem('notificationSettings') || '{}');
    
    // Valeurs par défaut : tout activé
    document.getElementById('expirationAlerts').checked = settings.expirationAlerts !== false;
    document.getElementById('budgetAlerts').checked = settings.budgetAlerts !== false;
    document.getElementById('monthlyReport').checked = settings.monthlyReport !== false;
    document.getElementById('newSubAlerts').checked = settings.newSubAlerts || false;
    document.getElementById('unusualExpenses').checked = settings.unusualExpenses || false;
}

// ========================================
// ENVOYER UN EMAIL DE TEST
// ========================================
async function sendTestEmail() {
    const email = document.getElementById('emailAddress').value;
    
    if (!validateEmail(email)) {
        showNotification('❌ Veuillez configurer une adresse email valide', 'error');
        return;
    }
    
    showNotification('📧 Envoi de l\'email de test...', 'info');
    
    // Préparer les données du test
    const testData = {
        to: email,
        subject: '✅ Test de notification - Gestion Abonnements',
        type: 'test',
        data: {
            userName: 'Utilisateur',
            testDate: new Date().toLocaleString('fr-FR')
        }
    };
    
    try {
        // Envoyer au backend
        const response = await fetch('/api/notifications/test', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(testData)
        });
        
        if (response.ok) {
            showNotification('✅ Email de test envoyé avec succès ! Vérifiez votre boîte de réception.', 'success');
            addNotificationToHistory('Email de test', 'Envoyé');
        } else {
            throw new Error('Erreur serveur');
        }
    } catch (error) {
        // En mode développement, simuler l'envoi
        console.log('Email de test simulé:', testData);
        showNotification('⚠️ Mode simulation : l\'email n\'a pas été envoyé (backend non connecté)', 'warning');
        addNotificationToHistory('Email de test', 'Simulé');
    }
}

// ========================================
// VÉRIFIER LES EXPIRATIONS ET ENVOYER ALERTES
// ========================================
function checkExpirations() {
    const settings = JSON.parse(localStorage.getItem('notificationSettings') || '{}');
    if (!settings.expirationAlerts) return;
    
    const subscriptions = JSON.parse(localStorage.getItem('subscriptions') || '[]');
    const daysBefore = parseInt(document.getElementById('daysBefore').value) || 7;
    const today = new Date();
    
    subscriptions.forEach(sub => {
        if (!sub.dateExpiration || !sub.actif) return;
        
        const expDate = new Date(sub.dateExpiration);
        const daysUntilExpiration = Math.floor((expDate - today) / (1000 * 60 * 60 * 24));
        
        if (daysUntilExpiration === daysBefore) {
            sendExpirationAlert(sub, daysUntilExpiration);
        }
    });
}

// ========================================
// ENVOYER ALERTE D'EXPIRATION
// ========================================
async function sendExpirationAlert(subscription, daysRemaining) {
    const emailSettings = JSON.parse(localStorage.getItem('emailSettings') || '{}');
    
    const emailData = {
        to: emailSettings.email,
        subject: `⚠️ Alerte : ${subscription.nom} expire dans ${daysRemaining} jours`,
        type: 'expiration',
        data: {
            subscriptionName: subscription.nom,
            price: subscription.prix,
            expirationDate: subscription.dateExpiration,
            daysRemaining: daysRemaining,
            category: subscription.categorie
        }
    };
    
    try {
        await fetch('/api/notifications/send', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(emailData)
        });
        
        addNotificationToHistory(`Expiration ${subscription.nom}`, 'Envoyé');
    } catch (error) {
        console.error('Erreur envoi notification:', error);
    }
}

// ========================================
// VÉRIFIER LE BUDGET
// ========================================
function checkBudget() {
    const settings = JSON.parse(localStorage.getItem('notificationSettings') || '{}');
    if (!settings.budgetAlerts) return;
    
    const expenses = JSON.parse(localStorage.getItem('expenses') || '[]');
    const subscriptions = JSON.parse(localStorage.getItem('subscriptions') || '[]');
    const budget = parseFloat(localStorage.getItem('monthlyBudget') || 500);
    
    const totalExpenses = expenses.reduce((sum, e) => sum + parseFloat(e.amount || 0), 0);
    const totalSubs = subscriptions.reduce((sum, s) => sum + parseFloat(s.prix || 0), 0);
    const total = totalExpenses + totalSubs;
    
    if (total > budget) {
        sendBudgetAlert(budget, total);
    }
}

// ========================================
// ENVOYER ALERTE DE BUDGET
// ========================================
async function sendBudgetAlert(budget, spent) {
    const emailSettings = JSON.parse(localStorage.getItem('emailSettings') || '{}');
    const overspend = spent - budget;
    const percentage = ((overspend / budget) * 100).toFixed(1);
    
    const emailData = {
        to: emailSettings.email,
        subject: `💸 Alerte Budget : Dépassement de ${overspend.toFixed(2)}€`,
        type: 'budget',
        data: {
            budget: budget.toFixed(2),
            spent: spent.toFixed(2),
            overspend: overspend.toFixed(2),
            percentage: percentage
        }
    };
    
    try {
        await fetch('/api/notifications/send', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(emailData)
        });
        
        addNotificationToHistory('Dépassement budget', 'Envoyé');
    } catch (error) {
        console.error('Erreur envoi notification:', error);
    }
}

// ========================================
// ENVOYER RAPPORT MENSUEL
// ========================================
async function sendMonthlyReport() {
    const settings = JSON.parse(localStorage.getItem('notificationSettings') || '{}');
    if (!settings.monthlyReport) return;
    
    const emailSettings = JSON.parse(localStorage.getItem('emailSettings') || '{}');
    const expenses = JSON.parse(localStorage.getItem('expenses') || '[]');
    const subscriptions = JSON.parse(localStorage.getItem('subscriptions') || '[]');
    
    // Calculer les statistiques du mois
    const now = new Date();
    const monthStart = new Date(now.getFullYear(), now.getMonth(), 1);
    const monthExpenses = expenses.filter(e => new Date(e.date) >= monthStart);
    
    const totalSpent = monthExpenses.reduce((sum, e) => sum + parseFloat(e.amount || 0), 0);
    const totalSubs = subscriptions.filter(s => s.actif).length;
    const subsCost = subscriptions.filter(s => s.actif).reduce((sum, s) => sum + parseFloat(s.prix || 0), 0);
    
    const emailData = {
        to: emailSettings.email,
        subject: `📊 Résumé Mensuel - ${now.toLocaleString('fr-FR', { month: 'long', year: 'numeric' })}`,
        type: 'monthly',
        data: {
            month: now.toLocaleString('fr-FR', { month: 'long', year: 'numeric' }),
            totalSpent: totalSpent.toFixed(2),
            totalSubs: totalSubs,
            subsCost: subsCost.toFixed(2),
            transactionCount: monthExpenses.length,
            topCategories: getTopCategories(monthExpenses)
        }
    };
    
    try {
        await fetch('/api/notifications/send', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(emailData)
        });
        
        addNotificationToHistory('Rapport mensuel', 'Envoyé');
    } catch (error) {
        console.error('Erreur envoi notification:', error);
    }
}

// ========================================
// HELPER FUNCTIONS
// ========================================
function validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

function getTopCategories(expenses) {
    const categoryTotals = {};
    
    expenses.forEach(exp => {
        const cat = exp.category || 'Autres';
        categoryTotals[cat] = (categoryTotals[cat] || 0) + parseFloat(exp.amount || 0);
    });
    
    return Object.entries(categoryTotals)
        .sort((a, b) => b[1] - a[1])
        .slice(0, 5)
        .map(([cat, total]) => ({ category: cat, total: total.toFixed(2) }));
}

function addNotificationToHistory(type, status) {
    const history = JSON.parse(localStorage.getItem('notificationHistory') || '[]');
    
    history.unshift({
        date: new Date().toISOString(),
        type,
        status
    });
    
    localStorage.setItem('notificationHistory', JSON.stringify(history.slice(0, 20)));
    updateHistoryTable();
}

function updateHistoryTable() {
    const history = JSON.parse(localStorage.getItem('notificationHistory') || '[]');
    const tbody = document.getElementById('notificationHistory');
    
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    history.slice(0, 10).forEach(item => {
        const row = document.createElement('tr');
        const statusClass = item.status === 'Envoyé' ? 'badge-success' : 
                           item.status === 'Simulé' ? 'badge-warning' : 'badge-danger';
        
        row.innerHTML = `
            <td>${new Date(item.date).toLocaleString('fr-FR')}</td>
            <td>${item.type}</td>
            <td><span class="badge ${statusClass}">${item.status}</span></td>
        `;
        tbody.appendChild(row);
    });
}

async function sendToBackend(endpoint, data) {
    try {
        const response = await fetch(endpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });
        
        if (!response.ok) {
            console.warn('Backend non disponible:', endpoint);
        }
    } catch (error) {
        console.log('Mode hors ligne:', endpoint, data);
    }
}

function showNotification(message, type) {
    const toast = document.createElement('div');
    toast.className = `alert alert-${type === 'success' ? 'success' : type === 'error' ? 'danger' : type === 'warning' ? 'warning' : 'info'} position-fixed top-0 start-50 translate-middle-x mt-3`;
    toast.style.zIndex = '9999';
    toast.innerHTML = message;
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.remove();
    }, 4000);
}

function toggleSMTPConfig() {
    const config = document.getElementById('smtpConfig');
    const icon = document.getElementById('smtpToggleIcon');
    
    if (config.style.display === 'none') {
        config.style.display = 'block';
        icon.className = 'bi bi-chevron-up';
    } else {
        config.style.display = 'none';
        icon.className = 'bi bi-chevron-down';
    }
}

// ========================================
// VÉRIFICATIONS AUTOMATIQUES
// ========================================

// Vérifier les expirations chaque jour
setInterval(checkExpirations, 24 * 60 * 60 * 1000);

// Vérifier le budget toutes les heures
setInterval(checkBudget, 60 * 60 * 1000);

// Envoyer le rapport mensuel le 1er de chaque mois
function scheduleMonthlyReport() {
    const now = new Date();
    const nextMonth = new Date(now.getFullYear(), now.getMonth() + 1, 1, 9, 0, 0);
    const timeUntilNextMonth = nextMonth - now;
    
    setTimeout(() => {
        sendMonthlyReport();
        scheduleMonthlyReport(); // Reprogrammer pour le mois suivant
    }, timeUntilNextMonth);
}

scheduleMonthlyReport();

// Export pour utilisation globale
window.EmailNotifications = {
    checkExpirations,
    checkBudget,
    sendMonthlyReport,
    sendTestEmail
};
