package server.websocket;

import com.google.gson.Gson;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import model.Pet;
import server.PetEventObserver;
import webSocketMessages.Action;
import webSocketMessages.Notification;

public class WebSocketObserver implements PetEventObserver, WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections;

    public WebSocketObserver(ConnectionManager connections) {
        this.connections = connections;
    }

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        Action action = new Gson().fromJson(ctx.message(), Action.class);
        var connection = new SessionConnection(ctx.session);
        switch (action.type()) {
            case ENTER -> enter(action.visitorName(), connection);
            case EXIT -> leave(connection);
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
        leave(new SessionConnection(ctx.session));
    }

    private void enter(String visitorName, Connection connection) {
        connections.add(connection, visitorName);
        var message = String.format("%s is in the shop", visitorName);
        var notification = new Notification(Notification.Type.ARRIVAL, message);
        connections.broadcastAll(connection, notification);
    }

    /**
     * Used by both an explicit EXIT and a dropped socket. The name comes from the
     * recorded ENTER rather than the client, and nothing is announced for a
     * connection that never entered or has already left.
     */
    private void leave(Connection connection) {
        String visitorName = connections.remove(connection);
        if (visitorName != null) {
            var message = String.format("%s left the shop", visitorName);
            var notification = new Notification(Notification.Type.DEPARTURE, message);
            connections.broadcastAll(notification);
        }
    }

    @Override
    public void makeNoise(Pet pet) {
        var message = String.format("%s says %s", pet.name(), pet.sound());
        var notification = new Notification(Notification.Type.NOISE, message);
        connections.broadcastAll(notification);
    }
}
