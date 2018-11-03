package P3212.ANFBackend.Services;

import P3212.ANFBackend.EntityClasses.User;
import P3212.ANFBackend.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

@Service
//@EnableJdbcRepositories
@ComponentScan("P3212.ANFBackend.Repositories")
public class ServiceBean {
    @Autowired
    public UserRepository userRepository;

    public Iterable<User> findAll() {
        return userRepository.findAll();
    }
}
