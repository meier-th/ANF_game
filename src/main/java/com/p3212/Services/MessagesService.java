package com.p3212.Services;

import com.p3212.EntityClasses.PrivateMessage;
import com.p3212.EntityClasses.User;
import com.p3212.Repositories.MessagesRepository;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessagesService {
    @Autowired
    MessagesRepository repository;

    public void addMessage(PrivateMessage message) {
        repository.save(message);
    }

    public PrivateMessage getMessage(int id) {
        return repository.findById(id).get();
    }

    public void removeMessage(int id) {
        repository.deleteById(id);
    }
    
    public List<PrivateMessage> getAllFromDialog(User firstUser, User secondUser) {
        return repository.getAllFromDialog(firstUser, secondUser);
    }
    
    public List<PrivateMessage> getUnreadMessages(User user) {
        return repository.getUnreadMessages(user);
    }
    
    public void setRead(User sender, User receiver, Date date) {
        repository.setRead(sender, receiver, date);
    }
}