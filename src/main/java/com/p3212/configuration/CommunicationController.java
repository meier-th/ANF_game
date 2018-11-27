package com.p3212.configuration;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.p3212.EntityClasses.FriendsRequest;
import com.p3212.EntityClasses.PrivateMessage;
import com.p3212.EntityClasses.User;
import com.p3212.Services.FriendsRequestService;
import com.p3212.Services.MessagesService;
import com.p3212.Services.UserService;

@RestController
public class CommunicationController {

    @Autowired
    private MessagesService messageServ;
    @Autowired
    private UserService userServ;
    @Autowired
    private FriendsRequestService requestServ;

    /*
     * Sends a message. Receives two Strings (receiver login and Message itself), takes sender object from SecurityContext
     */
    @PostMapping("/messages")
    public @ResponseBody
    Date sendMessage(@RequestParam("message") String message,
                     @RequestParam("receiver") String receiver) {
        PrivateMessage msg = new PrivateMessage();
        User sender = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
        msg.setIsRead(false);
        msg.setMessage(message);
        msg.setReceiver(userServ.getUser(receiver));
        msg.setSender(sender);
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

    /*
     * Returns all messages between User from SecurityContext and User with username provided
     */
    @GetMapping("/messages/dialog")
    public @ResponseBody
    List<PrivateMessage> getMessagesFromDialog(@RequestParam String secondName) {
        User sen = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
        User rec = userServ.getUser(secondName);
        return messageServ.getAllFromDialog(sen, rec);
    }

    /*
     * Deletes a message. User an only delete his own messages.
     */
    @DeleteMapping("/messages/{id}")
    public @ResponseBody
    String deleteMessage(@PathVariable int id) {
        try {
        	User applier = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            PrivateMessage msg = messageServ.getMessage(id);
            if (msg.getSender().equals(applier))
            	messageServ.removeMessage(id);
            else
            	throw new Exception("User can only remove his own messages.");
            return "OK";
        } catch (Throwable error) {
            return error.getMessage();
        }
    }

    /*
     * Returns a message by Id. Do we really need it?
     */
    @GetMapping("/messages/{id}")
    @ResponseBody
    public PrivateMessage getOneMessage(@PathVariable int id) {
        return messageServ.getMessage(id);
    }

    /*
     * Sets message as read. Takes receiver from SecurityContext.
     */
    @PostMapping("/messages/read")
    public void setMessageRead(@RequestParam String sender, @RequestParam Date date) {
    	User sendr = userServ.getUser(sender);
    	User recvr = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
        messageServ.setRead(sendr, recvr, date);
    }

    /*
     * Takes a user from SecurityContext and a User from username and removes either request from *username*
     * to current User or from current User to *username*. See @Query in FriendsRequestRepository
     */
    @DeleteMapping("/friends/requests")
    @ResponseBody
    public String deleteRequest(@RequestParam String username) {
        try {
        	User sendr = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
        	User recvr = userServ.getUser(username);
            requestServ.removeRequest(recvr, sendr);
            return "OK";
        } catch (Throwable error) {
            return error.getMessage();
        }
    }

    @GetMapping("/friends/requests/outgoing")
    @ResponseBody
    public List<User> getOutgoingRequests() {
        User sender = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
        return requestServ.requestedUsers(sender);
    }

    @GetMapping("/friends/requests/incoming")
    @ResponseBody
    public List<User> getIncomingRequests() {
        User receiver = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
        return requestServ.requestingUsers(receiver);
    }

}
