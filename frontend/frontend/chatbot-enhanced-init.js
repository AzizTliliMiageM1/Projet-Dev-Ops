/**
 * Chatbot Widget - Initialisation Améliorée v2.0
 * Intègre l'IA avancée avec interface moderne et animations fluides
 * Features: Historique persistant, suggestions dynamiques, mode responsive
 */

// Initialiser au chargement
document.addEventListener('DOMContentLoaded', initAdvancedChatbot);

let chatbotEngine = null;
let messageCount = 0;

function initAdvancedChatbot() {
    // Créer le HTML du widget
    const chatbotHTML = `
        <!-- Bulle de bienvenue -->
        <div class="chatbot-welcome-bubble" id="chatbotWelcomeBubble" style="pointer-events: auto;">
            👋 Besoin d'aide ?
        </div>

        <!-- Bouton flottant -->
        <div class="chatbot-trigger" id="chatbotTrigger" style="pointer-events: auto;">
            <i class="bi bi-robot"></i>
            <div class="chatbot-badge" id="chatbotBadge" style="display: none;">1</div>
        </div>

        <!-- Fenêtre du chatbot -->
        <div class="chatbot-window" id="chatbotWindow">
            <!-- Header -->
            <div class="chatbot-header">
                <div class="chatbot-header-info">
                    <div class="chatbot-avatar">
                        <i class="bi bi-robot"></i>
                    </div>
                    <div>
                        <h5>Assistant IA</h5>
                        <div class="chatbot-status">
                            <span class="chatbot-status-dot"></span>
                            <span>En ligne</span>
                        </div>
                    </div>
                </div>
                <div class="chatbot-actions">
                    <button class="chatbot-action-btn" onclick="resetAdvancedChatbot()" title="Nouvelle conversation">
                        <i class="bi bi-arrow-clockwise"></i>
                    </button>
                    <button class="chatbot-action-btn" onclick="toggleAdvancedChatbot()" title="Fermer">
                        <i class="bi bi-x-lg"></i>
                    </button>
                </div>
            </div>

            <!-- Messages -->
            <div class="chatbot-messages" id="chatbotMessages">
                <div class="chatbot-welcome" id="chatbotWelcome">
                    <div class="chatbot-welcome-icon">
                        <i class="bi bi-stars"></i>
                    </div>
                    <h4>Bienvenue ! 👋</h4>
                    <p>Je suis votre assistant IA. Comment puis-je vous aider ?</p>
                    
                    <div class="chatbot-quick-actions">
                        <div class="chatbot-quick-action" onclick="sendAdvancedChatMessage('Quel est mon budget mensuel ?')">
                            <i class="bi bi-currency-euro"></i>
                            <strong>Budget</strong>
                            <small>Voir dépenses</small>
                        </div>
                        <div class="chatbot-quick-action" onclick="sendAdvancedChatMessage('Mes alertes d\\'inactivité')">
                            <i class="bi bi-exclamation-triangle"></i>
                            <strong>Alertes</strong>
                            <small>Vérifier inactivité</small>
                        </div>
                        <div class="chatbot-quick-action" onclick="sendAdvancedChatMessage('Comment économiser ?')">
                            <i class="bi bi-lightbulb"></i>
                            <strong>Conseils</strong>
                            <small>Optimiser budget</small>
                        </div>
                        <div class="chatbot-quick-action" onclick="sendAdvancedChatMessage('aide')">
                            <i class="bi bi-question-circle"></i>
                            <strong>Aide</strong>
                            <small>Fonctionnalités</small>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Typing indicator -->
            <div class="chatbot-typing" id="chatbotTyping" style="display: none;">
                <div class="chatbot-typing-dot"></div>
                <div class="chatbot-typing-dot"></div>
                <div class="chatbot-typing-dot"></div>
            </div>

            <!-- Suggestions -->
            <div class="chatbot-suggestions" id="chatbotSuggestions">
                <div class="chatbot-suggestion-chip" onclick="sendAdvancedChatMessage('💰 Mon budget')">
                    💰 Budget
                </div>
                <div class="chatbot-suggestion-chip" onclick="sendAdvancedChatMessage('📋 Mes abonnements')">
                    📋 Liste
                </div>
                <div class="chatbot-suggestion-chip" onclick="sendAdvancedChatMessage('⚠️ Mes alertes')">
                    ⚠️ Alertes
                </div>
                <div class="chatbot-suggestion-chip" onclick="sendAdvancedChatMessage('💡 Conseils')">
                    💡 Conseils
                </div>
                <div class="chatbot-suggestion-chip" onclick="sendAdvancedChatMessage('📊 Analytics')">
                    📊 Stats
                </div>
                <div class="chatbot-suggestion-chip" onclick="sendAdvancedChatMessage('🆘 Aide')">
                    🆘 Aide
                </div>
            </div>

            <!-- Input -->
            <div class="chatbot-input-area">
                <input 
                    type="text" 
                    id="chatbotInput" 
                    placeholder="Posez une question..."
                    onkeypress="handleAdvancedChatKeyPress(event)"
                    autocomplete="off"
                >
                <button class="chatbot-send-btn" onclick="sendAdvancedChatMessage()">
                    <i class="bi bi-send-fill"></i>
                </button>
            </div>
        </div>
    `;

    // Injecter le HTML
    document.body.insertAdjacentHTML('beforeend', chatbotHTML);

    // Initialiser le moteur de chatbot
    chatbotEngine = new AdvancedAbonnementChatbot();

    // Event listeners
    document.getElementById('chatbotTrigger').addEventListener('click', toggleAdvancedChatbot);
    document.getElementById('chatbotWelcomeBubble').addEventListener('click', toggleAdvancedChatbot);

    // Fermer la bulle après 5 secondes si pas d'interaction
    setTimeout(() => {
        const bubble = document.getElementById('chatbotWelcomeBubble');
        if (bubble) bubble.classList.remove('show');
    }, 5000);
}

