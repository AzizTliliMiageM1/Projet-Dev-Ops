/**
 * Module UI - Rendu DOM simple et lisible
 * Pas d'animations complexes, juste de la logique claire
 */

// ==================== UTILITAIRES ====================

/**
 * Efface tous les enfants d'un élément
 */
function clearElement(element) {
  while (element.firstChild) {
    element.removeChild(element.firstChild);
  }
}

/**
 * Crée un élément avec classe et contenu
 */
function createElement(tag, className = '', text = '') {
  const el = document.createElement(tag);
  if (className) el.className = className;
  if (text) el.textContent = text;
  return el;
}

/**
 * Formate une date pour affichage
 */
function formatDate(dateString) {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
  return date.toLocaleDateString('fr-FR');
}

/**
 * Formate un montant en euros
 */
function formatCurrency(amount) {
  if (!amount) return '0,00 €';
  return new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency: 'EUR'
  }).format(amount);
}

// ==================== DASHBOARD ====================

/**
 * Affiche les cartes KPI du dashboard
 * @param {Object} analytics - Données analytics du backend
 */
function renderDashboardKPIs(analytics) {
  const container = document.getElementById('kpi-container');
  if (!container) return;
  
  clearElement(container);
  
  const kpis = [
    {
      title: 'Abonnements actifs',
      value: analytics.totalAbonnements || 0,
      icon: '📋'
    },
    {
      title: 'Coût mensuel',
      value: formatCurrency(analytics.coutMensuel || 0),
      icon: '💰'
    },
    {
      title: 'Santé du portefeuille',
      value: `${analytics.scoreSante || 0}%`,
      icon: '📊'
    },
    {
      title: 'À risque',
      value: analytics.abonnementsAuRisque || 0,
      icon: '⚠️'
    }
  ];
  
  kpis.forEach(kpi => {
    const card = createElement('div', 'kpi-card');
    card.innerHTML = `
      <div class="kpi-icon">${kpi.icon}</div>
      <div class="kpi-content">
        <div class="kpi-title">${kpi.title}</div>
        <div class="kpi-value">${kpi.value}</div>
      </div>
    `;
    container.appendChild(card);
  });
}

/**
 * Affiche la liste des abonnements à risque
 * @param {Array} abonnements - Liste des abonnements à risque
 */
function renderRiskList(abonnements) {
  const container = document.getElementById('risk-list');
  if (!container) return;
  
  clearElement(container);
  
  if (!abonnements || abonnements.length === 0) {
    container.innerHTML = '<p class="text-muted">Aucun abonnement à risque</p>';
    return;
  }
  
  const table = createElement('table', 'table table-sm');
  const thead = createElement('thead');
  thead.innerHTML = `
    <tr>
      <th>Client</th>
      <th>Service</th>
      <th>Expiration</th>
      <th>Risque</th>
    </tr>
  `;
  table.appendChild(thead);
  
  const tbody = createElement('tbody');
  abonnements.forEach(ab => {
    const tr = createElement('tr');
    tr.innerHTML = `
      <td>${ab.client || 'N/A'}</td>
      <td>${ab.service || 'N/A'}</td>
      <td>${formatDate(ab.dateExpiration)}</td>
      <td><span class="badge bg-danger">Élevé</span></td>
    `;
    tbody.appendChild(tr);
  });
  table.appendChild(tbody);
  
  container.appendChild(table);
}

// ==================== SUBSCRIPTIONS CRUD ====================

/**
 * Affiche la liste des abonnements dans un tableau
 * @param {Array} abonnements - Liste des abonnements
 */
function renderSubscriptionsTable(abonnements) {
  const container = document.getElementById('subscriptions-table');
  if (!container) return;
  
  clearElement(container);
  
  if (!abonnements || abonnements.length === 0) {
    container.innerHTML = '<p class="text-muted">Aucun abonnement trouvé</p>';
    return;
  }
  
  const table = createElement('table', 'table');
  const thead = createElement('thead');
  thead.innerHTML = `
    <tr>
      <th>#</th>
      <th>Client</th>
      <th>Service</th>
      <th>Coût mensuel</th>
      <th>Expiration</th>
      <th>Actions</th>
    </tr>
  `;
  table.appendChild(thead);
  
  const tbody = createElement('tbody');
  abonnements.forEach((ab, idx) => {
    const tr = createElement('tr');
    tr.innerHTML = `
      <td>${idx + 1}</td>
      <td>${ab.client || 'N/A'}</td>
      <td>${ab.service || 'N/A'}</td>
      <td>${formatCurrency(ab.coutMensuel)}</td>
      <td>${formatDate(ab.dateExpiration)}</td>
      <td>
        <button class="btn btn-sm btn-primary" onclick="editSubscriptionId('${ab.id}')">
          ✏️ Modifier
        </button>
        <button class="btn btn-sm btn-danger" onclick="deleteSubscriptionId('${ab.id}')">
          🗑️ Supprimer
        </button>
      </td>
    `;
    tbody.appendChild(tr);
  });
  table.appendChild(tbody);
  
  container.appendChild(table);
}

