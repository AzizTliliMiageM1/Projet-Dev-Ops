package com.projet.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.projet.backend.domain.SubAccount;
import com.projet.backend.domain.SubAccountPayment;
import com.projet.backend.domain.SubAccountSubscription;
import com.projet.config.AppConfig;

public class SubAccountManagementServiceImpl implements SubAccountManagementService {

    private static final Path DATA_DIR = Paths.get("data", "subaccounts");
    private static final Path SUB_ACCOUNTS_FILE = DATA_DIR.resolve("subaccounts.json");
    private static final Path PAYMENTS_FILE = DATA_DIR.resolve("payments.json");

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    @Override
    public SubAccount createSubAccount(String parentEmail, Map<String, Object> request) {
        String email = String.valueOf(request.getOrDefault("email", "")).trim().toLowerCase();
        String firstName = String.valueOf(request.getOrDefault("firstName", "")).trim();
        String lastName = String.valueOf(request.getOrDefault("lastName", "")).trim();

        if (email.isBlank() || firstName.isBlank() || lastName.isBlank()) {
            throw new IllegalArgumentException("email, firstName et lastName sont requis");
        }

        synchronized (this) {
            List<SubAccount> all = readSubAccounts();
            boolean exists = all.stream().anyMatch(s -> parentEmail.equalsIgnoreCase(s.getParentEmail())
                && email.equalsIgnoreCase(s.getEmail())
                && !"DISABLED".equalsIgnoreCase(s.getStatus()));
            if (exists) {
                throw new IllegalArgumentException("Un sous-compte actif existe deja avec cet email");
            }

            Map<String, String> providerData = createExternalSubAccount(email, firstName, lastName);

            SubAccount sub = new SubAccount();
            sub.setId(UUID.randomUUID().toString());
            sub.setParentEmail(parentEmail);
            sub.setEmail(email);
            sub.setFirstName(firstName);
            sub.setLastName(lastName);
            sub.setStatus("ACTIVE");
            sub.setProvider(providerData.getOrDefault("provider", "local"));
            sub.setExternalId(providerData.getOrDefault("externalId", ""));
            sub.setCreatedAt(LocalDateTime.now());
            sub.setUpdatedAt(LocalDateTime.now());

            all.add(sub);
            writeSubAccounts(all);
            return sub;
        }
    }

    @Override
    public List<SubAccount> listSubAccounts(String parentEmail) {
        synchronized (this) {
            return readSubAccounts().stream()
                .filter(s -> parentEmail.equalsIgnoreCase(s.getParentEmail()))
                .collect(Collectors.toList());
        }
    }

    @Override
    public SubAccount getSubAccount(String parentEmail, String subAccountId) {
        synchronized (this) {
            return findSubAccount(parentEmail, subAccountId, readSubAccounts());
        }
    }

    @Override
    public List<SubAccountSubscription> listSubscriptions(String parentEmail, String subAccountId) {
        synchronized (this) {
            SubAccount sub = findSubAccount(parentEmail, subAccountId, readSubAccounts());
            if (sub.getSubscriptions() == null) {
                return List.of();
            }
            return new ArrayList<>(sub.getSubscriptions());
        }
    }

    @Override
    public SubAccount disableSubAccount(String parentEmail, String subAccountId) {
        synchronized (this) {
            List<SubAccount> all = readSubAccounts();
            SubAccount sub = findSubAccount(parentEmail, subAccountId, all);
            if ("okta".equalsIgnoreCase(sub.getProvider()) && sub.getExternalId() != null && !sub.getExternalId().isBlank()) {
                disableOktaUser(sub.getExternalId());
            }

            sub.setStatus("DISABLED");
            sub.setUpdatedAt(LocalDateTime.now());
            writeSubAccounts(all);
            return sub;
        }
    }

    @Override
    public SubAccount deleteSubAccount(String parentEmail, String subAccountId) {
        synchronized (this) {
            List<SubAccount> all = readSubAccounts();
            SubAccount sub = findSubAccount(parentEmail, subAccountId, all);

            if ("okta".equalsIgnoreCase(sub.getProvider()) && sub.getExternalId() != null && !sub.getExternalId().isBlank()) {
                disableOktaUser(sub.getExternalId());
            }

            all.removeIf(s -> parentEmail.equalsIgnoreCase(s.getParentEmail()) && subAccountId.equals(s.getId()));
            writeSubAccounts(all);

            List<SubAccountPayment> payments = readPayments();
            payments.removeIf(p -> parentEmail.equalsIgnoreCase(p.getParentEmail()) && subAccountId.equals(p.getSubAccountId()));
            writePayments(payments);

            return sub;
        }
    }

