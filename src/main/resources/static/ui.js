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

// ==================== RECOMMANDATIONS ====================

function renderRecommendations(data) {
  const container = document.getElementById('recommendations-content');
  if (!container) return;

  clearElement(container);

  if (!data) {
    container.appendChild(createElement('p', 'text-muted', 'Aucune recommandation disponible.'));
    return;
  }

  const sections = [
    {
      title: 'Abonnements à risque élevé',
      icon: '⚠️',
      items: data.highRisk || [],
      renderItem: (abo) => {
        const risk = typeof abo.churnRisk === 'number' ? Math.round(abo.churnRisk) : 0;
        return `${abo.nomService || 'Service'} · ${risk}% de risque`;
      }
    },
    {
      title: 'Abonnements peu rentables',
      icon: '📉',
      items: data.lowValue || [],
      renderItem: (abo) => {
        const score = typeof abo.valueScore === 'number' ? abo.valueScore : null;
        return `${abo.nomService || 'Service'} · Score valeur ${score !== null ? score.toFixed(2) : 'N/A'}`;
      }
    },
    {
      title: 'Expirations proches (<30 jours)',
      icon: '⏰',
      items: data.upcomingExpirations || [],
      renderItem: (abo) => `${abo.nomService || 'Service'} · se termine le ${formatDate(abo.dateFin)}`
    }
  ];

  const savingsWrapper = createElement('div', 'recommendation-block savings-block');
  const savingsTitle = createElement('h4', '', '💡 Économies potentielles');
  savingsWrapper.appendChild(savingsTitle);

  const totalSavings = data.savings?.totalMonthly ? formatCurrency(data.savings.totalMonthly) : '0,00 €';
  savingsWrapper.appendChild(createElement('p', 'text-highlight', `Total estimé : ${totalSavings}/mois`));

  const savingsList = createElement('ul', 'recommendation-list');
  (data.savings?.candidates || []).forEach((abo) => {
    const li = createElement('li');
    li.textContent = `${abo.nomService || 'Service'} · ${formatCurrency(abo.prixMensuel)} - Dernière utilisation : ${formatDate(abo.derniereUtilisation)}`;
    savingsList.appendChild(li);
  });
  if (savingsList.childElementCount === 0) {
    savingsList.appendChild(createElement('li', 'text-muted', 'Aucun abonnement à supprimer pour le moment.'));
  }
  savingsWrapper.appendChild(savingsList);

  sections.forEach((section) => {
    const block = createElement('div', 'recommendation-block');
    const title = createElement('h4');
    title.textContent = `${section.icon} ${section.title}`;
    block.appendChild(title);

    if (!section.items.length) {
      block.appendChild(createElement('p', 'text-muted', 'Rien à signaler.'));
    } else {
      const list = createElement('ul', 'recommendation-list');
      section.items.forEach((item) => {
        const li = createElement('li');
        li.textContent = section.renderItem(item);
        list.appendChild(li);
      });
      block.appendChild(list);
    }

    container.appendChild(block);
  });

  container.prepend(savingsWrapper);
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
      <td>${ab.clientName || 'N/A'}</td>
      <td>${ab.nomService || 'N/A'}</td>
      <td>${formatDate(ab.dateFin)}</td>
      <td><span class="badge bg-danger">${typeof ab.churnRisk === 'number' ? Math.round(ab.churnRisk) : 0}%</span></td>
    `;
    tbody.appendChild(tr);
  });
  table.appendChild(tbody);
  
  container.appendChild(table);
}

// ==================== BUDGET PLAN ====================

function renderBudgetPlanLoading() {
  const container = document.getElementById('budget-plan-result');
  if (!container) return;
  container.innerHTML = '<p class="text-muted">Calcul du plan budgétaire...</p>';
}

function renderBudgetPlanError(message) {
  const container = document.getElementById('budget-plan-result');
  if (!container) return;
  clearElement(container);
  const p = createElement('p', 'text-danger', message);
  container.appendChild(p);
}

function renderBudgetPlanPanel(plan) {
  const container = document.getElementById('budget-plan-result');
  if (!container) return;

  if (!plan) {
    renderBudgetPlanError('Plan budgétaire indisponible.');
    return;
  }

  clearElement(container);

  const summaryValues = [
    { label: 'Budget actuel', value: formatCurrency(plan.currentMonthlyCost || 0) },
    { label: 'Budget cible', value: formatCurrency(plan.targetMonthlyBudget || 0) },
    { label: 'Économies à réaliser', value: formatCurrency(plan.requiredSavings || 0) },
    { label: 'Économies planifiées', value: formatCurrency(plan.achievedSavings || 0) },
    { label: 'Écart restant', value: formatCurrency(plan.shortfall || 0) }
  ];

  const summaryGrid = createElement('div', 'budget-summary-grid');
  summaryValues.forEach((item) => {
    const card = createElement('div', 'budget-summary-item');
    card.appendChild(createElement('div', 'budget-summary-label', item.label));
    card.appendChild(createElement('div', 'budget-summary-value', item.value));
    summaryGrid.appendChild(card);
  });
  container.appendChild(summaryGrid);

  const status = createElement('div', 'budget-status');
  status.classList.add(plan.targetFeasible ? 'budget-status-success' : 'budget-status-warning');
  status.textContent = plan.targetFeasible
    ? 'Plan atteignable : la cible peut être atteinte avec ces résiliations.'
    : 'Plan incomplet : la cible n\'est pas atteinte, ajustez le budget ou ajoutez des résiliations.';
  container.appendChild(status);

  const prioritySection = createElement('div', 'budget-section');
  prioritySection.appendChild(createElement('h4', 'budget-subtitle', 'Priorités de résiliation'));
  const priorityList = createElement('ul', 'budget-list');
  const priorities = Array.isArray(plan.recommendedCancellations) ? plan.recommendedCancellations : [];
  if (!priorities.length) {
    priorityList.appendChild(createElement('li', 'text-muted', 'Aucun abonnement à résilier pour atteindre la cible.'));
  } else {
    priorities.forEach((abo) => {
      const monthly = formatCurrency(Number(abo.prixMensuel) || 0);
      const risk = typeof abo.churnRisk === 'number' ? Math.round(abo.churnRisk) : 0;
      const valueScore = typeof abo.valueScore === 'number' ? abo.valueScore.toFixed(2) : 'N/A';
      const li = createElement('li', 'budget-list-item');
      li.textContent = `${abo.nomService || 'Service'} · ${monthly}/mois · Risque ${risk}% · Valeur ${valueScore}`;
      priorityList.appendChild(li);
    });
  }
  prioritySection.appendChild(priorityList);
  container.appendChild(prioritySection);

  const optionalSection = createElement('div', 'budget-section');
  optionalSection.appendChild(createElement('h4', 'budget-subtitle', 'Options supplémentaires'));
  const optionalList = createElement('ul', 'budget-list');
  const optionals = Array.isArray(plan.optionalCandidates) ? plan.optionalCandidates : [];
  if (!optionals.length) {
    optionalList.appendChild(createElement('li', 'text-muted', 'Aucun autre abonnement proposé.'));
  } else {
    optionals.forEach((abo, index) => {
      const monthly = formatCurrency(Number(abo.prixMensuel) || 0);
      const li = createElement('li', 'budget-list-item');
      li.textContent = `${index + 1}. ${abo.nomService || 'Service'} · ${monthly}/mois`;
      optionalList.appendChild(li);
    });
  }
  optionalSection.appendChild(optionalList);
  container.appendChild(optionalSection);
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
      <td>${ab.clientName || 'N/A'}</td>
      <td>${ab.nomService || 'N/A'}</td>
      <td>${formatCurrency(Number(ab.prixMensuel) || 0)}</td>
      <td>${formatDate(ab.dateFin)}</td>
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
      <input type="text" name="clientName" class="form-control" value="${abonnement?.clientName || ''}" required>
    </div>
    
    <div class="form-group">
      <label>Service</label>
      <input type="text" name="nomService" class="form-control" value="${abonnement?.nomService || ''}" required>
    </div>
    
    <div class="form-group">
      <label>Date de début</label>
      <input type="date" name="dateDebut" class="form-control" value="${abonnement?.dateDebut || ''}" required>
    </div>
    
    <div class="form-group">
      <label>Date de fin</label>
      <input type="date" name="dateFin" class="form-control" value="${abonnement?.dateFin || ''}" required>
    </div>

    <div class="form-group">
      <label>Coût mensuel (€)</label>
      <input type="number" name="prixMensuel" class="form-control" step="0.01" value="${abonnement?.prixMensuel || ''}" required>
    </div>

    <div class="form-group">
      <label>Catégorie</label>
      <input type="text" name="categorie" class="form-control" value="${abonnement?.categorie || ''}">
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
    const payload = {
      clientName: formData.get('clientName'),
      nomService: formData.get('nomService'),
      dateDebut: formData.get('dateDebut'),
      dateFin: formData.get('dateFin'),
      prixMensuel: parseFloat(formData.get('prixMensuel')) || 0,
      categorie: formData.get('categorie') || 'Non classé'
    };
    if (abonnement) {
      payload.id = abonnement.id;
      payload.notes = abonnement.notes || '';
      payload.derniereUtilisation = abonnement.derniereUtilisation || null;
      payload.tags = abonnement.tags || [];
      payload.nombreUtilisateurs = abonnement.nombreUtilisateurs || 1;
      payload.partage = abonnement.partage || false;
      payload.joursRappelAvantFin = abonnement.joursRappelAvantFin || 0;
      payload.frequencePaiement = abonnement.frequencePaiement || 'Mensuel';
    }

    try {
      if (abonnement) {
        await updateAbonnement(abonnement.id, payload);
        showSuccess('Abonnement mis à jour');
      } else {
        await createAbonnement(payload);
        showSuccess('Abonnement créé');
      }
      await loadSubscriptionsPage();
    } catch (error) {
      console.error('Erreur formulaire abonnement:', error);
      showError(`Impossible d'enregistrer l'abonnement: ${error.message}`);
    }
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
