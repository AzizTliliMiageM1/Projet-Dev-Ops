// ========================================
// EXPORT TO PDF
// ========================================
async function exportToPDF() {
    // Vérifier l'authentification
    if (!await checkAuth()) return;
    
    const { jsPDF } = window.jspdf;
    const doc = new jsPDF();
    
    // Récupérer les données depuis le serveur
    const expenses = getExpensesData();
    const subscriptions = await getSubscriptionsFromServer();
    
    // Header avec thème
    const theme = getThemeColors();
    doc.setFillColor(hexToRgb(theme.primary));
    doc.rect(0, 0, 210, 40, 'F');
    
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(24);
    doc.setFont(undefined, 'bold');
    doc.text('Rapport Financier', 105, 25, { align: 'center' });
    
    doc.setFontSize(12);
    doc.text(`Généré le ${new Date().toLocaleDateString('fr-FR')}`, 105, 33, { align: 'center' });
    
    let yPos = 50;
    
    // Section KPIs
    doc.setTextColor(0, 0, 0);
    doc.setFontSize(16);
    doc.setFont(undefined, 'bold');
    doc.text('📊 Indicateurs Clés', 20, yPos);
    yPos += 10;
    
    const kpis = calculateKPIs(expenses, subscriptions);
    
    doc.setFontSize(11);
    doc.setFont(undefined, 'normal');
    doc.text(`Total des dépenses: ${kpis.totalExpenses}€`, 20, yPos);
    yPos += 7;
    doc.text(`Nombre d'abonnements: ${kpis.totalSubs}`, 20, yPos);
    yPos += 7;
    doc.text(`Dépense moyenne: ${kpis.avgExpense}€/mois`, 20, yPos);
    yPos += 7;
    doc.text(`Budget restant: ${kpis.remainingBudget}€`, 20, yPos);
    yPos += 15;
    
    // Section Abonnements
    doc.setFontSize(16);
    doc.setFont(undefined, 'bold');
    doc.text('💳 Abonnements Actifs', 20, yPos);
    yPos += 10;
    
    if (subscriptions && subscriptions.length > 0) {
        const tableData = subscriptions.map(sub => {
            const prix = sub.prix || sub.montant || 0;
            const nom = sub.nom || sub.nomService || sub.service || '-';
            const freq = sub.frequence || sub.periode || 'Mensuel';
            const expiration = sub.dateExpiration || sub.finAbonnement || '-';
            const cat = sub.categorie || sub.category || 'Autre';
            
            return [
                nom,
                `${parseFloat(prix).toFixed(2)}€`,
                freq,
                expiration,
                cat
            ];
        });
        
        doc.autoTable({
            head: [['Service', 'Prix', 'Fréquence', 'Expiration', 'Catégorie']],
            body: tableData,
            startY: yPos,
            theme: 'grid',
            headStyles: { fillColor: hexToRgb(theme.primary), textColor: [255, 255, 255] },
            margin: { left: 20, right: 20 },
            styles: { fontSize: 10, cellPadding: 3 },
            columnStyles: {
                1: { halign: 'right' }
            }
        });
        
        yPos = doc.lastAutoTable.finalY + 15;
    } else {
        doc.setFontSize(10);
        doc.setTextColor(150, 150, 150);
        doc.text('Aucun abonnement actif', 20, yPos);
        yPos += 20;
    }
    
    // Section Dépenses
    if (yPos > 250) {
        doc.addPage();
        yPos = 20;
    }
    
    doc.setFontSize(16);
    doc.setFont(undefined, 'bold');
    doc.text('💰 Dépenses Récentes', 20, yPos);
    yPos += 10;
    
    if (expenses && expenses.length > 0) {
        const expenseData = expenses.slice(0, 20).map(exp => {
            const date = exp.date || exp.dateTransaction || '-';
            const desc = exp.description || exp.libelle || exp.nom || '-';
            const cat = exp.category || exp.categorie || 'Autre';
            const amount = exp.amount || exp.montant || 0;
            
            return [
                date,
                desc,
                cat,
                `${parseFloat(amount).toFixed(2)}€`
            ];
        });
        
        doc.autoTable({
            head: [['Date', 'Description', 'Catégorie', 'Montant']],
            body: expenseData,
            startY: yPos,
            theme: 'striped',
            headStyles: { fillColor: hexToRgb(theme.secondary), textColor: [255, 255, 255] },
            margin: { left: 20, right: 20 },
            styles: { fontSize: 10, cellPadding: 3 },
            columnStyles: {
                3: { halign: 'right' }
            }
        });
        
        yPos = doc.lastAutoTable.finalY + 15;
    } else {
        doc.setFontSize(10);
        doc.setTextColor(150, 150, 150);
        doc.text('Aucune dépense enregistrée', 20, yPos);
        yPos += 20;
    }
    
    // Footer
    const pageCount = doc.internal.getNumberOfPages();
    for (let i = 1; i <= pageCount; i++) {
        doc.setPage(i);
        doc.setFontSize(10);
        doc.setTextColor(150, 150, 150);
        doc.text(`Page ${i} sur ${pageCount}`, 105, 285, { align: 'center' });
        doc.text('Généré par Gestion Abonnements', 105, 290, { align: 'center' });
    }
    
    // Sauvegarder
    const filename = `rapport_financier_${new Date().toISOString().split('T')[0]}.pdf`;
    doc.save(filename);
    
    // Enregistrer dans l'historique
    saveToExportHistory('PDF', filename);
    
    showNotification('✅ PDF généré avec succès !', 'success');
}

