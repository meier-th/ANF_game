package com.p3212.Services;


import com.p3212.EntityClasses.User;
import com.p3212.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    public Optional<User> signIn(int vkId) {

        return userRepository.findUserByVkId(vkId);

    }

    public Optional<User> signIn(String email) {
        return userRepository.findUserByEmail(email);
    }
}
