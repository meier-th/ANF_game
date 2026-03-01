package com.anf.service;


import java.util.Optional;

import org.springframework.stereotype.Service;

import com.anf.model.User;
import com.anf.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;

    public Optional<User> signIn(int vkId) {

        return userRepository.findUserByVkId(vkId);

    }

    public Optional<User> signIn(String email) {
        return userRepository.findUserByEmail(email);
    }
}
