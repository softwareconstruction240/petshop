package server.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import webSocketMessages.Notification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionManagerTest {
    private static final Notification HELLO = new Notification(Notification.Type.INFO, "hello");

    private ConnectionManager manager;

    @BeforeEach
    void setUp() {
        manager = new ConnectionManager();
    }

    @Test
    void broadcastReachesEveryone() {
        var joe = new FakeConnection();
        var sally = new FakeConnection();
        manager.add(joe, "joe");
        manager.add(sally, "sally");

        manager.broadcastAll(HELLO);

        assertEquals(List.of(HELLO.toJson()), joe.received);
        assertEquals(List.of(HELLO.toJson()), sally.received);
    }

    @Test
    void broadcastSkipsExcludedConnection() {
        var joe = new FakeConnection();
        var sally = new FakeConnection();
        manager.add(joe, "joe");
        manager.add(sally, "sally");

        manager.broadcastAll(joe, HELLO);

        assertTrue(joe.received.isEmpty());
        assertEquals(List.of(HELLO.toJson()), sally.received);
    }

    @Test
    void failedConnectionDoesNotStopOthersAndIsDropped() {
        var broken = new FakeConnection();
        broken.failOnSend = true;
        var sally = new FakeConnection();
        manager.add(broken, "broken");
        manager.add(sally, "sally");

        assertDoesNotThrow(() -> manager.broadcastAll(HELLO));

        assertEquals(List.of(HELLO.toJson()), sally.received);
        assertNull(manager.remove(broken), "failed connection should have been dropped");
    }

    @Test
    void closedConnectionIsDropped() {
        var closed = new FakeConnection();
        closed.open = false;
        manager.add(closed, "closed");

        manager.broadcastAll(HELLO);

        assertTrue(closed.received.isEmpty());
        assertNull(manager.remove(closed));
    }

    @Test
    void removeReturnsVisitorNameOnce() {
        var joe = new FakeConnection();
        manager.add(joe, "joe");

        assertEquals("joe", manager.remove(joe));
        assertNull(manager.remove(joe));
    }

    private static class FakeConnection implements Connection {
        final List<String> received = new ArrayList<>();
        boolean open = true;
        boolean failOnSend = false;

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        public void send(String message) throws IOException {
            if (failOnSend) {
                throw new IOException("connection reset");
            }
            received.add(message);
        }
    }
}
