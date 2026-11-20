package remote;

import webSocketMessages.Notification;

public interface NotificationObserver {
    void notify(Notification notification);
}
