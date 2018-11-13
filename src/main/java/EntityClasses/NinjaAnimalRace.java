package EntityClasses;

import javax.persistence.Id;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;

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
    @Enumerated(EnumType.STRING)
    private races raceName;

}
