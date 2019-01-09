package com.p3212.Configurations;

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
    
    @MessageMapping("/send/msg")
    public void send(Principal principal, String message) {
        messagingTemplate.convertAndSendToUser(principal.getName(), "/msg", message);
    }
    
    @MessageMapping("/send/online")
    public void sendOnline(String message) {
        messagingTemplate.convertAndSend("/online", message);
    }
    
    @MessageMapping("/send/social")
    public void sendSocial(Principal principal, String message) {
        messagingTemplate.convertAndSendToUser(principal.getName(), "/social", message);
    }
    
}
