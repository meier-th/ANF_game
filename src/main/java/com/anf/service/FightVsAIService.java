package com.anf.service;

import com.anf.model.FightVsAI;
import com.anf.model.GameCharacter;
import com.anf.model.UserAIFight;
import com.anf.repository.FightVsAIRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FightVsAIService {
  private final FightVsAIRepository repository;

  public void addFight(FightVsAI fight) {
    repository.save(fight);
  }

  public List<UserAIFight> getByFighterId(GameCharacter ch) {
    int id = ch.getId();
    return repository.getAIFightsByUser(id);
  }

  public FightVsAI getFight(int id) {
    return repository.findById(id).get();
  }
}
