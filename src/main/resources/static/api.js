/**
 * Module API - Appels fetch vers le backend réel
 * API base: http://localhost:4567 (à adapter)
 */

const API_BASE = '/api';

// ==================== ABONNEMENTS ====================

/**
 * Récupère la liste de tous les abonnements
 * GET /api/abonnements
 */
async function fetchAbonnements() {
  try {
    const response = await fetch(`${API_BASE}/abonnements`);
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return await response.json();
  } catch (error) {
    console.error('Erreur fetchAbonnements:', error);
    return [];
  }
}

/**
 * Récupère un abonnement par ID
 * GET /api/abonnements/:id
 */
async function fetchAbonnement(id) {
  try {
    const response = await fetch(`${API_BASE}/abonnements/${id}`);
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return await response.json();
  } catch (error) {
    console.error('Erreur fetchAbonnement:', error);
    return null;
  }
}

/**
 * Crée un nouvel abonnement
 * POST /api/abonnements
 * @param {Object} data - Objet abonnement
 */
async function createAbonnement(data) {
  try {
    const response = await fetch(`${API_BASE}/abonnements`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return await response.json();
  } catch (error) {
    console.error('Erreur createAbonnement:', error);
    return null;
  }
}

/**
 * Met à jour un abonnement
 * PUT /api/abonnements/:id
 * @param {string|number} id - ID de l'abonnement
 * @param {Object} data - Données mises à jour
 */
async function updateAbonnement(id, data) {
  try {
    const response = await fetch(`${API_BASE}/abonnements/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return await response.json();
  } catch (error) {
    console.error('Erreur updateAbonnement:', error);
    return null;
  }
}

/**
 * Supprime un abonnement
 * DELETE /api/abonnements/:id
 * @param {string|number} id - ID de l'abonnement
 */
async function deleteAbonnement(id) {
  try {
    const response = await fetch(`${API_BASE}/abonnements/${id}`, {
      method: 'DELETE'
    });
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return true;
  } catch (error) {
    console.error('Erreur deleteAbonnement:', error);
    return false;
  }
}

// ==================== ANALYTICS ====================

/**
 * Récupère les statistiques générales
 * GET /api/analytics (ou endpoint similaire)
 */
async function fetchAnalytics() {
  try {
    // Endpoint à adapter selon ton backend
    const response = await fetch(`${API_BASE}/analytics`);
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return await response.json();
  } catch (error) {
    console.error('Erreur fetchAnalytics:', error);
    return {
      totalAbonnements: 0,
      coutMensuel: 0,
      scoreSante: 0,
      abonnementsAuRisque: 0
    };
  }
}

/**
 * Récupère les abonnements à risque (expiration proche, etc)
 * GET /api/abonnements/risk ou endpoint adapté
 */
async function fetchRiskAbonnements() {
  try {
    const abonnements = await fetchAbonnements();
    // Filtrer les abonnements à risque côté client
    // (si le backend n'a pas d'endpoint spécifique)
    return abonnements.filter(ab => ab.riskLevel === 'high' || ab.daysUntilExpiry < 30);
  } catch (error) {
    console.error('Erreur fetchRiskAbonnements:', error);
    return [];
  }
}

// ==================== UTILITAIRES ====================

/**
 * Teste la connexion au backend
 */
async function healthCheck() {
  try {
    const response = await fetch(`${API_BASE}/health`);
    return response.ok;
  } catch {
    return false;
  }
}
