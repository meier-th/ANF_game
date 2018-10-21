package P3212.ANFBackend.DAOs;

import P3212.ANFBackend.EntityClasses.Appearance;

import java.util.List;

public interface DAOAppearanceInterface {
    /**
     * Creates appearance for the character. One appearance per user
     *
     * @param id ID of the character
     * @param gender        Gender
     * @param skinColour    Skin colour
     * @param hairColour    hair colour
     * @param clothesColour clothes colour
     */
    void create(int id, Appearance.Gender gender,
                Appearance.SkinColour skinColour,
                Appearance.HairColour hairColour,
                Appearance.ClothesColour clothesColour);

    /**
     * Changes appearance of the character. One appearance per user
     *
     * @param id ID of the character
     * @param gender        Gender
     * @param skinColour    Skin colour
     * @param hairColour    hair colour
     * @param clothesColour clothes colour
     */
    void update(int id, Appearance.Gender gender,
                Appearance.SkinColour skinColour,
                Appearance.HairColour hairColour,
                Appearance.ClothesColour clothesColour);

    /**
     * Returns appearance for the character with specified id
     *
     * @param id id of the character
     * @return Appearance object
     */
    Appearance get(int id);

    /**
     * Deletes appearance of the character
     *
     * @param id id of the character
     */
    void delete(int id);
}
