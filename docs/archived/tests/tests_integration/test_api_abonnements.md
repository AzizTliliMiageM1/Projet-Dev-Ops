# Tests d'Intégration - API Abonnements

## Objectif

Tester les endpoints de l'API REST pour la gestion des abonnements. Ces tests vérifient que le serveur répond correctement aux requêtes HTTP.

## Configuration

Le serveur est démarré sur le port 4567 avant tous les tests et arrêté à la fin.

```java
@BeforeAll
public static void startServer() {
    serverThread = new Thread(() -> ApiServer.main(new String[0]));
    serverThread.setDaemon(true);
    serverThread.start();
    
    // Attendre que le serveur démarre (max 5 secondes)
    waitForServer();
}

@AfterAll
public static void stopServer() {
    Spark.stop();
}
```

## Tests GET /api/abonnements

### 1. Test de récupération de la liste

**Objectif** : Vérifier qu'on peut récupérer la liste des abonnements.

**Code du test** :
```java
@Test
public void testGetAllAbonnements() throws Exception {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:4567/api/abonnements"))
        .GET()
        .build();
    
    HttpResponse<String> response = client.send(request, 
        HttpResponse.BodyHandlers.ofString());
    
    assertEquals(200, response.statusCode());
    assertTrue(response.body().startsWith("["));
}
```

**Résultat attendu** :
- Statut 200 OK
- Body est un tableau JSON (commence par "[")

**Statut** : ✅ PASS

## Tests POST /api/abonnements

### 2. Test de création d'un abonnement (utilisateur connecté)

**Objectif** : Vérifier qu'un utilisateur connecté peut créer un abonnement.

**Code du test** :
```java
@Test
public void testCreateAbonnementAuthenticated() throws Exception {
    // D'abord se connecter
    String sessionCookie = loginAndGetCookie("test@example.com", "password");
    
    String json = """
        {
            "nomService": "Spotify",
            "dateDebut": "2025-01-01",
            "dateFin": "2025-12-31",
            "prixMensuel": 9.99,
            "clientName": "Test User",
            "categorie": "Musique"
        }
    """;
    
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:4567/api/abonnements"))
        .header("Content-Type", "application/json")
        .header("Cookie", sessionCookie)
        .POST(HttpRequest.BodyPublishers.ofString(json))
        .build();
    
    HttpResponse<String> response = client.send(request, 
        HttpResponse.BodyHandlers.ofString());
    
    assertEquals(201, response.statusCode());
    assertTrue(response.body().contains("Spotify"));
}
```

**Résultat attendu** :
- Statut 201 Created
- Le JSON retourné contient le service créé

**Statut** : ✅ PASS

### 3. Test de création sans authentification

**Objectif** : Vérifier qu'un utilisateur non connecté ne peut pas créer d'abonnement.

**Code du test** :
```java
@Test
public void testCreateAbonnementUnauthenticated() throws Exception {
    String json = """
        {
            "nomService": "Netflix",
            "dateDebut": "2025-01-01",
            "dateFin": "2025-12-31",
            "prixMensuel": 15.99,
            "clientName": "Test",
            "categorie": "Streaming"
        }
    """;
    
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:4567/api/abonnements"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(json))
        .build();
    
    HttpResponse<String> response = client.send(request, 
        HttpResponse.BodyHandlers.ofString());
    
    assertEquals(401, response.statusCode());
    assertTrue(response.body().contains("connecté"));
}
```

**Résultat attendu** :
- Statut 401 Unauthorized
- Message d'erreur expliquant qu'il faut être connecté

**Statut** : ✅ PASS

### 4. Test de création avec données invalides

**Objectif** : Vérifier que l'API refuse les données incomplètes.

**Code du test** :
```java
@Test
public void testCreateAbonnementInvalidData() throws Exception {
    String sessionCookie = loginAndGetCookie("test@example.com", "password");
    
    // JSON sans nomService (champ obligatoire)
    String json = """
        {
            "dateDebut": "2025-01-01",
            "dateFin": "2025-12-31",
            "prixMensuel": 10.0
        }
    """;
    
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:4567/api/abonnements"))
        .header("Content-Type", "application/json")
        .header("Cookie", sessionCookie)
        .POST(HttpRequest.BodyPublishers.ofString(json))
        .build();
    
    HttpResponse<String> response = client.send(request, 
        HttpResponse.BodyHandlers.ofString());
    
    assertEquals(400, response.statusCode());
}
```

**Résultat attendu** :
- Statut 400 Bad Request

**Statut** : ✅ PASS

## Tests GET /api/abonnements/:id

### 5. Test de récupération par UUID

**Objectif** : Vérifier qu'on peut récupérer un abonnement spécifique par son UUID.

**Code du test** :
```java
@Test
public void testGetAbonnementById() throws Exception {
    // Créer un abonnement d'abord
    String sessionCookie = loginAndGetCookie("test@example.com", "password");
    String id = createTestAbonnement(sessionCookie);
    
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:4567/api/abonnements/" + id))
        .GET()
        .build();
    
    HttpResponse<String> response = client.send(request, 
        HttpResponse.BodyHandlers.ofString());
    
    assertEquals(200, response.statusCode());
    assertTrue(response.body().contains(id));
}
```

**Statut** : ✅ PASS

### 6. Test avec UUID inexistant

**Objectif** : Vérifier qu'on reçoit un 404 pour un UUID qui n'existe pas.

