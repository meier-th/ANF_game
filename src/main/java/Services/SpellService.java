package Services;

import EntityClasses.Spell;
import Repositories.SpellRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpellService {
    @Autowired
    SpellRepository repository;

    public void addSpell(Spell spell) {
        repository.save(spell);
    }

    public Iterable<Spell> getAllSpells() {
        return repository.findAll();
    }

    public Spell get(int id) {
        return repository.findById(id).get();
    }

    public void update(Spell spell) {
        repository.save(spell);
    }

    public void removeSpell(int id) {
        repository.deleteById(id);
    }
}

