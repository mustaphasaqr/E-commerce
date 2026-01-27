package com.mustapha.ecommerce.user.application.usecase;

import com.mustapha.ecommerce.user.application.command.GetUserByEmailQuery;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class GetUserByEmailUseCase {
    private final UserRepository userRepository;

    public GetUserByEmailUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User execute(GetUserByEmailQuery query) {
        return userRepository.findByEmail(query.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
