package com.p3212.Configurations;

import com.p3212.EntityClasses.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public Message greeting(Message message) throws Exception {
        return message;
    }

}
