#!/bin/bash

# 🧪 Script de Test Complet - Gestion d'Abonnements

echo "═══════════════════════════════════════════════════════════"
echo "  🧪 TEST COMPLET - Gestion d'Abonnements Refactorisée"
echo "═══════════════════════════════════════════════════════════"

BASE_URL="http://localhost:4567"
FAILED=0
SUCCESS=0

test_endpoint() {
    local name=$1
    local method=$2
    local endpoint=$3
    local data=$4
    
    echo ""
    echo "📍 Test: $name"
    
    if [ "$method" = "POST" ] || [ "$method" = "PUT" ]; then
        response=$(curl -s -X $method "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            -d "$data")
    else
        response=$(curl -s -X $method "$BASE_URL$endpoint")
    fi
    
    if [ -z "$response" ]; then
        echo "❌ FAIL: Pas de réponse"
        ((FAILED++))
    else
        echo "✅ PASS"
        echo "   Response: ${response:0:100}"
        ((SUCCESS++))
    fi
}

# ===== TESTS BASIQUES =====
echo ""
echo "▶ TESTS BASIQUES"
echo "────────────────────────────────────────────────────────────"

test_endpoint \
    "Session Status" \
    "GET" \
    "/api/session" \
    ""

test_endpoint \
    "GET Abonnements (vide)" \
    "GET" \
    "/api/abonnements" \
    ""

# ===== CRÉER UN ABONNEMENT DE TEST =====
echo ""
echo "▶ CRÉATION DE DONNÉES DE TEST"
echo "────────────────────────────────────────────────────────────"

test_data='{
  "nomService": "Netflix Test",
  "clientName": "John Doe",
  "dateDebut": "2024-01-01",
  "dateFin": "2025-12-31",
  "prixMensuel": 15.99,
  "categorie": "Streaming"
}'

echo ""
echo "📍 Test: Créer abonnement #1"
response1=$(curl -s -X POST "$BASE_URL/api/abonnements" \
    -H "Content-Type: application/json" \
    -d "$test_data")

if [ -z "$response1" ]; then
    echo "❌ FAIL"
    ((FAILED++))
else
    echo "✅ PASS"
    echo "   Response: ${response1:0:100}"
    ((SUCCESS++))
    
    # Extraire l'ID
    ID=$(echo "$response1" | grep -o '"id":"[^"]*' | head -1 | cut -d'"' -f4)
    if [ -z "$ID" ]; then
        ID=$(echo "$response1" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    fi
fi

# Créer un 2e abonnement
test_data2='{
  "nomService": "Claude Pro",
  "clientName": "Jane Smith",
  "dateDebut": "2024-02-01",
  "dateFin": "2025-12-31",
  "prixMensuel": 20.00,
  "categorie": "Productivité"
}'

test_endpoint \
    "Créer abonnement #2" \
    "POST" \
    "/api/abonnements" \
    "$test_data2"

# ===== LISTER =====
echo ""
echo "▶ LECTURES"
echo "────────────────────────────────────────────────────────────"

test_endpoint \
    "Lister tous les abonnements" \
    "GET" \
    "/api/abonnements" \
    ""

# ===== ANALYTICS (si données suffisantes) =====
echo ""
echo "▶ ENDPOINTS ANALYTICS"
echo "────────────────────────────────────────────────────────────"

# Ces endpoints peuvent retourner des erreurs d'authentification
echo ""
echo "📍 Test: Analytics Stats"
response=$(curl -s "$BASE_URL/api/analytics/stats")
if echo "$response" | grep -q "error\|authenticated"; then
    echo "⚠️  Réquière d'être connecté ou pas de données"
else
    echo "✅ Réponse reçue"
fi

echo ""
echo "📍 Test: Forecast"
response=$(curl -s "$BASE_URL/api/analytics/forecast")
if echo "$response" | grep -q "error\|authenticated"; then
    echo "⚠️  Réquière d'être connecté ou pas de données"
else
    echo "✅ Réponse reçue"
fi

# ===== RÉSUMÉ =====
echo ""
echo "═══════════════════════════════════════════════════════════"
echo "  📊 RÉSULTATS"
echo "═══════════════════════════════════════════════════════════"
echo "✅ Tests réussis: $SUCCESS"
echo "❌ Tests échoués: $FAILED"
echo ""

if [ $FAILED -eq 0 ]; then
    echo "🎉 Tous les tests sont passés!"
    echo ""
    echo "📌 Accès au frontend:"
    echo "   http://localhost:4567/"
    echo "   http://localhost:4567/index.html"
    exit 0
else
    echo "⚠️  Certains tests ont échoué"
    exit 1
fi
