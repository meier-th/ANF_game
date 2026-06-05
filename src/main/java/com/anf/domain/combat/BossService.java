package com.anf.domain.combat;

import com.anf.model.database.Boss;
import com.anf.infrastructure.persistence.repository.BossRepository;
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
    if (name == null) {
      return null;
    }
    return bossRepository.getByName(name.trim());
  }

  public Boss getBoss(int id) {
    return bossRepository.findById(id).orElse(null);
  }
}
