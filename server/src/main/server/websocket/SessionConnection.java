package server.websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

/**
 * Adapts a Jetty WebSocket session to the Connection interface. As a record, two wrappers
 * around the same session are equal, so a new wrapper per event still finds its map entry.
 */
public record SessionConnection(Session session) implements Connection {

    @Override
    public boolean isOpen() {
        return session.isOpen();
    }

    @Override
    public void send(String message) throws IOException {
        // Blocking sends must not overlap on one session. Broadcasts can come from
        // both HTTP threads and WebSocket threads, so lock on the shared session.
        synchronized (session) {
            session.getRemote().sendString(message);
        }
    }
}
