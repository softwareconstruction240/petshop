package repl;

import java.util.*;

import exception.ResponseException;
import petclient.PetClient;
import remote.NotificationObserver;
import webSocketMessages.Notification;

import static petclient.EscapeSequences.*;

public class Repl implements NotificationObserver {
    private final PetClient client;
    private final CommandManager commandManager;
    private boolean running = true;

    public Repl(String serverUrl) throws ResponseException {
        client = new PetClient(serverUrl, this);
        commandManager = new CommandManager(client);
    }

    public void run() {
        System.out.println(LOGO + " Welcome to the pet store. Sign in to start or ask for help.");
        printPrompt(RESET, "");

        Scanner scanner = new Scanner(System.in);
        while (running) {
            String line = scanner.nextLine();

            try {
                eval(line);
            } catch (Throwable e) {
                printPrompt(RED, e.toString());
            }
        }
        System.out.println();
    }

    public void eval(String input) {
        try {
            String[] tokens = input.trim().split("\\s+");
            String name = (tokens.length > 0) ? tokens[0].toLowerCase() : "";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            var command = commandManager.get(name);
            if (params.length < command.requiredParams()) {
                printPrompt(BLUE, command.usage());
            } else {
                printPrompt(GREEN, command.handler().run(params));
            }
        } catch (ResponseException ex) {
            printPrompt(RED, ex.getMessage());
        }
    }

    public void notify(Notification notification) {
        printPrompt(GREEN, notification.message());
    }

    synchronized private void printPrompt(String color, String message) {
        System.out.print(color + message);
        System.out.print("\n" + RESET + ">>> " + GREEN);
    }

    private class CommandManager {
        private final Map<String, Command> commands = new HashMap<>();

        private record Command(int requiredParams, String usage, CommandHandler handler) {
        }

        public CommandManager(PetClient client) {
            commands.put("signin", new Command(1, "Expected: <yourname>", client::signIn));
            commands.put("rescue", new Command(2, "Expected: <name> <CAT|DOG|FROG|FISH>", client::rescuePet));
            commands.put("adopt", new Command(1, "Expected: <pet id>", client::adoptPet));
            commands.put("list", new Command(0, "", params -> client.listPets()));
            commands.put("adoptall", new Command(0, "", params -> client.adoptAllPets()));
            commands.put("signout", new Command(0, "", params -> client.signOut()));
            commands.put("help", new Command(0, "", params -> help()));
            commands.put("quit", new Command(0, "", params -> {
                running = false;
                return "";
            }));
        }

        public Command get(String name) {
            Command command = commands.get(name);
            if (command == null) {
                command = commands.get("help");
            }
            return command;
        }

        @FunctionalInterface
        private interface CommandHandler {
            String run(String[] params) throws ResponseException;
        }

        public String help() {
            if (!client.isSignedIn()) {
                return """
                        - signIn <yourname>
                        - quit
                        """;
            }
            return """
                    - list
                    - adopt <pet id>
                    - rescue <name> <CAT|DOG|FROG|FISH>
                    - adoptAll
                    - signOut
                    - quit
                    """;
        }
    }
}
