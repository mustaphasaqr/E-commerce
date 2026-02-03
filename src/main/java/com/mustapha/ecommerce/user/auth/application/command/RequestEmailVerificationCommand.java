package com.mustapha.ecommerce.user.auth.application.command;

import com.mustapha.ecommerce.user.domain.model.valueobject.Email;

public class RequestEmailVerificationCommand {
    private final Email email;

    public RequestEmailVerificationCommand(Email email) {
        this.email = email;
    }

    public Email getEmail() {
        return email;
    }
}