// ============================
// Gestion de l'interface
// ============================

function toggleAdvancedChatbot() {
    const window_el = document.getElementById('chatbotWindow');
    const trigger = document.getElementById('chatbotTrigger');
    
    if (window_el.style.display === 'flex') {
        window_el.style.display = 'none';
        trigger.classList.remove('active');
    } else {
        window_el.style.display = 'flex';
        trigger.classList.add('active');
        document.getElementById('chatbotInput').focus();
        
        // Masquer la bulle de bienvenue
        const bubble = document.getElementById('chatbotWelcomeBubble');
        if (bubble) bubble.style.display = 'none';
    }
}

async function sendAdvancedChatMessage(quickMessage = null) {
    const input = document.getElementById('chatbotInput');
    const message = quickMessage || input.value.trim();
    
    if (!message) return;

    // Masquer le welcome screen
    const welcomeScreen = document.getElementById('chatbotWelcome');
    if (welcomeScreen) {
        welcomeScreen.style.display = 'none';
    }

    // Masquer les suggestions initialement
    const suggestions = document.getElementById('chatbotSuggestions');
    if (suggestions && messageCount > 0) {
        suggestions.style.opacity = '0.3';
    }

    // Ajouter le message utilisateur
    addAdvancedChatMessage('user', message);
    input.value = '';
    input.focus();

    // Afficher l'indicateur de saisie
    showAdvancedChatTyping();

    try {
        // Générer la réponse
        const response = await chatbotEngine.generateResponse(message);

        // Simuler un délai de frappe réaliste
        const delay = 500 + Math.random() * 1000;
        setTimeout(() => {
            hideAdvancedChatTyping();
            addAdvancedChatMessage('bot', response);
            messageCount++;
        }, delay);
    } catch (error) {
        console.error('Erreur chatbot:', error);
        hideAdvancedChatTyping();
        addAdvancedChatMessage('bot', "❌ Désolé, une erreur s'est produite. Réessayez !");
    }
}

function addAdvancedChatMessage(sender, content) {
    const messagesContainer = document.getElementById('chatbotMessages');
    
    // Créer le message
    const messageDiv = document.createElement('div');
    messageDiv.className = `chatbot-message chatbot-message-${sender}`;
    messageDiv.style.animation = 'slideInMessage 0.3s ease-out';

    if (sender === 'bot') {
        messageDiv.innerHTML = `
            <div class="chatbot-message-avatar">
                <i class="bi bi-robot"></i>
            </div>
            <div class="chatbot-message-content">
                ${markdownToHtml(content)}
            </div>
        `;
    } else {
        messageDiv.innerHTML = `
            <div class="chatbot-message-content">
                ${escapeHtml(content)}
            </div>
        `;
    }

    messagesContainer.appendChild(messageDiv);

    // Auto-scroll vers le bas
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

function showAdvancedChatTyping() {
    const typing = document.getElementById('chatbotTyping');
    if (typing) typing.style.display = 'flex';
}

function hideAdvancedChatTyping() {
    const typing = document.getElementById('chatbotTyping');
    if (typing) typing.style.display = 'none';
}

function handleAdvancedChatKeyPress(event) {
    if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault();
        sendAdvancedChatMessage();
    }
}

