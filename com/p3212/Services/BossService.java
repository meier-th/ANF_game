package com.p3212.Services;

import com.p3212.EntityClasses.Boss;
import com.p3212.Repositories.BossRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BossService {
    @Autowired
    BossRepository bossRepository;

    public void addBoss(Boss boss) {
        bossRepository.save(boss);
    }
    
    public Boss getBossByName(String name) {
        return bossRepository.getByName(name);
    }
    
    public Boss getBoss(int id) {
        return bossRepository.findById(id).get();
    }
}
