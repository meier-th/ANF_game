package EntityClasses;


import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Represents Features entity
 * Used to operate on in-game characters appearances
 */
@Entity
@Table(name = "Features")
public class Appearance {

    public static enum Gender {
        MALE,
        FEMALE;
    }

    public static enum SkinColour {
        WHITE,
        LATIN,
        DARK,
        BLACK;
    }

    public static enum HairColour {
        YELLOW,
        BROWN,
        BLACK;
    }

    public static enum ClothesColour {
        GREEN,
        RED,
        BLUE;
    }

    /**
     * Gender of character
     * Responsible for model
     */
    private Gender gender;

    /**
     * Skin colour of a character
     */
    private SkinColour skinColour;

    /**
     * Hair colour of a character
     */
    private HairColour hairColour;

    /**
     * Clothes colour of a character
     */
    private ClothesColour clothesColour;


    /**
     * Getter
     * {@link Appearance#gender}
     */
    public Gender getGender() {
        return gender;
    }

    /**
     * Setter
     * {@link Appearance#gender}
     */
    public void setGender(Gender gender) {
        this.gender = gender;
    }

    /**
     * Getter
     * {@link Appearance#skinColour}
     */
    public SkinColour getSkinColour() {
        return skinColour;
    }

    /**
     * Setter
     * {@link Appearance#skinColour}
     */
    public void setSkinColour(SkinColour skinColour) {
        this.skinColour = skinColour;
    }

    /**
     * Getter
     * {@link Appearance#hairColour}
     */
    public HairColour getHairColour() {
        return hairColour;
    }

    /**
     * Setter
     * {@link Appearance#hairColour}
     */
    public void setHairColour(HairColour hairColour) {
        this.hairColour = hairColour;
    }

    /**
     * Getter
     * {@link Appearance#clothesColour}
     */
    public ClothesColour getClothesColour() {
        return clothesColour;
    }

    /**
     * Setter
     * {@link Appearance#clothesColour}
     */
    public void setClothesColour(ClothesColour clothesColour) {
        this.clothesColour = clothesColour;
    }


}
