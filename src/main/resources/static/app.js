const apiBase = '/api/abonnements';

/* ---------- helpers ---------- */
function escapeHtml(s){
  return String(s ?? "")
    .replace(/&/g,"&amp;")
    .replace(/</g,"&lt;")
    .replace(/>/g,"&gt;")
    .replace(/"/g,"&quot;")
    .replace(/'/g,"&#39;");
}

async function fetchAll(){
  const r = await fetch(apiBase);
  if (!r.ok) return [];
  return await r.json();
}

function isActive(a){
  const now = new Date();
  const d0 = new Date(a.dateDebut);
  const d1 = new Date(a.dateFin);
  return now >= d0 && now <= d1;
}

function daysBetween(d1, d2){
  const diff = Math.floor((d2 - d1) / (1000*60*60*24));
  return diff;
}

/* ---------- dashboard ---------- */
function renderDashboard(list){
  const dash = document.getElementById('dashboard');
  if (!dash) return;
  
  const total = list.length;
  const active = list.filter(isActive).length;
  const expired = total - active;
  const coutTotal = list.filter(isActive).reduce((sum, a) => sum + (parseFloat(a.prixMensuel) || 0), 0);
  const alerts = list.filter(a => isActive(a) && a.derniereUtilisation && daysBetween(new Date(a.derniereUtilisation), new Date()) > 30).length;

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
      <div class="metric-icon"><i class="bi bi-currency-euro"></i></div>
      <div class="metric-value">${coutTotal.toFixed(0)}€</div>
      <div class="metric-label">Coût mensuel</div>
    </div>
    <div class="metric-card animate-slide-in" style="animation-delay: 0.3s">
      <div class="metric-icon"><i class="bi bi-exclamation-triangle"></i></div>
      <div class="metric-value">${alerts}</div>
      <div class="metric-label">Alertes</div>
    </div>
  `;
}

/* ---------- cartes modernes ---------- */
function render(list){
  const cards = document.getElementById('cards');
  if (!cards) return;
  
  // Vider le conteneur
  cards.innerHTML = '';
  
  // Si la liste est vide, afficher l'état vide
  if (list.length === 0) {
    cards.innerHTML = `
      <div class="empty-state" data-empty-hint>
        <div class="empty-icon"><i class="bi bi-inbox"></i></div>
        <h3 class="empty-title">Aucun abonnement</h3>
        <p class="empty-text">Commencez par ajouter votre premier service via le panneau à droite</p>
      </div>
    `;
    return;
  }

  list.forEach((a, index) => {
    const clientName = a.nomClient || a.clientName || 'Client';
    const nomService = a.nomService || 'Service';
    const dateDebut = a.dateDebut || '-';
    const dateFin = a.dateFin || '-';
    const categorie = a.categorie || 'Autres';
    const estActif = isActive(a);
    const derniereUtilisation = a.derniereUtilisation
      ? new Date(a.derniereUtilisation).toLocaleDateString('fr-FR')
      : 'Jamais';
    const prixMensuel = Number(a.prixMensuel || 0);
    
    // Calculer le statut
    let statusClass = 'status-expired';
    let statusText = 'Expiré';
    let statusIcon = 'bi-x-circle';
    
    if (estActif) {
      const now = new Date();
      const dateFin = new Date(a.dateFin);
      const joursRestants = Math.ceil((dateFin - now) / (1000 * 60 * 60 * 24));
      
      if (joursRestants <= 7) {
        statusClass = 'status-warning';
        statusText = `Expire dans ${joursRestants}j`;
        statusIcon = 'bi-exclamation-triangle';
      } else {
        statusClass = 'status-active';
        statusText = 'Actif';
        statusIcon = 'bi-check-circle';
      }
    }

    const card = document.createElement('div');
    card.className = 'subscription-card animate-slide-in';
    card.style.animationDelay = `${index * 0.1}s`;
    
    card.innerHTML = `
      <div class="card-header-modern">
        <div class="service-name">
          <i class="bi bi-star-fill me-2" style="color: #ffd700;"></i>
          ${escapeHtml(nomService)}
        </div>
        <div class="client-name">
          <i class="bi bi-person me-1"></i>
          ${escapeHtml(clientName)}
        </div>
      </div>
      
      <div class="card-body-modern">
        <div class="status-badge ${statusClass}">
          <i class="${statusIcon} me-1"></i>
          ${statusText}
        </div>
        
        <div class="price-display">
          ${prixMensuel.toFixed(2)}€<span style="font-size: 1rem; opacity: 0.8;">/mois</span>
        </div>
        
        <div class="date-info">
          <i class="bi bi-calendar-range me-1"></i>
          Du ${escapeHtml(dateDebut)} au ${escapeHtml(dateFin)}
        </div>
        
        <div class="date-info">
          <i class="bi bi-tag me-1"></i>
          Catégorie: ${escapeHtml(categorie)}
        </div>
        
        <div class="date-info">
          <i class="bi bi-clock-history me-1"></i>
          Dernière utilisation: ${escapeHtml(derniereUtilisation)}
        </div>
        
        <div class="card-actions">
          <button class="btn btn-action" onclick="markAsUsed(${index})" title="Marquer comme utilisé">
            <i class="bi bi-check2"></i> Utilisé
          </button>
          <button class="btn btn-action" onclick="editAbonnement(${index})" title="Modifier">
            <i class="bi bi-pencil"></i> Modifier
          </button>
          <button class="btn btn-action" onclick="deleteAbonnement(${index})" title="Supprimer">
            <i class="bi bi-trash"></i> Supprimer
          </button>
        </div>
      </div>
    `;
    
    cards.appendChild(card);
  });
}

/* ---------- fonctions d'action ---------- */
async function markAsUsed(index) {
  try {
    const abonnements = await fetchAll();
    const abo = abonnements[index];
    if (!abo) return;
    
    abo.derniereUtilisation = new Date().toISOString().split('T')[0];
    
    const response = await fetch(`${apiBase}/${index}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(abo)
    });
    
    if (response.ok) {
      showFlash('Service marqué comme utilisé !', 'success');
      await loadAndRender();
    } else {
      showFlash('Erreur lors de la mise à jour', 'error');
    }
  } catch (error) {
    showFlash('Erreur: ' + error.message, 'error');
  }
}

