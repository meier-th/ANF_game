package Services;

import EntityClasses.Stats;
import Repositories.StatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

    @Autowired
    StatsRepository repository;

    void addStats(Stats stats) {
        repository.save(stats);
    }

    Stats get(int id) {
        return repository.findById(id).get();
    }

    Iterable<Stats> getAllStats() {
        return repository.findAll();
    }

    void updateStats(int id, Stats stats) {
        repository.save(stats); //TODO guess what
    }

    void removeStats(int id) {
        repository.deleteById(id);
    }
}
