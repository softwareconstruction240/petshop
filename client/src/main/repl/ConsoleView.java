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

    public void welcome() {
        print(LOGO + YELLOW + " Welcome to the pet store." + RESET + " SignIn <yourname> or ask for help.\n");
        printPrompt(RESET, "");
    }

    public void success(String message) {
        printPrompt(BLUE, message);
    }

    public void usage(String message) {
        printPrompt(BLUE, message);
    }

    public void error(String message) {
        printPrompt(RED, message);
    }

    public void goodbye() {
        print(GREEN + "\nGoodbye!\n" + RESET);
    }

    @Override
    public void notify(Notification notification) {
        print("\n" + BLUE + "<<< " + notification.message());
        print("\n" + RESET + ">>> ");
    }

    private void printPrompt(String color, String message) {
        print(color + message);
        print("\n" + RESET + ">>> ");
    }

    // Synchronized so we don't overlay messages from notifications
    private synchronized void print(String msg) {
        out.print(msg);
    }
}
