package de.bruss.demontoo.websockets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import javax.transaction.Transactional;

@Component
public class SessionListener {

    private final Logger logger = LoggerFactory.getLogger(SessionListener.class);

    int currentUsers = 0;

    @Autowired
    @Lazy
    private InstanceHealthChecker instanceHealthChecker;

    @EventListener(SessionConnectedEvent.class)
    @Transactional
    public void connected(SessionConnectedEvent event) {
        logger.info("Connected: " + String.valueOf(event.getTimestamp()) + " | currentUsers: " + currentUsers);
        currentUsers++;
        instanceHealthChecker.checkHealthStatus();
    }

    @EventListener(SessionDisconnectEvent.class)
    public void disconnected(SessionDisconnectEvent event) {
        currentUsers--;
        logger.info("Disconnected: " + String.valueOf(event.getTimestamp()) + " | currentUsers: " + currentUsers);
    }

    public int getCurrentUsers() {
        return currentUsers;
    }
}
