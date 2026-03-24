/**
 * 🏦 PDF Bank Statement Import Manager
 * Gère l'import de relevés bancaires en PDF et leur conversion
 */

class PDFBankStatementManager {
    constructor() {
        this.detections = [];
        this.csvContent = '';
        this.isProcessing = false;
    }

    /**
     * Traiter un fichier PDF
     */
    async processPDF(file) {
        if (!file.type.includes('pdf')) {
            throw new Error('Veuillez sélectionner un fichier PDF');
        }

        if (file.size > 10 * 1024 * 1024) { // 10MB
            throw new Error('Le fichier est trop volumineux (max 10MB)');
        }

        this.isProcessing = true;
        return this.uploadPDFToServer(file);
    }

    /**
     * Envoyer le PDF au serveur
     */
    async uploadPDFToServer(file) {
        try {
            console.log('📤 Envoi du PDF au serveur...');

            if (!window.APIClient || !window.APIClient.bank) {
                throw new Error('APIClient Bank non disponible');
            }

            const data = await window.APIClient.bank.importFile(file);
            
            this.csvContent = data.csvContent || '';
            this.detections = Array.isArray(data.detections) ? data.detections : [];
            
            console.log(`✅ ${this.detections.length} abonnements détectés`);
            
            this.isProcessing = false;
            return data;

        } catch (error) {
            this.isProcessing = false;
            console.error('❌ Erreur upload PDF:', error);
            if ((error.message || '').includes('HTTP 401')) {
                throw new Error('Session expirée ou non connectée. Connectez-vous sur /index.html puis revenez sur cette page.');
            }
            throw error;
        }
    }

    /**
     * Afficher les résultats de conversion
     */
    displayConversionResults(containerId) {
        const container = document.getElementById(containerId);
        if (!container) return;

        if (!this.csvContent) {
            container.innerHTML = `
                <div class="alert alert-info">
                    <i class="bi bi-info-circle"></i> Aucun contenu CSV généré
                </div>
            `;
            return;
        }

        // Afficher un aperçu du CSV
        const lines = this.csvContent.split('\n').slice(0, 6);
        let html = `
            <div class="csv-preview">
                <h5><i class="bi bi-file-text"></i> Aperçu CSV</h5>
                <pre style="background: #f8f9fa; padding: 12px; border-radius: 8px; font-size: 12px; overflow-x: auto;">
${lines.map(line => this.escapeHtml(line)).join('\n')}</pre>
            </div>
        `;

        container.innerHTML = html;
    }

    /**
     * Afficher les détections bancaires
     */
    displayDetections(containerId) {
        const container = document.getElementById(containerId);
        if (!container) return;

        if (!this.detections || this.detections.length === 0) {
            container.innerHTML = `
                <div class="alert alert-info">
                    <i class="bi bi-search"></i> Aucun abonnement détecté
                </div>
            `;
            return;
        }

        let html = `
            <div class="table-responsive">
            <table class="table table-hover table-striped">
                <thead class="table-light">
                    <tr>
                        <th>Service</th>
                        <th>Catégorie</th>
                        <th>Montant</th>
                        <th>Fréquence</th>
                        <th>Confiance</th>
                        <th>Occurrences</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
        `;

        this.detections.forEach((detection, index) => {
            const confidencePercent = Math.round((detection.confidence || 0) * 100);
            const confidenceClass = confidencePercent > 80 ? 'success' : 
                                   confidencePercent > 60 ? 'warning' : 'danger';
            
            html += `
                <tr class="align-middle">
                    <td>
                        <strong>${this.escapeHtml(detection.service || 'Inconnu')}</strong>
                    </td>
                    <td>
                        <span class="badge bg-info">${this.escapeHtml(detection.category || 'N/A')}</span>
                    </td>
                    <td>${detection.amount ? detection.amount.toFixed(2) : '0.00'} €</td>
                    <td>
                        <span class="badge bg-secondary">${this.escapeHtml(detection.frequency || 'MENSUEL')}</span>
                    </td>
                    <td>
                        <div class="progress" style="height: 20px; min-width: 100px;">
                            <div class="progress-bar bg-${confidenceClass}" 
                                 style="width: ${confidencePercent}%">
                                ${confidencePercent}%
                            </div>
                        </div>
                    </td>
                    <td><span class="badge bg-light text-dark">${detection.occurrences || 1}x</span></td>
                    <td>
                        <button class="btn btn-sm btn-success" 
                                onclick="pdfBankManager.addDetectionAsSubscription(${index})"
                                title="Ajouter cet abonnement">
                            <i class="bi bi-check-circle"></i>
                        </button>
                    </td>
                </tr>
            `;
        });

        html += `
                </tbody>
            </table>
            </div>
        `;

        container.innerHTML = html;
    }

