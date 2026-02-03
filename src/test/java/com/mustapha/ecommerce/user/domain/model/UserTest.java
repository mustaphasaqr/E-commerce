package com.mustapha.ecommerce.user.domain.model;

import com.mustapha.ecommerce.user.domain.event.*;
import com.mustapha.ecommerce.user.domain.exception.InvalidUserStateException;
import com.mustapha.ecommerce.user.domain.exception.InvalidPasswordException;
import com.mustapha.ecommerce.user.domain.model.User.UserStatus;
import com.mustapha.ecommerce.user.domain.model.valueobject.Email;
import com.mustapha.ecommerce.user.domain.model.valueobject.Password;
import com.mustapha.ecommerce.user.domain.model.valueobject.Role;
import com.mustapha.ecommerce.user.domain.model.valueobject.Username;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;
import com.mustapha.ecommerce.user.infrastructure.security.BCryptPasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UserTest {

    private BCryptPasswordHasher passwordHasher;
    private Username validUsername;
    private Email validEmail;
    private Password validPassword;

    @BeforeEach
    void setUp() {
        passwordHasher = new BCryptPasswordHasher();
        validUsername = Username.of("testuser");
        validEmail = Email.of("test@example.com");
        validPassword = Password.fromPlainText("SecurePass123!@#", passwordHasher);
    }

    @Test
    void create_ValidInputs_CreatesUser() {
        User user = User.create(validUsername, validEmail, validPassword, Role.CUSTOMER);

        assertThat(user).isNotNull();
        assertThat(user.getId()).isNotNull();
        assertThat(user.getUsername()).isEqualTo(validUsername);
        assertThat(user.getEmail()).isEqualTo(validEmail);
        assertThat(user.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(user.isEmailVerified()).isFalse();
        assertThat(user.isTermsAccepted()).isFalse();
        assertThat(user.getVersion()).isEqualTo(1);
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getLastModifiedAt()).isNotNull();
    }

    @Test
    void create_EmitsUserCreatedEvent() {
        User user = User.create(validUsername, validEmail, validPassword, Role.CUSTOMER);

        assertThat(user.getDomainEvents()).hasSize(1);
        assertThat(user.getDomainEvents().get(0)).isInstanceOf(UserCreatedEvent.class);
        
        UserCreatedEvent event = (UserCreatedEvent) user.getDomainEvents().get(0);
        assertThat(event.userId()).isEqualTo(user.getId());
        assertThat(event.username()).isEqualTo(validUsername);
        assertThat(event.email()).isEqualTo(validEmail);
    }

    @Test
    void acceptTerms_ValidVersion_UpdatesState() {
        User user = User.create(validUsername, validEmail, validPassword, Role.CUSTOMER);
        user.clearDomainEvents();

        user.acceptTerms("v1.0");

        assertThat(user.isTermsAccepted()).isTrue();
        assertThat(user.getTermsVersion()).isEqualTo("v1.0");
        assertThat(user.getVersion()).isEqualTo(2);
        assertThat(user.getDomainEvents()).hasSize(1);
        assertThat(user.getDomainEvents().get(0)).isInstanceOf(TermsAcceptedEvent.class);
    }

    @Test
    void acceptTerms_NullVersion_ThrowsException() {
        User user = User.create(validUsername, validEmail, validPassword, Role.CUSTOMER);

        assertThatThrownBy(() -> user.acceptTerms(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Terms version cannot be null or blank");
    }

    @Test
    void acceptTerms_EmptyVersion_ThrowsException() {
        User user = User.create(validUsername, validEmail, validPassword, Role.CUSTOMER);

        assertThatThrownBy(() -> user.acceptTerms(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Terms version cannot be null or blank");
    }

    @Test
    void verifyEmail_UnverifiedUser_VerifiesEmail() {
        User user = User.create(validUsername, validEmail, validPassword, Role.CUSTOMER);
        user.clearDomainEvents();

        user.verifyEmail();

        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.getVersion()).isEqualTo(2);
        assertThat(user.getDomainEvents()).hasSize(1);
        assertThat(user.getDomainEvents().get(0)).isInstanceOf(UserEmailVerifiedEvent.class);
    }

    @Test
    void activate_WithTermsAndEmail_ActivatesUser() {
        User user = User.create(validUsername, validEmail, validPassword, Role.CUSTOMER);
        user.acceptTerms("v1.0");
        user.verifyEmail();
        user.clearDomainEvents();

        user.activate("Admin approval");

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getVersion()).isEqualTo(4);
        assertThat(user.getDomainEvents()).hasSize(1);
        assertThat(user.getDomainEvents().get(0)).isInstanceOf(UserActivatedEvent.class);
    }

    @Test
    void activate_WithoutTerms_ThrowsException() {
        User user = User.create(validUsername, validEmail, validPassword, Role.CUSTOMER);
        user.verifyEmail();

        assertThatThrownBy(() -> user.activate("Test"))
            .isInstanceOf(InvalidUserStateException.class)
            .hasMessageContaining("User must accept terms before activation");
    }

    @Test
    void activate_WithoutEmailVerification_ThrowsException() {
        User user = User.create(validUsername, validEmail, validPassword, Role.CUSTOMER);
        user.acceptTerms("v1.0");

        assertThatThrownBy(() -> user.activate("Test"))
            .isInstanceOf(InvalidUserStateException.class)
            .hasMessageContaining("Email must be verified before activation");
    }

    @Test
    void block_ActiveUser_BlocksUser() {
        User user = User.create(validUsername, validEmail, validPassword, Role.CUSTOMER);
        user.acceptTerms("v1.0");
        user.verifyEmail();
        user.activate("Test");
        user.clearDomainEvents();

        user.block("Policy violation");

        assertThat(user.getStatus()).isEqualTo(UserStatus.BLOCKED);
        assertThat(user.getVersion()).isEqualTo(5);
        assertThat(user.getDomainEvents()).hasSize(1);
        assertThat(user.getDomainEvents().get(0)).isInstanceOf(UserBlockedEvent.class);
    }

    @Test
    void block_NullReason_ThrowsException() {
        User user = User.create(validUsername, validEmail, validPassword, Role.CUSTOMER);
        user.acceptTerms("v1.0");
        user.verifyEmail();
        user.activate("Test");

        assertThatThrownBy(() -> user.block(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be empty");
    }

    @Test
    void unblock_BlockedUser_UnblocksUser() {
        User user = User.create(validUsername, validEmail, validPassword, Role.CUSTOMER);
        user.acceptTerms("v1.0");
        user.verifyEmail();
        user.activate("Test");
        user.block("Policy violation");
        user.clearDomainEvents();

        user.unblock("Violation resolved");

        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(user.getVersion()).isEqualTo(6);
        assertThat(user.getDomainEvents()).hasSize(1);
        assertThat(user.getDomainEvents().get(0)).isInstanceOf(UserUnblockedEvent.class);
    }

    @Test
    void unblock_NotBlockedUser_ThrowsException() {
        User user = User.create(validUsername, validEmail, validPassword, Role.CUSTOMER);
        user.acceptTerms("v1.0");
        user.verifyEmail();
        user.activate("Test");

        assertThatThrownBy(() -> user.unblock("Test"))
            .isInstanceOf(InvalidUserStateException.class)
            .hasMessageContaining("User is not blocked");
    }

    @Test
    void changeRole_ValidRole_ChangesRole() {
        User user = User.create(validUsername, validEmail, validPassword, Role.CUSTOMER);
        user.acceptTerms("v1.0");
        user.verifyEmail();
        user.activate("Test");
        user.clearDomainEvents();

        user.changeRole(Role.EMPLOYEE, "Promotion");

        assertThat(user.getRole()).isEqualTo(Role.EMPLOYEE);
        assertThat(user.getVersion()).isEqualTo(5);
        assertThat(user.getDomainEvents()).hasSize(1);
        assertThat(user.getDomainEvents().get(0)).isInstanceOf(UserRoleChangedEvent.class);
    }

    @Test
    void changeRole_SameRole_ThrowsException() {
        User user = User.create(validUsername, validEmail, validPassword, Role.CUSTOMER);

        assertThatThrownBy(() -> user.changeRole(Role.CUSTOMER, "Test"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User already has role");
    }

    @Test
    void changePassword_ValidPassword_ChangesPassword() {
        String plainPassword = "SecurePass123!@#";
        Password password = Password.fromPlainText(plainPassword, passwordHasher);
        User user = User.create(validUsername, validEmail, password, Role.CUSTOMER);
        Password newPassword = Password.fromPlainText("NewSecurePass456!@#", passwordHasher);
        user.clearDomainEvents();

        user.changePassword(plainPassword, newPassword, passwordHasher);

        assertThat(user.getPassword()).isEqualTo(newPassword);
        assertThat(user.getVersion()).isEqualTo(2);
        assertThat(user.getDomainEvents()).hasSize(1);
        assertThat(user.getDomainEvents().get(0)).isInstanceOf(PasswordChangedEvent.class);
    }

    @Test
    void changePassword_WrongCurrentPassword_ThrowsException() {
        String plainPassword = "SecurePass123!@#";
        Password password = Password.fromPlainText(plainPassword, passwordHasher);
        User user = User.create(validUsername, validEmail, password, Role.CUSTOMER);
        Password newPassword = Password.fromPlainText("NewSecurePass456!@#", passwordHasher);

        assertThatThrownBy(() -> user.changePassword("WrongPassword!", newPassword, passwordHasher))
            .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void resetPassword_ValidPassword_ResetsPassword() {
        User user = User.create(validUsername, validEmail, validPassword, Role.CUSTOMER);
        Password newPassword = Password.fromPlainText("NewSecurePass456!@#", passwordHasher);
        user.clearDomainEvents();

        user.resetPassword(newPassword);

        assertThat(user.getPassword()).isEqualTo(newPassword);
        assertThat(user.getVersion()).isEqualTo(2);
        assertThat(user.getDomainEvents()).hasSize(1);
        assertThat(user.getDomainEvents().get(0)).isInstanceOf(PasswordChangedEvent.class);
    }

    @Test
    void changeEmail_ValidEmail_ChangesEmail() {
        User user = User.create(validUsername, validEmail, validPassword, Role.CUSTOMER);
        user.acceptTerms("v1.0");
        user.verifyEmail();
        user.activate("Test");
        Email newEmail = Email.of("newemail@example.com");
        user.clearDomainEvents();

        user.changeEmail(newEmail);

        assertThat(user.getEmail()).isEqualTo(newEmail);
        assertThat(user.isEmailVerified()).isFalse();
        assertThat(user.getVersion()).isEqualTo(5);
        assertThat(user.getDomainEvents()).hasSize(1);
        assertThat(user.getDomainEvents().get(0)).isInstanceOf(UserEmailChangedEvent.class);
    }

    @Test
    void verifyPassword_CorrectPassword_ReturnsTrue() {
        String plainPassword = "SecurePass123!@#";
        Password password = Password.fromPlainText(plainPassword, passwordHasher);
        User user = User.create(validUsername, validEmail, password, Role.CUSTOMER);

        boolean isValid = user.verifyPassword(plainPassword, passwordHasher);

        assertThat(isValid).isTrue();
    }

    @Test
    void verifyPassword_WrongPassword_ReturnsFalse() {
        User user = User.create(validUsername, validEmail, validPassword, Role.CUSTOMER);

        boolean isValid = user.verifyPassword("WrongPassword123!", passwordHasher);

        assertThat(isValid).isFalse();
    }

    @Test
    void clearDomainEvents_RemovesAllEvents() {
        User user = User.create(validUsername, validEmail, validPassword, Role.CUSTOMER);
        assertThat(user.getDomainEvents()).hasSize(1);

        user.clearDomainEvents();

        assertThat(user.getDomainEvents()).isEmpty();
    }

    @Test
    void reconstitute_ValidState_RecreatesUser() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        UserId userId = UserId.newId();
        User user = User.reconstitute(
            userId,
            validUsername,
            validEmail,
            validPassword,
            Role.CUSTOMER,
            UserStatus.ACTIVE,
            true,
            null,
            false,
            null,
            null,
            true,
            now,
            "v1.0",
            false,
            null,
            5,
            now,
            now
        );

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(userId);
        assertThat(user.getUsername()).isEqualTo(validUsername);
        assertThat(user.getEmail()).isEqualTo(validEmail);
        assertThat(user.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.isTermsAccepted()).isTrue();
        assertThat(user.getTermsVersion()).isEqualTo("v1.0");
        assertThat(user.getVersion()).isEqualTo(5);
        assertThat(user.getDomainEvents()).isEmpty();
    }
}