async function deleteAbonnement(index) {
  if (!confirm('Êtes-vous sûr de vouloir supprimer cet abonnement ?')) return;
  
  try {
    const response = await fetch(`${apiBase}/${index}`, { method: 'DELETE' });
    
    if (response.ok) {
      showFlash('Abonnement supprimé avec succès', 'success');
      await loadAndRender();
    } else {
      showFlash('Erreur lors de la suppression', 'error');
    }
  } catch (error) {
    showFlash('Erreur: ' + error.message, 'error');
  }
}

async function editAbonnement(index) {
  try {
    const abonnements = await fetchAll();
    const abo = abonnements[index];
    if (!abo) return;
    
    // Remplir le modal d'édition
    document.getElementById('editIndex').value = index;
    document.getElementById('editClient').value = abo.nomClient || '';
    document.getElementById('editService').value = abo.nomService || '';
    document.getElementById('editDebut').value = abo.dateDebut || '';
    document.getElementById('editFin').value = abo.dateFin || '';
    document.getElementById('editCategorie').value = abo.categorie || '';
    document.getElementById('editPrix').value = abo.prixMensuel || '';
    
    // Afficher le modal
    const modal = new bootstrap.Modal(document.getElementById('editModal'));
    modal.show();
  } catch (error) {
    showFlash('Erreur: ' + error.message, 'error');
  }
}

function showFlash(message, type = 'info') {
  const flash = document.getElementById('flash');
  if (!flash) return;
  
  const alertClass = type === 'success' ? 'alert-success' : type === 'error' ? 'alert-danger' : 'alert-info';
  const icon = type === 'success' ? 'bi-check-circle' : type === 'error' ? 'bi-x-circle' : 'bi-info-circle';
  
  flash.innerHTML = `
    <div class="flash-message alert ${alertClass} alert-dismissible fade show">
      <i class="${icon} me-2"></i>
      ${escapeHtml(message)}
      <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
  `;
  
  // Auto-hide après 5 secondes
  setTimeout(() => {
    const alertElement = flash.querySelector('.alert');
    if (alertElement) {
      alertElement.remove();
    }
  }, 5000);
}

