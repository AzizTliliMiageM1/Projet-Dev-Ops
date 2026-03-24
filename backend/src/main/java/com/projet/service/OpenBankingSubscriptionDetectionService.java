package com.projet.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.projet.backend.domain.BenchmarkResult;
import com.projet.backend.domain.DetectedSubscription;
import com.projet.backend.domain.Transaction;

/**
 * Service de détection d'abonnements récurrents via Open Banking.
 * Analyse des transactions bancaires pour identifier automatiquement les abonnements.
 */
public class OpenBankingSubscriptionDetectionService {

    private final ExchangeRateService exchangeRateService;
    private final BenchmarkService benchmarkService;

    public OpenBankingSubscriptionDetectionService() {
        this(new ExchangeRateServiceImpl(), new BenchmarkServiceImpl());
    }

    public OpenBankingSubscriptionDetectionService(ExchangeRateService exchangeRateService, BenchmarkService benchmarkService) {
        this.exchangeRateService = exchangeRateService;
        this.benchmarkService = benchmarkService;
    }

    // ===== DICTIONNAIRES DE RECONNAISSANCE =====

    private static final Map<String, String> SERVICE_MAPPING = Map.ofEntries(
        Map.entry("netflix", "Netflix"),
        Map.entry("spotify", "Spotify"),
        Map.entry("amazon", "Amazon Prime"),
        Map.entry("prime", "Amazon Prime"),
        Map.entry("amzn", "Amazon Prime"),
        Map.entry("apple", "Apple"),
        Map.entry("icloud", "Apple iCloud"),
        Map.entry("app store", "Apple App Store"),
        Map.entry("appstore", "Apple App Store"),
        Map.entry("itunes", "Apple iTunes"),
        Map.entry("google", "Google"),
        Map.entry("google one", "Google One"),
        Map.entry("youtube", "YouTube Premium"),
        Map.entry("disney", "Disney+"),
        Map.entry("adobe", "Adobe Creative Cloud"),
        Map.entry("microsoft", "Microsoft 365"),
        Map.entry("office", "Microsoft Office"),
        Map.entry("xbox", "Xbox Game Pass"),
        Map.entry("playstation", "PlayStation Plus"),
        Map.entry("deezer", "Deezer"),
        Map.entry("canal", "Canal+"),
        Map.entry("dropbox", "Dropbox"),
        Map.entry("chatgpt", "ChatGPT Plus"),
        Map.entry("openai", "OpenAI"),
        Map.entry("github", "GitHub Pro"),
        Map.entry("paypal", "PayPal"),
        Map.entry("notion", "Notion"),
        Map.entry("figma", "Figma"),
        Map.entry("canva", "Canva Pro"),
        Map.entry("crunchyroll", "Crunchyroll"),
        Map.entry("molotov", "Molotov"),
        Map.entry("hbo", "HBO Max"),
        Map.entry("ubisoft", "Ubisoft+"),
        Map.entry("ea play", "EA Play"),
        Map.entry("linkedin", "LinkedIn Premium"),
        Map.entry("slack", "Slack"),
        Map.entry("zoom", "Zoom Pro"),
        Map.entry("sfr", "SFR Mobile"),
        Map.entry("orange", "Orange Mobile"),
        Map.entry("free", "Free Mobile"),
        Map.entry("ovh", "OVH Cloud"),
        Map.entry("kimsufi", "Kimsufi"),
        Map.entry("basicfit", "BasicFit"),
        Map.entry("fitness", "Salle de Sport"),
        Map.entry("keepcool", "KeepCool Fitness")
    );

