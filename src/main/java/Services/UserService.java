package Services;

import EntityClasses.User;
import Repositories.UserRepository;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    
    public void addUser(User usr) {
        userRepository.save(usr);
    }
    
    public User getUser(String login) {
        return userRepository.findById(login).get();
    }
    
    public void updateUser(String login, User usr) {
        userRepository.save(usr);
    }
    
    public void removeUser(String login) {
        userRepository.deleteById(login);
    }
    
}
