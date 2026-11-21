package server.websocket;

import java.io.IOException;

/**
 * The part of a WebSocket connection that ConnectionManager needs. Keeping this small
 * decouples the manager from Jetty and lets tests supply their own connections.
 *
 * Implementations must define equals/hashCode so that the same underlying socket
 * always compares equal, because ConnectionManager uses connections as map keys.
 */
public interface Connection {
    boolean isOpen();

    void send(String message) throws IOException;
}
