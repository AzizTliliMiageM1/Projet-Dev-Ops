// ========================================
// NAVBAR COMPLÈTE STANDARDISÉE
// ========================================

function getStandardNavbar(activePage = '') {
    return `
    <nav class="navbar navbar-expand-lg navbar-dark" style="background: rgba(0, 0, 0, 0.2); backdrop-filter: blur(10px);">
        <div class="container-fluid px-4">
            <a class="navbar-brand" href="/index.html">
                <i class="bi bi-layers"></i> Dashboard Pro
            </a>
            
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="navbar-toggler-icon"></span>
            </button>
            
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav ms-auto">
                    <li class="nav-item">
                        <a class="nav-link ${activePage === 'dashboard' ? 'active' : ''}" href="/index.html">
                            <i class="bi bi-speedometer2 me-1"></i> Dashboard
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link ${activePage === 'analytics' ? 'active' : ''}" href="/analytics.html">
                            <i class="bi bi-bar-chart-line me-1"></i> Analytics
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link ${activePage === 'expenses' ? 'active' : ''}" href="/expenses.html">
                            <i class="bi bi-wallet2 me-1"></i> Dépenses
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link ${activePage === 'export' ? 'active' : ''}" href="/export-import.html">
                            <i class="bi bi-download me-1"></i> Export/Import
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link ${activePage === 'notifications' ? 'active' : ''}" href="/notifications.html">
                            <i class="bi bi-envelope me-1"></i> Notifications
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link ${activePage === 'themes' ? 'active' : ''}" href="/themes.html">
                            <i class="bi bi-palette me-1"></i> Thèmes
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link ${activePage === 'stats' ? 'active' : ''}" href="/stats.html">
                            <i class="bi bi-graph-up me-1"></i> Statistiques
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link ${activePage === 'api' ? 'active' : ''}" href="/api.html">
                            <i class="bi bi-code-slash me-1"></i> API
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link ${activePage === 'help' ? 'active' : ''}" href="/help.html">
                            <i class="bi bi-question-circle me-1"></i> Support
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link ${activePage === 'home' ? 'active' : ''}" href="/home.html">
                            <i class="bi bi-house me-1"></i> Home
                        </a>
                    </li>
                </ul>
            </div>
        </div>
    </nav>
    `;
}

// Injecter la navbar si un élément avec ID navbarContainer existe
document.addEventListener('DOMContentLoaded', () => {
    const navContainer = document.getElementById('navbarContainer');
    if (navContainer) {
        const activePage = navContainer.dataset.active || '';
        navContainer.innerHTML = getStandardNavbar(activePage);
    }
});