    /**
     * Ajouter une détection comme abonnement
     */
    async addDetectionAsSubscription(index) {
        const detection = this.detections[index];
        if (!detection) return;

        const btn = event.target.closest('button');
        btn.disabled = true;
        btn.innerHTML = '<i class="bi bi-hourglass"></i>';

        try {
            // Utiliser l'APIClient pour ajouter l'abonnement
            if (!window.APIClient || !window.APIClient.openBanking) {
                throw new Error('APIClient non disponible');
            }

            await APIClient.openBanking.addDetectedSubscription({
                service: detection.service || 'Abonnement détecté',
                category: detection.category || 'Autre',
                amount: detection.amount || 0
            });

            console.log(`✅ ${detection.service} ajouté`);
            
            // Retirer de la liste
            this.detections.splice(index, 1);
            this.displayDetections('pdf-bank-detections');
            
            if (window.showAlert) {
                showAlert('success', `✅ ${detection.service} ajouté avec succès!`);
            }

        } catch (error) {
            console.error('❌ Erreur ajout abonnement:', error);
            btn.disabled = false;
            btn.innerHTML = '<i class="bi bi-check-circle"></i>';
            
            if (window.showAlert) {
                showAlert('error', `❌ Erreur: ${error.message}`);
            }
        }
    }

    /**
     * Télécharger le CSV généré
     */
    downloadCSV() {
        if (!this.csvContent) {
            alert('Aucun CSV à télécharger');
            return;
        }

        const blob = new Blob([this.csvContent], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = `relevé_bancaire_${new Date().toISOString().split('T')[0]}.csv`;
        link.click();
    }

    /**
     * Réinitialiser le formulaire
     */
    reset() {
        this.detections = [];
        this.csvContent = '';
        this.isProcessing = false;
        
        const fileInput = document.getElementById('pdfFileInput');
        if (fileInput) fileInput.value = '';
        
        document.getElementById('pdf-bank-conversion').innerHTML = '';
        document.getElementById('pdf-bank-detections').innerHTML = '';
    }

    /**
     * Sécuriser l'affichage HTML
     */
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    /**
     * Obtenir le statut du service
     */
    async getServiceStatus() {
        try {
            const response = await fetch('/api/pdf-bank/status');
            return await response.json();
        } catch (error) {
            console.warn('⚠️ Statut indisponible:', error);
            return null;
        }
    }
}

// Instance globale
const pdfBankManager = new PDFBankStatementManager();

/**
 * Initialiser l'interface PDF
 */
function initPDFUploadUI() {
    const fileInput = document.getElementById('pdfFileInput');
    const uploadBtn = document.getElementById('uploadPDFBtn');
    const resetBtn = document.getElementById('resetPDFBtn');
    const downloadBtn = document.getElementById('downloadCSVBtn');
    const dragZone = document.getElementById('pdfDragZone');

    if (!fileInput || !uploadBtn) return;

    // Clic sur le bouton
    uploadBtn.addEventListener('click', () => {
        fileInput.click();
    });

    // Changement de fichier
    fileInput.addEventListener('change', async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        await processPDFUpload(file);
    });

    // Drag & Drop
    if (dragZone) {
        dragZone.addEventListener('dragover', (e) => {
            e.preventDefault();
            dragZone.classList.add('dragover');
        });

        dragZone.addEventListener('dragleave', () => {
            dragZone.classList.remove('dragover');
        });

        dragZone.addEventListener('drop', async (e) => {
            e.preventDefault();
            dragZone.classList.remove('dragover');
            
            const files = e.dataTransfer.files;
            if (files.length) {
                await processPDFUpload(files[0]);
            }
        });
    }

    // Bouton Reset
    if (resetBtn) {
        resetBtn.addEventListener('click', () => {
            pdfBankManager.reset();
            uploadBtn.disabled = false;
            uploadBtn.innerHTML = '📥 Importer PDF';
        });
    }

    // Bouton Download CSV
    if (downloadBtn) {
        downloadBtn.addEventListener('click', () => {
            pdfBankManager.downloadCSV();
        });
    }
}

/**
 * Traiter l'upload du PDF
 */
async function processPDFUpload(file) {
    const uploadBtn = document.getElementById('uploadPDFBtn');
    uploadBtn.disabled = true;
    uploadBtn.innerHTML = '<i class="bi bi-hourglass"></i> Traitement...';

    try {
        await pdfBankManager.processPDF(file);
        
        // Afficher les résultats
        pdfBankManager.displayConversionResults('pdf-bank-conversion');
        pdfBankManager.displayDetections('pdf-bank-detections');
        
        // Afficher la section résultats
        const resultsSection = document.getElementById('pdf-bank-results');
        if (resultsSection) {
            resultsSection.style.display = 'block';
        }

        if (window.showAlert) {
            showAlert('success', `✅ PDF traité avec succès! ${pdfBankManager.detections.length} abonnements détectés`);
        }

    } catch (error) {
        if (window.showAlert) {
            showAlert('error', `❌ Erreur: ${error.message}`);
        } else {
            alert(`Erreur: ${error.message}`);
        }
    } finally {
        uploadBtn.disabled = false;
        uploadBtn.innerHTML = '📥 Importer PDF';
    }
}

// Initialiser au chargement du DOM
document.addEventListener('DOMContentLoaded', initPDFUploadUI);

// Exporter pour utilisation globale
if (typeof window !== 'undefined') {
    window.PDFBankStatementManager = PDFBankStatementManager;
    window.pdfBankManager = pdfBankManager;
}
