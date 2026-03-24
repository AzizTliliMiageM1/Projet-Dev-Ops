/**
 * 🏦 OPEN BANKING INTEGRATION MODULE
 * Gère l'import et la détection des abonnements depuis CSV bancaire
 * Utilise l'APIClient unifié pour une meilleure intégration
 */

class OpenBankingManager {
    constructor() {
        this.detections = [];
        this.transactions = [];
    }

    /**
     * Lire un fichier CSV et l'importer
     */
    async importCSVFile(file) {
        if (!file) {
            throw new Error('Veuillez sélectionner un fichier');
        }

        const allowedTypes = ['text/csv', 'application/vnd.ms-excel', 'text/plain', ''];
        const hasCsvExtension = (file.name || '').toLowerCase().endsWith('.csv') || (file.name || '').toLowerCase().endsWith('.txt');
        if (!allowedTypes.includes(file.type) && !hasCsvExtension) {
            throw new Error('Format non supporté. Utilisez un fichier CSV.');
        }

        return this.submitCSVToBackend(file);
    }

    /**
     * Soumettre le CSV au backend via APIClient
     */
    async submitCSVToBackend(file) {
        try {
            console.log('📤 Envoi du fichier CSV au backend via APIClient...');
            
            if (!window.APIClient || !window.APIClient.bank) {
                throw new Error('APIClient Bank non disponible');
            }
            
            const data = await window.APIClient.bank.importFile(file);
            this.detections = Array.isArray(data?.detections) ? data.detections : [];
            
            console.log(`✅ ${this.detections.length} abonnements détectés`);
            
            return {
                success: Boolean(data?.success),
                detectedSubscriptions: this.detections,
                count: this.detections.length,
                transactionsProcessed: data?.transactionsProcessed || 0
            };
        } catch (error) {
            console.error('❌ Erreur submitCSVToBackend:', error);
            if ((error.message || '').includes('HTTP 401')) {
                throw new Error('Session expirée ou non connectée. Connectez-vous sur /index.html puis revenez sur cette page.');
            }
            throw new Error(`Erreur lors de l'import: ${error.message}`);
        }
    }

    /**
     * Sauvegarder un abonnement détecté
     */
    async addDetectedSubscription(detection) {
        try {
            console.log('💾 Sauvegarde:', detection);
            
            const payload = {
                service: detection.service || detection.name,
                category: detection.category || 'Autre',
                amount: detection.amount || detection.montant || 0
            };
            
            // Utiliser l'APIClient
            if (!window.APIClient || !window.APIClient.openBanking) {
                throw new Error('APIClient OpenBanking non disponible');
            }
            
            const result = await window.APIClient.openBanking.addDetectedSubscription(payload);
            console.log('✅ Abonnement sauvegardé:', result);
            return { success: true, data: result };
        } catch (error) {
            console.error('❌ Erreur saveDetectedSubscription:', error);
            throw error;
        }
    }

    /**
     * Afficher les détections dans le DOM
     */
    renderDetections(containerId) {
        const container = document.getElementById(containerId);
        if (!container) return;

        if (!this.detections || this.detections.length === 0) {
            container.innerHTML = '<div class="alert alert-info">ℹ️ Aucun abonnement détecté</div>';
            return;
        }

        let html = `
            <table class="table table-striped table-hover">
                <thead class="table-light">
                    <tr>
                        <th>Service</th>
                        <th>Montant</th>
                        <th>Fréquence</th>
                        <th>Confiance</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
        `;

        this.detections.forEach((detection, index) => {
            const name = detection.service || detection.name || 'Inconnu';
            const amount = (detection.amount || 0).toFixed(2);
            const freq = detection.frequency || 'MENSUELLE';
            const confidence = Math.round((detection.confidence || 0) * 100);
            
            html += `
                <tr>
                    <td><strong>${this.escapeHtml(name)}</strong></td>
                    <td>${amount} EUR</td>
                    <td><span class="badge bg-info">${freq}</span></td>
                    <td>
                        <div class="progress" style="height: 20px;">
                            <div class="progress-bar bg-success" style="width: ${confidence}%">${confidence}%</div>
                        </div>
                    </td>
                    <td>
                        <button class="btn btn-sm btn-success" onclick="openBankingMgr.addFromTable(${index})">
                            <i class="bi bi-check"></i> Ajouter
                        </button>
                    </td>
                </tr>
            `;
        });

        html += `
                </tbody>
            </table>
        `;

        container.innerHTML = html;
    }

