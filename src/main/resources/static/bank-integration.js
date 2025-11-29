/**
 * 🏦 Système d'Intégration Bancaire Intelligent
 * 
 * Fonctionnalités :
 * - Import transactions CSV/OFX/QIF
 * - Rapprochement automatique avec abonnements
 * - Détection abonnements non déclarés
 * - Simulation solde avec prévisions
 * - Analyse récurrences
 */

// État global
let transactions = [];
let userSubscriptions = [];
let currentBalance = 1500.00; // Solde initial simulé
let balanceChart = null;

// Patterns de détection abonnements
const SUBSCRIPTION_PATTERNS = [
    { name: 'Netflix', keywords: ['netflix', 'nflx'], category: 'Streaming', avgPrice: 13.49 },
    { name: 'Spotify', keywords: ['spotify', 'spot'], category: 'Musique', avgPrice: 9.99 },
    { name: 'Amazon Prime', keywords: ['amazon prime', 'amzn prime'], category: 'Streaming', avgPrice: 6.99 },
    { name: 'Disney+', keywords: ['disney+', 'disney plus'], category: 'Streaming', avgPrice: 8.99 },
    { name: 'Apple Music', keywords: ['apple music', 'itunes'], category: 'Musique', avgPrice: 10.99 },
    { name: 'YouTube Premium', keywords: ['youtube premium', 'youtube prem'], category: 'Streaming', avgPrice: 11.99 },
    { name: 'SFR', keywords: ['sfr', 'neuf'], category: 'Télécom', avgPrice: 35.00 },
    { name: 'Orange', keywords: ['orange', 'sosh'], category: 'Télécom', avgPrice: 30.00 },
    { name: 'Free', keywords: ['free mobile', 'free'], category: 'Télécom', avgPrice: 19.99 },
    { name: 'OVH', keywords: ['ovh', 'kimsufi'], category: 'Cloud', avgPrice: 9.99 },
    { name: 'Microsoft 365', keywords: ['microsoft 365', 'office 365'], category: 'Logiciels', avgPrice: 6.99 },
    { name: 'Adobe', keywords: ['adobe', 'photoshop'], category: 'Logiciels', avgPrice: 23.99 },
    { name: 'Salle de Sport', keywords: ['basic fit', 'fitness park', 'keep cool'], category: 'Sport', avgPrice: 19.99 },
    { name: 'LinkedIn Premium', keywords: ['linkedin premium'], category: 'Professionnel', avgPrice: 29.99 },
    { name: 'Dropbox', keywords: ['dropbox'], category: 'Cloud', avgPrice: 9.99 },
];

// Initialisation
document.addEventListener('DOMContentLoaded', function() {
    loadUserSubscriptions();
    updateBalanceDisplay();
    setupDragAndDrop();
});

// Chargement des abonnements utilisateur
async function loadUserSubscriptions() {
    try {
        const response = await fetch('/api/abonnements');
        if (response.ok) {
            userSubscriptions = await response.json();
            console.log(`✅ ${userSubscriptions.length} abonnements chargés`);
        }
    } catch (error) {
        console.error('Erreur chargement abonnements:', error);
        // Mode simulation si API indisponible
        userSubscriptions = [];
    }
}

// Configuration drag & drop
function setupDragAndDrop() {
    const uploadZone = document.getElementById('uploadZone');
    
    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        uploadZone.addEventListener(eventName, preventDefaults, false);
    });
    
    function preventDefaults(e) {
        e.preventDefault();
        e.stopPropagation();
    }
    
    ['dragenter', 'dragover'].forEach(eventName => {
        uploadZone.addEventListener(eventName, () => {
            uploadZone.classList.add('dragover');
        }, false);
    });
    
    ['dragleave', 'drop'].forEach(eventName => {
        uploadZone.addEventListener(eventName, () => {
            uploadZone.classList.remove('dragover');
        }, false);
    });
    
    uploadZone.addEventListener('drop', handleDrop, false);
}

function handleDrop(e) {
    const dt = e.dataTransfer;
    const files = dt.files;
    
    if (files.length > 0) {
        handleFileUpload({ target: { files: files } });
    }
}

