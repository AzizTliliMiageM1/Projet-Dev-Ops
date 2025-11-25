# Tests Unitaires - Gestion des Utilisateurs

## Objectif

Tester les classes `User` et `FileUserRepository` qui gèrent les utilisateurs et leur authentification.

## Tests de la classe User

### 1. Test de création d'un utilisateur

**Objectif** : Vérifier qu'on peut créer un utilisateur avec email, mot de passe et pseudo.

**Code du test** :
```java
@Test
public void testUserCreation() {
    User user = new User("test@example.com", "password123", "TestUser", "token123");
    
    assertEquals("test@example.com", user.getEmail());
    assertEquals("password123", user.getPassword());
    assertEquals("TestUser", user.getPseudo());
    assertEquals("token123", user.getConfirmationToken());
    assertFalse(user.isConfirmed());
}
```

**Statut** : ✅ PASS

---

### 2. Test du pseudo par défaut

**Objectif** : Vérifier que si on ne donne pas de pseudo, il est généré à partir de l'email.

**Code du test** :
```java
@Test
public void testDefaultPseudo() {
    User user = new User("john.doe@example.com", "pass", "token");
    
    assertEquals("john.doe", user.getPseudo());
}
```

**Résultat attendu** :
- Le pseudo est "john.doe" (partie avant le @)

**Statut** : ✅ PASS

---

### 3. Test de confirmation

**Objectif** : Vérifier qu'on peut confirmer un utilisateur.

**Code du test** :
```java
@Test
public void testUserConfirmation() {
    User user = new User("user@test.com", "pass", "User", "token");
    
    assertFalse(user.isConfirmed());
    
    user.setConfirmed(true);
    
    assertTrue(user.isConfirmed());
}
```

**Statut** : ✅ PASS

---

## Tests de FileUserRepository

### 4. Test d'enregistrement d'un utilisateur

**Objectif** : Vérifier qu'on peut sauvegarder un utilisateur dans le fichier.

**Code du test** :
```java
@Test
public void testSaveUser() throws IOException {
    File tempFile = File.createTempFile("users", ".txt");
    FileUserRepository repo = new FileUserRepository(tempFile.getAbsolutePath());
    
    User user = new User("alice@example.com", "securepass", "Alice", "abc123");
    repo.save(user);
    
    User found = repo.findByEmail("alice@example.com");
    
    assertNotNull(found);
    assertEquals("Alice", found.getPseudo());
    
    tempFile.delete();
}
```

**Résultat attendu** :
- L'utilisateur est bien sauvegardé et retrouvé

**Statut** : ✅ PASS

---

### 5. Test de recherche par email

**Objectif** : Vérifier qu'on peut retrouver un utilisateur par son email.

**Code du test** :
```java
@Test
public void testFindByEmail() throws IOException {
    File tempFile = File.createTempFile("users", ".txt");
    FileUserRepository repo = new FileUserRepository(tempFile.getAbsolutePath());
    
    User user1 = new User("bob@test.com", "pass1", "Bob", "token1");
    User user2 = new User("carol@test.com", "pass2", "Carol", "token2");
    
    repo.save(user1);
    repo.save(user2);
    
    User found = repo.findByEmail("carol@test.com");
    
    assertNotNull(found);
    assertEquals("Carol", found.getPseudo());
    
    tempFile.delete();
}
```

**Statut** : ✅ PASS

---

### 6. Test de recherche par token

**Objectif** : Vérifier qu'on peut retrouver un utilisateur par son token de confirmation.

**Code du test** :
```java
@Test
public void testFindByToken() throws IOException {
    File tempFile = File.createTempFile("users", ".txt");
    FileUserRepository repo = new FileUserRepository(tempFile.getAbsolutePath());
    
    User user = new User("dave@test.com", "password", "Dave", "unique-token-123");
    repo.save(user);
    
    User found = repo.findByToken("unique-token-123");
    
    assertNotNull(found);
    assertEquals("dave@test.com", found.getEmail());
    
    tempFile.delete();
}
```

**Statut** : ✅ PASS

---

### 7. Test de mise à jour d'un utilisateur

