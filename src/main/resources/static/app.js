const apiBase = '/api/abonnements';

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

function render(list){
  // list is expected to contain objects with a special '__idx' property
  // which denotes the original index in the full array returned by the API.
  const cards = document.getElementById('cards');
  cards.innerHTML = '';
  list.forEach((a, idx) => {
    const col = document.createElement('div'); col.className='col-12 col-md-6';
    const c = document.createElement('div'); c.className='card p-3';
    const title = document.createElement('div'); title.className='title'; title.innerText = a.clientName + ' — ' + a.nomService;
    const meta = document.createElement('div'); meta.className='small-muted';
    const debut = a.dateDebut || '-';
    const fin = a.dateFin || '-';
    meta.innerText = `De ${debut} à ${fin} · ${a.categorie || 'Non classé'}`;

    const stats = document.createElement('div'); stats.className='mt-2 small-muted';
    const activeStatus = isActive(a) ? 'Actif' : 'Expiré';
    let lastUse = a.derniereUtilisation ? new Date(a.derniereUtilisation).toLocaleDateString() : 'N/A';
    stats.innerText = `${activeStatus} · Dernière utilisation: ${lastUse}`;

  const bottom = document.createElement('div'); bottom.className='d-flex justify-content-between align-items-center mt-2';
    const price = document.createElement('div'); price.className='price-badge'; price.innerText = (a.prixMensuel||0).toFixed(2) + '€';

    const btns = document.createElement('div');
  const edit = document.createElement('button'); edit.className='btn btn-sm btn-outline-primary me-2'; edit.innerHTML='<i class="fa fa-pen me-1"></i>Modifier';
  edit.onclick = ()=> openEditModal(a.__uuid, a);
  const useBtn = document.createElement('button'); useBtn.className='btn btn-sm btn-success me-2'; useBtn.innerHTML='<i class="fa fa-check me-1"></i>Utilisé';
  useBtn.onclick = async ()=>{ await registerUsage(a.__uuid); load(); };
  const del = document.createElement('button'); del.className='btn btn-sm btn-danger'; del.innerHTML='<i class="fa fa-trash me-1"></i>Supprimer';
  del.onclick = async ()=>{ if(confirm('Confirmer la suppression ?')){ const id = a.__uuid; await fetch(`${apiBase}/${id}`, {method:'DELETE'}); load(); }}

  // selection checkbox for bulk actions (store uuid only)
  const selectBox = document.createElement('input'); selectBox.type='checkbox'; selectBox.className='form-check-input me-2 select-abonnement';
  selectBox.dataset.uuid = a.__uuid || '';

    btns.appendChild(edit); btns.appendChild(useBtn); btns.appendChild(del);
    bottom.appendChild(price); bottom.appendChild(btns);

  // insert selection before title
  const hdr = document.createElement('div'); hdr.className='d-flex align-items-center';
  hdr.appendChild(selectBox); hdr.appendChild(title);

    // alert badge
    const alertBadge = document.createElement('div');
    const daysSince = a.derniereUtilisation ? daysBetween(new Date(a.derniereUtilisation), new Date()) : null;
    if (isActive(a) && daysSince !== null && daysSince > 30){
      alertBadge.innerHTML = `<div class="badge bg-danger mb-2">Inactif ${daysSince}j</div>`;
    }

    c.appendChild(hdr); c.appendChild(meta); c.appendChild(stats); if (alertBadge.innerHTML) c.appendChild(alertBadge); c.appendChild(bottom);
    col.appendChild(c); cards.appendChild(col);
  })
}

async function load(){
  const all = await fetchAll();
  // attach uuid to each item
  const withUuid = all.map((it) => ({...it, __uuid: it.id}));
  renderDashboard(withUuid);

  // apply search filter if present
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
  await fetch(apiBase, {method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify(payload)});
  document.getElementById('addForm').reset();
  load();
});

document.getElementById('refreshBtn').addEventListener('click', load);
document.getElementById('exportBtn').addEventListener('click', async ()=>{
  const data = await fetchAll();
  const blob = new Blob([JSON.stringify(data,null,2)],{type:'application/json'});
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a'); a.href = url; a.download = 'abonnements_export.json'; a.click();
});

// visual feedback for refresh: add spinner briefly
const originalRefreshHtml = document.getElementById('refreshBtn').innerHTML;
async function showRefreshing(){
  const btn = document.getElementById('refreshBtn');
  btn.innerHTML = '<i class="fa fa-spinner fa-spin me-1"></i>Chargement...';
  await new Promise(r => setTimeout(r, 600));
  btn.innerHTML = originalRefreshHtml;
}
document.getElementById('refreshBtn').addEventListener('click', async ()=>{ await showRefreshing(); load(); });

// bulk delete handler
document.getElementById('bulkDeleteBtn')?.addEventListener('click', async ()=>{
  const checks = Array.from(document.querySelectorAll('.select-abonnement:checked'));
  if (checks.length === 0){ alert('Aucun abonnement sélectionné'); return; }
  if (!confirm(`Supprimer ${checks.length} abonnements sélectionnés ?`)) return;
  // gather uuid ids and delete sequentially
  const ids = checks.map(c => c.dataset.uuid).filter(Boolean);
  for(const id of ids){ await fetch(`${apiBase}/${id}`, {method:'DELETE'}); }
  load();
});

document.getElementById('searchInput')?.addEventListener('input', ()=> load());
document.getElementById('alertsBtn')?.addEventListener('click', async ()=>{
  const all = await fetchAll();
  const alerts = all.filter(a => isActive(a) && a.derniereUtilisation && daysBetween(new Date(a.derniereUtilisation), new Date()) > 30)
                    .map((it)=> ({...it, __uuid: it.id}));
  render(alerts);
});

// Import JSON: accepts a file containing a JSON array of abonnement-like objects.
// Each object should contain at least: clientName, nomService, dateDebut, dateFin.
document.getElementById('importFile').addEventListener('change', async (e)=>{
  const f = e.target.files[0];
  if (!f) return;
  const text = await f.text();
  try{
    const arr = JSON.parse(text);
    if (!Array.isArray(arr)){ alert('Le fichier doit contenir un tableau JSON'); return; }
    // basic validation
    const valid = [];
    const invalid = [];
    for(const it of arr){
      if (it && it.nomService && it.clientName && it.dateDebut && it.dateFin) valid.push(it);
      else invalid.push(it);
    }
    if (valid.length === 0){ alert('Aucun objet valide à importer (attendu: clientName, nomService, dateDebut, dateFin)'); return; }
    if (!confirm(`Importer ${valid.length} abonnements valides${invalid.length?(' (et ignorer '+invalid.length+' invalides)'):''} ?`)) return;
    for(const it of valid){ await fetch(apiBase,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(it)}); }
    alert(`Import terminé: ${valid.length} abonnements ajoutés.`);
    load();
  }
  catch(err){ console.error(err); alert('Fichier JSON invalide ou erreur de lecture'); }
});

// Edit modal logic
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

async function registerUsage(idx){
  const all = await fetchAll();
  // idx is expected to be uuid
  let a = all.find(x => x.id === idx);
  if (!a) return;
  a.derniereUtilisation = new Date().toISOString().split('T')[0];
  await fetch(`${apiBase}/${a.id}`, {method:'PUT', headers:{'Content-Type':'application/json'}, body:JSON.stringify(a)});
}

load();
