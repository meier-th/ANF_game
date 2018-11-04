package Services;

import EntityClasses.Character;
import Repositories.CharacterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class CharacterService {

    @Autowired
    CharacterRepository characterRepository;

    public List<Character> getAllCharacters() {
        List<Character> lst = new ArrayList<Character>();
        Iterator<Character> iterator = characterRepository.findAll().iterator();
        while (iterator.hasNext()) {
            lst.add(iterator.next());
        }
        return lst;
    }

    public void addCharacter(Character character) {
        characterRepository.save(character);
    }

    public Character getCharacter(int id) {
        return characterRepository.findById(id).get();
    }

    public void updateCharacter(String login, Character character) {
        characterRepository.save(character);
    }

    public void removeCharacter(int id) {
        characterRepository.deleteById(id);
    }

}
