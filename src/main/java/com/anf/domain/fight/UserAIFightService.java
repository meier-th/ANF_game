package com.anf.domain.fight;

import com.anf.model.database.AiFightParticipation;
import com.anf.infrastructure.persistence.repository.UserAIFightRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserAIFightService {

  private final UserAIFightRepository repository;

  public void add(AiFightParticipation uf) {
    repository.save(uf);
  }
}
