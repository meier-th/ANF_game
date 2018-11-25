package com.p3212.configuration;

import com.p3212.EntityClasses.PrivateMessage;
import com.p3212.EntityClasses.User;
import com.p3212.Services.FriendsRequestService;
import com.p3212.Services.MessagesService;
import com.p3212.Services.UserService;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommunicationController {

    @Autowired
    private MessagesService messageServ;
    @Autowired
    private UserService userServ;
    @Autowired
    private FriendsRequestService requestServ;

    @PostMapping("/messages")
    public @ResponseBody
    Date sendMessage(@RequestParam("message") String message,
                     @RequestParam("sender") String sender,
                     @RequestParam("receiver") String receiver) {
        PrivateMessage msg = new PrivateMessage();
        msg.setIsRead(false);
        msg.setMessage(message);
        msg.setReceiver(userServ.getUser(receiver));
        msg.setSender(userServ.getUser(sender));
        messageServ.addMessage(msg);
        return msg.getSendingDate();
    }

    @GetMapping("/messages/unread")
    public @ResponseBody
    List<PrivateMessage> getUnreadMessages() {
        return messageServ
                .getUnreadMessages(
                        userServ.getUser(
                                SecurityContextHolder.getContext()
                                        .getAuthentication().getName()));
    }

    @GetMapping("/messages/dialog")
    public @ResponseBody
    List<PrivateMessage> getMessagesFromDialog(@RequestParam String firstName, @RequestParam String secondName) {
        User sen = userServ.getUser(firstName);
        User rec = userServ.getUser(secondName);
        return messageServ.getAllFromDialog(sen, rec);
    }

//    @RequestMapping("*")
//    @ResponseBody public String fallbackMethod() {
//        return "Something failed.";
//    }

    @DeleteMapping("/messages/{id}")
    public @ResponseBody
    String deleteMessage(@PathVariable int id) {
        try {
            messageServ.removeMessage(id);
            return "OK";
        } catch (Throwable error) {
            return error.getMessage();
        }
    }

    @GetMapping("/messages/{id}")
    @ResponseBody
    public PrivateMessage getOneMessage(@PathVariable int id) {
        return messageServ.getMessage(id);
    }

    @PostMapping("/messages/read")
    public void setMessageRead(@RequestParam String sender, @RequestParam String receiver, @RequestParam Date date) {
        messageServ.setRead(sender, receiver, date);
    }

    @DeleteMapping("/friends/requests")
    @ResponseBody
    public String deleteRequest(@RequestParam int id) {
        try {
            requestServ.removeRequest(id);
            return "OK";
        } catch (Throwable error) {
            return error.getMessage();
        }
    }

    @GetMapping("/friends/requests/outgoing/{login}")
    @ResponseBody
    public List<User> getOutgoingRequests(@PathVariable String login) {
        User sender = userServ.getUser(login);
        return requestServ.requestedUsers(sender);
    }

    @GetMapping("/friends/requests/incoming/{login}")
    @ResponseBody
    public List<User> getIncomingRequests(@PathVariable String login) {
        User receiver = userServ.getUser(login);
        return requestServ.requestingUsers(receiver);
    }

}
