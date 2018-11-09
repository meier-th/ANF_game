package Services;

import EntityClasses.Appearance;
import Repositories.AppearanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for character's appearance
 */
@Service
public class AppearanceService {
    /**
     * Repository for appearance entity
     */
    @Autowired
    AppearanceRepository appearanceRepository;

    /**
     * Get all appearances
     *
     * @return Iterable with all appearances
     */
    public Iterable<Appearance> getAllAppearances() {
        return appearanceRepository.findAll();
    }

    /**
     * Add a new appearance
     *
     * @param appearance Appearance object to save
     */
    public void addAppearance(Appearance appearance) {
        appearanceRepository.save(appearance);
    }

    /**
     * Get appearance with the id
     *
     * @param id id of the character(appearance too)
     * @return Requested appearance
     */
    public Appearance getUserAppearance(int id) {
        return appearanceRepository.findById(id).get();
    }

    /**
     * Update an appearance
     *
     * @param appearance Appearance object to save
     */
    public void updateAppearance(Appearance appearance) {
        appearanceRepository.save(appearance); //TODO do we really need save
    }

    /**
     * Remove appearance
     *
     * @param id id of the appearance (character)
     */
    public void removeUserAppearance(int id) {
        appearanceRepository.deleteById(id);
    }

}
