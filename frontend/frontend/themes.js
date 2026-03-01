// Gestion des thèmes personnalisables
const THEMES = {
    violet: {
        primary: '#667eea',
        secondary: '#764ba2',
        accent: '#f093fb',
        name: 'Violet Premium'
    },
    bleu: {
        primary: '#4facfe',
        secondary: '#00f2fe',
        accent: '#43e97b',
        name: 'Bleu Océan'
    },
    rose: {
        primary: '#f857a6',
        secondary: '#ff5858',
        accent: '#feca57',
        name: 'Rose Sunset'
    },
    vert: {
        primary: '#11998e',
        secondary: '#38ef7d',
        accent: '#7ed6df',
        name: 'Vert Nature'
    },
    orange: {
        primary: '#f79d00',
        secondary: '#ff6b6b',
        accent: '#ee5a6f',
        name: 'Orange Énergie'
    },
    minimal: {
        primary: '#667eea',
        secondary: '#764ba2',
        accent: '#a8a8a8',
        name: 'Minimaliste'
    }
};

// Charger le thème sauvegardé
function loadSavedTheme() {
    const savedTheme = localStorage.getItem('userTheme');
    if (savedTheme) {
        const theme = JSON.parse(savedTheme);
        applyThemeColors(theme.primary, theme.secondary, theme.accent);
        updateColorPickers(theme.primary, theme.secondary, theme.accent);
    }
}

// Appliquer les couleurs du thème
function applyThemeColors(primary, secondary, accent) {
    document.documentElement.style.setProperty('--primary-color', primary);
    document.documentElement.style.setProperty('--secondary-color', secondary);
    document.documentElement.style.setProperty('--accent-color', accent);
    
    // Mettre à jour le gradient de fond
    const bgGradient = `radial-gradient(circle at 20% 50%, ${hexToRgba(primary, 0.3)}, transparent 50%),
                        radial-gradient(circle at 80% 80%, ${hexToRgba(secondary, 0.2)}, transparent 50%),
                        linear-gradient(135deg, #1e1e2e 0%, #2d1b4e 100%)`;
    document.documentElement.style.setProperty('--bg-gradient', bgGradient);
    
    // Sauvegarder le thème
    saveTheme(primary, secondary, accent);
    
    // Appliquer aussi aux autres pages
    applyThemeGlobally(primary, secondary, accent);
}

// Sauvegarder le thème
function saveTheme(primary, secondary, accent) {
    const theme = {
        primary,
        secondary,
        accent,
        timestamp: new Date().toISOString()
    };
    localStorage.setItem('userTheme', JSON.stringify(theme));
}

// Appliquer le thème globalement
function applyThemeGlobally(primary, secondary, accent) {
    // Enregistrer dans localStorage pour que toutes les pages l'utilisent
    localStorage.setItem('globalTheme', JSON.stringify({
        primary,
        secondary,
        accent
    }));
}

// Convertir hex en rgba
function hexToRgba(hex, alpha) {
    const r = parseInt(hex.slice(1, 3), 16);
    const g = parseInt(hex.slice(3, 5), 16);
    const b = parseInt(hex.slice(5, 7), 16);
    return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}

// Mettre à jour les color pickers
function updateColorPickers(primary, secondary, accent) {
    document.getElementById('primaryColor').value = primary;
    document.getElementById('secondaryColor').value = secondary;
    document.getElementById('accentColor').value = accent;
    
    document.getElementById('primaryHex').textContent = primary;
    document.getElementById('secondaryHex').textContent = secondary;
    document.getElementById('accentHex').textContent = accent;
}

// Appliquer un thème prédéfini
function applyPresetTheme(themeName) {
    const theme = THEMES[themeName];
    if (theme) {
        applyThemeColors(theme.primary, theme.secondary, theme.accent);
        updateColorPickers(theme.primary, theme.secondary, theme.accent);
        
        // Mettre à jour l'état actif des cartes
        document.querySelectorAll('.theme-card').forEach(card => {
            card.classList.remove('active');
        });
        document.querySelector(`[data-theme="${themeName}"]`).classList.add('active');
    }
}

// Appliquer le thème personnalisé
function applyCustomTheme() {
    const primary = document.getElementById('primaryColor').value;
    const secondary = document.getElementById('secondaryColor').value;
    const accent = document.getElementById('accentColor').value;
    
    applyThemeColors(primary, secondary, accent);
    
    // Animation de confirmation
    const btn = event.target;
    const originalText = btn.innerHTML;
    btn.innerHTML = '<i class="bi bi-check-circle-fill me-2"></i> Thème Appliqué !';
    btn.style.background = 'linear-gradient(135deg, #10b981, #059669)';
    
    setTimeout(() => {
        btn.innerHTML = originalText;
        btn.style.background = '';
    }, 2000);
}

// Réinitialiser au thème par défaut
function resetTheme() {
    const defaultTheme = THEMES.violet;
    applyThemeColors(defaultTheme.primary, defaultTheme.secondary, defaultTheme.accent);
    updateColorPickers(defaultTheme.primary, defaultTheme.secondary, defaultTheme.accent);
    
    // Réactiver la carte du thème violet
    document.querySelectorAll('.theme-card').forEach(card => {
        card.classList.remove('active');
    });
    document.querySelector('[data-theme="violet"]').classList.add('active');
    
    // Animation
    const btn = event.target;
    const originalText = btn.innerHTML;
    btn.innerHTML = '<i class="bi bi-check-circle-fill me-2"></i> Réinitialisé !';
    
    setTimeout(() => {
        btn.innerHTML = originalText;
    }, 2000);
}

// Event listeners pour les color pickers
document.addEventListener('DOMContentLoaded', () => {
    // Charger le thème sauvegardé
    loadSavedTheme();
    
    // Color pickers
    const primaryPicker = document.getElementById('primaryColor');
    const secondaryPicker = document.getElementById('secondaryColor');
    const accentPicker = document.getElementById('accentColor');
    
    primaryPicker.addEventListener('input', (e) => {
        document.getElementById('primaryHex').textContent = e.target.value;
        applyThemeColors(e.target.value, secondaryPicker.value, accentPicker.value);
    });
    
    secondaryPicker.addEventListener('input', (e) => {
        document.getElementById('secondaryHex').textContent = e.target.value;
        applyThemeColors(primaryPicker.value, e.target.value, accentPicker.value);
    });
    
    accentPicker.addEventListener('input', (e) => {
        document.getElementById('accentHex').textContent = e.target.value;
        applyThemeColors(primaryPicker.value, secondaryPicker.value, e.target.value);
    });
    
    // Cartes de thèmes prédéfinis
    document.querySelectorAll('.theme-card').forEach(card => {
        card.addEventListener('click', () => {
            const themeName = card.dataset.theme;
            applyPresetTheme(themeName);
        });
    });
});

// Export pour utilisation globale
window.ThemeManager = {
    loadSavedTheme,
    applyThemeColors,
    THEMES
};