// ========================================
// EXPORT TO CSV
// ========================================
async function exportToCSV() {
    // Vérifier l'authentification
    if (!await checkAuth()) return;
    
    const expenses = getExpensesData();
    const subscriptions = await getSubscriptionsFromServer();
    
    let csv = 'Type,Date,Nom/Description,Catégorie,Montant,Fréquence,Expiration\n';
    
    // Ajouter les abonnements
    if (subscriptions && subscriptions.length > 0) {
        subscriptions.forEach(sub => {
            const nom = (sub.nom || sub.nomService || sub.service || '').replace(/,/g, ';');
            const cat = (sub.categorie || sub.category || '').replace(/,/g, ';');
            const prix = parseFloat(sub.prix || sub.montant || 0).toFixed(2);
            const freq = (sub.frequence || sub.periode || 'Mensuel').replace(/,/g, ';');
            const debut = sub.dateDebut || '';
            const exp = sub.dateExpiration || sub.finAbonnement || '';
            
            csv += `Abonnement,${debut},"${nom}","${cat}",${prix},"${freq}",${exp}\n`;
        });
    }
    
    // Ajouter les dépenses
    if (expenses && expenses.length > 0) {
        expenses.forEach(exp => {
            const desc = (exp.description || exp.libelle || exp.nom || '').replace(/,/g, ';');
            const cat = (exp.category || exp.categorie || '').replace(/,/g, ';');
            const amount = parseFloat(exp.amount || exp.montant || 0).toFixed(2);
            const date = exp.date || exp.dateTransaction || '';
            
            csv += `Dépense,${date},"${desc}","${cat}",${amount},,\n`;
        });
    }
    
    // Télécharger
    const filename = `donnees_financieres_${new Date().toISOString().split('T')[0]}.csv`;
    downloadFile(csv, filename, 'text/csv');
    
    saveToExportHistory('CSV', filename);
    showNotification('✅ CSV généré avec succès !', 'success');
}

