#!/bin/bash

# Script de démarrage du serveur API
# Usage: ./start-server.sh

set -e

echo "🚀 Démarrage du serveur API..."
echo ""

cd "$(dirname "$0")/backend"

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Vérifier Java
if ! command -v java &> /dev/null; then
    echo -e "${RED}❌ Java n'est pas installé!${NC}"
    exit 1
fi

echo -e "${BLUE}✓ Java détecté: $(java -version 2>&1 | grep -oP '(?<=version ")[^"]*')${NC}"

# Compiler si nécessaire
if [ ! -d "target/classes" ]; then
    echo -e "\n${YELLOW}⏳ Compilation en cours...${NC}"
    mvn clean compile -q
    echo -e "${GREEN}✓ Compilation réussie${NC}"
fi

# Créer le répertoire de logs
mkdir -p logs

# Tuer l'ancien processus si existant
if [ -f "server.pid" ]; then
    OLD_PID=$(cat server.pid)
    if ps -p "$OLD_PID" > /dev/null 2>&1; then
        echo -e "${YELLOW}⚠️  Arrêt du serveur précédent (PID: $OLD_PID)...${NC}"
        kill -9 "$OLD_PID" 2>/dev/null || true
        sleep 1
    fi
    rm -f server.pid
fi

# Vérifier le port 4567
if lsof -i :4567 > /dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  Le port 4567 est déjà utilisé!${NC}"
    echo -e "${YELLOW}   Essai de libération du port...${NC}"
    lsof -ti :4567 | xargs kill -9 2>/dev/null || true
    sleep 1
fi

# Lancer le serveur
echo -e "\n${BLUE}🚀 Lancement du serveur sur le port 4567...${NC}"

# Option 1: Via Maven (recommandé pour le développement)
if [ "$1" == "--maven" ] || [ -z "$1" ]; then
    mvn exec:java -Dexec.mainClass="com.projet.api.ApiServer" \
        -Dexec.classpathScope=compile \
        -q 2>&1 | tee logs/server.log
else
    # Option 2: Via JAR (production)
    if [ ! -f "target/gestion-abonnements-1.0-SNAPSHOT.jar" ]; then
        echo -e "${YELLOW}📦 Création du JAR...${NC}"
        mvn package -DskipTests -q
    fi
    
    echo -e "${GREEN}✓ JAR trouvé${NC}"
    java -jar target/gestion-abonnements-1.0-SNAPSHOT.jar 2>&1 | tee logs/server.log
fi

echo "$$" > server.pid

echo ""
echo -e "${GREEN}✅ Serveur démarré!${NC}"
echo ""
echo -e "${BLUE}📝 Commandes utiles:${NC}"
echo ""
echo "  1. Tester la session:"
echo "     curl http://localhost:4567/api/session"
echo ""
echo "  2. Tester avec JSON:"
echo "     curl -s http://localhost:4567/api/session | jq ."
echo ""
echo "  3. Consulter les logs:"
echo "     tail -f logs/server.log"
echo ""
echo "  4. Arrêter le serveur:"
echo "     Ctrl+C"
echo ""
echo "⏳ Le serveur écoute sur ${BLUE}http://localhost:4567${NC}"
echo ""