// Gestion upload fichier
async function handleFileUpload(event) {
    const file = event.target.files[0];
    if (!file) return;
    
    const fileName = file.name.toLowerCase();
    const fileExtension = fileName.split('.').pop();
    
    console.log(`📁 Fichier uploadé: ${fileName}`);
    
    const reader = new FileReader();
    reader.onload = function(e) {
        const content = e.target.result;
        
        if (fileExtension === 'csv') {
            parseCSV(content);
        } else if (fileExtension === 'ofx') {
            parseOFX(content);
        } else if (fileExtension === 'qif') {
            parseQIF(content);
        } else {
            alert('Format non supporté. Utilisez CSV, OFX ou QIF.');
        }
    };
    
    reader.readAsText(file);
}

// Parser CSV
function parseCSV(content) {
    const lines = content.trim().split('\n');
    const headers = lines[0].split(',').map(h => h.trim().toLowerCase());
    
    transactions = [];
    
    for (let i = 1; i < lines.length; i++) {
        const values = parseCSVLine(lines[i]);
        
        const transaction = {
            date: findValue(headers, values, ['date', 'dateop', 'datevaleur']),
            description: findValue(headers, values, ['description', 'libelle', 'label']),
            amount: parseFloat(findValue(headers, values, ['montant', 'amount', 'debit', 'credit'])),
            category: findValue(headers, values, ['categorie', 'category']) || 'Non classé',
            matched: false,
            matchedSubscription: null,
            isRecurring: false
        };
        
        if (transaction.date && transaction.description && !isNaN(transaction.amount)) {
            transactions.push(transaction);
        }
    }
    
    console.log(`✅ ${transactions.length} transactions parsées`);
    processTransactions();
}

// Parser ligne CSV (gestion des guillemets)
function parseCSVLine(line) {
    const values = [];
    let current = '';
    let inQuotes = false;
    
    for (let i = 0; i < line.length; i++) {
        const char = line[i];
        
        if (char === '"') {
            inQuotes = !inQuotes;
        } else if (char === ',' && !inQuotes) {
            values.push(current.trim());
            current = '';
        } else {
            current += char;
        }
    }
    
    values.push(current.trim());
    return values;
}

