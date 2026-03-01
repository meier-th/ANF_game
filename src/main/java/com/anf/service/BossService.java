package com.anf.service;

import org.springframework.stereotype.Service;

import com.anf.model.Boss;
import com.anf.repository.BossRepository;

import lombok.AllArgsConstructor;

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
