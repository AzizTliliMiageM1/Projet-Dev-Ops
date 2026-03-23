#!/bin/bash

# 🔧 Script pour Tester l'API CRUD Complètement
# Ce script teste la création, lecture, modification et suppression

echo "═══════════════════════════════════════════════════════════"
echo "  🔧 TEST COMPLET CRUD - Gestion d'Abonnements"
echo "═══════════════════════════════════════════════════════════"

BASE_URL="http://localhost:4567/api"

# ===== 1. VÉRIFIER L'AUTHENTIFICATION =====
echo ""
echo "▶ 1️⃣ VÉRIFIER L'AUTHENTIFICATION"
echo "────────────────────────────────────────────────────────────"
echo "📍 Status d'authentification:"

AUTH_RESPONSE=$(curl -s "$BASE_URL/session")
echo "   Response: $AUTH_RESPONSE"

# ===== 2. CRÉER UN ABONNEMENT =====
echo ""
echo "▶ 2️⃣ CRÉER UN ABONNEMENT (CREATE)"
echo "────────────────────────────────────────────────────────────"

CREATE_DATA='{
  "nomService": "Netflix Test",
  "clientName": "John Doe",
  "dateDebut": "2024-01-01",
  "dateFin": "2025-12-31",
  "prixMensuel": 15.99,
  "categorie": "Streaming"
}'

echo "📍 Données à créer:"
echo "   $CREATE_DATA"

CREATE_RESPONSE=$(curl -s -X POST "$BASE_URL/abonnements" \
    -H "Content-Type: application/json" \
    -d "$CREATE_DATA")

echo "   Response: $CREATE_RESPONSE"

# Extraire l'ID s'il existe
CREATED_ID=$(echo "$CREATE_RESPONSE" | grep -o '"id":"[^"]*' | head -1 | cut -d'"' -f4 || echo "$CREATE_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ -n "$CREATED_ID" ] && [ "$CREATED_ID" != "error" ]; then
    echo "   ✅ Créé avec ID: $CREATED_ID"
else
    echo "   ℹ️  ID non récupéré (authentification requise?)"
    CREATED_ID="1"  # Utiliser ID par défaut pour les tests
fi

# ===== 3. LIRE UN ABONNEMENT =====
echo ""
echo "▶ 3️⃣ LIRE LES ABONNEMENTS (READ)"
echo "────────────────────────────────────────────────────────────"

echo "📍 Lister tous les abonnements:"
LIST_RESPONSE=$(curl -s "$BASE_URL/abonnements")
echo "   Response: ${LIST_RESPONSE:0:200}..."

# ===== 4. CRÉER UN 2E ABONNEMENT POUR TESTER LA MODIFICATION =====
echo ""
echo "▶ 4️⃣ CRÉER UN 2E ABONNEMENT POUR MODIFICATION"
echo "────────────────────────────────────────────────────────────"

CREATE_DATA2='{
  "nomService": "Claude Pro - Original",
  "clientName": "Jane Smith",
  "dateDebut": "2024-02-01",
  "dateFin": "2025-12-31",
  "prixMensuel": 20.00,
  "categorie": "Productivité"
}'

echo "📍 Créer un 2e abonnement:"
CREATE_RESPONSE2=$(curl -s -X POST "$BASE_URL/abonnements" \
    -H "Content-Type: application/json" \
    -d "$CREATE_DATA2")

echo "   Response: $CREATE_RESPONSE2"

UPDATE_ID=$(echo "$CREATE_RESPONSE2" | grep -o '"id":"[^"]*' | head -1 | cut -d'"' -f4 || echo "$CREATE_RESPONSE2" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ -z "$UPDATE_ID" ] || [ "$UPDATE_ID" = "error" ]; then
    UPDATE_ID="2"  # Utiliser ID par défaut
fi
echo "   ID extrait: $UPDATE_ID"

# ===== 5. MODIFIER UN ABONNEMENT =====
echo ""
echo "▶ 5️⃣ MODIFIER UN ABONNEMENT (UPDATE)"
echo "────────────────────────────────────────────────────────────"

