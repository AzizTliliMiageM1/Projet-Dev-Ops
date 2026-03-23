/**
 * Couche API REST - Spark Framework
 * 
 * Responsabilités:
 * - Gérer le routage HTTP des requêtes REST
 * - Mapper les paramètres HTTP aux objets métier
 * - Sérialiser les réponses en JSON
 * - Gérer l'authentification des sessions utilisateur
 * 
 * Pattern: Thin Controllers - la logique métier est dans la couche service
 * 
 * Endpoints principaux:
 * - /api/abonnements - Gestion des abonnements (CRUD)
 * - /api/auth - Authentification et sessions
 * - /analytics/* - Analyses et optimisations
 * - /convert - Conversions de devises
 * - /benchmark - Comparaison avec prix du marché
 */
package com.projet.api;