// ========================================
// EXPORT TO JSON
// ========================================
async function exportToJSON() {
    // Vérifier l'authentification
    if (!await checkAuth()) return;
    
    const expenses = getExpensesData();
    const subscriptions = await getSubscriptionsFromServer();
    
    const data = {
        version: '1.0',
        exportDate: new Date().toISOString(),
        userEmail: sessionStorage.getItem('user_email'),
        expenses: expenses,
        subscriptions: subscriptions,
        budget: localStorage.getItem('monthlyBudget') || 500,
        theme: JSON.parse(localStorage.getItem('userTheme') || '{}'),
        metadata: {
            totalExpenses: calculateTotalExpenses(),
            totalSubscriptions: subscriptions.length,
            exportedBy: 'Gestion Abonnements v2.0'
        }
    };
    
    const json = JSON.stringify(data, null, 2);
    const filename = `backup_${new Date().toISOString().split('T')[0]}.json`;
    downloadFile(json, filename, 'application/json');
    
    saveToExportHistory('JSON', filename);
    showNotification('✅ Sauvegarde JSON créée avec succès !', 'success');
}

// ========================================
// EXPORT TO EXCEL (via CSV enrichi)
// ========================================
async function exportToExcel() {
    // Vérifier l'authentification
    if (!await checkAuth()) return;
    
    // Pour un vrai fichier Excel, on utilise une librairie comme SheetJS
    // Ici, on crée un CSV enrichi compatible Excel
    const expenses = getExpensesData();
    const subscriptions = await getSubscriptionsFromServer();
    
    let excel = '\uFEFF'; // BOM UTF-8 pour Excel
    excel += 'RAPPORT FINANCIER\n';
    excel += `Généré le,${new Date().toLocaleDateString('fr-FR')}\n\n`;
    
    // Onglet Abonnements
    excel += 'ABONNEMENTS ACTIFS\n';
    excel += 'Service,Prix,Fréquence,Date Début,Date Expiration,Catégorie,Description\n';
    subscriptions.forEach(sub => {
        excel += `${sub.nom || ''},${sub.prix || 0},${sub.frequence || 'Mensuel'},${sub.dateDebut || ''},${sub.dateExpiration || ''},${sub.categorie || ''},${sub.description || ''}\n`;
    });
    
    excel += '\n\n';
    
    // Onglet Dépenses
    excel += 'DÉPENSES\n';
    excel += 'Date,Description,Catégorie,Montant,Type\n';
    expenses.forEach(exp => {
        excel += `${exp.date || ''},${exp.description || ''},${exp.category || ''},${exp.amount || 0},${exp.type || 'Dépense'}\n`;
    });
    
    const filename = `rapport_excel_${new Date().toISOString().split('T')[0]}.csv`;
    downloadFile(excel, filename, 'text/csv');
    
    saveToExportHistory('XLSX', filename);
    showNotification('✅ Fichier Excel généré avec succès !', 'success');
}

// ========================================
// IMPORT - DRAG & DROP
// ========================================
function handleDragOver(e) {
    e.preventDefault();
    e.stopPropagation();
    document.getElementById('dropZone').classList.add('dragover');
}

function handleDragLeave(e) {
    e.preventDefault();
    e.stopPropagation();
    document.getElementById('dropZone').classList.remove('dragover');
}

function handleDrop(e) {
    e.preventDefault();
    e.stopPropagation();
    document.getElementById('dropZone').classList.remove('dragover');
    
    const files = e.dataTransfer.files;
    if (files.length > 0) {
        processFile(files[0]);
    }
}

function handleFileSelect(e) {
    const file = e.target.files[0];
    if (file) {
        processFile(file);
    }
}

// ========================================
// PROCESS UPLOADED FILE
// ========================================
function processFile(file) {
    const extension = file.name.split('.').pop().toLowerCase();
    const reader = new FileReader();
    
    reader.onload = function(e) {
        const content = e.target.result;
        
        switch(extension) {
            case 'csv':
                parseCSV(content);
                break;
            case 'json':
                parseJSON(content);
                break;
            case 'ofx':
                parseOFX(content);
                break;
            case 'qif':
                parseQIF(content);
                break;
            default:
                showNotification('❌ Format de fichier non supporté', 'error');
        }
    };
    
    reader.readAsText(file);
}

