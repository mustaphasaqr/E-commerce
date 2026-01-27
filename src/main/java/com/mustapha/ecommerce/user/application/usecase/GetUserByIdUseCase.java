package com.mustapha.ecommerce.user.application.usecase;

import com.mustapha.ecommerce.user.application.command.GetUserByIdQuery;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Get User By ID Use Case
 * Responsibility: Retrieve user by ID
 * Pattern: Query Use Case
 */
@Component
@Transactional(readOnly = true)
public class GetUserByIdUseCase {
    
    private final UserRepository userRepository;

    public GetUserByIdUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User execute(GetUserByIdQuery query) {
        return userRepository.findById(query.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
