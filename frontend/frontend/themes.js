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

const STORAGE_KEYS = {
    USER_THEME: 'userTheme',
    GLOBAL_THEME: 'globalTheme'
};

function $(id) {
    return document.getElementById(id);
}

function isValidHexColor(value) {
    return /^#[0-9A-Fa-f]{6}$/.test(value);
}

function isValidTheme(theme) {
    return theme
        && isValidHexColor(theme.primary)
        && isValidHexColor(theme.secondary)
        && isValidHexColor(theme.accent);
}

function getDefaultTheme() {
    return THEMES.violet;
}

function getCurrentThemeFromInputs() {
    return {
        primary: $('primaryColor')?.value || getDefaultTheme().primary,
        secondary: $('secondaryColor')?.value || getDefaultTheme().secondary,
        accent: $('accentColor')?.value || getDefaultTheme().accent
    };
}

// Charger le thème sauvegardé
function loadSavedTheme() {
    try {
        const raw = localStorage.getItem(STORAGE_KEYS.USER_THEME);
        if (!raw) {
            const fallback = getDefaultTheme();
            applyThemeColors(fallback.primary, fallback.secondary, fallback.accent, false);
            updateColorPickers(fallback.primary, fallback.secondary, fallback.accent);
            setActiveThemeCard('violet');
            return;
        }

        const savedTheme = JSON.parse(raw);

        if (!isValidTheme(savedTheme)) {
            throw new Error('Theme invalide dans le localStorage');
        }

        applyThemeColors(savedTheme.primary, savedTheme.secondary, savedTheme.accent, false);
        updateColorPickers(savedTheme.primary, savedTheme.secondary, savedTheme.accent);
        setActiveCardFromTheme(savedTheme);
    } catch (error) {
        console.warn('Impossible de charger le thème sauvegardé :', error);
        const fallback = getDefaultTheme();
        applyThemeColors(fallback.primary, fallback.secondary, fallback.accent, true);
        updateColorPickers(fallback.primary, fallback.secondary, fallback.accent);
        setActiveThemeCard('violet');
    }
}

// Appliquer les couleurs du thème
function applyThemeColors(primary, secondary, accent, persist = true) {
    if (![primary, secondary, accent].every(isValidHexColor)) {
        console.warn('Couleurs du thème invalides:', { primary, secondary, accent });
        return;
    }

    document.documentElement.style.setProperty('--primary-color', primary);
    document.documentElement.style.setProperty('--secondary-color', secondary);
    document.documentElement.style.setProperty('--accent-color', accent);

    const bgGradient = `
        radial-gradient(circle at 20% 50%, ${hexToRgba(primary, 0.3)}, transparent 50%),
        radial-gradient(circle at 80% 80%, ${hexToRgba(secondary, 0.2)}, transparent 50%),
        linear-gradient(135deg, #1e1e2e 0%, #2d1b4e 100%)
    `.trim();

    document.documentElement.style.setProperty('--bg-gradient', bgGradient);

    if (persist) {
        saveTheme(primary, secondary, accent);
    }
}

// Sauvegarder le thème
function saveTheme(primary, secondary, accent) {
    const theme = {
        primary,
        secondary,
        accent,
        timestamp: new Date().toISOString()
    };

    localStorage.setItem(STORAGE_KEYS.USER_THEME, JSON.stringify(theme));
    localStorage.setItem(STORAGE_KEYS.GLOBAL_THEME, JSON.stringify({
        primary,
        secondary,
        accent
    }));
}

// Convertir hex en rgba
function hexToRgba(hex, alpha) {
    if (!isValidHexColor(hex)) {
        return `rgba(255, 255, 255, ${alpha})`;
    }

    const r = parseInt(hex.slice(1, 3), 16);
    const g = parseInt(hex.slice(3, 5), 16);
    const b = parseInt(hex.slice(5, 7), 16);

    return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}

// Mettre à jour les color pickers
function updateColorPickers(primary, secondary, accent) {
    if ($('primaryColor')) $('primaryColor').value = primary;
    if ($('secondaryColor')) $('secondaryColor').value = secondary;
    if ($('accentColor')) $('accentColor').value = accent;

    if ($('primaryHex')) $('primaryHex').textContent = primary;
    if ($('secondaryHex')) $('secondaryHex').textContent = secondary;
    if ($('accentHex')) $('accentHex').textContent = accent;
}

