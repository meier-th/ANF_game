package EntityClasses;

import org.springframework.data.annotation.Id;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents NinjaAnimalRaces entity
 * Used to operate on connection between characters and animals
 */
@Entity
@Table(name = "Ninja_animal_races")
public enum NinjaAnimalRace {
    CHISTI(1),
    VILKOY(2),
    GOVNO(3);
    @Id
    private int id;

    private NinjaAnimalRace(int id) {
        this.id = id;
    }

    private static Map map = new HashMap<Integer, NinjaAnimalRace>();

    static {
        for (NinjaAnimalRace pageType : NinjaAnimalRace.values()) {
            map.put(pageType.id, pageType);
        }
    }

    public static NinjaAnimalRace valueOf(int pageType) {
        return (NinjaAnimalRace) map.get(pageType);
    }

    public int getId() {
        return id;
    }
}
