package com.projet.api;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.path;
import static spark.Spark.post;
import static spark.Spark.put;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projet.backend.domain.SubAccount;
import com.projet.backend.domain.SubAccountPayment;
import com.projet.backend.domain.SubAccountSubscription;
import com.projet.service.SubAccountManagementService;
import com.projet.service.SubAccountManagementServiceImpl;

import spark.Request;

public final class SubAccountController {

    private static final SubAccountManagementService SUB_ACCOUNT_SERVICE = new SubAccountManagementServiceImpl();

    private SubAccountController() {
    }

    public static void register(ObjectMapper mapper) {
        path("/users", () -> {
            path("/subaccounts", () -> {
                post("", (req, res) -> {
                    res.type("application/json");
                    String user = requireMainUser(req, res, mapper);
                    if (user == null) {
                        return unauthorized(mapper);
                    }

                    try {
                        Map<String, Object> body = mapper.readValue(req.body(), new TypeReference<Map<String, Object>>() {});
                        SubAccount created = SUB_ACCOUNT_SERVICE.createSubAccount(user, body);
                        return mapper.writeValueAsString(Map.of("success", true, "subAccount", created));
                    } catch (Exception ex) {
                        res.status(400);
                        return mapper.writeValueAsString(Map.of("success", false, "error", ex.getMessage()));
                    }
                });

                get("", (req, res) -> {
                    res.type("application/json");
                    String user = requireMainUser(req, res, mapper);
                    if (user == null) {
                        return unauthorized(mapper);
                    }

                    List<SubAccount> subAccounts = SUB_ACCOUNT_SERVICE.listSubAccounts(user);
                    return mapper.writeValueAsString(Map.of("success", true, "subAccounts", subAccounts));
                });

                get("/:id", (req, res) -> {
                    res.type("application/json");
                    String user = requireMainUser(req, res, mapper);
                    if (user == null) {
                        return unauthorized(mapper);
                    }

                    try {
                        SubAccount sub = SUB_ACCOUNT_SERVICE.getSubAccount(user, req.params(":id"));
                        return mapper.writeValueAsString(Map.of("success", true, "subAccount", sub));
                    } catch (Exception ex) {
                        res.status(404);
                        return mapper.writeValueAsString(Map.of("success", false, "error", ex.getMessage()));
                    }
                });

                get("/:id/subscriptions", (req, res) -> {
                    res.type("application/json");
                    String user = requireMainUser(req, res, mapper);
                    if (user == null) {
                        return unauthorized(mapper);
                    }

                    try {
                        List<SubAccountSubscription> subscriptions = SUB_ACCOUNT_SERVICE.listSubscriptions(user, req.params(":id"));
                        return mapper.writeValueAsString(Map.of("success", true, "subscriptions", subscriptions));
                    } catch (Exception ex) {
                        res.status(404);
                        return mapper.writeValueAsString(Map.of("success", false, "error", ex.getMessage()));
                    }
                });

                get("/:id/dashboard", (req, res) -> {
                    res.type("application/json");
                    String user = requireMainUser(req, res, mapper);
                    if (user == null) {
                        return unauthorized(mapper);
                    }

                    try {
                        SubAccount sub = SUB_ACCOUNT_SERVICE.getSubAccount(user, req.params(":id"));
                        List<SubAccountSubscription> subscriptions = SUB_ACCOUNT_SERVICE.listSubscriptions(user, req.params(":id"));

                        double totalMonthly = subscriptions.stream().mapToDouble(SubAccountSubscription::getAmount).sum();
                        double annual = totalMonthly * 12;
                        int activeCount = (int) subscriptions.stream().filter(s -> "active".equalsIgnoreCase(s.getStatus())).count();
                        String topService = subscriptions.stream()
                            .sorted((a, b) -> Double.compare(b.getAmount(), a.getAmount()))
                            .map(SubAccountSubscription::getServiceName)
                            .findFirst()
                            .orElse("Aucun");

                        Map<String, Double> byService = subscriptions.stream().collect(
                            Collectors.groupingBy(
                                SubAccountSubscription::getServiceName,
                                Collectors.summingDouble(SubAccountSubscription::getAmount)
                            )
                        );

                        return mapper.writeValueAsString(Map.of(
                            "success", true,
                            "subAccount", sub,
                            "metrics", Map.of(
                                "count", subscriptions.size(),
                                "activeCount", activeCount,
                                "monthlyTotal", totalMonthly,
                                "annualTotal", annual,
                                "topService", topService
                            ),
                            "serviceBreakdown", byService,
                            "subscriptions", subscriptions
                        ));
                    } catch (Exception ex) {
                        res.status(404);
                        return mapper.writeValueAsString(Map.of("success", false, "error", ex.getMessage()));
                    }
                });

                put("/:id/preferences", (req, res) -> {
                    res.type("application/json");
                    String user = requireMainUser(req, res, mapper);
                    if (user == null) {
                        return unauthorized(mapper);
                    }

                    try {
                        Map<String, String> body = mapper.readValue(req.body(), new TypeReference<Map<String, String>>() {});
                        SubAccount updated = SUB_ACCOUNT_SERVICE.updatePreferences(user, req.params(":id"), body);
                        return mapper.writeValueAsString(Map.of("success", true, "subAccount", updated));
                    } catch (Exception ex) {
                        res.status(400);
                        return mapper.writeValueAsString(Map.of("success", false, "error", ex.getMessage()));
                    }
                });

                delete("/:id", (req, res) -> {
                    res.type("application/json");
                    String user = requireMainUser(req, res, mapper);
                    if (user == null) {
                        return unauthorized(mapper);
                    }

                    try {
                        SubAccount disabled = SUB_ACCOUNT_SERVICE.disableSubAccount(user, req.params(":id"));
                        return mapper.writeValueAsString(Map.of("success", true, "subAccount", disabled));
                    } catch (Exception ex) {
                        res.status(400);
                        return mapper.writeValueAsString(Map.of("success", false, "error", ex.getMessage()));
                    }
                });

                delete("/:id/permanent", (req, res) -> {
                    res.type("application/json");
                    String user = requireMainUser(req, res, mapper);
                    if (user == null) {
                        return unauthorized(mapper);
                    }

                    try {
                        SubAccount deleted = SUB_ACCOUNT_SERVICE.deleteSubAccount(user, req.params(":id"));
                        return mapper.writeValueAsString(Map.of("success", true, "subAccount", deleted));
                    } catch (Exception ex) {
                        res.status(400);
                        return mapper.writeValueAsString(Map.of("success", false, "error", ex.getMessage()));
                    }
                });
            });
        });

        path("/stripe", () -> {
            post("/subscription", (req, res) -> {
                res.type("application/json");
                String user = requireMainUser(req, res, mapper);
                if (user == null) {
                    return unauthorized(mapper);
                }

                try {
                    Map<String, Object> body = mapper.readValue(req.body(), new TypeReference<Map<String, Object>>() {});
                    Map<String, Object> created = SUB_ACCOUNT_SERVICE.addSubscription(user, body);
                    return mapper.writeValueAsString(Map.of("success", true, "result", created));
                } catch (Exception ex) {
                    res.status(400);
                    return mapper.writeValueAsString(Map.of("success", false, "error", ex.getMessage()));
                }
            });

            get("/payments", (req, res) -> {
                res.type("application/json");
                String user = requireMainUser(req, res, mapper);
                if (user == null) {
                    return unauthorized(mapper);
                }

                List<SubAccountPayment> payments = SUB_ACCOUNT_SERVICE.listPayments(user);
                return mapper.writeValueAsString(Map.of("success", true, "payments", payments));
            });
        });
    }

    private static String requireMainUser(Request req, spark.Response res, ObjectMapper mapper) {
        String user = req.session().attribute("user_email");
        if (user == null || user.isBlank()) {
            res.status(401);
        }
        return user;
    }

    private static String unauthorized(ObjectMapper mapper) throws Exception {
        return mapper.writeValueAsString(Map.of("success", false, "error", "Authentification requise"));
    }
}
