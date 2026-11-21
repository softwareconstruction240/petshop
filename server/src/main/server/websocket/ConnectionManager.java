package server.websocket;

import webSocketMessages.Notification;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which visitor is on each connection and broadcasts notifications to them.
 * Broadcasting is best effort: a failed connection is dropped and never stops the others.
 */
public class ConnectionManager {
    private final Map<Connection, String> visitors = new ConcurrentHashMap<>();

    public void add(Connection connection, String visitorName) {
        visitors.put(connection, visitorName);
    }

    /**
     * @return the visitor's name, or null if the connection never entered
     */
    public String remove(Connection connection) {
        return visitors.remove(connection);
    }

    public void broadcastAll(Notification notification) {
        broadcastAll(null, notification);
    }

    public void broadcastAll(Connection excludeConnection, Notification notification) {
        String msg = notification.toJson();
        for (Connection connection : visitors.keySet()) {
            if (connection.equals(excludeConnection)) {
                continue;
            }
            if (!connection.isOpen()) {
                visitors.remove(connection);
                continue;
            }
            try {
                connection.send(msg);
            } catch (IOException ex) {
                visitors.remove(connection);
            }
        }
    }
}
