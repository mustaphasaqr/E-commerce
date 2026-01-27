package com.mustapha.ecommerce.user.application.command;

import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

/**
 * Revoke Marketing Consent Command
 * Responsibility: Transfer user ID for revoking marketing consent
 */
public class RevokeMarketingConsentCommand {
    
    private final UserId userId;
    
    public RevokeMarketingConsentCommand(UserId userId) {
        this.userId = userId;
    }
    
    public UserId getUserId() {
        return userId;
    }
}
