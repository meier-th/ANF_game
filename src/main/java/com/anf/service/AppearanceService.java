package com.anf.service;

import com.anf.model.Appearance;
import com.anf.repository.AppearanceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/** Service for character's appearance */
@Service
@AllArgsConstructor
public class AppearanceService {
  /** Repository for appearance entity */
  private final AppearanceRepository appearanceRepository;

  /**
   * Add a new appearance
   *
   * @param appearance Appearance object to save
   */
  public void addAppearance(Appearance appearance) {
    appearanceRepository.save(appearance);
  }

  public void removeAppearance(int id) {
    appearanceRepository.deleteById(id);
  }
}
