package remote;

import webSocketMessages.Notification;

public interface NotificationHandler {
    void notify(Notification notification);
}
