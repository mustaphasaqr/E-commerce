package com.mustapha.ecommerce.user.application.command;

import com.mustapha.ecommerce.user.domain.model.valueobject.Email;
import com.mustapha.ecommerce.user.domain.model.valueobject.Password;
import com.mustapha.ecommerce.user.domain.model.valueobject.Role;
import com.mustapha.ecommerce.user.domain.model.valueobject.Username;

/**
 * Register User Command (Input DTO)
 * Responsibility: Transfer data from Facade to Use Case
 * Pattern: Command (CQS - Command Query Separation)
 * 
 * Note: Uses value objects for type safety
 * The Facade converts primitives from API DTOs → value objects for this command
 */
public class RegisterUserCommand {
    
    private final Email email;
    private final Password password;
    private final Username username;
    private final Role role;
    private final boolean termsAccepted;
    
    public RegisterUserCommand(Email email, Password password, Username username, 
                              Role role, boolean termsAccepted) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.role = role;
        this.termsAccepted = termsAccepted;
    }
    
    public Email getEmail() {
        return email;
    }
    
    public Password getPassword() {
        return password;
    }
    
    public Username getUsername() {
        return username;
    }
    
    public Role getRole() {
        return role;
    }
    
    public boolean isTermsAccepted() {
        return termsAccepted;
    }
}
