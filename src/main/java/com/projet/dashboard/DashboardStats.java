package com.projet.dashboard;

import java.util.List;
import java.util.Map;

import com.projet.backend.domain.Abonnement;

public class DashboardStats {
    private double totalMonthlyCost;
    private Abonnement mostExpensiveAbonnement;
    private List<Abonnement> expiringSoonAbonnements;
    private Map<String, Double> categorySpending;

    public DashboardStats(double totalMonthlyCost, Abonnement mostExpensiveAbonnement, List<Abonnement> expiringSoonAbonnements, Map<String, Double> categorySpending) {
        this.totalMonthlyCost = totalMonthlyCost;
        this.mostExpensiveAbonnement = mostExpensiveAbonnement;
        this.expiringSoonAbonnements = expiringSoonAbonnements;
        this.categorySpending = categorySpending;
    }

    public double getTotalMonthlyCost() {
        return totalMonthlyCost;
    }

    public Abonnement getMostExpensiveAbonnement() {
        return mostExpensiveAbonnement;
    }

    public List<Abonnement> getExpiringSoonAbonnements() {
        return expiringSoonAbonnements;
    }

    public Map<String, Double> getCategorySpending() {
        return categorySpending;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- Tableau de Bord ---\n");
        sb.append(String.format("  Coût Mensuel Total: %.2f€\n", totalMonthlyCost));
        sb.append("  Abonnement le plus cher: ");
        if (mostExpensiveAbonnement != null) {
            sb.append(String.format("%s (%.2f€)\n", mostExpensiveAbonnement.getNomService(), mostExpensiveAbonnement.getPrixMensuel()));
        } else {
            sb.append("N/A\n");
        }
        sb.append("  Abonnements expirant bientôt (dans les 30 jours):\n");
        if (expiringSoonAbonnements.isEmpty()) {
            sb.append("    Aucun.\n");
        } else {
            for (Abonnement ab : expiringSoonAbonnements) {
                sb.append(String.format("    - %s (%s) expire le %s\n", ab.getNomService(), ab.getClientName(), ab.getDateFin()));
            }
        }
        sb.append("  Dépenses par Catégorie:\n");
        if (categorySpending.isEmpty()) {
            sb.append("    Aucune catégorie définie.\n");
        } else {
            categorySpending.forEach((category, cost) -> sb.append(String.format("    - %s: %.2f€\n", category, cost)));
        }
        sb.append("-------------------------------");
        return sb.toString();
    }
}

