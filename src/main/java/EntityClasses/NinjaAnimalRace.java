package EntityClasses;

import javax.persistence.Id;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;

/**
 * Represents NinjaAnimalRaces entity
 * Used to operate on connection between characters and animals
 */
@Entity
@Table(name = "Ninja_animal_races")
public class NinjaAnimalRace {
    static enum races {
    CHISTI,
    VILKOY,
    GOVNO;
    }
    @Id
    @GeneratedValue
    private int id;
    
    private races raceName;
    
}
