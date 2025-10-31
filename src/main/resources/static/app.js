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
    const edit = document.createElement('button'); edit.className='btn btn-sm btn-outline-primary me-2'; edit.innerText='Modifier';
    edit.onclick = ()=> openEditModal(idx, a);
    const useBtn = document.createElement('button'); useBtn.className='btn btn-sm btn-success me-2'; useBtn.innerText='Utilisé';
    useBtn.onclick = async ()=>{ await registerUsage(idx); load(); };
    const del = document.createElement('button'); del.className='btn btn-sm btn-danger'; del.innerText='Supprimer';
    del.onclick = async ()=>{ if(confirm('Confirmer la suppression ?')){ await fetch(`${apiBase}/${idx}`, {method:'DELETE'}); load(); }}

    btns.appendChild(edit); btns.appendChild(useBtn); btns.appendChild(del);
    bottom.appendChild(price); bottom.appendChild(btns);

    // alert badge
    const alertBadge = document.createElement('div');
    const daysSince = a.derniereUtilisation ? daysBetween(new Date(a.derniereUtilisation), new Date()) : null;
    if (isActive(a) && daysSince !== null && daysSince > 30){
      alertBadge.innerHTML = `<div class="badge bg-danger mb-2">Inactif ${daysSince}j</div>`;
    }

    c.appendChild(title); c.appendChild(meta); c.appendChild(stats); if (alertBadge.innerHTML) c.appendChild(alertBadge); c.appendChild(bottom);
    col.appendChild(c); cards.appendChild(col);
  })
}

async function load(){
  const all = await fetchAll();
  renderDashboard(all);
  // apply search filter if present
  const q = (document.getElementById('searchInput')?.value || '').toLowerCase().trim();
  const filtered = all.filter(a => {
    if (!q) return true;
    return (a.clientName||'').toLowerCase().includes(q) || (a.nomService||'').toLowerCase().includes(q);
  });
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

document.getElementById('searchInput')?.addEventListener('input', ()=> load());
document.getElementById('alertsBtn')?.addEventListener('click', async ()=>{
  const all = await fetchAll();
  const alerts = all.filter(a => isActive(a) && a.derniereUtilisation && daysBetween(new Date(a.derniereUtilisation), new Date()) > 30);
  render(alerts);
});

document.getElementById('importFile').addEventListener('change', async (e)=>{
  const f = e.target.files[0];
  if (!f) return;
  const text = await f.text();
  try{ const arr = JSON.parse(text); for(const it of arr){ await fetch(apiBase,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(it)}); } load(); }
  catch(err){ alert('Fichier invalide'); }
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
  if (idx < 0 || idx >= all.length) return;
  const a = all[idx];
  a.derniereUtilisation = new Date().toISOString().split('T')[0];
  await fetch(`${apiBase}/${idx}`, {method:'PUT', headers:{'Content-Type':'application/json'}, body:JSON.stringify(a)});
}

load();
