package EntityClasses;

import org.springframework.data.annotation.Id;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDate;

/**
 * Represents Person entity
 * Used to operate on in-game character's properties
 */
@Entity
@Table(name = "Persons")
public class Character implements Creature {

    /**
     * The date of creating a character
     */
    private LocalDate creationDate;
    
    /**
     * Identifier
     */
    @Id
    private int id;
    /**
     * The maximum amount of chakra (mana)
     */
    private int maxChakraAmount;

    /**
     * The race of ninja animal that character is able to summon
     */
    private NinjaAnimalRace animalRace;


    /**
     * Appearance object for this character
     */
    private Appearance appearance;

    /**
     * The maximum amount of HP
     */
    private int maxHP;

    /**
     * The damage of a physical attack
     */
    private int physicalDamage;

    /**
     * The portion of income damage to be blocked.
     * 0 <= resistance < 0.5
     * next level: resistance = resistance + (1-resistance)/4
     */
    private float resistance;

    /**
     * Getter
     * {@link Character#creationDate}
     */
    public LocalDate getCreationDate() {
        return creationDate;
    }

    /**
     * Setter
     * {@link Character#creationDate}
     */
    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Getter
     * {@link Character#maxChakraAmount}
     */
    public int getMaxChakraAmount() {
        return maxChakraAmount;
    }

    /**
     * Setter
     * {@link Character#maxChakraAmount}
     */
    public void setMaxChakraAmount(int maxChakraAmount) {
        this.maxChakraAmount = maxChakraAmount;
    }

    /**
     * Getter
     * {@link Character#animalRace}
     */
    public NinjaAnimalRace getAnimalRace() {
        return animalRace;
    }

    /**
     * Setter
     * {@link Character#animalRace}
     */
    public void setAnimalRace(NinjaAnimalRace animalRace) {
        this.animalRace = animalRace;
    }

    /**
     * Getter
     * {@link Character#id}
     */
    public int getId() {
        return id;
    }

    /**
     * Setter
     * {@link Character#id}
     */
    public void setId(int id) {
        this.id = id;
    }

    
    /**
     * Getter
     * {@link Character#appearance}
     */
    public Appearance getAppearance() {
        return appearance;
    }

    /**
     * Setter
     * {@link Character#appearance}
     */
    public void setAppearance(Appearance appearance) {
        this.appearance = appearance;
    }

    /**
     * Getter
     * {@link Character#maxHP}
     */
    public int getMaxHP() {
        return maxHP;
    }

    /**
     * Setter
     * {@link Character#maxHP}
     */
    public void setMaxHP(int maxHP) {
        this.maxHP = maxHP;
    }

    /**
     * Getter
     * {@link Character#physicalDamage}
     */
    public int getPhysicalDamage() {
        return physicalDamage;
    }

    /**
     * Setter
     * {@link Character#physicalDamage}
     */
    public void setPhysicalDamage(int physicalDamage) {
        this.physicalDamage = physicalDamage;
    }

    /**
     * Getter
     * {@link Character#resistance}
     */
    public float getResistance() {
        return resistance;
    }

    /**
     * Setter
     * {@link Character#resistance}
     */
    public void setResistance(float resistance) {
        this.resistance = resistance;
    }

    /**
     * Default constructor. To be used for dependency injection
     */
    public Character() {
    }

    /**
     * To be used when retrieved from database
     *
     * @param date       - creation date
     * @param resistance - resistance float value
     * @param hp         - max hp value
     * @param damage     - physical damahe value
     * @param chakra     - amount of chakra
     */
    public Character(LocalDate date, float resistance, int hp, int damage, int chakra) {
        this.creationDate = date;
        this.resistance = resistance;
        this.maxHP = hp;
        this.physicalDamage = damage;
        this.maxChakraAmount = chakra;
    }

}

