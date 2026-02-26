package com.anf.Services;

import com.anf.EntityClasses.PrivateMessage;
import com.anf.EntityClasses.User;
import com.anf.Repositories.MessagesRepository;

import java.util.List;
import javax.transaction.Transactional;

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
        return repository.findById(id).orElse(null);
    }

    public void removeMessage(int id) {
        repository.deleteById(id);
    }

    public List<PrivateMessage> getAllFromDialog(User firstUser, User secondUser) {
        return repository.getAllFromDialog(firstUser, secondUser);
    }

    public List<String> getDialogs(User user) {
        return repository.getDialogs(user);
    }

    public List<PrivateMessage> getUnreadMessages(User user) {
        return repository.getUnreadMessages(user);
    }

    @Transactional
    public void setRead(int id) {
        repository.setRead(id);
    }
}