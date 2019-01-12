package com.p3212.Configurations;

import com.p3212.EntityClasses.StompPrincipal;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketsController {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @MessageMapping("/send/message")
    public void notify(String message) {
        messagingTemplate.convertAndSend("/chat", message+" - "+ new SimpleDateFormat("HH:mm:ss").format(new Date()));
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
    
    public void sendInvitation(String username, String author, String type) {
        String message = type+":"+author;
        Principal principal = new StompPrincipal(username);
        messagingTemplate.convertAndSendToUser(principal.getName(), "/invite", message);
    }
    
}
