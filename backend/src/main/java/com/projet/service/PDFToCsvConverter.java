package com.projet.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * 🏦 Convertisseur PDF Relevé Bancaire → CSV
 * Extrait automatiquement les transactions d'un relevé bancaire PDF
 */
public class PDFToCsvConverter {

    // Format observé: 30.01 0,60DU 050126 LIBELLE ... 30.01 3001.00
    private static final Pattern BANK_LINE_WITH_RUNNING_BALANCE = Pattern.compile(
        "^(\\d{1,2}[./]\\d{1,2})\\s+([0-9]+[.,][0-9]{2})\\s*DU\\s+(\\d{6})\\s+(.+?)\\s+(\\d{1,2}[./]\\d{1,2})\\s+([0-9]+(?:[.,][0-9]{2}))$",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern DATE_PATTERN = Pattern.compile(
        "(\\d{4}[-/.]\\d{2}[-/.]\\d{2}|\\d{1,2}[-/.]\\d{1,2}[-/.]\\d{2,4}|\\d{1,2}[-/.]\\d{1,2})"
    );

    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
        "([-+]?\\d{1,3}(?:[\\s.]\\d{3})*(?:[.,]\\d{2})|[-+]?\\d+(?:[.,]\\d{2}))\\s*(€|EUR|DEBIT|CREDIT|CR|DR)?"
    );

    /**
     * Convertir un PDF en CSV au format: Date;Label;Amount
     */
    public static String convertPdfToCsv(InputStream pdfInputStream) throws IOException {
        StringBuilder csvContent = new StringBuilder();
        csvContent.append("Date;Label;Amount\n");
        
        // Convertir InputStream en byte array
        byte[] pdfBytes = pdfInputStream.readAllBytes();
        
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            List<BankTransaction> transactions = parseBankTransactions(text);
            
            for (BankTransaction transaction : transactions) {
                csvContent.append(String.format("%s;%s;%.2f\n",
                    transaction.date,
                    sanitizeCsvField(transaction.label),
                    transaction.amount
                ));
            }
        }
        
