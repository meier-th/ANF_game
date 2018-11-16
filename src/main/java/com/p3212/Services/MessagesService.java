package com.p3212.Services;

import com.p3212.EntityClasses.MessageCompositeKey;
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

    public PrivateMessage getMessage(MessageCompositeKey id) {
        return repository.findById(id).get();
    }

    public void removeMessage(MessageCompositeKey id) {
        repository.deleteById(id);
    }
    
    public List<PrivateMessage> getAllFromDialog(User firstUser, User secondUser) {
        String fname = firstUser.getLogin();
        String sname = secondUser.getLogin();
        return repository.getAllFromDialog(fname, sname);
    }
    
    public List<PrivateMessage> getUnreadMessages(String userName) {
        return repository.getUnreadMessages(userName);
    }
    
    public void setRead(String sender, String receiver, Date date) {
        repository.setRead(sender, receiver, date);
    }
}