package com.p3212.EntityClasses;

import java.util.ArrayList;
import javax.persistence.Column;
import javax.persistence.Id;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

/**
 * Represents NinjaAnimalRaces entity
 * Used to operate on connection between characters and animals
 */
@Entity
@Table(name = "Ninja_animal_races")
public class NinjaAnimalRace {
    public static enum races {
    VESELIBA,
    BOJAJUMUS,
    LIDZSVARU,
    DALNIY_RODSTVENNIK;
    }
    @Id
    @Column(length=20)
    @Enumerated(EnumType.STRING)
    private races raceName;

    public races getRaceName() {
        return raceName;
    }
    
    public NinjaAnimalRace(String race) {
        this.raceName = races.valueOf(race);
    }
    
    public ArrayList<NinjaAnimalRace> getAllRaces() {
        ArrayList<NinjaAnimalRace> toRet = new ArrayList<>();
        toRet.add(new NinjaAnimalRace(NinjaAnimalRace.races.VESELIBA.toString()));
        toRet.add(new NinjaAnimalRace(NinjaAnimalRace.races.BOJAJUMUS.toString()));
        toRet.add(new NinjaAnimalRace(NinjaAnimalRace.races.DALNIY_RODSTVENNIK.toString()));
        toRet.add(new NinjaAnimalRace(NinjaAnimalRace.races.LIDZSVARU.toString()));
        return toRet;
    }
    
    public NinjaAnimalRace(){}

    public void setRaceName(races raceName) {
        this.raceName = raceName;
    }
    
}
