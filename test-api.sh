#!/bin/bash

# Script de test complet de l'API
# Usage: ./test-api.sh

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

API="http://localhost:4567"
FAILURES=0
SUCCESSES=0
COOKIES="/tmp/api_cookies.txt"

# Fonction pour afficher un test
test_api() {
    local name=$1
    local method=$2
    local endpoint=$3
    local data=$4
    local desc=$5
    
    echo -e "\n${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}🧪 TEST: $name${NC}"
    echo -e "${YELLOW}$desc${NC}"
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    
    local cmd="curl -s -X $method"
    
    if [ -f "$COOKIES" ]; then
        cmd="$cmd -b $COOKIES -c $COOKIES"
    else
        cmd="$cmd -c $COOKIES"
    fi
    
    cmd="$cmd -H 'Content-Type: application/json'"
    
    if [ ! -z "$data" ]; then
        cmd="$cmd -d '$data'"
    fi
    
    cmd="$cmd $API$endpoint"
    
    echo -e "${YELLOW}Requête:${NC} $method $endpoint"
    
    if [ ! -z "$data" ]; then
        echo -e "${YELLOW}Données:${NC}"
        echo "$data" | jq . 2>/dev/null || echo "$data"
    fi
    
    echo ""
    echo -e "${YELLOW}Réponse:${NC}"
    
    local response=$(eval $cmd)
    
    if [ ! -z "$response" ]; then
        echo "$response" | jq . 2>/dev/null || echo "$response"
        ((SUCCESSES++))
        echo -e "${GREEN}✅ OK${NC}"
    else
        echo -e "${RED}❌ ERREUR - Pas de réponse${NC}"
        ((FAILURES++))
    fi
}

# Vérifier la connexion
echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  🚀 Tests API - Gestion Abonnements  ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"

echo ""
echo -e "${CYAN}Vérification du serveur...${NC}"
if ! curl -s -m 2 "$API/api/session" > /dev/null 2>&1; then
    echo -e "${RED}❌ Le serveur n'est pas accessible sur $API${NC}"
    echo ""
    echo -e "${YELLOW}Pour démarrer le serveur:${NC}"
    echo "  ./start-server.sh"
    exit 1
fi

echo -e "${GREEN}✅ Serveur accessible${NC}"

# ========== TESTS ==========

# Test 1: Session anonyme
test_api \
    "Session Anonyme" \
    "GET" \
    "/api/session" \
    "" \
    "Doit retourner {authenticated: false}"

# Test 2: Créer un utilisateur
USER_EMAIL="test_$(date +%s)@example.com"
USER_PASSWORD="Password123!"
USER_PSEUDO="testuser_$(date +%s)"

test_api \
    "Créer Utilisateur" \
    "POST" \
    "/api/auth/register" \
    "{
        \"email\": \"$USER_EMAIL\",
        \"password\": \"$USER_PASSWORD\",
        \"pseudo\": \"$USER_PSEUDO\"
    }" \
    "Doit créer un nouvel utilisateur"

# Test 3: Se connecter
test_api \
    "Authentification" \
    "POST" \
    "/api/auth/login" \
    "{
        \"email\": \"$USER_EMAIL\",
        \"password\": \"$USER_PASSWORD\"
    }" \
    "Doit se connecter et créer une session"

# Test 4: Vérifier la session
test_api \
    "Session Authentifiée" \
    "GET" \
    "/api/session" \
    "" \
    "Doit retourner {authenticated: true}"

# Test 5: Ajouter un abonnement
SUB_ID=$(date +%s)
test_api \
    "Ajouter Abonnement" \
    "POST" \
    "/api/subscriptions" \
    "{
        \"nomService\": \"Netflix\",
        \"prix\": 15.99,
        \"dateDebut\": \"2025-01-01\",
        \"dateFin\": \"2026-12-31\",
        \"categorie\": \"Streaming\",
        \"priorite\": \"Important\"
    }" \
    "Doit créer un nouvel abonnement"

# Test 6: Lister les abonnements
test_api \
    "Lister Abonnements" \
    "GET" \
    "/api/subscriptions" \
    "" \
    "Doit retourner la liste des abonnements"

# Test 7: Portfolio
test_api \
    "Portfolio Utilisateur" \
    "GET" \
    "/api/portfolio/list" \
    "" \
    "Doit afficher le portfolio avec total, etc."

# Test 8: Statistiques
test_api \
    "Statistiques" \
    "GET" \
    "/api/analytics/stats" \
    "" \
    "Doit retourner les statistiques globales"

# Test 9: Optimisation
test_api \
    "Recommandations Optimisation" \
    "GET" \
    "/api/analytics/optimize" \
    "" \
    "Doit proposer des économies"

# Test 10: Prévisions
test_api \
    "Prévisions Trésorerie" \
    "GET" \
    "/api/analytics/forecast?months=6" \
    "" \
    "Doit calculer les dépenses futures"

# Test 11: Détecteur de doublons
test_api \
    "Détecteur de Doublons" \
    "GET" \
    "/api/analytics/duplicates" \
    "" \
    "Doit détecter les abonnements similaires"

# Test 12: Lifecycle Planning
test_api \
    "Planification Lifecycle (NEW)" \
    "POST" \
    "/api/portfolio/lifecycle-plan" \
    "{
        \"months\": 6,
        \"budget\": 100.0
    }" \
    "Doit générer un plan optimisé multi-mois"

# Test 13: Devises
test_api \
    "Conversion Devises" \
    "GET" \
    "/api/devise/convert?montant=100&deviseSource=EUR&deviseCible=USD" \
    "" \
    "Doit convertir 100 EUR en USD"

# Test 14: Se déconnecter
test_api \
    "Déconnexion" \
    "POST" \
    "/api/auth/logout" \
    "" \
    "Doit se déconnecter"

# ========== RÉSUMÉ ==========

echo ""
echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║           📊 RÉSUMÉ DES TESTS        ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
echo ""
echo -e "${GREEN}✅ Succès:  $SUCCESSES${NC}"
echo -e "${RED}❌ Échecs:   $FAILURES${NC}"
echo ""

if [ $FAILURES -eq 0 ]; then
    echo -e "${GREEN}🎉 Tous les tests sont passés!${NC}"
    exit 0
else
    echo -e "${RED}⚠️  Certains tests ont échoué.${NC}"
    exit 1
fi
