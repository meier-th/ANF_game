package com.anf.service;

import com.anf.model.database.User;
import com.anf.repository.UserRepository;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

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
