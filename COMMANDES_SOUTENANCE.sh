#!/bin/bash
# ================================================================
# COMMANDES À ESSAYER DEVANT LE PROFESSEUR
# Projet: Gestion des Abonnements SaaS
# ================================================================

cd /workspaces/Projet-Dev-Ops/backend

echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║   COMMANDES SOUTENANCE - PROJET GESTION ABONNEMENTS        ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# 1. VÉRIFIER LA STRUCTURE DU PROJET
echo "1️⃣ STRUCTURE DU PROJET"
echo "────────────────────────────────────────"
echo "Fichiers de test (16 fichiers) :"
find src/test/java -name "*Test.java" -type f | sort | nl
echo ""

# 2. VÉRIFIER LA COMPILATION
echo "2️⃣ COMPILATION"
echo "────────────────────────────────────────"
echo "Commande : mvn clean compile -q"
mvn clean compile -q && echo "✓ Compilation réussie !"
echo ""

# 3. COMPTER LES TESTS
echo "3️⃣ COMPTE DES TESTS"
echo "────────────────────────────────────────"
echo "Nombre total de methods @Test :"
grep -r "@Test" src/test/java --include="*.java" -h | wc -l
echo ""
echo "Nombre de methods 'public void test*' :"
grep -r "public void test" src/test/java --include="*.java" | wc -l
echo ""

# 4. AFFICHER 10 PREMIERS TESTS
echo "4️⃣ EXEMPLES DE TESTS"
echo "────────────────────────────────────────"
echo "Premiers 10 tests :"
grep -r "public void test" src/test/java --include="*.java" | head -10 | cut -d':' -f2 | sed 's/^[[:space:]]*//' | nl
echo ""

# 5. AFFICHER STATISTIQUES DE CODE
echo "5️⃣ STATISTIQUES DE CODE"
echo "────────────────────────────────────────"
echo "Lignes de code production :"
find src/main -name "*.java" -type f | xargs wc -l | tail -1 | awk '{print $1 " lignes"}'
echo ""
echo "Lignes de code test :"
find src/test -name "*.java" -type f | xargs wc -l | tail -1 | awk '{print $1 " lignes"}'
echo ""

# 6. AFFICHER FICHIER CSV DE TEST
echo "6️⃣ DONNÉES DE TEST"
echo "────────────────────────────────────────"
echo "Fichier CSV de test :"
head -3 test_abonnements_dashboard.csv
echo ""

# 7. EXÉCUTER LES TESTS (optional - à faire si on a du temps)
echo "7️⃣ EXÉCUTER LES TESTS"
echo "────────────────────────────────────────"
echo "Commande : mvn clean test -q"
echo "(À exécuter pour montrer au professeur que tout passe)"
echo ""

# 8. AFFICHER LE RAPPORT
echo "8️⃣ RAPPORT DE TESTS"
echo "────────────────────────────────────────"
echo "Commande pour voir le rapport complet :"
echo "  cat ../TEST_CORRECTIONS_REPORT.md"
echo ""

# 9. LANCER LE SERVEUR
echo "9️⃣ DÉMARRER LE SERVEUR"
echo "────────────────────────────────────────"
echo "Première : compiler le JAR"
echo "  mvn clean package -DskipTests -q"
echo ""
echo "Puis lancer :"
echo "  java -cp target/gestion-abonnements-1.0-SNAPSHOT-jar-with-dependencies.jar com.projet.Main"
echo ""

echo "╔════════════════════════════════════════════════════════════╗"
echo "║   PRÊT POUR LA SOUTENANCE !                               ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""
