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
  dash.innerHTML = '';
  const total = list.length;
  const active = list.filter(isActive).length;
  const expired = total - active;
  const alerts = list.filter(a => isActive(a) && a.derniereUtilisation && daysBetween(new Date(a.derniereUtilisation), new Date()) > 30).length;

  const makeCard = (title, count, color) => {
    const col = document.createElement('div'); col.className='col-6 col-md-3';
    const c = document.createElement('div'); c.className='dashboard-card bg-white shadow-sm';
    c.innerHTML = `<div class="small-muted">${title}</div><div class="dashboard-count" style="color:${color}">${count}</div>`;
    col.appendChild(c); return col;
  }

  dash.appendChild(makeCard('Total', total, 'var(--primary)'));
  dash.appendChild(makeCard('Actifs', active, 'green'));
  dash.appendChild(makeCard('Expirés', expired, 'gray'));
  dash.appendChild(makeCard('Alertes', alerts, 'crimson'));
}

/* ---------- cartes (NOUVEAU LAYOUT) ---------- */
function render(list){
  const cards = document.getElementById('cards');
  cards.innerHTML = '';

  list.forEach((a) => {
    const col = document.createElement('div');
    col.className = 'col-12 col-md-6';

    const clientName = a.clientName || '';
    const nomService = a.nomService || '';
    const dateDebut = a.dateDebut || '-';
    const dateFin = a.dateFin || '-';
    const categorie = a.categorie || 'Non classé';
    const estActif = isActive(a);
    const derniereUtilisation = a.derniereUtilisation
      ? new Date(a.derniereUtilisation).toLocaleDateString()
      : 'N/A';
    const prixMensuel = Number(a.prixMensuel || 0);

    const card = document.createElement('div');
    card.className = 'sub-card';
    card.innerHTML = `
      <!-- Prix en haut à gauche -->
      <div class="price-badge left">${prixMensuel.toFixed(2)}€</div>

      <div class="sub-card-inner">
        <div class="sub-title">
          <input type="checkbox" class="sub-check select-abonnement" data-select data-uuid="${a.__uuid || ''}"/>
          <span><strong>${escapeHtml(clientName)}</strong> — ${escapeHtml(nomService)}</span>
        </div>

        <div class="sub-meta">
          De ${escapeHtml(dateDebut)} à ${escapeHtml(dateFin)} · ${escapeHtml(categorie)}
        </div>
        <div class="sub-meta">
          ${estActif ? "Actif" : "Expiré"} · Dernière utilisation: ${escapeHtml(derniereUtilisation)}
        </div>

        <!-- Bouton pleine largeur -->
        <div class="actions-stack">
          <button class="btn-used-full" data-used>✅ Utilisé</button>
        </div>

        <!-- Deux boutons 50/50 -->
        <div class="actions-split">
          <button class="btn-edit-split" data-edit>✏️ Modifier</button>
          <button class="btn-delete-split" data-delete>🗑️ Supprimer</button>
        </div>
      </div>
    `;

    // Wiring des actions
    card.querySelector('[data-used]').onclick = async () => {
      await registerUsage(a.__uuid);
      load();
    };
    card.querySelector('[data-edit]').onclick = () => {
      openEditModal(a.__uuid, a);
    };
    card.querySelector('[data-delete]').onclick = async () => {
      if (confirm('Confirmer la suppression ?')) {
        await fetch(`${apiBase}/${a.__uuid}`, { method:'DELETE' });
        load();
      }
    };

    col.appendChild(card);
    cards.appendChild(col);
  });
}

/* ---------- chargement + filtres ---------- */
async function load(){
  const all = await fetchAll();
  // attach uuid to each item
  const withUuid = all.map((it) => ({...it, __uuid: it.id}));
  renderDashboard(withUuid);

  // apply search/filter/sort
  const q = (document.getElementById('searchInput')?.value || '').toLowerCase().trim();
  const status = (document.getElementById('statusFilter')?.value || 'all');
  const sortBy = (document.getElementById('sortSelect')?.value || 'none');

  let filtered = withUuid.filter(a => {
    if (!q) return true;
    return (a.clientName||'').toLowerCase().includes(q) || (a.nomService||'').toLowerCase().includes(q);
  });

  if (status === 'actifs') filtered = filtered.filter(isActive);
  else if (status === 'expires') filtered = filtered.filter(a => !isActive(a));

  if (sortBy === 'nom') filtered.sort((a,b)=> (a.clientName||'').localeCompare(b.clientName||'')); 
  else if (sortBy === 'dateDebut') filtered.sort((a,b)=> new Date(a.dateDebut) - new Date(b.dateDebut));
  else if (sortBy === 'dateFin') filtered.sort((a,b)=> new Date(a.dateFin) - new Date(b.dateFin));

  render(filtered);
}

/* ---------- ajout ---------- */
document.getElementById('addForm').addEventListener('submit', async (e)=>{
  e.preventDefault();
  const payload = {
    nomService: document.getElementById('nomService').value,
    dateDebut: document.getElementById('dateDebut').value,
    dateFin: document.getElementById('dateFin').value,
    prixMensuel: parseFloat(document.getElementById('prixMensuel').value),
    clientName: document.getElementById('clientName').value,
    categorie: document.getElementById('categorie').value || 'Non classé'
  };
  const btn = e.target.querySelector('button[type="submit"]');
  const original = btn.innerHTML;
  btn.disabled = true;
  btn.innerHTML = '<i class="fa fa-spinner fa-spin me-1"></i>Ajout...';
  const flash = document.getElementById('flash'); flash.innerHTML = '';
  try {
    const r = await fetch(apiBase, {method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify(payload)});
    if (r.status === 201) {
      const created = await r.json();
      flash.innerHTML = `<div class="alert alert-success">Abonnement ajouté (id: ${created.id}).</div>`;
      document.getElementById('addForm').reset();
      load();
    } else {
      let msg = 'Erreur lors de la création';
      try { const err = await r.json(); if (err && err.error) msg = err.error; if (err && err.message) msg = err.message; } catch(e){}
      flash.innerHTML = `<div class="alert alert-danger">${msg}</div>`;
    }
  } catch (err) {
    flash.innerHTML = `<div class="alert alert-danger">Erreur réseau: ${err.message}</div>`;
  } finally {
    btn.disabled = false;
    btn.innerHTML = original;
  }
});

