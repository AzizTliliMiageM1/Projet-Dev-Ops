// Vérifie la session et met à jour la navbar
async function checkSessionAndUpdateNavbar() {
    try {
        const response = await fetch('/api/session');
        const data = await response.json();
        
        if (data.authenticated) {
            // Trouve les boutons de connexion/inscription (avec ou sans /)
            const loginBtn = document.querySelector('a[href="/login.html"], a[href="login.html"]');
            const registerBtn = document.querySelector('a[href="/register.html"], a[href="register.html"]');
            
            if (loginBtn) {
                loginBtn.innerHTML = `<i class="bi bi-person-circle"></i> ${data.pseudo}`;
                loginBtn.href = '#';
                loginBtn.style.pointerEvents = 'none';
                loginBtn.style.background = 'rgba(16, 185, 129, 0.2)';
                loginBtn.style.borderColor = 'rgba(16, 185, 129, 0.4)';
            }
            
            if (registerBtn) {
                registerBtn.innerHTML = '<i class="bi bi-box-arrow-right"></i> Déconnexion';
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