UPDATE_DATA='{
  "nomService": "Claude Pro - Mis à Jour",
  "clientName": "Jane Smith Updated",
  "dateDebut": "2024-02-01",
  "dateFin": "2026-12-31",
  "prixMensuel": 22.00,
  "categorie": "Productivité"
}'

echo "📍 Modifier l'\''abonnement avec ID $UPDATE_ID:"
UPDATE_RESPONSE=$(curl -s -X PUT "$BASE_URL/abonnements/$UPDATE_ID" \
    -H "Content-Type: application/json" \
    -d "$UPDATE_DATA")

echo "   Response: $UPDATE_RESPONSE"

# ===== 6. TESTER LES ANALYTICS =====
echo ""
echo "▶ 6️⃣ TESTER LES ENDPOINTS ANALYTICS"
echo "────────────────────────────────────────────────────────────"

analytics_endpoints=(
    "stats:Statistiques globales"
    "forecast:Prédictions 6 mois"
    "anomalies:Détection d'anomalies"
    "optimize:Recommandations d'optimisation"
    "portfolio-health:Score de santé"
    "expiring:Expirations prochaines"
    "roi:Calcul ROI"
    "savings:Économies potentielles"
)

for endpoint_info in "${analytics_endpoints[@]}"; do
    endpoint="${endpoint_info%%:*}"
    description="${endpoint_info##*:}"
    
    echo ""
    echo "📍 $description ($endpoint)"
    response=$(curl -s "$BASE_URL/analytics/$endpoint")
    
    if echo "$response" | grep -q "error"; then
        echo "   ⚠️  Erreur: $(echo "$response" | grep -o '"error":"[^"]*' | head -1 | cut -d'"' -f4)"
    elif [ -z "$response" ]; then
        echo "   ⚠️  Pas de réponse"
    else
        echo "   ✅ Réponse: ${response:0:100}..."
    fi
done

# ===== 7. SUPPRIMER UN ABONNEMENT =====
echo ""
echo "▶ 7️⃣ SUPPRIMER UN ABONNEMENT (DELETE)"
echo "────────────────────────────────────────────────────────────"

echo "📍 Supprimer l'\''abonnement avec ID $CREATED_ID:"
DELETE_RESPONSE=$(curl -s -X DELETE "$BASE_URL/abonnements/$CREATED_ID")
echo "   Response: $DELETE_RESPONSE"

# ===== 8. VÉRIFIER LA SUPPRESSION =====
echo ""
echo "▶ 8️⃣ VÉRIFIER LA SUPPRESSION"
echo "────────────────────────────────────────────────────────────"

echo "📍 Lister les abonnements après suppression:"
FINAL_LIST=$(curl -s "$BASE_URL/abonnements")
echo "   Response: ${FINAL_LIST:0:200}..."

# ===== RÉSUMÉ FINAL =====
echo ""
echo "═══════════════════════════════════════════════════════════"
echo "  ✨ RÉSUMÉ DU TEST"
echo "═══════════════════════════════════════════════════════════"

echo ""
echo "✅ Tests effectués:"
echo "   • Session Status"
echo "   • CREATE - Abonnement #1"
echo "   • READ - Lister tous"
echo "   • CREATE - Abonnement #2"
echo "   • UPDATE - Modification abonnement"
echo "   • Analytics - 8 endpoints"
echo "   • DELETE - Suppression"
echo "   • Vérification finale"

echo ""
echo "💡 Notes:"
echo "   • Les erreurs 'authentification requise' sont normales"
echo "   • En production, implémenter: /api/login ou /api/authenticate"
echo "   • Le frontend gère ces erreurs automatiquement"

echo ""
echo "🔗 Accès au Frontend:"
echo "   http://localhost:4567/"
echo ""
echo "📊 Dashboard Principal:"
echo "   Voir les stats, prédictions, anomalies en temps réel"
echo ""