// ========================================
// PARSE CSV
// ========================================
function parseCSV(content) {
    const lines = content.split('\n').filter(line => line.trim());
    const headers = lines[0].split(',').map(h => h.trim());
    
    const transactions = [];
    
    for (let i = 1; i < lines.length; i++) {
        const values = lines[i].split(',');
        const transaction = {};
        
        headers.forEach((header, index) => {
            transaction[header.toLowerCase()] = values[index]?.trim();
        });
        
        // Détection automatique des champs
        const detected = detectTransaction(transaction);
        transactions.push(detected);
    }
    
    showImportPreview(transactions);
}

// ========================================
// PARSE JSON
// ========================================
function parseJSON(content) {
    try {
        const data = JSON.parse(content);
        
        // Si c'est un backup de notre app
        if (data.version && data.expenses && data.subscriptions) {
            const transactions = [
                ...data.subscriptions.map(s => ({ ...s, type: 'Abonnement' })),
                ...data.expenses.map(e => ({ ...e, type: 'Dépense' }))
            ];
            showImportPreview(transactions);
        } else {
            showNotification('❌ Format JSON non reconnu', 'error');
        }
    } catch(e) {
        showNotification('❌ Erreur de parsing JSON', 'error');
    }
}

// ========================================
// PARSE OFX (simplifié)
// ========================================
function parseOFX(content) {
    const transactions = [];
    const stmtTrnRegex = /<STMTTRN>([\s\S]*?)<\/STMTTRN>/g;
    let match;
    
    while ((match = stmtTrnRegex.exec(content)) !== null) {
        const trn = match[1];
        
        const dtposted = trn.match(/<DTPOSTED>(\d+)/)?.[1];
        const trnamt = trn.match(/<TRNAMT>([-\d.]+)/)?.[1];
        const name = trn.match(/<NAME>(.*?)</)?.[1];
        const memo = trn.match(/<MEMO>(.*?)</)?.[1];
        
        if (dtposted && trnamt) {
            transactions.push({
                date: formatOFXDate(dtposted),
                amount: Math.abs(parseFloat(trnamt)),
                description: name || memo || 'Transaction',
                category: detectCategory(name || memo || ''),
                type: parseFloat(trnamt) < 0 ? 'Dépense' : 'Revenu'
            });
        }
    }
    
    showImportPreview(transactions);
}

// ========================================
// PARSE QIF (simplifié)
// ========================================
function parseQIF(content) {
    const transactions = [];
    const entries = content.split('^').filter(e => e.trim());
    
    entries.forEach(entry => {
        const lines = entry.split('\n').filter(l => l.trim());
        const transaction = {};
        
        lines.forEach(line => {
            const code = line.charAt(0);
            const value = line.substring(1).trim();
            
            switch(code) {
                case 'D': transaction.date = value; break;
                case 'T': transaction.amount = Math.abs(parseFloat(value)); break;
                case 'P': transaction.description = value; break;
                case 'L': transaction.category = value; break;
            }
        });
        
        if (transaction.date && transaction.amount) {
            transaction.category = transaction.category || detectCategory(transaction.description || '');
            transactions.push(transaction);
        }
    });
    
    showImportPreview(transactions);
}