    private static final Map<String, String> CATEGORY_MAPPING = Map.ofEntries(
        Map.entry("Netflix", "Streaming"),
        Map.entry("Spotify", "Musique"),
        Map.entry("Amazon Prime", "Streaming"),
        Map.entry("Apple", "Technologie"),
        Map.entry("Apple iCloud", "Cloud"),
        Map.entry("Apple App Store", "Technologie"),
        Map.entry("Apple iTunes", "Musique"),
        Map.entry("Google", "Technologie"),
        Map.entry("Google One", "Cloud"),
        Map.entry("YouTube Premium", "Streaming"),
        Map.entry("Disney+", "Streaming"),
        Map.entry("Adobe Creative Cloud", "Productivité"),
        Map.entry("Microsoft 365", "Productivité"),
        Map.entry("Microsoft Office", "Productivité"),
        Map.entry("Xbox Game Pass", "Gaming"),
        Map.entry("PlayStation Plus", "Gaming"),
        Map.entry("Deezer", "Musique"),
        Map.entry("Canal+", "Streaming"),
        Map.entry("Dropbox", "Cloud"),
        Map.entry("ChatGPT Plus", "IA"),
        Map.entry("OpenAI", "IA"),
        Map.entry("GitHub Pro", "Développement"),
        Map.entry("PayPal", "Paiement"),
        Map.entry("Notion", "Productivité"),
        Map.entry("Figma", "Productivité"),
        Map.entry("Canva Pro", "Productivité"),
        Map.entry("Crunchyroll", "Streaming"),
        Map.entry("Molotov", "Streaming"),
        Map.entry("HBO Max", "Streaming"),
        Map.entry("Ubisoft+", "Gaming"),
        Map.entry("EA Play", "Gaming"),
        Map.entry("LinkedIn Premium", "Professionnel"),
        Map.entry("Slack", "Professionnel"),
        Map.entry("Zoom Pro", "Professionnel"),
        Map.entry("SFR Mobile", "Télécom"),
        Map.entry("Orange Mobile", "Télécom"),
        Map.entry("Free Mobile", "Télécom"),
        Map.entry("OVH Cloud", "Cloud"),
        Map.entry("Kimsufi", "Cloud"),
        Map.entry("BasicFit", "Fitness"),
        Map.entry("Salle de Sport", "Fitness"),
        Map.entry("KeepCool Fitness", "Fitness")
    );

    // ===== PARSING =====

    /**
     * Parse un CSV simple (Date;Label;Amount).
     */
    public List<Transaction> parseCSV(String csvContent) {
        List<Transaction> transactions = new ArrayList<>();
        
        if (csvContent == null || csvContent.trim().isEmpty()) {
            return transactions;
        }

        String normalizedContent = csvContent.replace("\uFEFF", "").trim();
        String[] lines = normalizedContent.split("\\r?\\n");
        if (lines.length == 0) {
            return transactions;
        }

        char delimiter = detectDelimiter(lines[0]);
        Map<String, Integer> headerIndexes = extractHeaderIndexes(lines[0], delimiter);
        int startLine = headerIndexes.isEmpty() ? 0 : 1;

        for (int i = startLine; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            List<String> parts = splitCsvLine(line, delimiter);
            if (parts.size() < 3) continue;

            try {
                String rawDate = getColumn(parts, headerIndexes, List.of("date"), 0);
                String label = getColumn(parts, headerIndexes, List.of("label", "description", "libelle", "libellé", "service"), 1);
                String rawAmount = getColumn(parts, headerIndexes, List.of("amount", "montant", "prix", "debit", "débit"), 2);

                LocalDate date = parseDate(rawDate);
                double amount = parseAmount(rawAmount);

                if (date == null || label == null || label.isBlank()) {
                    continue;
                }

                Transaction tx = new Transaction(label, amount, date);
                transactions.add(tx);
            } catch (Exception e) {
                // Ignorer les lignes invalides
            }
        }

        return transactions;
    }

    // ===== NORMALISATION =====

    /**
     * Normalise un label de transaction en identifiant le service connu.
     */
    public String normalizeServiceName(String rawLabel) {
        String mappedService = findMappedService(rawLabel);
        return mappedService != null ? mappedService : rawLabel;
    }

    /**
     * Catégorise un service détecté.
     */
    public String categorizeService(String serviceName) {
        return CATEGORY_MAPPING.getOrDefault(serviceName, "Autre");
    }

    // ===== DÉTECTION DE RÉCURRENCE =====

