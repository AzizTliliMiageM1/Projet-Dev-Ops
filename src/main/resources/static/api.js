/**
 * Module API - Appels fetch vers le backend réel
 * API base: http://localhost:4567 (à adapter)
 */

const API_BASE = '/api';

async function buildErrorMessage(response) {
  try {
    const data = await response.clone().json();
    if (data && typeof data === 'object') {
      if (data.error) return data.error;
      if (data.message) return data.message;
    }
  } catch (_) {
    // ignore JSON parse errors and fall back to text
  }

  try {
    const text = await response.text();
    if (text) return text;
  } catch (_) {
    // ignore text read errors
  }

  return `HTTP ${response.status}`;
}

// ==================== ABONNEMENTS ====================

/**
 * Récupère la liste de tous les abonnements
 * GET /api/abonnements
 */
async function fetchAbonnements() {
  try {
    const response = await fetch(`${API_BASE}/abonnements`);
    if (!response.ok) {
      throw new Error(await buildErrorMessage(response));
    }
    return await response.json();
  } catch (error) {
    console.error('Erreur fetchAbonnements:', error);
    throw error;
  }
}

/**
 * Récupère un abonnement par ID
 * GET /api/abonnements/:id
 */
async function fetchAbonnement(id) {
  try {
    const response = await fetch(`${API_BASE}/abonnements/${id}`);
    if (!response.ok) {
      throw new Error(await buildErrorMessage(response));
    }
    return await response.json();
  } catch (error) {
    console.error('Erreur fetchAbonnement:', error);
    throw error;
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
    if (!response.ok) {
      throw new Error(await buildErrorMessage(response));
    }
    return await response.json();
  } catch (error) {
    console.error('Erreur createAbonnement:', error);
    throw error;
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
    if (!response.ok) {
      throw new Error(await buildErrorMessage(response));
    }
    return await response.json();
  } catch (error) {
    console.error('Erreur updateAbonnement:', error);
    throw error;
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
    if (!response.ok) {
      throw new Error(await buildErrorMessage(response));
    }
    return true;
  } catch (error) {
    console.error('Erreur deleteAbonnement:', error);
    throw error;
  }
}

// ==================== RECOMMANDATIONS ====================

/**
 * Récupère les recommandations intelligentes
 * GET /api/recommendations
 */
async function fetchRecommendations() {
  try {
    const response = await fetch(`${API_BASE}/recommendations`);
    if (!response.ok) {
      throw new Error(await buildErrorMessage(response));
    }
    return await response.json();
  } catch (error) {
    console.error('Erreur fetchRecommendations:', error);
    throw error;
  }
}

/**
 * Récupère un plan de réduction budgétaire
 * GET /api/analytics/budget-plan?target=...
 */
async function fetchBudgetPlan(target) {
  try {
    const url = `${API_BASE}/analytics/budget-plan?target=${encodeURIComponent(target)}`;
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(await buildErrorMessage(response));
    }
    return await response.json();
  } catch (error) {
    console.error('Erreur fetchBudgetPlan:', error);
    throw error;
  }
}
