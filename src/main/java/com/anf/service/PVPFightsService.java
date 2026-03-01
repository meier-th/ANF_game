package com.anf.service;

import com.anf.model.FightPVP;
import com.anf.model.GameCharacter;
import com.anf.repository.PVPFightsRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PVPFightsService {
  @Autowired private PVPFightsRepository repository;

  public void addFight(FightPVP fight) {
    repository.save(fight);
  }

  public List<FightPVP> getUsersFights(GameCharacter ch) {
    int id = ch.getId();
    return repository.getUsersPVPFights(id);
  }

  public FightPVP getFight(int id) {
    return repository.findById(id).get();
  }
}
