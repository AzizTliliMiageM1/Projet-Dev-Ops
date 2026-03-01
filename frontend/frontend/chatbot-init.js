/**
 * Chatbot Widget Intégré - Script d'initialisation
 * À inclure dans toutes les pages pour avoir le chatbot disponible
 */

// Initialiser le chatbot au chargement de la page
document.addEventListener('DOMContentLoaded', function() {
    initChatbotWidget();
});

function initChatbotWidget() {
    // Créer le HTML du widget
    const chatbotHTML = `
        <!-- Bulle de message d'accueil -->
        <div class="chatbot-welcome-bubble show" id="chatbotWelcomeBubble" style="pointer-events: auto; cursor: pointer;">
            👋 Bonjour ! Besoin d'aide ? Je suis là !
        </div>
        
        <!-- Bouton flottant -->
        <div class="chatbot-trigger" id="chatbotTrigger" style="pointer-events: auto; cursor: pointer;">
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
                    <button class="chatbot-action-btn" onclick="resetChatbot()" title="Nouvelle conversation">
                        <i class="bi bi-arrow-clockwise"></i>
                    </button>
                    <button class="chatbot-action-btn" onclick="toggleChatbotWidget()" title="Fermer">
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
                    <h4>Bonjour ! 👋</h4>
                    <p>Je suis votre assistant IA. Comment puis-je vous aider ?</p>
                    
                    <div class="chatbot-quick-actions">
                        <div class="chatbot-quick-action" onclick="sendChatbotQuickMessage('Quel est mon budget mensuel ?')">
                            <i class="bi bi-currency-euro"></i>
                            <strong>Budget</strong>
                            <small>Voir dépenses</small>
                        </div>
                        <div class="chatbot-quick-action" onclick="sendChatbotQuickMessage('Mes alertes d\\'inactivité')">
                            <i class="bi bi-exclamation-triangle"></i>
                            <strong>Alertes</strong>
                            <small>Vérifier inactivité</small>
                        </div>
                        <div class="chatbot-quick-action" onclick="sendChatbotQuickMessage('Comment économiser ?')">
                            <i class="bi bi-lightbulb"></i>
                            <strong>Conseils</strong>
                            <small>Optimiser budget</small>
                        </div>
                        <div class="chatbot-quick-action" onclick="sendChatbotQuickMessage('aide')">
                            <i class="bi bi-question-circle"></i>
                            <strong>Aide</strong>
                            <small>Fonctionnalités</small>
                        </div>
                    </div>
                </div>

                <!-- Typing indicator -->
                <div class="chatbot-typing" id="chatbotTyping">
                    <div class="chatbot-typing-dot"></div>
                    <div class="chatbot-typing-dot"></div>
                    <div class="chatbot-typing-dot"></div>
                </div>
            </div>

            <!-- Suggestions -->
            <div class="chatbot-suggestions">
                <div class="chatbot-suggestion-chip" onclick="sendChatbotQuickMessage('💰 Mon budget')">
                    💰 Budget
                </div>
                <div class="chatbot-suggestion-chip" onclick="sendChatbotQuickMessage('⚠️ Mes alertes')">
                    ⚠️ Alertes
                </div>
                <div class="chatbot-suggestion-chip" onclick="sendChatbotQuickMessage('💡 Conseils')">
                    💡 Conseils
                </div>
                <div class="chatbot-suggestion-chip" onclick="sendChatbotQuickMessage('📊 Analytics')">
                    📊 Stats
                </div>
            </div>

            <!-- Input -->
            <div class="chatbot-input-area">
                <input 
                    type="text" 
                    id="chatbotInput" 
                    placeholder="Posez une question..."
                    onkeypress="handleChatbotKeyPress(event)"
                >
                <button class="chatbot-send-btn" onclick="sendChatbotMessage()">
                    <i class="bi bi-send-fill"></i>
                </button>
            </div>
        </div>
    `;

    // Injecter le HTML dans le body
    document.body.insertAdjacentHTML('beforeend', chatbotHTML);

    // Initialiser le chatbot engine
    window.chatbotEngine = new AbonnementChatbot();
    window.chatbotMessageCount = 0;

    // Event listener pour le bouton trigger
    document.getElementById('chatbotTrigger').addEventListener('click', toggleChatbotWidget);

    // Masquer la bulle de bienvenue après 8 secondes ou au clic
    const welcomeBubble = document.getElementById('chatbotWelcomeBubble');
    setTimeout(() => {
        if (welcomeBubble) {
            welcomeBubble.classList.remove('show');
            setTimeout(() => welcomeBubble.remove(), 500);
        }
    }, 8000);
    
    if (welcomeBubble) {
        welcomeBubble.addEventListener('click', () => {
            welcomeBubble.classList.remove('show');
            setTimeout(() => welcomeBubble.remove(), 500);
            toggleChatbotWidget();
        });
    }

    // Afficher le badge après 3 secondes
    setTimeout(() => {
        if (!document.getElementById('chatbotWindow').classList.contains('active')) {
            const badge = document.getElementById('chatbotBadge');
            badge.style.display = 'flex';
        }
    }, 3000);
}