    /**
     * Détecte les abonnements récurrents dans une liste de transactions.
     */
    public List<DetectedSubscription> detectRecurringSubscriptions(List<Transaction> transactions) {
        Map<String, List<Transaction>> grouped = groupByService(transactions);
        List<DetectedSubscription> detected = new ArrayList<>();

        for (String service : grouped.keySet()) {
            List<Transaction> group = grouped.get(service);

            // Normaliser le service
            String normalized = normalizeServiceName(service);
            String category = categorizeService(normalized);

            // Analyser la récurrence
            if (group.size() >= 2) {
                double avgAmount = group.stream().mapToDouble(Transaction::getAmount).average().orElse(0);
                boolean isStable = isAmountStable(group);
                boolean isMonthly = isMonthlyRecurrence(group);

                if (isStable && isMonthly) {
                    DetectedSubscription sub = new DetectedSubscription(normalized, category, avgAmount, "Mensuel");

                    sub.setOccurrences(group.size());
                    sub.setFirstDetected(group.get(group.size() - 1).getDate());
                    sub.setLastDetected(group.get(0).getDate());

                    // Calculer confiance et score
                    double confidence = calculateConfidence(isStable, true, group.size());
                    sub.setConfidence(confidence);

                    double score = calculateOptimizationScore(avgAmount, group.size(), confidence, category, 0.0, false);
                    sub.setOptimizationScore(score);

                    String recommendation = generateRecommendation(avgAmount, confidence, category, group.size(), 0.0, "OPTIMIZED", "");
                    sub.setRecommendation(recommendation);

                    detected.add(sub);
                }
            } else if (isKnownService(group.get(0).getRawLabel(), normalized)) {
                Transaction tx = group.get(0);
                DetectedSubscription sub = new DetectedSubscription(normalized, category, tx.getAmount(), "Probable");
                sub.setOccurrences(1);
                sub.setFirstDetected(tx.getDate());
                sub.setLastDetected(tx.getDate());
                sub.setConfidence(0.55);
                sub.setOptimizationScore(calculateOptimizationScore(tx.getAmount(), 1, 0.55, category, 0.0, false));
                sub.setRecommendation("Service connu détecté une fois. Vérifiez si ce paiement est récurrent.");
                detected.add(sub);
            }
        }

        return detected;
    }

    /**
     * Groupe les transactions par service normalisé.
     */
    private Map<String, List<Transaction>> groupByService(List<Transaction> transactions) {
        Map<String, List<Transaction>> grouped = new HashMap<>();

        for (Transaction tx : transactions) {
            String normalized = normalizeServiceName(tx.getRawLabel());
            grouped.computeIfAbsent(normalized, k -> new ArrayList<>()).add(tx);
        }

        // Trier les transactions de chaque groupe par date (plus récente d'abord)
        grouped.values().forEach(list -> list.sort((a, b) -> b.getDate().compareTo(a.getDate())));

        return grouped;
    }

    private boolean isKnownService(String rawLabel, String normalized) {
        if (rawLabel == null || normalized == null) {
            return false;
        }

        return findMappedService(rawLabel) != null;
    }

