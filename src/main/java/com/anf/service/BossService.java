package com.anf.service;

import com.anf.model.database.Boss;
import com.anf.repository.BossRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BossService {
  private final BossRepository bossRepository;

  public void addBoss(Boss boss) {
    bossRepository.save(boss);
  }

  public Boss getBossByName(String name) {
    return bossRepository.getByName(name);
  }

  public Boss getBoss(int id) {
    return bossRepository.findById(id).orElse(null);
  }
}