/* ---------- header / actions ---------- */
document.getElementById('refreshBtn').addEventListener('click', load);
document.getElementById('exportBtn').addEventListener('click', async ()=>{
  const data = await fetchAll();
  const blob = new Blob([JSON.stringify(data,null,2)],{type:'application/json'});
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a'); a.href = url; a.download = 'abonnements_export.json'; a.click();
});

// visual feedback for refresh
const originalRefreshHtml = document.getElementById('refreshBtn').innerHTML;
async function showRefreshing(){
  const btn = document.getElementById('refreshBtn');
  btn.innerHTML = '<i class="fa fa-spinner fa-spin me-1"></i>Chargement...';
  await new Promise(r => setTimeout(r, 600));
  btn.innerHTML = originalRefreshHtml;
}
document.getElementById('refreshBtn').addEventListener('click', async ()=>{ await showRefreshing(); load(); });

/* ---------- suppressions groupées ---------- */
document.getElementById('bulkDeleteBtn')?.addEventListener('click', async ()=>{
  const checks = Array.from(document.querySelectorAll('.select-abonnement:checked'));
  if (checks.length === 0){ alert('Aucun abonnement sélectionné'); return; }
  if (!confirm(`Supprimer ${checks.length} abonnements sélectionnés ?`)) return;
  const ids = checks.map(c => c.dataset.uuid).filter(Boolean);
  for(const id of ids){ await fetch(`${apiBase}/${id}`, {method:'DELETE'}); }
  load();
});

/* ---------- filtres / alertes ---------- */
document.getElementById('searchInput')?.addEventListener('input', ()=> load());
document.getElementById('alertsBtn')?.addEventListener('click', async ()=>{
  const all = await fetchAll();
  const alerts = all.filter(a => isActive(a) && a.derniereUtilisation && daysBetween(new Date(a.derniereUtilisation), new Date()) > 30)
                    .map((it)=> ({...it, __uuid: it.id}));
  render(alerts);
});

/* ---------- import JSON ---------- */
document.getElementById('importFile').addEventListener('change', async (e)=>{
  const f = e.target.files[0];
  if (!f) return;
  const flash = document.getElementById('flash'); flash.innerHTML = '';
  try{
    const text = await f.text();
    const arr = JSON.parse(text);
    if (!Array.isArray(arr)){ alert('Le fichier doit contenir un tableau JSON'); return; }
    if (!confirm(`Importer ${arr.length} objets depuis le fichier ?`)) return;
    const r = await fetch(`${apiBase}/import`, {method:'POST', headers:{'Content-Type':'application/json'}, body:text});
    if (r.status === 201) {
      const info = await r.json();
      flash.innerHTML = `<div class="alert alert-success">Import réussi : ${info.imported} abonnements ajoutés.</div>`;
      load();
    } else {
      let msg = 'Erreur lors de l\'import';
      try { const err = await r.json(); if (err && err.error) msg = err.error; } catch(e){}
      flash.innerHTML = `<div class="alert alert-danger">${msg}</div>`;
    }
  }
  catch(err){ console.error(err); flash.innerHTML = `<div class="alert alert-danger">Fichier JSON invalide ou erreur: ${err.message}</div>`; }
});

/* ---------- édition ---------- */
function openEditModal(idx, a){
  document.getElementById('editIndex').value = idx;
  document.getElementById('editClient').value = a.clientName || '';
  document.getElementById('editService').value = a.nomService || '';
  document.getElementById('editDebut').value = a.dateDebut || '';
  document.getElementById('editFin').value = a.dateFin || '';
  document.getElementById('editPrix').value = a.prixMensuel || 0;
  document.getElementById('editCategorie').value = a.categorie || '';
  const modal = new bootstrap.Modal(document.getElementById('editModal'));
  modal.show();
}

document.getElementById('saveEditBtn').addEventListener('click', async ()=>{
  const idx = document.getElementById('editIndex').value;
  const payload = {
    nomService: document.getElementById('editService').value,
    dateDebut: document.getElementById('editDebut').value,
    dateFin: document.getElementById('editFin').value,
    prixMensuel: parseFloat(document.getElementById('editPrix').value),
    clientName: document.getElementById('editClient').value,
    categorie: document.getElementById('editCategorie').value || 'Non classé'
  };
  await fetch(`${apiBase}/${idx}`, {method:'PUT', headers:{'Content-Type':'application/json'}, body:JSON.stringify(payload)});
  bootstrap.Modal.getInstance(document.getElementById('editModal')).hide();
  load();
});

/* ---------- usage ---------- */
async function registerUsage(idx){
  const all = await fetchAll();
  const a = all.find(x => x.id === idx);
  if (!a) return;
  a.derniereUtilisation = new Date().toISOString().split('T')[0];
  await fetch(`${apiBase}/${a.id}`, {method:'PUT', headers:{'Content-Type':'application/json'}, body:JSON.stringify(a)});
}

/* ---------- go ---------- */
load();