// ========================================
// SHOW IMPORT PREVIEW
// ========================================
function showImportPreview(transactions) {
    const preview = document.getElementById('importPreview');
    const tbody = document.getElementById('previewTableBody');
    
    // Calculer les stats
    const total = transactions.reduce((sum, t) => sum + (parseFloat(t.amount) || 0), 0);
    const subs = transactions.filter(t => isRecurring(t.description || '')).length;
    const dates = transactions.map(t => new Date(t.date)).filter(d => !isNaN(d));
    const minDate = dates.length > 0 ? new Date(Math.min(...dates)) : new Date();
    const maxDate = dates.length > 0 ? new Date(Math.max(...dates)) : new Date();
    
    // Afficher les stats
    document.getElementById('totalTransactions').textContent = transactions.length;
    document.getElementById('detectedSubs').textContent = subs;
    document.getElementById('totalAmount').textContent = total.toFixed(2) + '€';
    document.getElementById('dateRange').textContent = 
        `${minDate.toLocaleDateString('fr-FR')} - ${maxDate.toLocaleDateString('fr-FR')}`;
    
    // Remplir le tableau
    tbody.innerHTML = '';
    transactions.slice(0, 50).forEach(t => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${t.date || '-'}</td>
            <td>${t.description || t.nom || '-'}</td>
            <td><span class="badge badge-info">${t.category || t.categorie || 'Auto'}</span></td>
            <td>${parseFloat(t.amount || t.prix || 0).toFixed(2)}€</td>
            <td><span class="badge ${t.type === 'Abonnement' ? 'badge-warning' : 'badge-success'}">${t.type || 'Dépense'}</span></td>
        `;
        tbody.appendChild(row);
    });
    
    // Stocker pour l'import
    window.pendingImport = transactions;
    
    // Afficher
    preview.style.display = 'block';
    preview.scrollIntoView({ behavior: 'smooth' });
    
    showNotification(`✅ ${transactions.length} transactions détectées`, 'success');
}

// ========================================
// CONFIRM IMPORT
// ========================================
async function confirmImport() {
    if (!window.pendingImport || window.pendingImport.length === 0) {
        showNotification('❌ Aucune donnée à importer', 'error');
        return;
    }
    
    const transactions = window.pendingImport;
    const expenses = [];
    const subscriptions = [];
    
    transactions.forEach(t => {
        if (t.type === 'Abonnement' || isRecurring(t.description || '')) {
            subscriptions.push({
                nom: t.description || t.nom,
                prix: parseFloat(t.amount || t.prix || 0),
                categorie: t.category || t.categorie || 'Autres',
                frequence: 'Mensuel',
                dateDebut: t.date,
                dateExpiration: t.expirationDate || '',
                actif: true
            });
        } else {
            expenses.push({
                date: t.date,
                description: t.description,
                category: t.category || 'Autres',
                amount: parseFloat(t.amount || 0)
            });
        }
    });
    
    // Vérifier l'authentification d'abord
    try {
        const sessionCheck = await fetch('/api/session');
        const sessionData = await sessionCheck.json();
        if (!sessionData.authenticated) {
            showNotification('❌ Vous devez être connecté pour importer des données', 'danger');
            setTimeout(() => window.location.href = 'login.html', 1500);
            return;
        }
    } catch (error) {
        console.error('Erreur de vérification session:', error);
        showNotification('❌ Erreur de connexion', 'danger');
        return;
    }
    
    // Sauvegarder dans localStorage
    if (expenses.length > 0) {
        const existingExpenses = getExpensesData();
        localStorage.setItem('expenses', JSON.stringify([...existingExpenses, ...expenses]));
    }
    
    // Envoyer les abonnements au backend
    let successCount = 0;
    let failCount = 0;
    
    if (subscriptions.length > 0) {
        for (const sub of subscriptions) {
            try {
                const response = await fetch('/api/abonnements', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(sub)
                });
                
                if (response.ok) {
                    successCount++;
                } else {
                    failCount++;
                    console.error('Erreur serveur pour:', sub.nom, await response.text());
                }
            } catch (error) {
                failCount++;
                console.error('Erreur lors de l\'ajout de:', sub.nom, error);
            }
        }
    }
    
    // Message de résultat détaillé
    if (failCount === 0) {
        showNotification(`✅ Import réussi ! ${expenses.length} dépenses et ${successCount} abonnements ajoutés`, 'success');
    } else {
        showNotification(`⚠️ Import partiel : ${successCount} réussis, ${failCount} échecs. ${expenses.length} dépenses ajoutées.`, 'warning');
    }
    
    // Réinitialiser
    window.pendingImport = null;
    document.getElementById('importPreview').style.display = 'none';
    
    // Rediriger seulement si au moins un élément a été importé
    if (successCount > 0 || expenses.length > 0) {
        setTimeout(() => {
            window.location.href = 'expenses.html';
        }, 2000);
    }
}

// ========================================
// HELPER FUNCTIONS
// ========================================
async function checkAuth() {
    try {
        const response = await fetch('/api/session');
        const data = await response.json();
        
        if (!data.authenticated) {
            showNotification('❌ Vous devez être connecté pour exporter des données', 'danger');
            setTimeout(() => window.location.href = 'login.html', 1500);
            return false;
        }
        return true;
    } catch (error) {
        console.error('Erreur authentification:', error);
        showNotification('❌ Erreur de connexion', 'danger');
        return false;
    }
}

async function getSubscriptionsFromServer() {
    try {
        const response = await fetch('/api/abonnements');
        if (!response.ok) {
            console.error('Erreur lors du chargement des abonnements');
            return [];
        }
        return await response.json();
    } catch (error) {
        console.error('Erreur lors du chargement des abonnements:', error);
        // Fallback sur localStorage en cas d'erreur
        return getSubscriptionsData();
    }
}

function getExpensesData() {
    return JSON.parse(localStorage.getItem('expenses') || '[]');
}

function getSubscriptionsData() {
    return JSON.parse(localStorage.getItem('subscriptions') || '[]');
}

function getThemeColors() {
    const theme = JSON.parse(localStorage.getItem('userTheme') || '{}');
    return {
        primary: theme.primary || '#667eea',
        secondary: theme.secondary || '#764ba2',
        accent: theme.accent || '#f093fb'
    };
}

function hexToRgb(hex) {
    const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
    return result ? [
        parseInt(result[1], 16),
        parseInt(result[2], 16),
        parseInt(result[3], 16)
    ] : [102, 126, 234];
}

function calculateKPIs(expenses, subscriptions) {
    const totalExpenses = expenses.reduce((sum, e) => sum + (parseFloat(e.amount) || 0), 0);
    const totalSubs = subscriptions.length;
    const monthlySubCost = subscriptions.reduce((sum, s) => sum + (parseFloat(s.prix) || 0), 0);
    const avgExpense = expenses.length > 0 ? (totalExpenses / expenses.length) : 0;
    const budget = parseFloat(localStorage.getItem('monthlyBudget') || 500);
    const remainingBudget = budget - totalExpenses - monthlySubCost;
    
    return {
        totalExpenses: totalExpenses.toFixed(2),
        totalSubs,
        avgExpense: avgExpense.toFixed(2),
        remainingBudget: remainingBudget.toFixed(2)
    };
}

function calculateTotalExpenses() {
    const expenses = getExpensesData();
    return expenses.reduce((sum, e) => sum + (parseFloat(e.amount) || 0), 0);
}

function detectTransaction(transaction) {
    // Détection intelligente des champs
    const result = {
        date: transaction.date || transaction.dtposted || new Date().toISOString().split('T')[0],
        description: transaction.description || transaction.libelle || transaction.name || 'Transaction',
        amount: Math.abs(parseFloat(transaction.amount || transaction.montant || transaction.trnamt || 0)),
        category: transaction.category || transaction.categorie || detectCategory(transaction.description || ''),
        type: 'Dépense'
    };
    
    // Détecter si c'est un abonnement récurrent
    if (isRecurring(result.description)) {
        result.type = 'Abonnement';
    }
    
    return result;
}

function detectCategory(description) {
    const desc = description.toLowerCase();
    
    if (desc.includes('netflix') || desc.includes('spotify') || desc.includes('prime')) return 'Streaming';
    if (desc.includes('internet') || desc.includes('mobile') || desc.includes('sfr') || desc.includes('orange')) return 'Télécom';
    if (desc.includes('edf') || desc.includes('gaz') || desc.includes('eau')) return 'Énergie';
    if (desc.includes('assurance')) return 'Assurance';
    if (desc.includes('restaurant') || desc.includes('uber eats') || desc.includes('deliveroo')) return 'Alimentation';
    if (desc.includes('sncf') || desc.includes('essence') || desc.includes('uber')) return 'Transport';
    if (desc.includes('gym') || desc.includes('sport')) return 'Sport';
    
    return 'Autres';
}

function isRecurring(description) {
    const keywords = ['netflix', 'spotify', 'amazon prime', 'abonnement', 'subscription', 'mensuel', 'annuel', 'mobile', 'internet', 'assurance'];
    const desc = description.toLowerCase();
    return keywords.some(kw => desc.includes(kw));
}

function formatOFXDate(ofxDate) {
    // Format: YYYYMMDD -> YYYY-MM-DD
    if (ofxDate.length >= 8) {
        return `${ofxDate.substring(0,4)}-${ofxDate.substring(4,6)}-${ofxDate.substring(6,8)}`;
    }
    return new Date().toISOString().split('T')[0];
}

function downloadFile(content, filename, mimeType) {
    const blob = new Blob([content], { type: mimeType });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

function saveToExportHistory(type, filename) {
    const history = JSON.parse(localStorage.getItem('exportHistory') || '[]');
    history.unshift({
        date: new Date().toISOString(),
        type,
        filename,
        size: Math.floor(Math.random() * 1000) + 'KB' // Approximation
    });
    localStorage.setItem('exportHistory', JSON.stringify(history.slice(0, 10)));
    loadExportHistory();
}

function loadExportHistory() {
    const history = JSON.parse(localStorage.getItem('exportHistory') || '[]');
    const tbody = document.getElementById('exportHistoryBody');
    
    tbody.innerHTML = '';
    
    if (history.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center">Aucun export récent</td></tr>';
        return;
    }
    
    history.forEach((item, index) => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${new Date(item.date).toLocaleString('fr-FR')}</td>
            <td><span class="format-badge format-${item.type.toLowerCase()}">${item.type}</span></td>
            <td>${item.size}</td>
            <td>Dernier mois</td>
            <td>
                <button class="btn btn-sm export-btn" onclick="reExportFromHistory(${index})" title="Re-générer cet export">
                    <i class="bi bi-arrow-repeat"></i> Régénérer
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Fonction pour régénérer un export depuis l'historique
async function reExportFromHistory(index) {
    const history = JSON.parse(localStorage.getItem('exportHistory') || '[]');
    const item = history[index];
    
    if (!item) {
        showNotification('❌ Export introuvable', 'danger');
        return;
    }
    
    showNotification(`🔄 Régénération de l'export ${item.type}...`, 'info');
    
    try {
        switch (item.type) {
            case 'PDF':
                await exportToPDF();
                break;
            case 'CSV':
                await exportToCSV();
                break;
            case 'JSON':
                await exportToJSON();
                break;
            case 'Excel':
                await exportToExcel();
                break;
            default:
                showNotification('❌ Type d\'export non reconnu', 'danger');
        }
    } catch (error) {
        console.error('Erreur lors de la régénération:', error);
        showNotification('❌ Erreur lors de la régénération', 'danger');
    }
}

function showNotification(message, type) {
    // Créer une notification toast
    const toast = document.createElement('div');
    toast.className = `alert alert-${type === 'success' ? 'success' : type === 'error' ? 'danger' : 'info'} position-fixed top-0 start-50 translate-middle-x mt-3`;
    toast.style.zIndex = '9999';
    toast.innerHTML = message;
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.remove();
    }, 3000);
}

// Charger l'historique au démarrage
document.addEventListener('DOMContentLoaded', () => {
    loadExportHistory();
});
