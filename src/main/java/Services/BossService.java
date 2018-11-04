package Services;

import EntityClasses.Boss;
import Repositories.BossRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BossService {
    @Autowired
    BossRepository bossRepository;

    void addBoss(Boss boss) {
        bossRepository.save(boss);
    }

    Boss getBoss(int id) {
        return bossRepository.findById(id);
    }
}
