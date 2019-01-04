package com.p3212.Configurations;

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
    
}
