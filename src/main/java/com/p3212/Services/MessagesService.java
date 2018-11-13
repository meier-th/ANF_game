package com.p3212.Services;

import com.p3212.EntityClasses.MessageCompositeKey;
import com.p3212.EntityClasses.PrivateMessage;
import com.p3212.EntityClasses.User;
import com.p3212.Repositories.MessagesRepository;
import java.util.List;
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
    
    List<PrivateMessage> getAllFromDialog(User firstUser, User secondUser) {
        String fname = firstUser.getLogin();
        String sname = secondUser.getLogin();
        return repository.getAllFromDialog(fname, sname);
    }
    
    List<PrivateMessage> getUnreadMessages(User user) {
        String name = user.getLogin();
        return repository.getUnreadMessages(name);
    }
    
    void setRead(String sender, String receiver, String text) {
        repository.setRead(sender, receiver, text);
    }
}