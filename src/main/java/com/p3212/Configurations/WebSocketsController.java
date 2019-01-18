package com.p3212.Configurations;

import com.p3212.EntityClasses.State;
import com.p3212.EntityClasses.StompPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class WebSocketsController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/send/message")
    public void notify(String message) {
        if (message.substring(message.length()-4, message.length()).equals("test")) {
            State state = new State("Attacker", "Victim", "Air Strike", 100, 120, 70, false, false, "nextAttacker");
            sendFightState(state, message.substring(0, message.length()-6));
        }
        messagingTemplate.convertAndSend("/chat", message + " - " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
    }

    public void send(Principal principal, String message) {
        messagingTemplate.convertAndSendToUser(principal.getName(), "/msg", message);
    }

    public void sendOnline(String message) {
        messagingTemplate.convertAndSend("/online", message);
    }

    public void sendSocial(Principal principal, String message) {
        messagingTemplate.convertAndSendToUser(principal.getName(), "/social", message);
    }

    public void sendAdmin(String username) {
        messagingTemplate.convertAndSend("/admin/admins", username);
    }

    public void sendInvitation(String username, String author, String type, int id) {
        String message = type + ":" + author + ":" + id;
        Principal principal = new StompPrincipal(username);
        messagingTemplate.convertAndSendToUser(principal.getName(), "/invite", message);
    }

    public void sendApproval(String author, String username, int id) {
        String message = username + ":" + id;
        Principal principal = new StompPrincipal(author);
        messagingTemplate.convertAndSendToUser(principal.getName(), "/approval", message);
    }

    public void sendStart(String author, String username, int id) {
        String message = author + ":" + id;
        Principal principal = new StompPrincipal(username);
        messagingTemplate.convertAndSendToUser(principal.getName(), "/start", message);
    }

    public void sendFightState (State state, String username) {
        messagingTemplate.convertAndSendToUser(username, "/fightState", state);
    }
    
}
