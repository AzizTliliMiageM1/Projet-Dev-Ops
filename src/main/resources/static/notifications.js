// ========================================
// GESTION DES NOTIFICATIONS SIMPLIFIÉE
// ========================================

let userSubscriptions = [];
let selectedSubscriptions = [];

// Initialisation
document.addEventListener('DOMContentLoaded', async () => {
    await loadUserEmail();
    await loadSubscriptions();
});

// Charger l'email de l'utilisateur
async function loadUserEmail() {
    try {
        const response = await fetch('/api/session');
        const data = await response.json();
        
        if (data.authenticated && data.email) {
            document.getElementById('emailAddress').value = data.email;
        }
    } catch (error) {
        console.error('Erreur chargement email:', error);
    }
}

// Sauvegarder l'email
function saveEmail() {
    const email = document.getElementById('emailAddress').value;
    if (!email || !validateEmail(email)) {
        showResult('❌ Veuillez entrer une adresse email valide', 'danger');
        return;
    }
    
    localStorage.setItem('notificationEmail', email);
    showResult('✅ Email enregistré avec succès !', 'success');
}

// Valider email
function validateEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

// Charger les abonnements de l'utilisateur
async function loadSubscriptions() {
    const loadingMsg = document.getElementById('loadingMessage');
    const subsList = document.getElementById('subscriptionsList');
    const noSubs = document.getElementById('noSubscriptions');
    
    try {
        // Vérifier l'authentification
        const sessionResponse = await fetch('/api/session');
        const sessionData = await sessionResponse.json();
        
        if (!sessionData.authenticated) {
            window.location.href = 'login.html';
            return;
        }
        
        // Charger les abonnements
        const response = await fetch('/api/abonnements');
        if (!response.ok) throw new Error('Erreur de chargement');
        
        userSubscriptions = await response.json();
        
        loadingMsg.style.display = 'none';
        
        if (userSubscriptions.length === 0) {
            noSubs.style.display = 'block';
        } else {
            subsList.style.display = 'block';
            renderSubscriptions();
        }
    } catch (error) {
        console.error('Erreur:', error);
        loadingMsg.innerHTML = '<div class="alert alert-danger">Erreur lors du chargement des abonnements</div>';
    }
}

// Afficher les abonnements
function renderSubscriptions() {
    const container = document.getElementById('subscriptionsList');
    container.innerHTML = '';
    
    userSubscriptions.forEach((sub, index) => {
        const div = document.createElement('div');
        div.className = 'sub-checkbox';
        div.innerHTML = `
            <div class="d-flex align-items-center justify-content-between">
                <div class="sub-info flex-grow-1">
                    <div class="sub-icon">
                        ${getCategoryIcon(sub.categorie)}
                    </div>
                    <div>
                        <h5 class="mb-1">${sub.nom || sub.nomService || 'Sans nom'}</h5>
                        <div class="text-muted small">
                            ${sub.categorie || 'Non classé'} • ${sub.frequence || 'Mensuel'}
                            ${sub.dateExpiration ? ` • Expire le ${formatDate(sub.dateExpiration)}` : ''}
                        </div>
                    </div>
                </div>
                <div class="d-flex align-items-center gap-3">
                    <span class="badge-price">${parseFloat(sub.prix || 0).toFixed(2)}€</span>
                    <input type="checkbox" class="form-check-input" id="sub_${index}" value="${index}" onchange="updateSelection()">
                </div>
            </div>
        `;
        container.appendChild(div);
    });
}

// Icône selon catégorie
function getCategoryIcon(category) {
    const icons = {
        'Streaming': '📺',
        'Gaming': '🎮',
        'Productivité': '💼',
        'Fitness': '💪',
        'Éducation': '📚',
        'Musique': '🎵',
        'Cloud': '☁️',
        'Finance': '💳'
    };
    return icons[category] || '📦';
}

// Formater date
function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR');
}

// Mettre à jour la sélection
function updateSelection() {
    selectedSubscriptions = [];
    userSubscriptions.forEach((sub, index) => {
        const checkbox = document.getElementById(`sub_${index}`);
        if (checkbox && checkbox.checked) {
            selectedSubscriptions.push(sub);
        }
    });
}

// Sélectionner tous
function selectAll() {
    userSubscriptions.forEach((sub, index) => {
        const checkbox = document.getElementById(`sub_${index}`);
        if (checkbox) checkbox.checked = true;
    });
    updateSelection();
    showResult(`✅ ${selectedSubscriptions.length} abonnement(s) sélectionné(s)`, 'info');
}