// Toggle chatbot window
function toggleChatbotWidget() {
    const chatWindow = document.getElementById('chatbotWindow');
    const trigger = document.getElementById('chatbotTrigger');
    const badge = document.getElementById('chatbotBadge');
    const welcomeBubble = document.getElementById('chatbotWelcomeBubble');
    
    if (!chatWindow) {
        console.error('Chatbot window not found!');
        return;
    }
    
    // Masquer la bulle de bienvenue si elle est encore visible
    if (welcomeBubble) {
        welcomeBubble.classList.remove('show');
        setTimeout(() => welcomeBubble.remove(), 500);
    }
    
    chatWindow.classList.toggle('active');
    trigger.classList.toggle('active');
    
    if (chatWindow.classList.contains('active')) {
        const input = document.getElementById('chatbotInput');
        if (input) input.focus();
        if (badge) badge.style.display = 'none';
    }
}

// Send message
async function sendChatbotMessage() {
    const input = document.getElementById('chatbotInput');
    const message = input.value.trim();
    
    if (!message) return;
    
    // Hide welcome screen
    const welcomeScreen = document.getElementById('chatbotWelcome');
    if (welcomeScreen) {
        welcomeScreen.style.display = 'none';
    }
    
    // Add user message
    addChatbotMessage('user', message);
    input.value = '';
    
    // Show typing indicator
    showChatbotTyping();
    
    // Get bot response
    try {
        const response = await window.chatbotEngine.generateResponse(message);
        
        // Simulate delay for realistic typing
        setTimeout(() => {
            hideChatbotTyping();
            addChatbotMessage('bot', response);
        }, 800 + Math.random() * 800);
    } catch (error) {
        hideChatbotTyping();
        addChatbotMessage('bot', "Désolé, une erreur s'est produite. Réessayez !");
    }
}

// Send quick message
function sendChatbotQuickMessage(message) {
    document.getElementById('chatbotInput').value = message;
    sendChatbotMessage();
}

// Add message to chat
function addChatbotMessage(sender, content) {
    const messagesContainer = document.getElementById('chatbotMessages');
    const messageDiv = document.createElement('div');
    messageDiv.className = `chatbot-message ${sender}`;
    
    const time = new Date().toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
    
    messageDiv.innerHTML = `
        <div class="chatbot-message-avatar">
            <i class="bi bi-${sender === 'bot' ? 'robot' : 'person'}"></i>
        </div>
        <div>
            <div class="chatbot-message-content">${content}</div>
            <div class="chatbot-message-time">${time}</div>
        </div>
    `;
    
    messagesContainer.appendChild(messageDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
    
    window.chatbotMessageCount++;
}

// Typing indicator
function showChatbotTyping() {
    document.getElementById('chatbotTyping').classList.add('active');
    const container = document.getElementById('chatbotMessages');
    container.scrollTop = container.scrollHeight;
}

function hideChatbotTyping() {
    document.getElementById('chatbotTyping').classList.remove('active');
}

// Handle Enter key
function handleChatbotKeyPress(event) {
    if (event.key === 'Enter') {
        sendChatbotMessage();
    }
}

// Reset chat
function resetChatbot() {
    const messagesContainer = document.getElementById('chatbotMessages');
    messagesContainer.innerHTML = `
        <div class="chatbot-welcome" id="chatbotWelcome">
            <div class="chatbot-welcome-icon">
                <i class="bi bi-stars"></i>
            </div>
            <h4>Nouvelle conversation 🔄</h4>
            <p>Comment puis-je vous aider ?</p>
            
            <div class="chatbot-quick-actions">
                <div class="chatbot-quick-action" onclick="sendChatbotQuickMessage('Quel est mon budget mensuel ?')">
                    <i class="bi bi-currency-euro"></i>
                    <strong>Budget</strong>
                    <small>Voir dépenses</small>
                </div>
                <div class="chatbot-quick-action" onclick="sendChatbotQuickMessage('Mes alertes d\\'inactivité')">
                    <i class="bi bi-exclamation-triangle"></i>
                    <strong>Alertes</strong>
                    <small>Vérifier inactivité</small>
                </div>
                <div class="chatbot-quick-action" onclick="sendChatbotQuickMessage('Comment économiser ?')">
                    <i class="bi bi-lightbulb"></i>
                    <strong>Conseils</strong>
                    <small>Optimiser budget</small>
                </div>
                <div class="chatbot-quick-action" onclick="sendChatbotQuickMessage('aide')">
                    <i class="bi bi-question-circle"></i>
                    <strong>Aide</strong>
                    <small>Fonctionnalités</small>
                </div>
            </div>
        </div>
        <div class="chatbot-typing" id="chatbotTyping">
            <div class="chatbot-typing-dot"></div>
            <div class="chatbot-typing-dot"></div>
            <div class="chatbot-typing-dot"></div>
        </div>
    `;
    
    window.chatbotEngine.reset();
    window.chatbotMessageCount = 0;
}