    @Override
    public Map<String, Object> addSubscription(String parentEmail, Map<String, Object> request) {
        String subAccountId = String.valueOf(request.getOrDefault("subAccountId", "")).trim();
        String serviceName = String.valueOf(request.getOrDefault("serviceName", "")).trim();
        String priceId = String.valueOf(request.getOrDefault("priceId", "")).trim();
        String currency = String.valueOf(request.getOrDefault("currency", "EUR")).trim().toUpperCase();
        double amount = Double.parseDouble(String.valueOf(request.getOrDefault("amount", 0)));

        if (subAccountId.isBlank() || serviceName.isBlank()) {
            throw new IllegalArgumentException("subAccountId et serviceName sont requis");
        }

        synchronized (this) {
            List<SubAccount> all = readSubAccounts();
            SubAccount sub = findSubAccount(parentEmail, subAccountId, all);

            if (!"ACTIVE".equalsIgnoreCase(sub.getStatus())) {
                throw new IllegalArgumentException("Le sous-compte n'est pas actif");
            }

            Map<String, String> stripeResult = createStripeSubscription(sub.getEmail(), serviceName, priceId);

            SubAccountSubscription subscription = new SubAccountSubscription();
            subscription.setId(UUID.randomUUID().toString());
            subscription.setServiceName(serviceName);
            subscription.setPriceId(priceId);
            subscription.setAmount(amount);
            subscription.setCurrency(currency);
            subscription.setStatus(stripeResult.getOrDefault("status", "active"));
            subscription.setProvider(stripeResult.getOrDefault("provider", "local"));
            subscription.setExternalCustomerId(stripeResult.getOrDefault("customerId", ""));
            subscription.setExternalSubscriptionId(stripeResult.getOrDefault("subscriptionId", ""));
            subscription.setCreatedAt(LocalDateTime.now());

            if (sub.getSubscriptions() == null) {
                sub.setSubscriptions(new ArrayList<>());
            }
            sub.getSubscriptions().add(subscription);
            sub.setUpdatedAt(LocalDateTime.now());
            writeSubAccounts(all);

            SubAccountPayment payment = new SubAccountPayment();
            payment.setId(UUID.randomUUID().toString());
            payment.setParentEmail(parentEmail);
            payment.setSubAccountId(subAccountId);
            payment.setServiceName(serviceName);
            payment.setAmount(amount);
            payment.setCurrency(currency);
            payment.setStatus("SUCCESS");
            payment.setProvider(subscription.getProvider());
            payment.setExternalPaymentId(subscription.getExternalSubscriptionId());
            payment.setCreatedAt(LocalDateTime.now());

            List<SubAccountPayment> payments = readPayments();
            payments.add(payment);
            writePayments(payments);

            Map<String, Object> response = new HashMap<>();
            response.put("subscription", subscription);
            response.put("payment", payment);
            response.put("subAccount", sub);
            return response;
        }
    }

    @Override
    public List<SubAccountPayment> listPayments(String parentEmail) {
        synchronized (this) {
            return readPayments().stream()
                .filter(p -> parentEmail.equalsIgnoreCase(p.getParentEmail()))
                .collect(Collectors.toList());
        }
    }

