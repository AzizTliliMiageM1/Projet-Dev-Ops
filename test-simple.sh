#!/bin/bash

# Script de test complet - Version Simple et Robuste
# Gère les cookies de session automatiquement

COOKIE_JAR="/tmp/api_cookies.txt"
API="http://localhost:4567"

# Couleurs
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

rm -f "$COOKIE_JAR"

echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  🚀 Tests API Complets - Mode Simple  ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
echo ""

# ===== TEST 1: Vérifier la session (avant connexion) =====
echo -e "${YELLOW}1️⃣  Vérifier la session (anonyme)${NC}"
curl -s -b "$COOKIE_JAR" -c "$COOKIE_JAR" "$API/api/session" | jq .
echo ""

# ===== TEST 2: S'inscrire =====
echo -e "${YELLOW}2️⃣  S'inscrire${NC}"
REGISTER_RESP=$(curl -s -X POST -b "$COOKIE_JAR" -c "$COOKIE_JAR" "$API/api/register?email=aziz@example.com&password=Pass123&pseudo=aziztlili")
echo "$REGISTER_RESP"
echo ""

# ===== TEST 2.5: Confirmer l'email (DEV MODE) =====
echo -e "${YELLOW}2️⃣.5️⃣  Confirmer l'email (mode dev)${NC}"
CONFIRM_RESP=$(curl -s -X POST -b "$COOKIE_JAR" -c "$COOKIE_JAR" "$API/api/confirm-dev?email=aziz@example.com")
echo "$CONFIRM_RESP"
echo ""

# ===== TEST 3: Se connecter =====
echo -e "${YELLOW}3️⃣  Se connecter (attention: pas d'exclamation!)${NC}"
LOGIN_RESP=$(curl -s -X POST -b "$COOKIE_JAR" -c "$COOKIE_JAR" "$API/api/login?email=aziz@example.com&password=Pass123")
echo "$LOGIN_RESP"
echo ""

# ===== TEST 4: Vérifier la session (après connexion) =====
echo -e "${YELLOW}4️⃣  Vérifier la session (authentifié)${NC}"
curl -s -b "$COOKIE_JAR" "$API/api/session" | jq .
echo ""

# ===== TEST 5: Lister les abonnements =====
echo -e "${YELLOW}5️⃣  Lister les abonnements${NC}"
curl -s -b "$COOKIE_JAR" "$API/api/abonnements" | jq . 2>/dev/null || curl -s -b "$COOKIE_JAR" "$API/api/abonnements"
echo ""

# ===== TEST 6: Ajouter un abonnement =====
echo -e "${YELLOW}6️⃣  Ajouter un abonnement${NC}"
curl -s -X POST \
  -b "$COOKIE_JAR" \
  -H "Content-Type: application/json" \
  -d '{"nomService":"Netflix","prixMensuel":15.99,"clientName":"John Doe","dateDebut":"2025-01-01","dateFin":"2026-12-31"}' \
  "$API/api/abonnements" | jq . 2>/dev/null || curl -s -X POST \
  -b "$COOKIE_JAR" \
  -H "Content-Type: application/json" \
  -d '{"nomService":"Netflix","prixMensuel":15.99,"clientName":"John Doe","dateDebut":"2025-01-01","dateFin":"2026-12-31"}' \
  "$API/api/abonnements"
echo ""

# ===== TEST 7: Consulter les stats =====
echo -e "${YELLOW}7️⃣  Consulter les statistiques${NC}"
curl -s -b "$COOKIE_JAR" "$API/api/analytics/stats" | jq . 2>/dev/null || curl -s -b "$COOKIE_JAR" "$API/api/analytics/stats"
echo ""

# ===== TEST 8: Optimisation =====
echo -e "${YELLOW}8️⃣  Recommandations d'optimisation${NC}"
curl -s -b "$COOKIE_JAR" "$API/api/analytics/optimize" | jq . 2>/dev/null || curl -s -b "$COOKIE_JAR" "$API/api/analytics/optimize"
echo ""

echo -e "${GREEN}✅ Tests terminés!${NC}"
echo ""
echo -e "${BLUE}📝 Notes:${NC}"
echo "  - Les cookies sont sauvegardés automatiquement"
echo "  - La session est maintenue entre les requêtes"
echo "  - Fichier cookies: $COOKIE_JAR"
