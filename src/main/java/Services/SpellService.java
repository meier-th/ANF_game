package Services;

import EntityClasses.Spell;
import Repositories.SpellRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpellService {
    @Autowired
    SpellRepository repository;

    void addSpell(Spell spell) {
        repository.save(spell);
    }

    Iterable<Spell> getAllSpells() {
        return repository.findAll();
    }

    Spell get(int id) {
        return repository.findById(id).get();
    }

    void update(Spell spell) {
        repository.save(spell);
    }

    void removeSpell(int id) {
        repository.deleteById(id);
    }
}

