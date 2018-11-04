package Services;

import EntityClasses.PrivateMessage;
import Repositories.MessagesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessagesService {
    @Autowired
    MessagesRepository repository;

    void addMessage(PrivateMessage message) {
        repository.save(message);
    }

    PrivateMessage getMessage(int id) {
        return repository.findById(id).get();
    }

    void removeMessage(int id) {
        repository.deleteById(id);
    }

    void setRead(int id) {
        repository.setRead(id);
    }
}
