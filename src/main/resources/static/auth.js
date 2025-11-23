/**
 * Système d'authentification global
 * Vérifie la session utilisateur et protège les pages
 */

// Liste des pages qui NE nécessitent PAS d'authentification
const PUBLIC_PAGES = ['login.html', 'register.html', 'confirm.html'];

// Vérifier si la page actuelle est publique
function isPublicPage() {
    const currentPage = window.location.pathname.split('/').pop();
    return PUBLIC_PAGES.includes(currentPage);
}

// Vérifier la session utilisateur
async function checkAuth() {
    // Si on est sur une page publique, pas de vérification
    if (isPublicPage()) {
        return;
    }

    try {
        const response = await fetch('/api/session', {
            method: 'GET',
            credentials: 'same-origin'
        });

        const data = await response.json();

        if (!data.authenticated) {
            // Non connecté → redirection vers login
            window.location.href = '/login.html';
            return;
        }

        // Connecté → afficher l'email dans la navbar si disponible
        displayUserInfo(data.email);

    } catch (error) {
        console.error('Erreur lors de la vérification de session:', error);
        // En cas d'erreur, rediriger vers login par sécurité
        window.location.href = '/login.html';
    }
}

// Afficher les informations utilisateur dans la navbar
function displayUserInfo(email) {
    // Chercher la navbar
    const navbar = document.querySelector('.navbar-nav.ms-auto');
    if (!navbar) return;

    // Vérifier si les éléments n'existent pas déjà
    if (document.getElementById('userEmail')) return;

    // Créer l'élément d'affichage de l'email
    const userItem = document.createElement('li');
    userItem.className = 'nav-item d-flex align-items-center me-3';
    userItem.innerHTML = `
        <span id="userEmail" class="text-white-50" style="font-size: 0.9rem;">
            <i class="bi bi-person-circle me-1"></i>${email}
        </span>
    `;

    // Créer le bouton de déconnexion
    const logoutItem = document.createElement('li');
    logoutItem.className = 'nav-item';
    logoutItem.innerHTML = `
        <button id="logoutBtn" class="btn btn-outline-light btn-sm" style="border-radius: 20px;">
            <i class="bi bi-box-arrow-right me-1"></i>Déconnexion
        </button>
    `;

    // Ajouter au début de la navbar
    navbar.prepend(logoutItem);
    navbar.prepend(userItem);

    // Gérer la déconnexion
    document.getElementById('logoutBtn').addEventListener('click', logout);
}

// Fonction de déconnexion
async function logout() {
    try {
        const response = await fetch('/api/logout', {
            method: 'POST',
            credentials: 'same-origin'
        });

        if (response.ok) {
            // Redirection vers la page de login
            window.location.href = '/login.html';
        } else {
            alert('Erreur lors de la déconnexion');
        }
    } catch (error) {
        console.error('Erreur de déconnexion:', error);
        alert('Erreur lors de la déconnexion');
    }
}

// Exécuter la vérification au chargement de la page
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', checkAuth);
} else {
    checkAuth();
}
