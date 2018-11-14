package EntityClasses;

import javax.persistence.GeneratedValue;
import javax.persistence.Transient;

/**
 * Represents statistics during a fight
 */

public class Fight {
    @Transient
    transient private int fighter1HP;
    @Transient
    transient private int fighter2HP;
    @Transient
    transient private int fighter1Chakra;
    @Transient
    transient private int fighter2Chakra;

    @Transient
    @GeneratedValue()
    transient int id;

    public void setFighter1HP(int fighter1HP) {
        this.fighter1HP = fighter1HP;
    }

    public void setFighter2HP(int fighter2HP) {
        this.fighter2HP = fighter2HP;
    }

    public void setFighter1Chakra(int fighter1Chakra) {
        this.fighter1Chakra = fighter1Chakra;
    }

    public void setFighter2Chakra(int fighter2Chakra) {
        this.fighter2Chakra = fighter2Chakra;
    }

    public int getFighter1HP() {
        return fighter2HP;
    }

    public int getFighter2HP() {
        return fighter2HP;
    }

    public int getFighter1Chakra() {
        return fighter1Chakra;
    }


    public int getFighter2Chakra() {
        return fighter2Chakra;
    }

    public int getId() {
        return id;
    }
}
