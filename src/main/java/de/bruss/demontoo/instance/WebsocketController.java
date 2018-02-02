package de.bruss.demontoo.instance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebsocketController {

    @Autowired
    private SimpMessagingTemplate template;

    @MessageMapping("/send/message")
    public void send(String message) {
        this.template.convertAndSend("/chat", "Hallo Welt - " + message);
    }
}