/* ---------- chargement + filtres ---------- */
async function loadAndRender(){
  const all = await fetchAll();
  renderDashboard(all);

  // apply search/filter/sort
  const q = (document.getElementById('searchInput')?.value || '').toLowerCase().trim();
  const status = (document.getElementById('statusFilter')?.value || '');
  const sortBy = (document.getElementById('sortSelect')?.value || 'date-desc');

  let filtered = all.filter(a => {
    if (!q) return true;
    const searchText = `${a.nomClient || ''} ${a.nomService || ''} ${a.categorie || ''}`.toLowerCase();
    return searchText.includes(q);
  });

  // Filtrage par statut
  if (status === 'actif') filtered = filtered.filter(isActive);
  else if (status === 'expire') filtered = filtered.filter(a => !isActive(a));
  else if (status === 'bientot-expire') {
    filtered = filtered.filter(a => {
      if (!isActive(a)) return false;
      const now = new Date();
      const dateFin = new Date(a.dateFin);
      const joursRestants = Math.ceil((dateFin - now) / (1000 * 60 * 60 * 24));
      return joursRestants <= 7;
    });
  }

  // Tri
  if (sortBy === 'client-asc') filtered.sort((a,b) => (a.nomClient||'').localeCompare(b.nomClient||''));
  else if (sortBy === 'client-desc') filtered.sort((a,b) => (b.nomClient||'').localeCompare(a.nomClient||''));
  else if (sortBy === 'date-asc') filtered.sort((a,b) => new Date(a.dateDebut) - new Date(b.dateDebut));
  else if (sortBy === 'date-desc') filtered.sort((a,b) => new Date(b.dateDebut) - new Date(a.dateDebut));
  else if (sortBy === 'prix-asc') filtered.sort((a,b) => (parseFloat(a.prixMensuel) || 0) - (parseFloat(b.prixMensuel) || 0));
  else if (sortBy === 'prix-desc') filtered.sort((a,b) => (parseFloat(b.prixMensuel) || 0) - (parseFloat(a.prixMensuel) || 0));

  render(filtered);
}

// Alias pour compatibilité
const load = loadAndRender;

