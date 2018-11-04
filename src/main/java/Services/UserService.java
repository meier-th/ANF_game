package Services;

import EntityClasses.User;
import Repositories.UserRepository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

/**
 * Service to operate with user Entity
 */
@Service
public class UserService {

    /**
     * Repository for user entity
     */

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all Users
     *
     * @return Iterable with all users
     */
    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Adds new user to db
     *
     * @param usr new user
     */

    public void addUser(User usr) {
        userRepository.save(usr);
    }

    /**
     * Get user with the login
     *
     * @param login login of the user
     * @return requested user
     */
    public User getUser(String login) {
        return userRepository.findById(login).get();
    }

    /**
     * Updates a user
     *
     * @param login login of user to update
     * @param usr   A User object to be saved in db
     */
    public void updateUser(String login, User usr) {
        userRepository.save(usr);
    }

    /**
     * Remove a user
     *
     * @param login login of the user
     */
    public void removeUser(String login) {
        userRepository.deleteById(login);
    }

}