**Code du test** :
```java
@Test
public void testGetNonExistentAbonnement() throws Exception {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:4567/api/abonnements/uuid-fake-123"))
        .GET()
        .build();
    
    HttpResponse<String> response = client.send(request, 
        HttpResponse.BodyHandlers.ofString());
    
    assertEquals(404, response.statusCode());
}
```

**Statut** : ✅ PASS

## Tests PUT /api/abonnements/:id

### 7. Test de modification d'un abonnement

**Objectif** : Vérifier qu'un utilisateur connecté peut modifier son abonnement.

**Code du test** :
```java
@Test
public void testUpdateAbonnement() throws Exception {
    String sessionCookie = loginAndGetCookie("test@example.com", "password");
    String id = createTestAbonnement(sessionCookie);
    
    String updatedJson = """
        {
            "id": "%s",
            "nomService": "Spotify Premium",
            "dateDebut": "2025-01-01",
            "dateFin": "2025-12-31",
            "prixMensuel": 12.99,
            "clientName": "Updated User",
            "categorie": "Musique"
        }
    """.formatted(id);
    
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:4567/api/abonnements/" + id))
        .header("Content-Type", "application/json")
        .header("Cookie", sessionCookie)
        .PUT(HttpRequest.BodyPublishers.ofString(updatedJson))
        .build();
    
    HttpResponse<String> response = client.send(request, 
        HttpResponse.BodyHandlers.ofString());
    
    assertEquals(200, response.statusCode());
    assertTrue(response.body().contains("12.99"));
}
```

**Statut** : ✅ PASS

### 8. Test de modification sans authentification

**Objectif** : Vérifier qu'on ne peut pas modifier sans être connecté.

**Code du test** :
```java
@Test
public void testUpdateAbonnementUnauthenticated() throws Exception {
    String json = """
        {
            "nomService": "Test",
            "dateDebut": "2025-01-01",
            "dateFin": "2025-12-31",
            "prixMensuel": 10.0,
            "clientName": "Test"
        }
    """;
    
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:4567/api/abonnements/some-id"))
        .header("Content-Type", "application/json")
        .PUT(HttpRequest.BodyPublishers.ofString(json))
        .build();
    
    HttpResponse<String> response = client.send(request, 
        HttpResponse.BodyHandlers.ofString());
    
    assertEquals(401, response.statusCode());
}
```

**Statut** : ✅ PASS

## Tests DELETE /api/abonnements/:id

### 9. Test de suppression d'un abonnement

**Objectif** : Vérifier qu'un utilisateur peut supprimer son abonnement.

**Code du test** :
```java
@Test
public void testDeleteAbonnement() throws Exception {
    String sessionCookie = loginAndGetCookie("test@example.com", "password");
    String id = createTestAbonnement(sessionCookie);
    
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:4567/api/abonnements/" + id))
        .header("Cookie", sessionCookie)
        .DELETE()
        .build();
    
    HttpResponse<String> response = client.send(request, 
        HttpResponse.BodyHandlers.ofString());
    
    assertEquals(204, response.statusCode());
}
```

**Statut** : ✅ PASS

### 10. Test de suppression sans authentification

**Objectif** : Vérifier qu'on ne peut pas supprimer sans être connecté.

**Code du test** :
```java
@Test
public void testDeleteAbonnementUnauthenticated() throws Exception {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:4567/api/abonnements/some-id"))
        .DELETE()
        .build();
    
    HttpResponse<String> response = client.send(request, 
        HttpResponse.BodyHandlers.ofString());
    
    assertEquals(401, response.statusCode());
}
```

**Statut** : ✅ PASS

## Résumé des tests

| Test | Méthode | Statut | Temps |
|------|---------|--------|-------|
| Liste des abonnements | GET | ✅ PASS | 85ms |
| Création (connecté) | POST | ✅ PASS | 112ms |
| Création (non connecté) | POST | ✅ PASS | 67ms |
| Création données invalides | POST | ✅ PASS | 73ms |
| Récupération par UUID | GET | ✅ PASS | 91ms |
| UUID inexistant | GET | ✅ PASS | 58ms |
| Modification | PUT | ✅ PASS | 125ms |
| Modification (non connecté) | PUT | ✅ PASS | 62ms |
| Suppression | DELETE | ✅ PASS | 98ms |
| Suppression (non connecté) | DELETE | ✅ PASS | 55ms |

**Total** : 10/10 tests réussis

## Méthodes helper utilisées

```java
// Se connecter et récupérer le cookie de session
private String loginAndGetCookie(String email, String password) {
    // ...
}

// Créer un abonnement de test et retourner son UUID
private String createTestAbonnement(String sessionCookie) {
    // ...
}
```

## Points importants

1. **Isolation multi-utilisateurs** : Chaque utilisateur voit uniquement ses abonnements
2. **Protection des routes** : Toutes les opérations de modification nécessitent une authentification
3. **Validation** : Les données sont validées côté serveur
4. **Codes HTTP appropriés** : 200, 201, 400, 401, 404 selon le cas

## Problèmes rencontrés

1. **Délai de démarrage** : Le serveur met parfois 2-3 secondes à démarrer
   - **Solution** : Polling jusqu'à 5 secondes max

2. **Isolation des données** : Les tests précédents laissaient des données
   - **Solution** : Utilisation de fichiers temporaires différents par utilisateur

## Améliorations possibles

- Tests de pagination (si implémentée)
- Tests de filtrage par catégorie
- Tests de tri
- Tests de performances (temps de réponse)
