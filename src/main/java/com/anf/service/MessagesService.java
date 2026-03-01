package com.anf.service;

import com.anf.model.database.PrivateMessage;
import com.anf.model.database.User;
import com.anf.repository.MessagesRepository;
import java.util.List;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MessagesService {
  private final MessagesRepository repository;

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
