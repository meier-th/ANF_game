package com.p3212.Services;

import com.p3212.Repositories.CharacterRepository;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.p3212.EntityClasses.Character;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CharacterService {
    @Autowired
    CharacterRepository charRepository;
    
    public List<Character> getAllCharacters() {
        List<Character> lst = new ArrayList<Character>();
        Iterator<Character> iterator = charRepository.findAll().iterator();
        while (iterator.hasNext()) {
            lst.add(iterator.next());
        }
        return lst;
    }
    
    public void addCharacter(Character usr) {
        charRepository.save(usr);
    }
    
    public Character getCharacter(int id) {
        return charRepository.findById(id).get();
    }
    
    public void updateCharacter(Character usr) {
        charRepository.save(usr);
    }
    
    public void removeCharacter(int id) {
        charRepository.deleteById(id);
    }
}
