package com.projet.user;

import com.projet.backend.domain.User;

/**
 * Service interface for user authentication and registration operations.
 * 
 * <p>Defines the contract for user management services, handling user registration
 * and related authentication flows. This interface isolates user service implementation
 * from REST controllers, supporting dependency injection and testability.</p>
 * 
 * <p><b>Implementations:</b></p>
 * <ul>
 *   <li>{@link UserServiceImpl} - Default implementation with file-based user persistence</li>
 * </ul>
 * 
 * @author Project Team
 * @version 1.0
 * @see UserServiceImpl
 * @see User
 */
public interface UserService {
    
    /**
     * Registers a new user with the provided credentials.
     * 
     * @param email the user's email address (unique identifier)
     * @param password the user's password (typically hashed in implementation)
     * @param pseudo the user's display name
     * @return confirmation message if registration successful
     * @throws IllegalArgumentException if email already exists or parameters invalid
     */
    String register(String email, String password, String pseudo);
}
