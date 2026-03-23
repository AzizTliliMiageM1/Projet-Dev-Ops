// Vérifie la session et met à jour la navbar
async function checkSessionAndUpdateNavbar() {
    try {
        const response = await fetch('/api/session');
        const data = await response.json();
        
if (data.authenticated) {
    const loginBtn = document.querySelector('a[href="/login.html"], a[href="login.html"]');
    const registerBtn = document.querySelector('a[href="/register.html"], a[href="register.html"]');

    // 🔥 Transforme le bouton 'Se connecter' → Menu utilisateur (alex)
    if (loginBtn) {
        loginBtn.outerHTML = `
        <li class="nav-item dropdown">
            <a class="nav-link dropdown-toggle" href="#" id="userDropdown"
               role="button" data-bs-toggle="dropdown" aria-expanded="false">
                <i class="bi bi-person-circle me-1"></i> ${data.pseudo}
            </a>

            <ul class="dropdown-menu dropdown-menu-end shadow" aria-labelledby="userDropdown">
                <li><h6 class="dropdown-header">Mon compte</h6></li>

                <li>
                    <a class="dropdown-item" href="/account.html">
                        <i class="bi bi-person-bounding-box me-2"></i> Profil
                    </a>
                </li>

                <li>
                    <a class="dropdown-item" href="/status.html">
                        <i class="bi bi-award me-2"></i> Statut / Niveau
                    </a>
                </li>

                <li>
                    <a class="dropdown-item" href="/personal-info.html">
                        <i class="bi bi-card-list me-2"></i> Informations personnelles
                    </a>
                </li>

                <li>
                    <a class="dropdown-item" href="/password.html">
                        <i class="bi bi-key me-2"></i> Modifier le mot de passe
                    </a>
                </li>

                <li>
                    <a class="dropdown-item" href="/upgrade.html">
                        <i class="bi bi-stars me-2"></i> Upgrade du compte
                    </a>
                </li>
            </ul>
        </li>`;
    }

    // 🔥 Le bouton "S'inscrire" → Devient Déconnexion
    if (registerBtn) {
        registerBtn.innerHTML = '<i class="bi bi-box-arrow-right me-1"></i> Déconnexion';
        registerBtn.href = '#';
        registerBtn.onclick = async (e) => {
            e.preventDefault();
            await fetch('/api/logout', { method: 'POST' });
            window.location.href = '/home.html';
        };
    }
}

    } catch (error) {
        console.error('Erreur lors de la vérification de session:', error);
    }
}

// Exécuter au chargement de la page
document.addEventListener('DOMContentLoaded', checkSessionAndUpdateNavbar);
