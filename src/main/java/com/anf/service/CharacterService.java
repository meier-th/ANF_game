package com.anf.service;

import com.anf.model.GameCharacter;
import com.anf.repository.CharacterRepository;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CharacterService {
  private final CharacterRepository charRepository;

  public List<GameCharacter> getAllCharacters() {
    List<GameCharacter> lst = new ArrayList<>();
    Iterator<GameCharacter> iterator = charRepository.findAll().iterator();
    while (iterator.hasNext()) {
      lst.add(iterator.next());
    }
    return lst;
  }

  public void addCharacter(GameCharacter usr) {
    charRepository.save(usr);
  }

  public GameCharacter getCharacter(int id) {
    return charRepository.findById(id).orElse(null);
  }

  public void removeCharacter(int id) {
    charRepository.deleteById(id);
  }
}