// Parser OFX (simplifié)
function parseOFX(content) {
    transactions = [];
    const stmtTrnRegex = /<STMTTRN>([\s\S]*?)<\/STMTTRN>/g;
    let match;
    
    while ((match = stmtTrnRegex.exec(content)) !== null) {
        const trn = match[1];
        
        const dateMatch = trn.match(/<DTPOSTED>(\d{8})/);
        const amountMatch = trn.match(/<TRNAMT>([-\d.]+)/);
        const descMatch = trn.match(/<NAME>(.*?)<\//);
        
        if (dateMatch && amountMatch && descMatch) {
            const dateStr = dateMatch[1];
            const date = `${dateStr.substring(6, 8)}/${dateStr.substring(4, 6)}/${dateStr.substring(0, 4)}`;
            
            transactions.push({
                date: date,
                description: descMatch[1].trim(),
                amount: parseFloat(amountMatch[1]),
                category: 'Non classé',
                matched: false,
                matchedSubscription: null,
                isRecurring: false
            });
        }
    }
    
    console.log(`✅ ${transactions.length} transactions OFX parsées`);
    processTransactions();
}

// Parser QIF (simplifié)
function parseQIF(content) {
    transactions = [];
    const lines = content.split('\n');
    let currentTransaction = {};
    
    for (const line of lines) {
        const trimmed = line.trim();
        if (!trimmed) continue;
        
        if (trimmed === '^') {
            if (currentTransaction.date && currentTransaction.description) {
                transactions.push({
                    date: currentTransaction.date,
                    description: currentTransaction.description,
                    amount: currentTransaction.amount || 0,
                    category: currentTransaction.category || 'Non classé',
                    matched: false,
                    matchedSubscription: null,
                    isRecurring: false
                });
            }
            currentTransaction = {};
        } else if (trimmed.startsWith('D')) {
            currentTransaction.date = trimmed.substring(1);
        } else if (trimmed.startsWith('P')) {
            currentTransaction.description = trimmed.substring(1);
        } else if (trimmed.startsWith('T')) {
            currentTransaction.amount = parseFloat(trimmed.substring(1).replace(',', ''));
        } else if (trimmed.startsWith('L')) {
            currentTransaction.category = trimmed.substring(1);
        }
    }
    
    console.log(`✅ ${transactions.length} transactions QIF parsées`);
    processTransactions();
}

// Trouver valeur dans headers/values
function findValue(headers, values, possibleNames) {
    for (const name of possibleNames) {
        const index = headers.indexOf(name);
        if (index !== -1 && values[index]) {
            return values[index].replace(/"/g, '');
        }
    }
    return '';
}

// Traitement des transactions
function processTransactions() {
    // Tri par date
    transactions.sort((a, b) => parseDate(b.date) - parseDate(a.date));
    
    // Rapprochement avec abonnements
    performMatching();
    
    // Détection récurrences
    detectRecurring();
    
    // Affichage
    displayResults();
    calculateBalanceImpact();
    renderBalanceChart();
}

// Rapprochement automatique
function performMatching() {
    let matched = 0;
    
    for (const transaction of transactions) {
        // Recherche dans abonnements déclarés
        for (const sub of userSubscriptions) {
            const subName = (sub.nom || sub.nomService || '').toLowerCase();
            const transDesc = transaction.description.toLowerCase();
            
            if (transDesc.includes(subName) || subName.includes(transDesc)) {
                const subPrice = parseFloat(sub.prix || sub.montant || 0);
                const transAmount = Math.abs(transaction.amount);
                
                // Correspondance si prix similaire (±2€)
                if (Math.abs(transAmount - subPrice) <= 2) {
                    transaction.matched = true;
                    transaction.matchedSubscription = sub.nom || sub.nomService;
                    matched++;
                    break;
                }
            }
        }
        
        // Si pas de correspondance, vérifier patterns connus
        if (!transaction.matched) {
            for (const pattern of SUBSCRIPTION_PATTERNS) {
                const transDesc = transaction.description.toLowerCase();
                
                for (const keyword of pattern.keywords) {
                    if (transDesc.includes(keyword)) {
                        const transAmount = Math.abs(transaction.amount);
                        
                        // Correspond si proche du prix moyen
                        if (Math.abs(transAmount - pattern.avgPrice) <= 5) {
                            transaction.detectedPattern = pattern;
                            break;
                        }
                    }
                }
            }
        }
    }
    
    console.log(`✅ ${matched} transactions rapprochées`);
}

// Détection transactions récurrentes
function detectRecurring() {
    const groupedByDesc = {};
    
    for (const transaction of transactions) {
        const key = transaction.description.toLowerCase().substring(0, 15);
        if (!groupedByDesc[key]) {
            groupedByDesc[key] = [];
        }
        groupedByDesc[key].push(transaction);
    }
    
    for (const key in groupedByDesc) {
        const group = groupedByDesc[key];
        
        if (group.length >= 2) {
            // Vérifier régularité (environ 30 jours d'écart)
            const dates = group.map(t => parseDate(t.date)).sort((a, b) => a - b);
            
            for (let i = 1; i < dates.length; i++) {
                const daysDiff = (dates[i] - dates[i-1]) / (1000 * 60 * 60 * 24);
                
                if (daysDiff >= 25 && daysDiff <= 35) {
                    group.forEach(t => t.isRecurring = true);
                    break;
                }
            }
        }
    }
}

// Affichage résultats
function displayResults() {
    const matchedCount = transactions.filter(t => t.matched).length;
    const unmatchedCount = transactions.filter(t => !t.matched && t.amount < 0).length;
    const detectedPatterns = transactions.filter(t => t.detectedPattern && !t.matched);
    
    document.getElementById('totalTransactions').textContent = transactions.length;
    document.getElementById('matchedCount').textContent = matchedCount;
    document.getElementById('unmatchedCount').textContent = unmatchedCount;
    document.getElementById('detectedCount').textContent = detectedPatterns.length;
    
    // Affichage stats
    document.getElementById('statsCard').style.display = 'block';
    
    // Abonnements détectés
    if (detectedPatterns.length > 0) {
        displayDetectedSubscriptions(detectedPatterns);
    }
    
    // Liste transactions
    displayTransactionsList();
}

// Affichage abonnements détectés
function displayDetectedSubscriptions(detectedTransactions) {
    const detectedCard = document.getElementById('detectedCard');
    const detectedList = document.getElementById('detectedList');
    
    detectedCard.style.display = 'block';
    detectedList.innerHTML = '';
    
    // Grouper par pattern
    const grouped = {};
    for (const trans of detectedTransactions) {
        const pattern = trans.detectedPattern;
        if (!grouped[pattern.name]) {
            grouped[pattern.name] = { pattern: pattern, transactions: [] };
        }
        grouped[pattern.name].transactions.push(trans);
    }
    
    for (const name in grouped) {
        const item = grouped[name];
        const pattern = item.pattern;
        const count = item.transactions.length;
        const avgAmount = item.transactions.reduce((sum, t) => sum + Math.abs(t.amount), 0) / count;
        
        const html = `
            <div class="alert alert-warning d-flex align-items-center justify-content-between mb-3">
                <div>
                    <span class="detected-badge">
                        <i class="bi bi-exclamation-circle"></i> NON DÉCLARÉ
                    </span>
                    <h5 class="mb-1 mt-2">${pattern.name}</h5>
                    <p class="mb-1 text-muted">
                        <i class="bi bi-tag"></i> ${pattern.category} • 
                        <i class="bi bi-arrow-repeat"></i> ${count} transaction(s) • 
                        <strong>${avgAmount.toFixed(2)}€/mois</strong>
                    </p>
                    <small class="text-muted">Dernière: ${item.transactions[0].date}</small>
                </div>
                <button class="btn btn-success" onclick="declareSubscription('${pattern.name}', ${avgAmount.toFixed(2)}, '${pattern.category}')">
                    <i class="bi bi-plus-circle"></i> Déclarer
                </button>
            </div>
        `;
        
        detectedList.innerHTML += html;
    }
}

// Affichage liste transactions
function displayTransactionsList() {
    const transactionsCard = document.getElementById('transactionsCard');
    const transactionsList = document.getElementById('transactionsList');
    
    transactionsCard.style.display = 'block';
    transactionsList.innerHTML = '';
    
    // Grouper par mois
    const byMonth = {};
    for (const trans of transactions) {
        const date = parseDate(trans.date);
        const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
        
        if (!byMonth[monthKey]) {
            byMonth[monthKey] = [];
        }
        byMonth[monthKey].push(trans);
    }
    
    // Affichage par mois
    const months = Object.keys(byMonth).sort().reverse();
    
    for (const monthKey of months) {
        const [year, month] = monthKey.split('-');
        const monthName = new Date(year, parseInt(month) - 1).toLocaleDateString('fr-FR', { month: 'long', year: 'numeric' });
        
        let html = `<div class="timeline-month"><h5 class="mb-3">${monthName}</h5>`;
        
        for (const trans of byMonth[monthKey]) {
            const isDebit = trans.amount < 0;
            const matchClass = trans.matched ? 'transaction-matched' : (trans.detectedPattern ? 'transaction-unmatched' : '');
            
            html += `
                <div class="transaction-row ${matchClass}">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <strong>${trans.description}</strong>
                            ${trans.matched ? `<span class="badge bg-success ms-2"><i class="bi bi-check-circle"></i> ${trans.matchedSubscription}</span>` : ''}
                            ${trans.detectedPattern && !trans.matched ? `<span class="badge bg-warning ms-2"><i class="bi bi-exclamation-triangle"></i> ${trans.detectedPattern.name}</span>` : ''}
                            ${trans.isRecurring ? `<span class="badge bg-info ms-2"><i class="bi bi-arrow-repeat"></i> Récurrent</span>` : ''}
                            <div class="small text-muted">${trans.date} • ${trans.category}</div>
                        </div>
                        <div class="text-end">
                            <strong class="${isDebit ? 'text-danger' : 'text-success'}" style="font-size: 1.2rem;">
                                ${isDebit ? '-' : '+'}${Math.abs(trans.amount).toFixed(2)}€
                            </strong>
                        </div>
                    </div>
                </div>
            `;
        }
        
        html += '</div>';
        transactionsList.innerHTML += html;
    }
}

// Calcul impact solde
function calculateBalanceImpact() {
    // Calculer dépenses mensuelles d'abonnements
    const monthlySubscriptionCost = userSubscriptions.reduce((sum, sub) => {
        return sum + parseFloat(sub.prix || sub.montant || 0);
    }, 0);
    
    // Projection sur 30 jours
    const projectedBalance = currentBalance - monthlySubscriptionCost;
    
    document.getElementById('projectedBalance').textContent = projectedBalance.toFixed(2) + '€';
    
    const impact = projectedBalance - currentBalance;
    const impactHtml = `
        <span class="${impact >= 0 ? 'impact-positive' : 'impact-negative'}">
            <i class="bi bi-${impact >= 0 ? 'arrow-up' : 'arrow-down'}-circle"></i>
            ${impact >= 0 ? '+' : ''}${impact.toFixed(2)}€
        </span>
        <small class="opacity-75 ms-2">(Abonnements uniquement)</small>
    `;
    
    document.getElementById('balanceImpact').innerHTML = impactHtml;
}

// Mise à jour affichage solde
function updateBalanceDisplay() {
    document.getElementById('currentBalance').textContent = currentBalance.toFixed(2) + '€';
}

// Graphique évolution solde
function renderBalanceChart() {
    const chartCard = document.getElementById('chartCard');
    chartCard.style.display = 'block';
    
    const ctx = document.getElementById('balanceChart').getContext('2d');
    
    // Simulation sur 6 mois
    const labels = [];
    const balanceData = [];
    
    let simulatedBalance = currentBalance;
    const monthlyExpenses = userSubscriptions.reduce((sum, sub) => {
        return sum + parseFloat(sub.prix || sub.montant || 0);
    }, 0);
    
    const today = new Date();
    
    for (let i = 0; i < 6; i++) {
        const month = new Date(today.getFullYear(), today.getMonth() + i, 1);
        labels.push(month.toLocaleDateString('fr-FR', { month: 'short', year: 'numeric' }));
        
        balanceData.push(simulatedBalance);
        simulatedBalance -= monthlyExpenses;
    }
    
    if (balanceChart) {
        balanceChart.destroy();
    }
    
    balanceChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Solde Simulé',
                data: balanceData,
                borderColor: 'rgb(102, 126, 234)',
                backgroundColor: 'rgba(102, 126, 234, 0.1)',
                tension: 0.4,
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return context.dataset.label + ': ' + context.parsed.y.toFixed(2) + '€';
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: false,
                    ticks: {
                        callback: function(value) {
                            return value.toFixed(0) + '€';
                        }
                    }
                }
            }
        }
    });
}