/* ---------- ajout ---------- */
document.addEventListener('DOMContentLoaded', function() {
  document.getElementById('addForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
      nomClient: document.getElementById('nomClient').value,
      nomService: document.getElementById('nomService').value,
      dateDebut: document.getElementById('dateDebut').value,
      dateFin: document.getElementById('dateFin').value,
      prixMensuel: parseFloat(document.getElementById('prixMensuel').value) || 0,
      categorie: document.getElementById('categorie').value || 'Autres'
    };
    
    const btn = e.target.querySelector('button[type="submit"]');
    const original = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<i class="bi bi-arrow-repeat spinner-border spinner-border-sm me-1"></i>Ajout...';
    
    try {
      const r = await fetch(apiBase, {
        method: 'POST', 
        headers: {'Content-Type': 'application/json'}, 
        body: JSON.stringify(payload)
      });
      
      if (r.status === 201) {
        const created = await r.json();
        showFlash(`Service ${payload.nomService} ajouté avec succès !`, 'success');
        document.getElementById('addForm').reset();
        await loadAndRender();
      } else {
        let msg = 'Erreur lors de la création';
        try { 
          const err = await r.json(); 
          if (err && err.error) msg = err.error; 
          if (err && err.message) msg = err.message; 
        } catch(e){}
        showFlash(msg, 'error');
      }
    } catch (err) {
      showFlash(`Erreur réseau: ${err.message}`, 'error');
    } finally {
      btn.disabled = false;
      btn.innerHTML = original;
    }
  });

  /* ---------- modal d'édition ---------- */
  document.getElementById('saveEditBtn').addEventListener('click', async () => {
    const index = document.getElementById('editIndex').value;
    const payload = {
      nomClient: document.getElementById('editClient').value,
      nomService: document.getElementById('editService').value,
      dateDebut: document.getElementById('editDebut').value,
      dateFin: document.getElementById('editFin').value,
      prixMensuel: parseFloat(document.getElementById('editPrix').value) || 0,
      categorie: document.getElementById('editCategorie').value || 'Autres'
    };
    
    try {
      const r = await fetch(`${apiBase}/${index}`, {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(payload)
      });
      
      if (r.ok) {
        showFlash('Abonnement modifié avec succès !', 'success');
        const modal = bootstrap.Modal.getInstance(document.getElementById('editModal'));
        modal.hide();
        await loadAndRender();
      } else {
        showFlash('Erreur lors de la modification', 'error');
      }
    } catch (err) {
      showFlash(`Erreur: ${err.message}`, 'error');
    }
  });

  /* ---------- actions rapides ---------- */
  document.getElementById('refreshBtn').addEventListener('click', async () => {
    const btn = document.getElementById('refreshBtn');
    const original = btn.innerHTML;
    btn.innerHTML = '<i class="bi bi-arrow-repeat" style="animation: spin 1s linear infinite;"></i> Sync';
    await loadAndRender();
    setTimeout(() => {
      btn.innerHTML = original;
    }, 1000);
  });

  document.getElementById('exportBtn').addEventListener('click', async () => {
    const data = await fetchAll();
    const blob = new Blob([JSON.stringify(data, null, 2)], {type: 'application/json'});
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `abonnements_export_${new Date().toISOString().split('T')[0]}.json`;
    a.click();
    URL.revokeObjectURL(url);
    showFlash('Export terminé !', 'success');
  });

  document.getElementById('clearAllBtn').addEventListener('click', async () => {
    if (!confirm('⚠️ Êtes-vous sûr de vouloir supprimer TOUS les abonnements ?\n\nCette action est irréversible !')) return;
    
    try {
      const abonnements = await fetchAll();
      for (let i = abonnements.length - 1; i >= 0; i--) {
        await fetch(`${apiBase}/${i}`, { method: 'DELETE' });
      }
      showFlash('Tous les abonnements ont été supprimés', 'success');
      await loadAndRender();
    } catch (error) {
      showFlash('Erreur lors de la suppression', 'error');
    }
  });

  document.getElementById('importFile').addEventListener('change', async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    
    try {
      const text = await file.text();
      const data = JSON.parse(text);
      
      if (Array.isArray(data)) {
        for (const item of data) {
          await fetch(apiBase, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(item)
          });
        }
        showFlash(`${data.length} abonnements importés avec succès !`, 'success');
        await loadAndRender();
      } else {
        showFlash('Format de fichier invalide', 'error');
      }
    } catch (error) {
      showFlash('Erreur lors de l\'import: ' + error.message, 'error');
    }
    e.target.value = '';
  });

  document.getElementById('alertesBtn').addEventListener('click', async () => {
    const abonnements = await fetchAll();
    const alertes = [];
    
    abonnements.forEach((abo, index) => {
      if (isActive(abo)) {
        const now = new Date();
        const dateFin = new Date(abo.dateFin);
        const joursRestants = Math.ceil((dateFin - now) / (1000 * 60 * 60 * 24));
        
        if (joursRestants <= 7) {
          alertes.push(`${abo.nomService} (${abo.nomClient}) expire dans ${joursRestants} jour(s)`);
        }
        
        if (abo.derniereUtilisation) {
          const dernierUtilisation = new Date(abo.derniereUtilisation);
          const joursInactivite = Math.ceil((now - dernierUtilisation) / (1000 * 60 * 60 * 24));
          
          if (joursInactivite > 30) {
            alertes.push(`${abo.nomService} (${abo.nomClient}) non utilisé depuis ${joursInactivite} jours`);
          }
        }
      }
    });
    
    if (alertes.length > 0) {
      alert('🚨 ALERTES DÉTECTÉES 🚨\n\n' + alertes.join('\n\n'));
    } else {
      showFlash('✅ Aucune alerte détectée !', 'success');
    }
  });

  /* ---------- filtres en temps réel ---------- */
  document.getElementById('searchInput').addEventListener('input', loadAndRender);
  document.getElementById('statusFilter').addEventListener('change', loadAndRender);
  document.getElementById('sortSelect').addEventListener('change', loadAndRender);

  // CSS pour l'animation de rotation
  const style = document.createElement('style');
  style.textContent = `
    @keyframes spin {
      from { transform: rotate(0deg); }
      to { transform: rotate(360deg); }
    }
  `;
  document.head.appendChild(style);

  /* ---------- chargement initial ---------- */
  loadAndRender();
});
