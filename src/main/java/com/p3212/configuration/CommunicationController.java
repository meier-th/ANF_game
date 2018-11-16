package com.p3212.configuration;

import com.p3212.EntityClasses.FriendRequestCompositeKey;
import com.p3212.EntityClasses.Friends;
import com.p3212.EntityClasses.FriendsCompositeKey;
import com.p3212.EntityClasses.FriendsRequest;
import com.p3212.EntityClasses.MessageCompositeKey;
import com.p3212.EntityClasses.PrivateMessage;
import com.p3212.EntityClasses.User;
import com.p3212.Services.FriendsRequestService;
import com.p3212.Services.FriendsService;
import com.p3212.Services.MessagesService;
import com.p3212.Services.UserService;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommunicationController {
    
    @Autowired
    private MessagesService messageServ;
    @Autowired
    private UserService userServ;
    @Autowired
    private FriendsService friendServ;
    @Autowired
    private FriendsRequestService requestServ;
    
    @PostMapping("/messages")
    public @ResponseBody Date sendMessage(@RequestBody String senderName,@RequestBody String receiverName,@RequestBody String body) {
	User sen = userServ.getUser(senderName);
        User rec = userServ.getUser(receiverName);
        PrivateMessage msg = new PrivateMessage(new MessageCompositeKey(sen, rec), body);
        messageServ.addMessage(msg);
	return msg.getMessage_id().getSendingDate();
}
    
    @GetMapping("/messages/unread")
    public @ResponseBody List<PrivateMessage> getUnreadMessages(@RequestBody String user) {
        return messageServ.getUnreadMessages(user);
    }
    
    @GetMapping("/messages/dialog")
    public @ResponseBody List<PrivateMessage> getMessagesFromDialog(@RequestBody String firstName, @RequestBody String secondName) {
        User sen = userServ.getUser(firstName);
        User rec = userServ.getUser(secondName);
        return messageServ.getAllFromDialog(sen, rec);
    }
    
    @RequestMapping("*")
    @ResponseBody public String fallbackMethod() {
        return "Something failed.";
    }
    
    @DeleteMapping("/messages")
    public @ResponseBody String deleteMessage(@RequestBody String sender, @RequestBody String receiver, @RequestBody Date date) {
        try {
            User sen = userServ.getUser(sender);
            User rec = userServ.getUser(receiver);
            MessageCompositeKey msgKey = new MessageCompositeKey(rec, sen, date);
            messageServ.removeMessage(msgKey);
            return "OK";
        } catch (Throwable error) {
            return error.getMessage();
        }
    }
    
    @PostMapping("/messages/one")
    @ResponseBody public PrivateMessage getOneMessage(@RequestBody String sender, @RequestBody String receiver, @RequestBody Date date){
        User sen = userServ.getUser(sender);
        User rec = userServ.getUser(receiver);
        MessageCompositeKey msgKey = new MessageCompositeKey(rec, sen, date);
        return messageServ.getMessage(msgKey);
    }
    
    @PostMapping("/messages/read")
    public void setMessageRead(@RequestBody String sender, @RequestBody String receiver, @RequestBody Date date) {
        messageServ.setRead(sender, receiver, date);
    }
    
    @PostMapping("/friends")
    @ResponseBody public String addFriends(@RequestBody String user1, @RequestBody String user2){
        try {
            User us1 = userServ.getUser(user1);
            User us2 = userServ.getUser(user2);
            Friends friends = new Friends(new FriendsCompositeKey(us1, us2));
            friendServ.addFriend(friends);
            return "OK";
        } catch (Throwable error) {
            return error.getMessage();
        }
    }
    
    @DeleteMapping("/friends")
    @ResponseBody public String deleteFriends(@RequestBody String user1, @RequestBody String user2) {
        try {
            User us1 = userServ.getUser(user1);
            User us2 = userServ.getUser(user2);
            Friends friends = new Friends(new FriendsCompositeKey(us1, us2));
            friendServ.removeFriend(friends);
            return "OK";
        } catch (Throwable error) {
            return error.getMessage();
        }
    }
    
    @GetMapping("/friends/{login}")
    @ResponseBody public List<User> getUsersFriends(@PathVariable String login) {
        User user = userServ.getUser(login);
        return friendServ.getUsersFriends(user);
    }
    
    @PostMapping("/friends/requests")
    @ResponseBody public String addRequest (@RequestBody String senderName, @RequestBody String receiverName) {
        try {
            User sender = userServ.getUser(senderName);
            User receiver = userServ.getUser(receiverName);
            FriendsRequest req = new FriendsRequest(new FriendRequestCompositeKey(sender, receiver));
            requestServ.addRequest(req);
            return "OK";
        } catch (Throwable error) {
            return error.getMessage();
        }
    }
    
    @DeleteMapping("/friends/requests")
    @ResponseBody public String deleteRequest (@RequestBody String senderName, @RequestBody String receiverName) {
        try {
            User sender = userServ.getUser(senderName);
            User receiver = userServ.getUser(receiverName);
            FriendRequestCompositeKey req = new FriendRequestCompositeKey(sender, receiver);
            requestServ.removeRequest(req);
            return "OK";
        } catch (Throwable error) {
            return error.getMessage();
        }
    }
    
    @GetMapping("/friends/requests/outgoing/{login}")
    @ResponseBody public List<User> getOutgoingRequests (@PathVariable String login) {
        User sender = userServ.getUser(login);
        return requestServ.requestedUsers(sender);
    }
    
    @GetMapping("/friends/requests/incoming/{login}")
    @ResponseBody public List<User> getIncomingRequests (@PathVariable String login) {
        User receiver = userServ.getUser(login);
        return requestServ.requestingUsers(receiver);
    }
    
}
