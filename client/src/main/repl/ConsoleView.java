package repl;

import remote.NotificationObserver;
import webSocketMessages.Notification;

import java.io.PrintStream;

import static petclient.EscapeSequences.*;

/**
 * Writes to the terminal. Notifications arrive on the WebSocket thread while the REPL
 * writes from the main thread, so every write is synchronized to keep output from interleaving.
 */
public class ConsoleView implements NotificationObserver {
    private final PrintStream out;

    public ConsoleView(PrintStream out) {
        this.out = out;
    }

    public synchronized void welcome() {
        out.println(LOGO + " Welcome to the pet store. Sign in to start or ask for help.");
        printPrompt(RESET, "");
    }

    public void success(String message) {
        printPrompt(GREEN, message);
    }

    public void usage(String message) {
        printPrompt(BLUE, message);
    }

    public void error(String message) {
        printPrompt(RED, message);
    }

    public synchronized void goodbye() {
        out.println(RESET);
    }

    @Override
    public void notify(Notification notification) {
        out.print("\n" + BLUE + "<<< " + notification.message());
        out.print("\n" + RESET + ">>> " + GREEN);
    }

    private synchronized void printPrompt(String color, String message) {
        out.print(color + message);
        out.print("\n" + RESET + ">>> " + GREEN);
    }
}
