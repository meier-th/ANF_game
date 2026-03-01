package com.anf.service;

import com.anf.model.UserAIFight;
import com.anf.repository.UserAIFightRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserAIFightService {

  private final UserAIFightRepository repository;

  public void add(UserAIFight uf) {
    repository.save(uf);
  }
}
