package com.anf.service;

import com.anf.model.Stats;
import com.anf.repository.StatsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StatsService {

  private final StatsRepository repository;

  public void addStats(Stats stats) {
    repository.save(stats);
  }

  public void removeStats(int id) {
    repository.deleteById(id);
  }
}