        return csvContent.toString();
    }

    /**
     * Parser le texte PDF et extraire les transactions bancaires
     */
    private static List<BankTransaction> parseBankTransactions(String text) {
        List<BankTransaction> transactions = new ArrayList<>();

        String[] lines = text.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            BankTransaction transaction = parseTransactionLine(line);

            // Certains relevés coupent une transaction sur 2 lignes
            if (transaction == null && i + 1 < lines.length) {
                String merged = (line + " " + lines[i + 1].trim()).trim();
                transaction = parseTransactionLine(merged);
                if (transaction != null) {
                    i++; // La ligne suivante est déjà consommée
                }
            }

            if (transaction != null) {
                transactions.add(transaction);
            }
        }

        // Déduplication (plusieurs transactions identiques)
        return deduplicateTransactions(transactions);
    }

    /**
     * Parser une ligne de transaction
     */
    private static BankTransaction parseTransactionLine(String line) {
        if (line == null) {
            return null;
        }

        String normalizedLine = line.replace('\u00A0', ' ').trim();
        if (normalizedLine.isEmpty()) {
            return null;
        }

        // Écarter les lignes structurelles non transactionnelles
        String lowerLine = normalizedLine.toLowerCase();
        if (lowerLine.contains("iban") || lowerLine.contains("bic") || lowerLine.contains("releve") || lowerLine.contains("page")) {
            return null;
        }

        // 1) Cas prioritaire: ligne avec montant opération + solde courant en fin de ligne
        Matcher dedicatedMatcher = BANK_LINE_WITH_RUNNING_BALANCE.matcher(normalizedLine);
        if (dedicatedMatcher.find()) {
            String amountStr = dedicatedMatcher.group(2);
            String operationDate = dedicatedMatcher.group(3);
            String rawLabel = dedicatedMatcher.group(4);

            String cleanLabel = cleanupMerchantLabel(rawLabel);
            if (!isValidLabel(cleanLabel)) {
                return null;
            }

            return new BankTransaction(
                convertCompactDate(operationDate),
                cleanLabel,
                parseAmount(amountStr)
            );
        }

        Matcher dateMatcher = DATE_PATTERN.matcher(normalizedLine);
        if (!dateMatcher.find()) {
            return null;
        }

        String dateStr = dateMatcher.group(1);
        String afterDate = normalizedLine.substring(dateMatcher.end()).trim();
        if (afterDate.isEmpty()) {
            return null;
        }

        // Extraire tous les montants de la ligne
        Matcher amountMatcher = AMOUNT_PATTERN.matcher(afterDate);
        List<AmountCandidate> candidates = new ArrayList<>();
        while (amountMatcher.find()) {
            String rawAmount = amountMatcher.group(1);
            String suffix = amountMatcher.group(2);
            candidates.add(new AmountCandidate(rawAmount, suffix, amountMatcher.start()));
        }

        if (candidates.isEmpty()) {
            return null;
        }

        AmountCandidate selected = selectAmountCandidate(candidates);
        String amountStr = selected.amount;
        int amountStart = selected.start;

        String label = afterDate.substring(0, amountStart).trim();
        label = cleanupMerchantLabel(label);

        if (!isValidLabel(label)) {
            return null;
        }

        return new BankTransaction(
            convertDateFormat(dateStr),
            label,
            parseAmount(amountStr)
        );
    }

    /**
     * Convertir un montant au format double
     */
    private static double parseAmount(String amountStr) {
        String cleaned = amountStr.replace(" ", "")
                                  .replace("\u00A0", "")
                                  .replace(".", "")
                                  .replace(",", ".");

        // Garder seulement le dernier point décimal potentiel
        int lastDot = cleaned.lastIndexOf('.');
        if (lastDot > 0) {
            String intPart = cleaned.substring(0, lastDot).replace(".", "");
            String decPart = cleaned.substring(lastDot + 1);
            cleaned = intPart + "." + decPart;
        }

        try {
            return Math.abs(Double.parseDouble(cleaned));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Convertir la date au format YYYY-MM-DD
     */
    private static String convertDateFormat(String date) {
        String normalized = date.trim().replace("-", "/").replace(".", "/");
        String[] parts = normalized.split("/");

        if (parts.length == 3) {
            String day = pad2(parts[0]);
            String month = pad2(parts[1]);
            String year = parts[2];

            // Format déjà ISO: YYYY/MM/DD
            if (year.length() <= 2 && parts[0].length() == 4) {
                year = parts[0];
                month = pad2(parts[1]);
                day = pad2(parts[2]);
            } else {
                if (year.length() == 2) {
                    year = "20" + year;
                }
            }

            return String.format("%s-%s-%s", year, month, day);
        }

        // Certains relevés n'ont pas l'année (DD/MM) -> année courante
        if (parts.length == 2) {
            String year = String.valueOf(LocalDate.now().getYear());
            String day = pad2(parts[0]);
            String month = pad2(parts[1]);
            return String.format("%s-%s-%s", year, month, day);
        }

        return date;
    }

    /**
     * Convertit un format compact DDMMYY vers YYYY-MM-DD.
     */
    private static String convertCompactDate(String ddmmyy) {
        if (ddmmyy == null || ddmmyy.length() != 6) {
            return ddmmyy;
        }

        String day = ddmmyy.substring(0, 2);
        String month = ddmmyy.substring(2, 4);
        String year = "20" + ddmmyy.substring(4, 6);
        return year + "-" + month + "-" + day;
    }

    private static String pad2(String value) {
        String v = value == null ? "" : value.trim();
        return v.length() == 1 ? "0" + v : v;
    }

    /**
     * Vérifier si le label est valide
     */
    private static boolean isValidLabel(String label) {
        // Ignorer les mots-clés inutiles
        String lower = label.toLowerCase();
        return !lower.contains("solde") 
            && !lower.contains("total")
            && !lower.contains("page")
            && !lower.contains("compte")
            && !lower.contains("iban")
            && !lower.contains("bic")
            && !lower.matches("^[\\d\\s.,]+$")
            && label.length() > 2;
    }

    /**
     * Nettoie les artefacts fréquents des relevés: codes/date/solde parasites.
     */
    private static String cleanupMerchantLabel(String label) {
        if (label == null) {
            return "";
        }

        String cleaned = label;
        cleaned = cleaned.replaceAll("\\b\\d{6}\\b", " ");
        cleaned = cleaned.replaceAll("\\b\\d{1,2}[./]\\d{1,2}\\b", " ");
        cleaned = cleaned.replaceAll("\\b(DU|CB|PAIEMENT|CARTE)\\b", " ");
        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        return cleaned;
    }

    /**
     * Choisit le montant le plus probable de transaction quand plusieurs montants sont sur la ligne.
     */
    private static AmountCandidate selectAmountCandidate(List<AmountCandidate> candidates) {
        if (candidates.size() == 1) {
            return candidates.get(0);
        }

        // Priorité 1: montant explicitement marqué débit/crédit
        for (AmountCandidate c : candidates) {
            String suffix = c.suffix == null ? "" : c.suffix.toLowerCase();
            if (suffix.equals("debit") || suffix.equals("credit") || suffix.equals("cr") || suffix.equals("dr")) {
                return c;
            }
        }

        // Priorité 2: montant négatif explicite
        for (AmountCandidate c : candidates) {
            if (c.amount.startsWith("-")) {
                return c;
            }
        }

        // Priorité 3: souvent le dernier montant est le solde, donc prendre l'avant-dernier
        return candidates.get(candidates.size() - 2);
    }

    /**
     * Dédupliquer les transactions identiques
     */
    private static List<BankTransaction> deduplicateTransactions(List<BankTransaction> transactions) {
        Map<String, BankTransaction> map = new LinkedHashMap<>();
        
        for (BankTransaction t : transactions) {
            String key = String.format("%s_%s_%.2f", t.date, t.label, t.amount);
            map.putIfAbsent(key, t);
        }
        
        return new ArrayList<>(map.values());
    }

    /**
     * Nettoyer un champ pour le CSV
     */
    private static String sanitizeCsvField(String field) {
        // Supprimer les caractères spéciaux et les espaces inutiles
        return field.replaceAll(";", ",")
                   .replaceAll("\n", " ")
                   .trim();
    }

    /**
     * Classe représentant une transaction bancaire
     */
    static class BankTransaction {
        String date;
        String label;
        double amount;

        BankTransaction(String date, String label, double amount) {
            this.date = date;
            this.label = label;
            this.amount = amount;
        }
    }

    static class AmountCandidate {
        String amount;
        String suffix;
        int start;

        AmountCandidate(String amount, String suffix, int start) {
            this.amount = amount;
            this.suffix = suffix;
            this.start = start;
        }
    }
}