// Parser date
function parseDate(dateStr) {
    // Formats supportés: DD/MM/YYYY, YYYY-MM-DD, YYYYMMDD
    if (dateStr.includes('/')) {
        const [day, month, year] = dateStr.split('/');
        return new Date(year, month - 1, day);
    } else if (dateStr.includes('-')) {
        return new Date(dateStr);
    } else if (dateStr.length === 8) {
        const year = dateStr.substring(0, 4);
        const month = dateStr.substring(4, 6);
        const day = dateStr.substring(6, 8);
        return new Date(year, month - 1, day);
    }
    return new Date(dateStr);
}

// Déclarer un abonnement détecté
async function declareSubscription(name, amount, category) {
    if (!confirm(`Ajouter "${name}" (${amount}€/mois) à vos abonnements ?`)) {
        return;
    }
    
    const newSubscription = {
        nom: name,
        nomService: name,
        prix: amount,
        montant: amount,
        categorie: category,
        dateDebut: new Date().toISOString().split('T')[0],
        statut: 'actif',
        frequence: 'mensuel'
    };
    
    try {
        const response = await fetch('/api/abonnements', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(newSubscription)
        });
        
        if (response.ok) {
            alert(`✅ Abonnement "${name}" ajouté avec succès !`);
            await loadUserSubscriptions();
            processTransactions(); // Retraiter pour rapprochement
        } else {
            alert('❌ Erreur lors de l\'ajout. Vérifiez que le serveur est démarré.');
        }
    } catch (error) {
        console.error('Erreur:', error);
        alert('❌ Impossible de contacter le serveur.');
    }
}