    /**
     * Ajouter depuis le tableau
     */
    async addFromTable(index) {
        const detection = this.detections[index];
        const btn = event.target.closest('button');
        
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span>';

        try {
            const result = await this.addDetectedSubscription(detection);
            
            if (result.success) {
                console.log(`✅ Abonnement "${detection.service}" ajouté!`);
                this.detections.splice(index, 1);
                this.renderDetections('open-banking-detections');
                
                const serviceName = detection.service || detection.name || 'Abonnement';
                if (window.showAlert) {
                    showAlert('success', `✅ ${serviceName} ajouté avec succès!`);
                }
                
                // Recharger dashboard si possible
                if (window.dashboardManager && window.dashboardManager.loadAbonnements) {
                    window.dashboardManager.loadAbonnements();
                }
            }
        } catch (error) {
            btn.disabled = false;
            btn.innerHTML = '<i class="bi bi-check"></i> Ajouter';
            if (window.showAlert) {
                showAlert('error', `❌ Erreur: ${error.message}`);
            } else {
                alert(`❌ Erreur: ${error.message}`);
            }
        }
    }

    /**
     * Afficher les statistiques d'import
     */
    renderStats(statsContainerId, importResult) {
        const container = document.getElementById(statsContainerId);
        if (!container) return;

        const count = importResult.count || 0;
        const html = `
            <div class="row">
                <div class="col-md-6">
                    <div class="alert alert-info">
                        <strong>📊 Détections:</strong> ${count} abonnement(s)
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="alert alert-success">
                        <strong>✅ Prêt:</strong> Cliquez sur "Ajouter" pour enregistrer
                    </div>
                </div>
            </div>
        `;

        container.innerHTML = html;
    }

    /**
     * Sécuriser l'affichage HTML
     */
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

// Instance globale
const openBankingMgr = new OpenBankingManager();

/**
 * Initialiser le gestionnaire d'import CSV
 */
function initOpenBankingUI() {
    const fileInput = document.getElementById('csvFileInput');
    const importBtn = document.getElementById('importCSVBtn');

    if (importBtn && fileInput) {
        importBtn.addEventListener('click', async () => {
            if (!fileInput.files.length) {
                alert('Veuillez sélectionner un fichier CSV');
                return;
            }

            importBtn.disabled = true;
            importBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Importation...';

            try {
                const result = await openBankingMgr.importCSVFile(fileInput.files[0]);
                
                // Afficher les statistiques
                openBankingMgr.renderStats('open-banking-stats', result);
                
                // Afficher les détections
                openBankingMgr.renderDetections('open-banking-detections');
                
                // Montrer la section des résultats
                const resultsSection = document.getElementById('open-banking-results');
                if (resultsSection) {
                    resultsSection.style.display = 'block';
                }
            } catch (error) {
                if (window.showAlert) {
                    showAlert('error', `❌ Erreur lors de l'import: ${error.message}`);
                } else {
                    alert(`Erreur lors de l'import: ${error.message}`);
                }
            } finally {
                importBtn.disabled = false;
                importBtn.innerHTML = '<i class="bi bi-download"></i> Importer CSV';
            }
        });
    }
}

// Initialiser au chargement de la page
document.addEventListener('DOMContentLoaded', initOpenBankingUI);
