package com.anf.service;

import com.anf.model.User;
import com.anf.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AnfUserDetailsService implements UserDetailsService {
  private final UserRepository repository;

  @Override
  public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
    User user = repository.findById(s).get();
    // return new org.springframework.security.core.userdetails.User();
    return null;
  }
}
