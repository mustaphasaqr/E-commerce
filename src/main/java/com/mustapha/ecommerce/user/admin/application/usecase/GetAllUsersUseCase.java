package com.mustapha.ecommerce.user.admin.application.usecase;

import com.mustapha.ecommerce.user.admin.application.command.GetAllUsersCommand;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

/**
 * Get All Users Use Case
 * Responsibility: Retrieve paginated list of all users (admin only)
 * Pattern: Use Case (Application Service)
 */
@Service
public class GetAllUsersUseCase {

    private final UserRepository userRepository;

    public GetAllUsersUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<User> execute(GetAllUsersCommand command) {
        return userRepository.findAll(command.pageable());
    }
}