    private String findMappedService(String rawLabel) {
        if (rawLabel == null || rawLabel.isBlank()) {
            return null;
        }

        String lower = rawLabel.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, String> entry : SERVICE_MAPPING.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Vérifie si les montants sont stables (+/- 1€).
     */
    private boolean isAmountStable(List<Transaction> group) {
        if (group.size() < 2) return false;

        double avg = group.stream().mapToDouble(Transaction::getAmount).average().orElse(0);

        for (Transaction tx : group) {
            if (Math.abs(tx.getAmount() - avg) > 1.0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Vérifie si les transactions sont mensuelles.
     */
    private boolean isMonthlyRecurrence(List<Transaction> group) {
        if (group.size() < 2) return false;

        group.sort(Comparator.comparing(Transaction::getDate));

        for (int i = 0; i < group.size() - 1; i++) {
            long daysBetween = ChronoUnit.DAYS.between(group.get(i).getDate(), group.get(i + 1).getDate());
            if (daysBetween < 20 || daysBetween > 40) {
                return false; // Pas mensuel
            }
        }

        return true;
    }

    private char detectDelimiter(String headerLine) {
        int semicolonCount = countOccurrences(headerLine, ';');
        int commaCount = countOccurrences(headerLine, ',');
        return semicolonCount >= commaCount ? ';' : ',';
    }

    private int countOccurrences(String value, char needle) {
        int count = 0;
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == needle) {
                count++;
            }
        }
        return count;
    }

    private Map<String, Integer> extractHeaderIndexes(String headerLine, char delimiter) {
        Map<String, Integer> indexes = new HashMap<>();
        List<String> headers = splitCsvLine(headerLine, delimiter);
        for (int i = 0; i < headers.size(); i++) {
            indexes.put(normalizeHeader(headers.get(i)), i);
        }

        boolean containsKnownHeader = indexes.containsKey("date")
            || indexes.containsKey("description")
            || indexes.containsKey("label")
            || indexes.containsKey("montant")
            || indexes.containsKey("amount");

        return containsKnownHeader ? indexes : Collections.emptyMap();
    }

    private String normalizeHeader(String value) {
        return value == null ? "" : value.trim()
            .toLowerCase(Locale.ROOT)
            .replace("é", "e")
            .replace("è", "e")
            .replace("ê", "e")
            .replace("à", "a")
            .replace("ç", "c");
    }

    private List<String> splitCsvLine(String line, char delimiter) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                inQuotes = !inQuotes;
                continue;
            }

            if (ch == delimiter && !inQuotes) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        result.add(current.toString().trim());
        return result;
    }

