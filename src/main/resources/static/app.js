const apiBase = '/api/abonnements';

async function fetchAll(){
  const r = await fetch(apiBase);
  if (!r.ok) return [];
  return await r.json();
}

function render(list){
  const cards = document.getElementById('cards');
  cards.innerHTML = '';
  list.forEach((a, idx) => {
    const col = document.createElement('div'); col.className='col-12 col-md-6';
    const c = document.createElement('div'); c.className='card p-3';
    const title = document.createElement('div'); title.className='title'; title.innerText = a.clientName + ' — ' + a.nomService;
    const meta = document.createElement('div'); meta.className='small-muted'; meta.innerText = `De ${a.dateDebut} à ${a.dateFin} · ${a.categorie}`;
    const bottom = document.createElement('div'); bottom.className='d-flex justify-content-between align-items-center mt-2';
    const price = document.createElement('div'); price.className='price-badge'; price.innerText = a.prixMensuel.toFixed(2) + '€';
    const btns = document.createElement('div');
    const del = document.createElement('button'); del.className='btn btn-sm btn-danger me-2'; del.innerText='Supprimer';
    del.onclick = async ()=>{ await fetch(`${apiBase}/${idx}`, {method:'DELETE'}); load(); }
    btns.appendChild(del);
    bottom.appendChild(price); bottom.appendChild(btns);
    c.appendChild(title); c.appendChild(meta); c.appendChild(bottom);
    col.appendChild(c); cards.appendChild(col);
  })
}

async function load(){
  const all = await fetchAll();
  render(all);
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

document.getElementById('importFile').addEventListener('change', async (e)=>{
  const f = e.target.files[0];
  if (!f) return;
  const text = await f.text();
  try{ const arr = JSON.parse(text); for(const it of arr){ await fetch(apiBase,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(it)}); } load(); }
  catch(err){ alert('Fichier invalide'); }
});

load();