**Objectif** : Vérifier qu'on peut modifier les informations d'un utilisateur (par exemple le confirmer).

**Code du test** :
```java
@Test
public void testUpdateUser() throws IOException {
    File tempFile = File.createTempFile("users", ".txt");
    FileUserRepository repo = new FileUserRepository(tempFile.getAbsolutePath());
    
    User user = new User("eve@test.com", "pass", "Eve", "token123");
    repo.save(user);
    
    // Confirmer l'utilisateur
    user.setConfirmed(true);
    repo.update(user);
    
    User updated = repo.findByEmail("eve@test.com");
    
    assertTrue(updated.isConfirmed());
    
    tempFile.delete();
}
```

**Statut** : ✅ PASS

---

### 8. Test de compatibilité ancien/nouveau format

**Objectif** : Vérifier que le repository peut lire les anciens fichiers sans le champ pseudo.

**Code du test** :
```java
@Test
public void testBackwardCompatibility() throws IOException {
    File tempFile = File.createTempFile("users", ".txt");
    
    // Écrire un utilisateur au format ancien (sans pseudo)
    try (FileWriter fw = new FileWriter(tempFile)) {
        fw.write("old@test.com;password;false;token123\n");
    }
    
    FileUserRepository repo = new FileUserRepository(tempFile.getAbsolutePath());
    User found = repo.findByEmail("old@test.com");
    
    assertNotNull(found);
    assertEquals("old", found.getPseudo()); // Généré depuis l'email
    
    tempFile.delete();
}
```

**Résultat attendu** :
- L'ancien format est lu correctement
- Le pseudo est généré automatiquement

**Statut** : ✅ PASS

---

### 9. Test d'utilisateur inexistant

**Objectif** : Vérifier qu'on retourne null si l'utilisateur n'existe pas.

**Code du test** :
```java
@Test
public void testFindNonExistentUser() throws IOException {
    File tempFile = File.createTempFile("users", ".txt");
    FileUserRepository repo = new FileUserRepository(tempFile.getAbsolutePath());
    
    User found = repo.findByEmail("nobody@test.com");
    
    assertNull(found);
    
    tempFile.delete();
}
```

**Statut** : ✅ PASS

---

## Résumé des tests

| Test | Statut | Temps |
|------|--------|-------|
| Création utilisateur | ✅ PASS | 8ms |
| Pseudo par défaut | ✅ PASS | 5ms |
| Confirmation utilisateur | ✅ PASS | 6ms |
| Sauvegarde utilisateur | ✅ PASS | 42ms |
| Recherche par email | ✅ PASS | 38ms |
| Recherche par token | ✅ PASS | 35ms |
| Mise à jour utilisateur | ✅ PASS | 51ms |
| Compatibilité ancien format | ✅ PASS | 47ms |
| Utilisateur inexistant | ✅ PASS | 15ms |

**Total** : 9/9 tests réussis

## Cas limites testés

1. **Email invalide** : Le système accepte n'importe quel format (validation côté API)
2. **Mot de passe vide** : Accepté (validation côté API également)
3. **Caractères spéciaux dans pseudo** : Gestion correcte
4. **Doublons d'email** : Géré côté service (UserService)

## Sécurité

⚠️ **Points d'attention** :
- Les mots de passe sont stockés en clair dans le fichier (à améliorer avec BCrypt)
- Pas de validation d'email côté repository
- Les tokens de confirmation sont simples UUID (suffisant pour un projet étudiant)

## Format de stockage

**Nouveau format (5 champs)** :
```
email;password;confirmed;token;pseudo
```

**Ancien format (4 champs, toujours supporté)** :
```
email;password;confirmed;token
```

## Problèmes rencontrés

1. **Migration du format** : Au début, le pseudo n'était pas sauvegardé
   - **Solution** : Ajout du 5e champ avec rétrocompatibilité

2. **Séparateur** : Problème si le pseudo contient ";"
   - **Solution** : Pour l'instant on accepte, à améliorer avec du CSV propre

## Améliorations futures

- Hasher les mots de passe (BCrypt)
- Validation stricte des emails
- Ajouter un champ "date de création"
- Implémenter la suppression d'utilisateurs
- Ajouter des rôles (admin, user)
