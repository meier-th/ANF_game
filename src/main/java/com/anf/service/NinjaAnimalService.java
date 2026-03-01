package com.anf.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.anf.model.NinjaAnimal;
import com.anf.model.NinjaAnimalRace;

@Service
public class NinjaAnimalService {

    public List<NinjaAnimal> getRaceAnimals(NinjaAnimalRace race) {
        return NinjaAnimal.animals.stream().filter((animal) -> (animal.getRace().equals(race))).collect(Collectors.toList());
    }


    public NinjaAnimal findByName(String name) {
        for (NinjaAnimal animal : NinjaAnimal.animals){
            if (animal.getName().equals(name)) {
                return animal;
            }
        }
        return null;
    }

    public NinjaAnimalRace getRaceByName(String name) {
        return NinjaAnimalRace.valueOf(name);
    }
    
}
