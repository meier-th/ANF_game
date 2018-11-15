package Services;

import com.p3212.EntityClasses.User;
import com.p3212.Repositories.UserRepository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service to operate with user Entity
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        List<User> lst = new ArrayList<User>();
        Iterator<User> iterator = userRepository.findAll().iterator();
        while (iterator.hasNext()) {
            lst.add(iterator.next());
        }
        return lst;
    }

    /**
     * Adds new user to db
     *
     * @param usr new user
     */

    public boolean addUser(User usr) {
        if (!userRepository.existsById(usr.getLogin())) {
            userRepository.save(usr);
            return true;
        } else return false;
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
