package com.p3212.Services;

import com.p3212.EntityClasses.Appearance;
import com.p3212.Repositories.AppearanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for character's appearance
 */
@Service
public class AppearanceService {
    /**
     * Repository for appearance entity
     */
    @Autowired
    AppearanceRepository appearanceRepository;

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