// Charger données exemple
function loadSampleData() {
    const sampleCSV = `Date,Description,Montant,Categorie
15/11/2024,NETFLIX.COM,13.49,Streaming
10/11/2024,SPOTIFY AB,9.99,Musique
05/11/2024,AMAZON PRIME,6.99,Shopping
01/11/2024,SFR MOBILE,35.00,Telecom
15/10/2024,NETFLIX.COM,13.49,Streaming
10/10/2024,SPOTIFY AB,9.99,Musique
05/10/2024,AMAZON PRIME,6.99,Shopping
01/10/2024,SFR MOBILE,35.00,Telecom
20/11/2024,CARREFOUR,78.50,Courses
22/11/2024,RESTAURANT,42.00,Loisirs
18/11/2024,ESSENCE,65.00,Transport
12/11/2024,DISNEY+ HOTSTAR,8.99,Streaming`;
    
    parseCSV(sampleCSV);
}

// Télécharger template CSV
function downloadTemplate() {
    const template = `Date,Description,Montant,Categorie
15/11/2024,NETFLIX.COM,-13.49,Streaming
10/11/2024,SPOTIFY AB,-9.99,Musique
05/11/2024,VIREMENT SALAIRE,2500.00,Revenus
01/11/2024,SFR MOBILE,-35.00,Telecom`;
    
    const blob = new Blob([template], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = 'template_import_bancaire.csv';
    link.click();
}

console.log('🏦 Module Intégration Bancaire chargé');
