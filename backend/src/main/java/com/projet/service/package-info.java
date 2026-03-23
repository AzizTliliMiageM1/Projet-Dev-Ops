/**
 * Couche Service - Logique Métier
 * 
 * Responsabilités:
 * - Exécuter la logique métier indépendante des I/O
 * - Orchestrer les opérations complexes
 * - Valider les données selon les règles métier
 * - Aucune dépendance aux contrôleurs ou persistance
 * 
 * Organization:
 * - Subscription* : Gestion des abonnements
 * - Portfolio* : Gestion du portefeuille
 * - Exchange* : Conversions de devises
 * - Benchmark* : Comparaisons de prix
 * - Forecast* : Prévisions financières
 * 
 * Pattern: Service Layer - Chaque service a une responsabilité unique
 */
package com.projet.service;
