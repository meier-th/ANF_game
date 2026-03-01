package com.anf.config;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.anf.model.FriendsRequest;
import com.anf.model.PrivateMessage;
import com.anf.model.StompPrincipal;
import com.anf.model.User;
import com.anf.service.FriendsRequestService;
import com.anf.service.MessagesService;
import com.anf.service.UserService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class CommunicationController {

    private final MessagesService messageService;
    private final UserService userService;
    private final FriendsRequestService requestService;
    private final WebSocketsController notifService;
    private final WebSocketsController wsController;

    /**
     * Sends a message. Receives two Strings (receiver login and Message itself), takes sender object from SecurityContext
     */
    @PostMapping("/profile/messages")
    public ResponseEntity<String> sendMessage(@RequestParam("message") String message, @RequestParam("receiver") String receiver) {
        try {
            User recvr = userService.getUser(receiver);
            if (recvr == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"User " + receiver + " wasn't found.\"}");
            User sender = userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            PrivateMessage msg = new PrivateMessage(recvr, sender);
            msg.setIsRead(false);
            msg.setMessage(message);
            messageService.addMessage(msg);
            String wsmessage = SecurityContextHolder.getContext().getAuthentication().getName() + ":" + message;
            wsController.send(new StompPrincipal(receiver), wsmessage);
            return ResponseEntity.status(HttpStatus.OK).body(msg.getSendingDate().toString());
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
        }
    }

    @GetMapping("/profile/messages/unread")
    public ResponseEntity<?> getUnreadMessages() {
        try {
            User user = userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            return ResponseEntity.status(HttpStatus.OK).body(messageService.getUnreadMessages(user));
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getMessage());
        }
    }

    @GetMapping("/profile/dialogs")
    public ResponseEntity<?> getDialogs() {
        User user = userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
        List<String> list = messageService.getDialogs(user);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    /**
     * Returns all messages between User from SecurityContext and User with username provided
     */
    @GetMapping("/profile/messages/dialog")
    public ResponseEntity<?> getMessagesFromDialog(@RequestParam String secondName) {
        try {
            User sen = userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            User rec = userService.getUser(secondName);
            if (rec == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"User with name " + secondName + " wasn't found.\"}");
            return ResponseEntity.status(HttpStatus.OK).body(messageService.getAllFromDialog(sen, rec));
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getMessage());
        }
    }

    /**
     * Deletes a message. User can only delete his own messages.
     */
    @DeleteMapping("/profile/messages/{id}")
    public ResponseEntity<String> deleteMessage(@PathVariable int id) {
        try {
            User applier = userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            PrivateMessage msg = messageService.getMessage(id);
            if (msg == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"Message with id = " + id + " wasn't found.\"}");
            if (msg.getSender().equals(applier)) {
                messageService.removeMessage(id);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"User can only remove his own messages.\"}");
            }
            return ResponseEntity.status(HttpStatus.OK).body("{\"Message is deleted.\"}");
        } catch (Exception error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
        }
    }

    /**
     * Sets message as read. Takes receiver from SecurityContext.
     */
    @PostMapping("/profile/messages/{id}/read")
    public ResponseEntity<String> setMessageRead(@PathVariable int id) {
        try {
            User recvr = userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            PrivateMessage message = messageService.getMessage(id);
            if (message == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"Message with id " + id + " wasn't found.\"}");
            if (message.getReceiver().equals(recvr)) {
                messageService.setRead(id);
                return ResponseEntity.status(HttpStatus.OK).body("{\"Message 'is read' state is confirmed.\"}");
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"User can only set read status on messages (s)he received.\"}");
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error.getMessage());
        }
    }

    /**
     * Takes a user from SecurityContext and a User from username and removes either request from *username*
     * to current User or from current User to *username*. See @Query in FriendsRequestRepository
     */
    @DeleteMapping("/profile/friends/requests")
    public ResponseEntity<String> deleteRequest(@RequestParam String username, @RequestParam String type) {
        try {
            String wsMessage = SecurityContextHolder.getContext().getAuthentication().getName();
            User sendr;
            User recvr;
            if (type.equalsIgnoreCase("in")) {
                sendr = userService.getUser(username);
                recvr = userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
                wsMessage = "decline:" + wsMessage;
                wsController.sendSocial(new StompPrincipal(recvr.getLogin()), "request:-"+username);
            } else {
                sendr = userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
                recvr = userService.getUser(username);
                wsMessage = "request:-" + wsMessage;
                wsController.sendSocial(new StompPrincipal(sendr.getLogin()), "request:/"+username);
            }
            if (!(requestService.requestedUsers(sendr).contains(recvr)))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"Request wasn't found.\"}");
            if (recvr == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"User with name " + username + " wasn't found.\"}");
            requestService.removeRequest(recvr, sendr);
            wsController.sendSocial(new StompPrincipal(username), wsMessage);
            return ResponseEntity.status(HttpStatus.OK).body("{\"Request is deleted.\"}");
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
        }
    }

    @GetMapping("/friends/requests/outgoing")
    public ResponseEntity<?> getOutgoingRequests() {
        try {
            User sender = userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            return ResponseEntity.status(HttpStatus.OK).body(requestService.requestedUsers(sender));
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
        }
    }

    @GetMapping("/friends/requests/incoming")
    public ResponseEntity<?> getIncomingRequests() {
        try {
            User receiver = userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            return ResponseEntity.status(HttpStatus.OK).body(requestService.requestingUsers(receiver));
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
        }
    }

    @PostMapping("/profile/friends/requests")
    public ResponseEntity<String> sendRequest(@RequestParam String username) {
        try {
            String wsMessage = "request:+" + SecurityContextHolder.getContext().getAuthentication().getName();
            User receiver = userService.getUser(username);
            if (receiver == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"User with name " + username + " wasn't found.\"}");
            User sender = userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            FriendsRequest request = new FriendsRequest(sender, receiver);
            requestService.addRequest(request);
            wsController.sendSocial(new StompPrincipal(username), wsMessage);
            wsController.sendSocial(new StompPrincipal(sender.getLogin()), "request:o"+username);
            return ResponseEntity.status(HttpStatus.CREATED).body("{\"Friend request created!\"}");
        } catch (Throwable th) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(th.getMessage());
        }
    }

    @PostMapping("/profile/friends")
    public ResponseEntity<String> addFriend(@RequestParam String login) {
        try {
            String wsMessage = "friend:+" + SecurityContextHolder.getContext().getAuthentication().getName();
            User acceptor = userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            User sender = userService.getUser(login);
            int reqId = -1;
            for (FriendsRequest req: acceptor.getFriendRequestsIn()) {
                if (req.getRequestingUser().getLogin().equals(login)) {
                    reqId = req.request_id;
                    break;
                }
            }
            if (reqId!=-1)
                userService.addFriend(acceptor, sender);
            else
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"No friend request was found.\"}");
            requestService.removeById(reqId);
            wsController.sendSocial(new StompPrincipal(login), wsMessage);
            wsController.sendSocial(new StompPrincipal(acceptor.getLogin()), "friend:o"+login);
            return ResponseEntity.status(HttpStatus.CREATED).body("{\"Friends relationship is created.\"}");
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
        }
    }

    @DeleteMapping("/profile/friends")
    public ResponseEntity<String> removeFriend(@RequestParam String username) {
        try {
            String wsMessage = "friend:-" + SecurityContextHolder.getContext().getAuthentication().getName();
            User remover = userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            User removed = userService.getUser(username);
            if (removed == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"User with name " + username + " wasn't found.\"}");
            if (!(remover.getFriends().contains(removed)))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"User " + username + " is not a friend of a user " + remover.getLogin() + ".\"}");
            userService.removeFriend(remover, removed);
            wsController.sendSocial(new StompPrincipal(username), wsMessage);
            wsController.sendSocial(new StompPrincipal(remover.getLogin()), "friend:/"+username);
            return ResponseEntity.status(HttpStatus.OK).body("{\"Friends relationship is removed.\"}");
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getMessage());
        }
    }

    @PostMapping("/admin/chat")
    public ResponseEntity<String> sendAdminWarning(@RequestBody String warning) {
        /*Message notif = new Message();
        notif.setAuthor("SYSTEM");
        notif.setText(warning);
        notifServ.notify(notif);*/
        notifService.notify("SYSTEM:"+warning);
        return ResponseEntity.status(HttpStatus.CREATED).body("{\"Warning is sent.\"}");
    }

//    @GetMapping("/sendinvite")
//    public ResponseEntity<String> inviteToFight(@RequestParam String type, @RequestParam String username) {
//        String author = SecurityContextHolder.getContext().getAuthentication().getName();
//        wsController.sendInvitation(username, author, type);
//        return ResponseEntity.ok().body("OK");
//    }
    
}
