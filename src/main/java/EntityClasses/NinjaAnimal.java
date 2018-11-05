package EntityClasses;

import javax.persistence.Id;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Represents NinjaAnimal entity
 * Used to operate on ninja animals
 */
@Entity
@Table(name = "Ninja_animals")
public class NinjaAnimal {
    /**
     * id of the animal
     */
    @Id
    @GeneratedValue
    private int id;
    /**
     * Name of the ninja animal
     */
    private String name;

    /**
     * The required level to summon the animal
     */
    private int requiredLevel;

    /**
     * HP
     */
    private int hp;

    /**
     * Damage
     */
    private int damage;

    /**
     * Race of the animal
     */
    @ManyToOne
    @JoinColumn(name="race")
    private NinjaAnimalRace race;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * Getter
     * {@link NinjaAnimal#name}
     */
    public String getName() {
        return name;
    }

    /**
     * Setter
     * {@link NinjaAnimal#name}
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter
     * {@link NinjaAnimal#requiredLevel}
     */
    public int getRequiredLevel() {
        return requiredLevel;
    }

    /**
     * Setter
     * {@link NinjaAnimal#requiredLevel}
     */
    public void setRequiredLevel(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    /**
     * Getter
     * {@link NinjaAnimal#hp}
     */
    public int getHp() {
        return hp;
    }

    /**
     * Setter
     * {@link NinjaAnimal#hp}
     */
    public void setHp(int hp) {
        this.hp = hp;
    }

    /**
     * Getter
     * {@link NinjaAnimal#damage}
     */
    public int getDamage() {
        return damage;
    }

    /**
     * Setter
     * {@link NinjaAnimal#damage}
     */
    public void setDamage(int damage) {
        this.damage = damage;
    }

    /**
     * Getter
     * {@link NinjaAnimal#race}
     */
    public NinjaAnimalRace getRace() {
        return race;
    }

    /**
     * Setter
     * {@link NinjaAnimal#race}
     */
    public void setRace(NinjaAnimalRace race) {
        this.race = race;
    }

    /**
     * Default constructor
     * Used for dependency injection
     */
    public NinjaAnimal() {
    }

    /**
     * To be used when retrieved from database
     *
     * @param id       ID of the animal
     * @param name     name of the animal
     * @param race     race of the animal
     * @param damage   physical damage of the animal
     * @param hp       hit points of the animal
     * @param reqlevel required level to summon the animal
     */
    public NinjaAnimal(int id, String name, NinjaAnimalRace race, int damage, int hp, int reqlevel) {
        this.id = id;
        this.damage = damage;
        this.hp = hp;
        this.name = name;
        this.race = race;
        this.requiredLevel = reqlevel;
    }
}
