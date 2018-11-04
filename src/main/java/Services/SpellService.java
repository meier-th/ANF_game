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

    void update(int id, Spell spell) {
        repository.save(spell); //TODO guess what
    }

    void removeSpell(int id) {
        repository.deleteById(id);
    }
}

