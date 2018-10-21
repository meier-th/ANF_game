package P3212.ANFBackend.EntityClasses;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents NinjaAnimalRaces entity
 * Used to operate on connection between characters and animals
 */
public enum NinjaAnimalRace {
    CHISTI(1),
    VILKOY(2),
    GOVNO(3);  
    private int id;
    private NinjaAnimalRace(int id) {
        this.id = id;
    }
    
    private static Map map = new HashMap<>();
    
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