function resetAdvancedChatbot() {
    if (chatbotEngine) {
        chatbotEngine.reset();
    }

    const messagesContainer = document.getElementById('chatbotMessages');
    messagesContainer.innerHTML = `
        <div class="chatbot-welcome" id="chatbotWelcome">
            <div class="chatbot-welcome-icon">
                <i class="bi bi-stars"></i>
            </div>
            <h4>Nouvelle conversation 🔄</h4>
            <p>Comment puis-je vous aider ?</p>
            
            <div class="chatbot-quick-actions">
                <div class="chatbot-quick-action" onclick="sendAdvancedChatMessage('Quel est mon budget mensuel ?')">
                    <i class="bi bi-currency-euro"></i>
                    <strong>Budget</strong>
                    <small>Voir dépenses</small>
                </div>
                <div class="chatbot-quick-action" onclick="sendAdvancedChatMessage('Mes alertes d\\'inactivité')">
                    <i class="bi bi-exclamation-triangle"></i>
                    <strong>Alertes</strong>
                    <small>Vérifier inactivité</small>
                </div>
                <div class="chatbot-quick-action" onclick="sendAdvancedChatMessage('Comment économiser ?')">
                    <i class="bi bi-lightbulb"></i>
                    <strong>Conseils</strong>
                    <small>Optimiser budget</small>
                </div>
                <div class="chatbot-quick-action" onclick="sendAdvancedChatMessage('aide')">
                    <i class="bi bi-question-circle"></i>
                    <strong>Aide</strong>
                    <small>Fonctionnalités</small>
                </div>
            </div>
        </div>
    `;

    messageCount = 0;
}

// ============================
// Utilitaires
// ============================

function markdownToHtml(markdown) {
    // Conversion simple Markdown → HTML
    let html = escapeHtml(markdown);
    
    // Gras **texte** ou __texte__
    html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
    html = html.replace(/__(.*?)__/g, '<strong>$1</strong>');
    
    // Italique *texte* ou _texte_
    html = html.replace(/\*(.*?)\*/g, '<em>$1</em>');
    html = html.replace(/_(.*?)_/g, '<em>$1</em>');
    
    // Listes
    html = html.replace(/^• (.*?)$/gm, '<li>$1</li>');
    html = html.replace(/(<li>.*?<\/li>)/s, '<ul>$1</ul>');
    
    // Sauts de ligne
    html = html.replace(/\n/g, '<br>');
    
    return html;
}

function escapeHtml(text) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return text.replace(/[&<>"']/g, m => map[m]);
}

// Ajouter les styles CSS pour les animations
const chatbotStyles = `
    @keyframes slideInMessage {
        from {
            opacity: 0;
            transform: translateY(10px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }

    .chatbot-message {
        animation: slideInMessage 0.3s ease-out;
    }

    .chatbot-typing {
        display: flex;
        gap: 4px;
        padding: 12px;
        background: rgba(102, 126, 234, 0.05);
        border-radius: 12px;
        width: fit-content;
    }

    .chatbot-typing-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: #667eea;
        animation: bounce 1.4s infinite;
    }

    .chatbot-typing-dot:nth-child(1) { animation-delay: -0.32s; }
    .chatbot-typing-dot:nth-child(2) { animation-delay: -0.16s; }

    @keyframes bounce {
        0%, 80%, 100% { transform: scale(0); opacity: 0.5; }
        40% { transform: scale(1); opacity: 1; }
    }

    .chatbot-suggestion-chip {
        cursor: pointer;
        transition: all 0.2s ease;
    }

    .chatbot-suggestion-chip:hover {
        transform: translateY(-2px);
        background: #667eea;
        color: white;
    }
`;

// Injecter les styles
const style = document.createElement('style');
style.textContent = chatbotStyles;
document.head.appendChild(style);
