package com.mustapha.ecommerce.user.infrastructure.persistence.repository;

import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.*;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@EntityScan("com.mustapha.ecommerce")
@Import({JpaUserRepository.class, com.mustapha.ecommerce.user.infrastructure.persistence.mapper.UserMapper.class})
@DisplayName("UserRepository Integration Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.create(
            Username.of("johndoe"),
            Email.of("john@example.com"),
            Password.fromHashed("$2a$10$hashedpassword"),
            Role.CUSTOMER
        );
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperationsTests {

        @Test
        @DisplayName("Should save new user successfully")
        void shouldSaveNewUser() {
            User saved = userRepository.save(testUser);

            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getUsername()).isEqualTo(testUser.getUsername());
            assertThat(saved.getEmail()).isEqualTo(testUser.getEmail());
        }

        @Test
        @DisplayName("Should update existing user")
        void shouldUpdateExistingUser() {
            testUser.acceptTerms("v1.0");
            testUser.verifyEmail();
            testUser.activate("Test activation");
            
            User saved = userRepository.save(testUser);
            saved.changeEmail(Email.of("newemail@example.com"));

            User updated = userRepository.save(saved);

            assertThat(updated.getEmail().getValue()).isEqualTo("newemail@example.com");
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperationsTests {

        @Test
        @DisplayName("Should find user by ID")
        void shouldFindUserById() {
            User saved = userRepository.save(testUser);

            Optional<User> found = userRepository.findById(saved.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(saved.getId());
        }

        @Test
        @DisplayName("Should find user by email")
        void shouldFindUserByEmail() {
            userRepository.save(testUser);

            Optional<User> found = userRepository.findByEmail(Email.of("john@example.com"));

            assertThat(found).isPresent();
            assertThat(found.get().getEmail().getValue()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("Should find user by username")
        void shouldFindUserByUsername() {
            userRepository.save(testUser);

            Optional<User> found = userRepository.findByUsername(Username.of("johndoe"));

            assertThat(found).isPresent();
            assertThat(found.get().getUsername().getValue()).isEqualTo("johndoe");
        }

        @Test
        @DisplayName("Should return empty when user not found by ID")
        void shouldReturnEmptyWhenNotFoundById() {
            Optional<User> found = userRepository.findById(UserId.newId());

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should return empty when user not found by email")
        void shouldReturnEmptyWhenNotFoundByEmail() {
            Optional<User> found = userRepository.findByEmail(Email.of("nonexistent@example.com"));

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Exists Operations")
    class ExistsOperationsTests {

        @Test
        @DisplayName("Should return true when email exists")
        void shouldReturnTrueWhenEmailExists() {
            userRepository.save(testUser);

            boolean exists = userRepository.existsByEmail(Email.of("john@example.com"));

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false when email does not exist")
        void shouldReturnFalseWhenEmailDoesNotExist() {
            boolean exists = userRepository.existsByEmail(Email.of("nonexistent@example.com"));

            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Should return true when username exists")
        void shouldReturnTrueWhenUsernameExists() {
            userRepository.save(testUser);

            boolean exists = userRepository.existsByUsername(Username.of("johndoe"));

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false when username does not exist")
        void shouldReturnFalseWhenUsernameDoesNotExist() {
            boolean exists = userRepository.existsByUsername(Username.of("nonexistent"));

            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("Pagination and Search")
    class PaginationAndSearchTests {

        @Test
        @DisplayName("Should find all users with pagination")
        void shouldFindAllUsersWithPagination() {
            userRepository.save(testUser);
            User user2 = User.create(
                Username.of("janedoe"),
                Email.of("jane@example.com"),
                Password.fromHashed("$2a$10$hashedpassword"),
                Role.CUSTOMER
            );
            userRepository.save(user2);

            Page<User> page = userRepository.findAll(PageRequest.of(0, 10));

            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should search users by email")
        void shouldSearchUsersByEmail() {
            userRepository.save(testUser);

            Page<User> page = userRepository.search("john@example.com", null, null, null, PageRequest.of(0, 10));

            assertThat(page.getContent()).hasSize(1);
            assertThat(page.getContent().get(0).getEmail().getValue()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("Should search users by username")
        void shouldSearchUsersByUsername() {
            userRepository.save(testUser);

            Page<User> page = userRepository.search(null, "johndoe", null, null, PageRequest.of(0, 10));

            assertThat(page.getContent()).hasSize(1);
            assertThat(page.getContent().get(0).getUsername().getValue()).isEqualTo("johndoe");
        }

        @Test
        @DisplayName("Should search users by role")
        void shouldSearchUsersByRole() {
            userRepository.save(testUser);

            Page<User> page = userRepository.search(null, null, null, Role.CUSTOMER, PageRequest.of(0, 10));

            assertThat(page.getContent()).hasSize(1);
            assertThat(page.getContent().get(0).getRole()).isEqualTo(Role.CUSTOMER);
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperationsTests {

        @Test
        @DisplayName("Should delete user by ID")
        void shouldDeleteUserById() {
            User saved = userRepository.save(testUser);

            userRepository.delete(saved.getId());

            Optional<User> found = userRepository.findById(saved.getId());
            assertThat(found).isEmpty();
        }
    }
}