/**
 * Affiche le formulaire de création/modification
 * @param {Object} abonnement - null pour création, données pour modification
 */
function renderSubscriptionForm(abonnement = null) {
  const container = document.getElementById('subscription-form');
  if (!container) return;
  
  clearElement(container);
  
  const form = createElement('form', 'subscription-form');
  form.innerHTML = `
    <div class="form-group">
      <label>Client</label>
      <input type="text" name="client" class="form-control" value="${abonnement?.client || ''}" required>
    </div>
    
    <div class="form-group">
      <label>Service</label>
      <input type="text" name="service" class="form-control" value="${abonnement?.service || ''}" required>
    </div>
    
    <div class="form-group">
      <label>Coût mensuel (€)</label>
      <input type="number" name="coutMensuel" class="form-control" step="0.01" value="${abonnement?.coutMensuel || ''}" required>
    </div>
    
    <div class="form-group">
      <label>Date d'expiration</label>
      <input type="date" name="dateExpiration" class="form-control" value="${abonnement?.dateExpiration || ''}" required>
    </div>
    
    <div class="form-group">
      <button type="submit" class="btn btn-success">
        ${abonnement ? '✔️ Mettre à jour' : '➕ Créer'}
      </button>
    </div>
  `;
  
  form.onsubmit = async (e) => {
    e.preventDefault();
    const formData = new FormData(form);
    const data = {
      client: formData.get('client'),
      service: formData.get('service'),
      coutMensuel: parseFloat(formData.get('coutMensuel')),
      dateExpiration: formData.get('dateExpiration')
    };
    
    if (abonnement) {
      const result = await updateAbonnement(abonnement.id, data);
    } else {
      const result = await createAbonnement(data);
    }
    
    // Recharger la liste
    loadSubscriptionsPage();
  };
  
  container.appendChild(form);
}

// ==================== STATS ====================

/**
 * Affiche les statistiques en cartes
 * @param {Object} stats - Données stats du backend
 */
function renderStats(stats) {
  const container = document.getElementById('stats-container');
  if (!container) return;
  
  clearElement(container);
  
  const statsArray = [
    { label: 'Total annuel', value: formatCurrency(stats.totalAnnuel || 0) },
    { label: 'Abonnements actifs', value: stats.abonnementsActifs || 0 },
    { label: 'Moyenne par mois', value: formatCurrency(stats.moyenneMensuelle || 0) },
    { label: 'Économies potentielles', value: formatCurrency(stats.economiePotentielle || 0) }
  ];
  
  statsArray.forEach(stat => {
    const card = createElement('div', 'stat-card');
    card.innerHTML = `
      <div class="stat-label">${stat.label}</div>
      <div class="stat-value">${stat.value}</div>
    `;
    container.appendChild(card);
  });
}

/**
 * Affiche un message de succès
 */
function showSuccess(message) {
  const alertDiv = createElement('div', 'alert alert-success');
  alertDiv.textContent = message;
  document.body.insertBefore(alertDiv, document.body.firstChild);
  setTimeout(() => alertDiv.remove(), 3000);
}

/**
 * Affiche un message d'erreur
 */
function showError(message) {
  const alertDiv = createElement('div', 'alert alert-danger');
  alertDiv.textContent = message;
  document.body.insertBefore(alertDiv, document.body.firstChild);
  setTimeout(() => alertDiv.remove(), 5000);
}

/**
 * Affiche un message de chargement
 */
function showLoading(message = 'Chargement...') {
  const loadDiv = createElement('div', 'alert alert-info');
  loadDiv.textContent = message;
  return document.body.insertBefore(loadDiv, document.body.firstChild);
}
