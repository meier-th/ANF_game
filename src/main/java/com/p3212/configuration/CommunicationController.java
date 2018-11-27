package com.p3212.configuration;

import com.p3212.EntityClasses.FriendsRequest;
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

import com.p3212.EntityClasses.PrivateMessage;
import com.p3212.EntityClasses.User;
import com.p3212.Services.FriendsRequestService;
import com.p3212.Services.MessagesService;
import com.p3212.Services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
    @PostMapping("/messages") //WORKS, but we can send a message to no one. 'receiver' field is nullable.
    @ResponseBody
    public ResponseEntity sendMessage(@RequestParam("message") String message, @RequestParam("receiver") String receiver) {
        try {
            User recvr = userServ.getUser(receiver);
            User sender = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            PrivateMessage msg = new PrivateMessage(recvr, sender);
            msg.setIsRead(false);
            msg.setMessage(message);
            messageServ.addMessage(msg);
            return ResponseEntity.status(HttpStatus.OK).body(msg.getSendingDate());
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(error.getMessage());
        }
    }

    @GetMapping("/messages/unread") // WORKS
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
    @GetMapping("/messages/dialog") //WORKS
    public @ResponseBody
    List<PrivateMessage> getMessagesFromDialog(@RequestParam String secondName) {
        User sen = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
        User rec = userServ.getUser(secondName);
        return messageServ.getAllFromDialog(sen, rec);
    }

    /*
     * Deletes a message. User can only delete his own messages.
     */
    @DeleteMapping("/messages/{id}") // WORKS
    @ResponseBody
    public ResponseEntity deleteMessage(@PathVariable int id) {
        try {
        	User applier = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            PrivateMessage msg = messageServ.getMessage(id);
            if (msg.getSender().equals(applier))
            	messageServ.removeMessage(id);
            else
            	throw new Exception("User can only remove his own messages."); // WORKS
            return ResponseEntity.status(HttpStatus.OK).body("Message is deleted.");
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.OK).body(error.getMessage());
        }
    }

    /*
     * Sets message as read. Takes receiver from SecurityContext.
     */
    @PostMapping("/messages/{id}/read") //org.hibernate.hql.internal.QueryExecutionRequestException: Not supported for DML operations [update com.p3212.EntityClasses.PrivateMessage p set isRead = true where p.message_id = :id]; nested exception is java.lang.IllegalStateException: org.hibernate.hql.internal.QueryExecutionRequestException: 
            //Not supported for DML operations [update com.p3212.EntityClasses.PrivateMessage p set isRead = true where p.message_id = :id]
    public ResponseEntity setMessageRead(@PathVariable int id) {
        try {
            User recvr = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            PrivateMessage message = messageServ.getMessage(id);
            if (message.getReceiver().equals(recvr)) {
                messageServ.setRead(id);
                return ResponseEntity.status(HttpStatus.OK).body("Message 'is read' state is confirmed.");
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User can only set read status on messages (s)he received.");
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error.getMessage());
        }
    }

    /*
     * Takes a user from SecurityContext and a User from username and removes either request from *username*
     * to current User or from current User to *username*. See @Query in FriendsRequestRepository
     */
    @DeleteMapping("/friends/requests") //org.hibernate.hql.internal.QueryExecutionRequestException: Not supported for DML operations [delete from com.p3212.EntityClasses.FriendsRequest fr where 
            //fr.friendUser = :friend and fr.requestingUser = :requester or fr.friendUser = :requester and fr.requestingUser = :friend]; nested exception is java.lang.IllegalStateException: org.hibernate.hql.internal.QueryExecutionRequestException: 
            //Not supported for DML operations [delete from com.p3212.EntityClasses.FriendsRequest 
            //fr where fr.friendUser = :friend and fr.requestingUser = :requester or fr.friendUser = :requester and fr.requestingUser = :friend]
    @ResponseBody
    public ResponseEntity deleteRequest(@RequestParam String username) {
        try {
        	User sendr = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
        	User recvr = userServ.getUser(username);
            requestServ.removeRequest(recvr, sendr);
            return ResponseEntity.status(HttpStatus.OK).body("Request is deleted.");
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getMessage());
        }
    }

    @GetMapping("/friends/requests/outgoing") //WORKS
    @ResponseBody
    public List<User> getOutgoingRequests() {
        User sender = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
        return requestServ.requestedUsers(sender);
    }

    @GetMapping("/friends/requests/incoming") //WORKS
    @ResponseBody
    public List<User> getIncomingRequests() {
        User receiver = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
        return requestServ.requestingUsers(receiver);
    }

    @PostMapping("/friends/requests") // WORKS. but we can create request to no one. Field is nullable.
    @ResponseBody
    public ResponseEntity sendRequest(@RequestParam String userName) {
        try {
        User receiver = userServ.getUser(userName);
        User sender = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
        FriendsRequest request = new FriendsRequest(sender, receiver);
        requestServ.addRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Friend request created!");
        } catch (Throwable th) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(th.getMessage());
        }
    }
    
}
