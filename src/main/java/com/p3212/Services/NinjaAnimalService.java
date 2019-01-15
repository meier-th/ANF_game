package com.p3212.Services;

import com.p3212.EntityClasses.NinjaAnimal;
import com.p3212.EntityClasses.NinjaAnimalRace;
import java.util.ArrayList;
import org.springframework.stereotype.Service;

@Service
public class NinjaAnimalService {

    public ArrayList<NinjaAnimal> getRaceAnimals(NinjaAnimalRace race) {
        ArrayList<NinjaAnimal> toRet = new ArrayList<>();
        NinjaAnimal.animals.stream().filter((animal) -> (animal.getRace().equals(race))).forEachOrdered((animal) -> {
            toRet.add(animal);
        });
        return toRet;
    }


    public NinjaAnimal get(String name) {
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
