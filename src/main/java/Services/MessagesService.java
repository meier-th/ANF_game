package Services;

import EntityClasses.MessageCompositeKey;
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

    PrivateMessage getMessage(MessageCompositeKey id) {
        return repository.findById(id).get();
    }

    void removeMessage(MessageCompositeKey id) {
        repository.deleteById(id);
    }

    void setRead(String sender, String receiver, String text) {
        repository.setRead(sender, receiver, text);
    }
}