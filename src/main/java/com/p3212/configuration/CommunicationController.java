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
import java.util.Optional;
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
    @PostMapping("/profile/messages")
    @ResponseBody
    public ResponseEntity<String> sendMessage(@RequestParam("message") String message, @RequestParam("receiver") String receiver) {
        try {
            User recvr = userServ.getUser(receiver);
            User sender = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            PrivateMessage msg = new PrivateMessage(recvr, sender);
            msg.setIsRead(false);
            msg.setMessage(message);
            messageServ.addMessage(msg);
            return ResponseEntity.status(HttpStatus.OK).body(msg.getSendingDate().toString());
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(error.getMessage());
        }
    }

    @GetMapping("/profile/messages/unread")
    @ResponseBody
    public ResponseEntity<List<PrivateMessage>> getUnreadMessages() {
        try {
            User user = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            return ResponseEntity.status(HttpStatus.OK).body(messageServ.getUnreadMessages(user));
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /*
     * Returns all messages between User from SecurityContext and User with username provided
     */
    @GetMapping("/profile/messages/dialog")
    @ResponseBody
    public ResponseEntity<List<PrivateMessage>> getMessagesFromDialog(@RequestParam String secondName) {
        try {
            User sen = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            User rec = userServ.getUser(secondName);
            return ResponseEntity.status(HttpStatus.OK).body(messageServ.getAllFromDialog(sen, rec));
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /*
     * Deletes a message. User can only delete his own messages.
     */
    @DeleteMapping("/profile/messages/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteMessage(@PathVariable int id) {
        try {
            User applier = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            PrivateMessage msg = messageServ.getMessage(id);
            if (msg.getSender().equals(applier)) {
                messageServ.removeMessage(id);
            } else {
                throw new Exception("User can only remove his own messages.");
            }
            return ResponseEntity.status(HttpStatus.OK).body("Message is deleted.");
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.OK).body(error.getMessage());
        }
    }

    /*
     * Sets message as read. Takes receiver from SecurityContext.
     */
    @PostMapping("/profile/messages/{id}/read")
    public ResponseEntity<String> setMessageRead(@PathVariable int id) {
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
    @DeleteMapping("/profile/friends/requests")
    @ResponseBody
    public ResponseEntity<String> deleteRequest(@RequestParam String username) {
        try {
            User sendr = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            User recvr = userServ.getUser(username);
            requestServ.removeRequest(recvr, sendr);
            return ResponseEntity.status(HttpStatus.OK).body("Request is deleted.");
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getMessage());
        }
    }

    @GetMapping("/profile/friends/requests/outgoing")
    @ResponseBody
    public ResponseEntity<List<User>> getOutgoingRequests() {
        try {
            User sender = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            return ResponseEntity.status(HttpStatus.OK).body(requestServ.requestedUsers(sender));
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/friends/requests/incoming")
    @ResponseBody
    public ResponseEntity<List<User>> getIncomingRequests() {
        try {
            User receiver = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            return ResponseEntity.status(HttpStatus.OK).body(requestServ.requestingUsers(receiver));
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping("/profile/friends/requests")
    @ResponseBody
    public ResponseEntity<String> sendRequest(@RequestParam String username) {
        try {
            User receiver = userServ.getUser(username);
            User sender = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            FriendsRequest request = new FriendsRequest(sender, receiver);
            requestServ.addRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("Friend request created!");
        } catch (Throwable th) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(th.getMessage());
        }
    }

    @PostMapping("/profile/friends")
    @ResponseBody
    public ResponseEntity<String> addFriend(@RequestParam int requestId) {
        try {
            User acceptor = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            Optional<FriendsRequest> opRequest = requestServ.getRequest(requestId);
            if (!(opRequest.isPresent()))
                throw new Throwable("No friend request with id = "+requestId+" was found.");
            if (!(acceptor.equals(opRequest.get().getFriendUser())))
                throw new Throwable("Friend request with id = "+requestId+" was not sent to current user.");
            User friend = opRequest.get().getFriendUser();
            userServ.addFriend(acceptor, friend);
            requestServ.removeRequest(acceptor, friend);
            return ResponseEntity.status(HttpStatus.CREATED).body("Friends relationship is created.");
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getMessage());
        }
    }

    @DeleteMapping("/profile/friends")
    @ResponseBody
    public ResponseEntity<String> removeFriend(@RequestParam String username) {
        try {
            User remover = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            User removed = userServ.getUser(username);
            userServ.removeFriend(remover, removed);
            return ResponseEntity.status(HttpStatus.OK).body("Friends relationship is removed.");
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getMessage());
        }
    }

}