// Désélectionner tous
function deselectAll() {
    userSubscriptions.forEach((sub, index) => {
        const checkbox = document.getElementById(`sub_${index}`);
        if (checkbox) checkbox.checked = false;
    });
    updateSelection();
    showResult('ℹ️ Sélection effacée', 'info');
}

// Envoyer les notifications
async function sendNotifications() {
    updateSelection();
    
    const email = document.getElementById('emailAddress').value;
    const notifType = document.querySelector('input[name="notifType"]:checked').value;
    
    // Validation
    if (!email || !validateEmail(email)) {
        showResult('❌ Veuillez entrer une adresse email valide', 'danger');
        return;
    }
    
    if (selectedSubscriptions.length === 0) {
        showResult('❌ Veuillez sélectionner au moins un abonnement', 'warning');
        return;
    }
    
    // Afficher loading
    showResult(`<div class="text-center">
        <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Envoi en cours...</span>
        </div>
        <p class="mt-2">Envoi de ${selectedSubscriptions.length} notification(s)...</p>
    </div>`, 'info');
    
    try {
        // Préparer les données
        const emailData = {
            to: email,
            type: notifType,
            subscriptions: selectedSubscriptions.map(sub => ({
                nom: sub.nom || sub.nomService,
                prix: sub.prix,
                categorie: sub.categorie,
                frequence: sub.frequence,
                dateExpiration: sub.dateExpiration,
                dateDebut: sub.dateDebut
            })),
            summary: {
                total: selectedSubscriptions.length,
                totalCost: selectedSubscriptions.reduce((sum, s) => sum + (parseFloat(s.prix) || 0), 0),
                expiringsSoon: selectedSubscriptions.filter(s => {
                    if (!s.dateExpiration) return false;
                    const exp = new Date(s.dateExpiration);
                    const now = new Date();
                    const diff = (exp - now) / (1000 * 60 * 60 * 24);
                    return diff <= 30 && diff > 0;
                }).length
            }
        };
        
        // Envoyer au backend
        const response = await fetch('/api/notifications/send', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(emailData)
        });
        
        if (response.ok) {
            const result = await response.json();
            showResult(`✅ ${selectedSubscriptions.length} notification(s) envoyée(s) avec succès à ${email}!
                <br><small>Vérifiez votre boîte mail dans quelques instants.</small>`, 'success');
            
            // Enregistrer dans l'historique
            saveNotificationHistory(emailData);
        } else {
            // Mode simulation si le backend n'est pas configuré
            console.log('Simulation - Email Data:', emailData);
            showResult(`📧 Mode Simulation : ${selectedSubscriptions.length} notification(s) préparée(s) pour ${email}
                <br><small>Backend email non configuré. Les données ont été préparées correctement.</small>
                <br><br>
                <strong>Résumé:</strong><br>
                • Abonnements: ${emailData.summary.total}<br>
                • Coût total: ${emailData.summary.totalCost.toFixed(2)}€/mois<br>
                • Expirent bientôt: ${emailData.summary.expiringsSoon}`, 'warning');
            
            saveNotificationHistory(emailData);
        }
    } catch (error) {
        console.error('Erreur:', error);
        showResult('❌ Erreur lors de l\'envoi. Vérifiez votre connexion.', 'danger');
    }
}

// Sauvegarder dans l'historique
function saveNotificationHistory(data) {
    const history = JSON.parse(localStorage.getItem('notificationHistory') || '[]');
    history.unshift({
        date: new Date().toISOString(),
        email: data.to,
        type: data.type,
        count: data.subscriptions.length,
        totalCost: data.summary.totalCost
    });
    
    // Garder seulement les 20 derniers
    if (history.length > 20) history.pop();
    
    localStorage.setItem('notificationHistory', JSON.stringify(history));
}

// Afficher un résultat
function showResult(message, type) {
    const resultDiv = document.getElementById('resultMessage');
    resultDiv.innerHTML = `<div class="alert alert-${type} alert-custom">${message}</div>`;
    resultDiv.style.display = 'block';
    
    // Auto-hide après 10 secondes sauf pour success
    if (type !== 'info') {
        setTimeout(() => {
            resultDiv.style.display = 'none';
        }, 10000);
    }
}