function setActiveThemeCard(themeName) {
    document.querySelectorAll('.theme-card').forEach(card => {
        card.classList.toggle('active', card.dataset.theme === themeName);
    });
}

function setActiveCardFromTheme(theme) {
    const matchedThemeName = Object.keys(THEMES).find(key => {
        const preset = THEMES[key];
        return (
            preset.primary === theme.primary &&
            preset.secondary === theme.secondary &&
            preset.accent === theme.accent
        );
    });

    if (matchedThemeName) {
        setActiveThemeCard(matchedThemeName);
    } else {
        document.querySelectorAll('.theme-card').forEach(card => card.classList.remove('active'));
    }
}

// Appliquer un thème prédéfini
function applyPresetTheme(themeName) {
    const theme = THEMES[themeName];
    if (!theme) return;

    applyThemeColors(theme.primary, theme.secondary, theme.accent, true);
    updateColorPickers(theme.primary, theme.secondary, theme.accent);
    setActiveThemeCard(themeName);
}

// Animation de confirmation
function animateButtonFeedback(button, successText, resetDelay = 2000, customBackground = '') {
    if (!button) return;

    const originalText = button.innerHTML;
    const originalBackground = button.style.background;

    button.innerHTML = successText;
    if (customBackground) {
        button.style.background = customBackground;
    }

    setTimeout(() => {
        button.innerHTML = originalText;
        button.style.background = originalBackground;
    }, resetDelay);
}

// Appliquer le thème personnalisé
function applyCustomTheme(button = null) {
    const { primary, secondary, accent } = getCurrentThemeFromInputs();

    applyThemeColors(primary, secondary, accent, true);
    setActiveCardFromTheme({ primary, secondary, accent });

    animateButtonFeedback(
        button,
        '<i class="bi bi-check-circle-fill me-2"></i> Thème appliqué !',
        2000,
        'linear-gradient(135deg, #10b981, #059669)'
    );
}

// Réinitialiser au thème par défaut
function resetTheme(button = null) {
    const defaultTheme = getDefaultTheme();

    applyThemeColors(defaultTheme.primary, defaultTheme.secondary, defaultTheme.accent, true);
    updateColorPickers(defaultTheme.primary, defaultTheme.secondary, defaultTheme.accent);
    setActiveThemeCard('violet');

    animateButtonFeedback(
        button,
        '<i class="bi bi-check-circle-fill me-2"></i> Réinitialisé !'
    );
}

// Initialisation
document.addEventListener('DOMContentLoaded', () => {
    loadSavedTheme();

    const primaryPicker = $('primaryColor');
    const secondaryPicker = $('secondaryColor');
    const accentPicker = $('accentColor');

    if (primaryPicker && secondaryPicker && accentPicker) {
        primaryPicker.addEventListener('input', e => {
            $('primaryHex').textContent = e.target.value;
            applyThemeColors(e.target.value, secondaryPicker.value, accentPicker.value, false);
        });

        secondaryPicker.addEventListener('input', e => {
            $('secondaryHex').textContent = e.target.value;
            applyThemeColors(primaryPicker.value, e.target.value, accentPicker.value, false);
        });

        accentPicker.addEventListener('input', e => {
            $('accentHex').textContent = e.target.value;
            applyThemeColors(primaryPicker.value, secondaryPicker.value, e.target.value, false);
        });

        primaryPicker.addEventListener('change', () => {
            const { primary, secondary, accent } = getCurrentThemeFromInputs();
            saveTheme(primary, secondary, accent);
            setActiveCardFromTheme({ primary, secondary, accent });
        });

        secondaryPicker.addEventListener('change', () => {
            const { primary, secondary, accent } = getCurrentThemeFromInputs();
            saveTheme(primary, secondary, accent);
            setActiveCardFromTheme({ primary, secondary, accent });
        });

        accentPicker.addEventListener('change', () => {
            const { primary, secondary, accent } = getCurrentThemeFromInputs();
            saveTheme(primary, secondary, accent);
            setActiveCardFromTheme({ primary, secondary, accent });
        });
    }

    document.querySelectorAll('.theme-card').forEach(card => {
        card.addEventListener('click', () => {
            const themeName = card.dataset.theme;
            applyPresetTheme(themeName);
        });
    });
});

// Export global
window.ThemeManager = {
    THEMES,
    loadSavedTheme,
    applyThemeColors,
    applyPresetTheme,
    applyCustomTheme,
    resetTheme
};