package com.mustapha.ecommerce.user.application.usecase;

import com.mustapha.ecommerce.user.application.command.GetUserByUsernameQuery;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class GetUserByUsernameUseCase {
    private final UserRepository userRepository;

    public GetUserByUsernameUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User execute(GetUserByUsernameQuery query) {
        return userRepository.findByUsername(query.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
