package com.mustapha.ecommerce.user.application.command;

import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

/**
 * Grant Marketing Consent Command
 * Responsibility: Transfer user ID for granting marketing consent
 */
public class GrantMarketingConsentCommand {
    
    private final UserId userId;
    
    public GrantMarketingConsentCommand(UserId userId) {
        this.userId = userId;
    }
    
    public UserId getUserId() {
        return userId;
    }
}