    private String getColumn(List<String> parts, Map<String, Integer> headerIndexes, List<String> names, int fallbackIndex) {
        for (String name : names) {
            Integer idx = headerIndexes.get(name);
            if (idx != null && idx >= 0 && idx < parts.size()) {
                return parts.get(idx).trim();
            }
        }

        return fallbackIndex < parts.size() ? parts.get(fallbackIndex).trim() : "";
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        List<DateTimeFormatter> formatters = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(value.trim(), formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        return null;
    }

    private double parseAmount(String value) {
        if (value == null || value.isBlank()) {
            return 0.0;
        }

        String cleaned = value.trim()
            .replace("€", "")
            .replace("\u00A0", "")
            .replace(" ", "")
            .replace(",", ".");

        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // ===== SCORING =====

    /**
     * Calcule le score de confiance.
     */
    private double calculateConfidence(boolean amountStable, boolean isMonthly, int occurrences) {
        double confidence = 0.0;

        if (amountStable) confidence += 0.3;
        if (isMonthly) confidence += 0.3;
        if (occurrences >= 3) confidence += 0.2;
        if (occurrences >= 6) confidence += 0.2;

        return Math.min(1.0, confidence);
    }

    /**
     * Calcule le score d'optimisation (0-100).
     */
    private double calculateOptimizationScore(
        double monthlyAmount,
        int occurrences,
        double confidence,
        String category,
        double marketDeviationPercent,
        boolean nonEurPayment
    ) {
        double score = 0.0;

        // Montant élevé = plus optimisable
        if (monthlyAmount >= 50) score += 35;
        else if (monthlyAmount >= 20) score += 25;
        else if (monthlyAmount >= 10) score += 15;
        else score += 5;

        // Récurrence = plus d'impact
        if (occurrences >= 6) score += 15;
        else if (occurrences >= 3) score += 10;
        else score += 5;

        // Confiance = fiabilité
        score += (confidence * 20);

        // Catégorie loisir = plus optimisable
        if (category.equals("Streaming") || category.equals("Musique") || category.equals("Gaming")) {
            score += 15;
        } else if (category.equals("Professionnel") || category.equals("Développement")) {
            score -= 10; // Moins optimisable
        }

        // Déviation marché (API benchmark) = levier d'optimisation supplémentaire
        if (marketDeviationPercent >= 30) {
            score += 15;
        } else if (marketDeviationPercent >= 15) {
            score += 10;
        } else if (marketDeviationPercent >= 5) {
            score += 6;
        } else if (marketDeviationPercent <= -20) {
            score -= 5;
        } else {
            score += 2;
        }

        // Paiement hors EUR = légère pénalité de risque de change
        if (nonEurPayment) {
            score += 5;
        }

        return Math.max(0.0, Math.min(100.0, score));
    }

    /**
     * Génère une recommandation métier.
     */
    private String generateRecommendation(
        double amount,
        double confidence,
        String category,
        int occurrences,
        double marketDeviationPercent,
        String marketStatus,
        String benchmarkRecommendation
    ) {
        if (confidence < 0.5) {
            return "Détection incertaine - à vérifier manuellement";
        }

        StringBuilder rec = new StringBuilder();

        if (amount >= 50) {
            rec.append("⚠️ Abonnement coûteux détecté (").append(String.format("%.2f", amount)).append("€/mois)");
        } else if (amount >= 20) {
            rec.append("📊 Abonnement actif détecté");
        } else {
            rec.append("✓ Petit abonnement détecté");
        }

        if (occurrences >= 6) {
            rec.append(" - Récurrence confirmée sur plusieurs mois");
        }

        if (category.equals("Streaming") || category.equals("Musique") || category.equals("Gaming")) {
            rec.append(". Vérifiez son utilité personnelle");
        }

        if ("OVERPRICED".equals(marketStatus) && marketDeviationPercent > 10) {
            rec.append(". Potentiel d'économie marché: ")
                .append(String.format(Locale.ROOT, "%.1f", marketDeviationPercent))
                .append("% au-dessus de la moyenne");
        } else if ("UNDERPRICED".equals(marketStatus) && marketDeviationPercent < -10) {
            rec.append(". Bon tarif par rapport au marché");
        }

        if (benchmarkRecommendation != null && !benchmarkRecommendation.isBlank()) {
            rec.append(". ").append(benchmarkRecommendation);
        }

        return rec.toString();
    }

    /**
     * Enrichit les détections avec des appels API externes :
     * - taux de change (ExchangeRate)
     * - benchmark de prix marché (service externe)
     */
    public List<Map<String, Object>> buildDetectionApiPayload(
        List<DetectedSubscription> detected,
        String sourceCurrency,
        String targetCurrency
    ) {
        List<Map<String, Object>> payload = new ArrayList<>();
        if (detected == null || detected.isEmpty()) {
            return payload;
        }

        String source = normalizeCurrency(sourceCurrency);
        String target = normalizeCurrency(targetCurrency);

        for (int i = 0; i < detected.size(); i++) {
            DetectedSubscription sub = detected.get(i);
            double amountSource = sub.getAmount();
            double amountEur = amountSource;
            double amountTarget = amountSource;
            boolean exchangeApiUsed = false;

            try {
                if (!"EUR".equals(source)) {
                    amountEur = exchangeRateService.convertAmount(amountSource, source, "EUR");
                    exchangeApiUsed = true;
                }

                if (!source.equals(target)) {
                    amountTarget = exchangeRateService.convertAmount(amountSource, source, target);
                    exchangeApiUsed = true;
                }
            } catch (Exception ignored) {
                amountEur = amountSource;
                amountTarget = amountSource;
            }

            boolean benchmarkApiUsed = false;
            double marketAvg = 0.0;
            double marketMin = 0.0;
            double marketMax = 0.0;
            double marketDeviation = 0.0;
            String marketStatus = "UNKNOWN";
            String benchmarkRecommendation = "";

            try {
                BenchmarkResult benchmark = benchmarkService.benchmark("detected-" + i, sub.getService(), amountEur);
                benchmarkApiUsed = true;
                marketAvg = benchmark.getMarketAveragePrice();
                marketMin = benchmark.getMarketMinPrice();
                marketMax = benchmark.getMarketMaxPrice();
                marketDeviation = benchmark.getPriceDeviation();
                marketStatus = benchmark.getStatus();
                benchmarkRecommendation = benchmark.getRecommendation();
            } catch (Exception ignored) {
            }

            double score = calculateOptimizationScore(
                amountEur,
                sub.getOccurrences(),
                sub.getConfidence(),
                sub.getCategory(),
                marketDeviation,
                !"EUR".equals(source)
            );
            sub.setOptimizationScore(score);

            String recommendation = generateRecommendation(
                amountEur,
                sub.getConfidence(),
                sub.getCategory(),
                sub.getOccurrences(),
                marketDeviation,
                marketStatus,
                benchmarkRecommendation
            );
            sub.setRecommendation(recommendation);

            Map<String, Object> scoreBreakdown = new HashMap<>();
            scoreBreakdown.put("amountWeight", getAmountWeight(amountEur));
            scoreBreakdown.put("recurrenceWeight", getRecurrenceWeight(sub.getOccurrences()));
            scoreBreakdown.put("confidenceWeight", round2(sub.getConfidence() * 20));
            scoreBreakdown.put("categoryWeight", getCategoryWeight(sub.getCategory()));
            scoreBreakdown.put("marketWeight", getMarketWeight(marketDeviation));
            scoreBreakdown.put("currencyRiskWeight", !"EUR".equals(source) ? 5 : 0);
            scoreBreakdown.put("total", round2(score));

            Map<String, Object> item = new HashMap<>();
            item.put("service", sub.getService());
            item.put("category", sub.getCategory());
            item.put("amount", round2(sub.getAmount()));
            item.put("frequency", sub.getFrequency());
            item.put("confidence", sub.getConfidence());
            item.put("optimizationScore", round2(sub.getOptimizationScore()));
            item.put("recommendation", sub.getRecommendation());
            item.put("occurrences", sub.getOccurrences());
            item.put("firstDetected", sub.getFirstDetected());
            item.put("lastDetected", sub.getLastDetected());
            item.put("sourceCurrency", source);
            item.put("targetCurrency", target);
            item.put("amountEUR", round2(amountEur));
            item.put("amountTargetCurrency", round2(amountTarget));
            item.put("marketAveragePrice", round2(marketAvg));
            item.put("marketMinPrice", round2(marketMin));
            item.put("marketMaxPrice", round2(marketMax));
            item.put("marketDeviationPercent", round2(marketDeviation));
            item.put("marketStatus", marketStatus);
            item.put("scoreBreakdown", scoreBreakdown);
            item.put("externalApis", Map.of(
                "exchangeRateApi", exchangeApiUsed,
                "benchmarkApi", benchmarkApiUsed
            ));
            payload.add(item);
        }

        return payload;
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return "EUR";
        }

        String normalized = currency.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("^[A-Z]{3}$")) {
            return "EUR";
        }

        return normalized;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private int getAmountWeight(double monthlyAmount) {
        if (monthlyAmount >= 50) return 35;
        if (monthlyAmount >= 20) return 25;
        if (monthlyAmount >= 10) return 15;
        return 5;
    }

    private int getRecurrenceWeight(int occurrences) {
        if (occurrences >= 6) return 15;
        if (occurrences >= 3) return 10;
        return 5;
    }

    private int getCategoryWeight(String category) {
        if ("Streaming".equals(category) || "Musique".equals(category) || "Gaming".equals(category)) {
            return 15;
        }
        if ("Professionnel".equals(category) || "Développement".equals(category)) {
            return -10;
        }
        return 0;
    }

    private int getMarketWeight(double marketDeviationPercent) {
        if (marketDeviationPercent >= 30) return 15;
        if (marketDeviationPercent >= 15) return 10;
        if (marketDeviationPercent >= 5) return 6;
        if (marketDeviationPercent <= -20) return -5;
        return 2;
    }

    // ===== CONVERSION EN ABONNEMENT =====

    /**
     * Convertit un DetectedSubscription en Abonnement.
     */
    public com.projet.backend.domain.Abonnement convertToAbonnement(DetectedSubscription detected, String userEmail) {
        LocalDate today = LocalDate.now();
        LocalDate dateDebut = today.minusMonths(1);
        LocalDate dateFin = today.plusMonths(11);

        com.projet.backend.domain.Abonnement abo = new com.projet.backend.domain.Abonnement(
            detected.getService(),
            dateDebut,
            dateFin,
            detected.getAmount(),
            userEmail != null ? userEmail : "Détection bancaire"
        );

        abo.setCategorie(detected.getCategory());
        abo.setDerniereUtilisation(today);

        return abo;
    }
}
