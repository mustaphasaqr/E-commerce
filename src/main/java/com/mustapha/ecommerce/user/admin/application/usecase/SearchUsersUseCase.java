package com.mustapha.ecommerce.user.admin.application.usecase;

import com.mustapha.ecommerce.user.admin.application.command.SearchUsersCommand;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

/**
 * Search Users Use Case
 * Responsibility: Search users by criteria with pagination (admin only)
 * Pattern: Use Case (Application Service)
 */
@Service
public class SearchUsersUseCase {

    private final UserRepository userRepository;

    public SearchUsersUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<User> execute(SearchUsersCommand command) {
        return userRepository.search(
            command.email(),
            command.username(),
            command.status(),
            command.role(),
            command.pageable()
        );
    }
}
