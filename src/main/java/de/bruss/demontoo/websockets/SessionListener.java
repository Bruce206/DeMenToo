package de.bruss.demontoo.websockets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.WebSocketMessageBrokerStats;
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

	@Autowired
	WebSocketMessageBrokerStats webSocketMessageBrokerStats;

    @EventListener(SessionConnectedEvent.class)
    @Transactional
    public void connected(SessionConnectedEvent event) {
		String stats = webSocketMessageBrokerStats.getWebSocketSessionStatsInfo();

        currentUsers = Integer.parseInt(stats.substring(0, stats.indexOf(" c")));
        instanceHealthChecker.checkHealthStatus();

		logger.info("Connected: " + String.valueOf(event.getTimestamp()) + " | currentUsers: " + currentUsers);
		logger.info("--Connected by Stats: " + webSocketMessageBrokerStats.getWebSocketSessionStatsInfo());
    }

    @EventListener(SessionDisconnectEvent.class)
    public void disconnected(SessionDisconnectEvent event) {
		String stats = webSocketMessageBrokerStats.getWebSocketSessionStatsInfo();
		currentUsers = Integer.parseInt(stats.substring(0, stats.indexOf(" c")));
        logger.info("Disconnected: " + String.valueOf(event.getTimestamp()) + " | currentUsers: " + currentUsers);
		logger.info("--Disconnected by Stats: " + webSocketMessageBrokerStats.getWebSocketSessionStatsInfo());
    }



    public int getCurrentUsers() {
        return currentUsers;
    }
}