    @Override
    public SubAccount updatePreferences(String parentEmail, String subAccountId, Map<String, String> preferences) {
        synchronized (this) {
            List<SubAccount> all = readSubAccounts();
            SubAccount sub = findSubAccount(parentEmail, subAccountId, all);

            Map<String, String> sanitized = new HashMap<>();
            for (Map.Entry<String, String> entry : preferences.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    sanitized.put(entry.getKey().trim(), entry.getValue().trim());
                }
            }
            sub.setPreferences(sanitized);
            sub.setUpdatedAt(LocalDateTime.now());
            writeSubAccounts(all);
            return sub;
        }
    }

    private Map<String, String> createExternalSubAccount(String email, String firstName, String lastName) {
        String password = UUID.randomUUID().toString();
        String oktaOrg = env("OKTA_ORG_URL");
        String oktaToken = env("OKTA_API_TOKEN");

        if (oktaOrg.isBlank() || oktaToken.isBlank()) {
            return Map.of("provider", "local", "externalId", "");
        }

        try {
            Map<String, Object> body = new HashMap<>();
            Map<String, Object> profile = new HashMap<>();
            profile.put("firstName", firstName);
            profile.put("lastName", lastName);
            profile.put("email", email);
            profile.put("login", email);

            Map<String, Object> credentials = new HashMap<>();
            credentials.put("password", Map.of("value", password));

            body.put("profile", profile);
            body.put("credentials", credentials);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(oktaOrg + "/api/v1/users?activate=true"))
                .header("Authorization", "SSWS " + oktaToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
                .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Okta create user error: HTTP " + response.statusCode() + " - " + response.body());
            }

            Map<String, Object> payload = MAPPER.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
            return Map.of(
                "provider", "okta",
                "externalId", String.valueOf(payload.getOrDefault("id", ""))
            );
        } catch (Exception ex) {
            return Map.of("provider", "local", "externalId", "");
        }
    }

    private void disableOktaUser(String externalId) {
        String oktaOrg = env("OKTA_ORG_URL");
        String oktaToken = env("OKTA_API_TOKEN");
        if (oktaOrg.isBlank() || oktaToken.isBlank()) {
            return;
        }

        try {
            HttpRequest deactivate = HttpRequest.newBuilder()
                .uri(URI.create(oktaOrg + "/api/v1/users/" + externalId + "/lifecycle/deactivate?sendEmail=false"))
                .header("Authorization", "SSWS " + oktaToken)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
            HTTP_CLIENT.send(deactivate, HttpResponse.BodyHandlers.ofString());

            HttpRequest delete = HttpRequest.newBuilder()
                .uri(URI.create(oktaOrg + "/api/v1/users/" + externalId))
                .header("Authorization", "SSWS " + oktaToken)
                .DELETE()
                .build();
            HTTP_CLIENT.send(delete, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
            // Keep local state change even if provider deletion fails.
        }
    }

    private Map<String, String> createStripeSubscription(String subEmail, String serviceName, String priceId) {
        String stripeKey = env("STRIPE_SECRET_KEY");
        if (stripeKey.isBlank()) {
            return Map.of(
                "provider", "local",
                "status", "active",
                "customerId", "local-customer",
                "subscriptionId", "local-sub-" + UUID.randomUUID()
            );
        }

        if (priceId == null || priceId.isBlank()) {
            throw new IllegalArgumentException("priceId est requis pour une souscription Stripe");
        }

        try {
            String customerBody = "email=" + urlEncode(subEmail)
                + "&description=" + urlEncode("Sous-compte - " + serviceName);
            Map<String, Object> customer = callStripeForm("https://api.stripe.com/v1/customers", customerBody, stripeKey);
            String customerId = String.valueOf(customer.getOrDefault("id", ""));

            String subBody = "customer=" + urlEncode(customerId)
                + "&items[0][price]=" + urlEncode(priceId)
                + "&items[0][quantity]=1";
            Map<String, Object> subscription = callStripeForm("https://api.stripe.com/v1/subscriptions", subBody, stripeKey);

            return Map.of(
                "provider", "stripe",
                "status", String.valueOf(subscription.getOrDefault("status", "active")),
                "customerId", customerId,
                "subscriptionId", String.valueOf(subscription.getOrDefault("id", ""))
            );
        } catch (Exception ex) {
            return Map.of(
                "provider", "local",
                "status", "active",
                "customerId", "local-customer",
                "subscriptionId", "local-sub-" + UUID.randomUUID()
            );
        }
    }

    private Map<String, Object> callStripeForm(String url, String formBody, String stripeKey) throws IOException, InterruptedException {
        String auth = Base64.getEncoder().encodeToString((stripeKey + ":").getBytes(StandardCharsets.UTF_8));

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Basic " + auth)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(formBody))
            .build();

        HttpResponse<String> response = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Stripe API error: HTTP " + response.statusCode() + " - " + response.body());
        }
        return MAPPER.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String env(String key) {
        return AppConfig.get(key);
    }

    private SubAccount findSubAccount(String parentEmail, String subAccountId, List<SubAccount> all) {
        Optional<SubAccount> maybe = all.stream()
            .filter(s -> parentEmail.equalsIgnoreCase(s.getParentEmail()) && subAccountId.equals(s.getId()))
            .findFirst();

        if (maybe.isEmpty()) {
            throw new IllegalArgumentException("Sous-compte introuvable");
        }
        return maybe.get();
    }

    private List<SubAccount> readSubAccounts() {
        ensureFiles();
        try {
            String json = Files.readString(SUB_ACCOUNTS_FILE, StandardCharsets.UTF_8);
            List<SubAccount> list = MAPPER.readValue(json, new TypeReference<List<SubAccount>>() {});
            return list == null ? new ArrayList<>() : list;
        } catch (IOException ex) {
            return new ArrayList<>();
        }
    }

    private void writeSubAccounts(List<SubAccount> subAccounts) {
        ensureFiles();
        try {
            Files.writeString(SUB_ACCOUNTS_FILE, MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(subAccounts), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible d'ecrire subaccounts.json", ex);
        }
    }

    private List<SubAccountPayment> readPayments() {
        ensureFiles();
        try {
            String json = Files.readString(PAYMENTS_FILE, StandardCharsets.UTF_8);
            List<SubAccountPayment> list = MAPPER.readValue(json, new TypeReference<List<SubAccountPayment>>() {});
            return list == null ? new ArrayList<>() : list;
        } catch (IOException ex) {
            return new ArrayList<>();
        }
    }

    private void writePayments(List<SubAccountPayment> payments) {
        ensureFiles();
        try {
            Files.writeString(PAYMENTS_FILE, MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(payments), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible d'ecrire payments.json", ex);
        }
    }

    private void ensureFiles() {
        try {
            if (!Files.exists(DATA_DIR)) {
                Files.createDirectories(DATA_DIR);
            }
            if (!Files.exists(SUB_ACCOUNTS_FILE)) {
                Files.writeString(SUB_ACCOUNTS_FILE, "[]", StandardCharsets.UTF_8);
            }
            if (!Files.exists(PAYMENTS_FILE)) {
                Files.writeString(PAYMENTS_FILE, "[]", StandardCharsets.UTF_8);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible d'initialiser le stockage des sous-comptes", ex);
        }
    }
}
