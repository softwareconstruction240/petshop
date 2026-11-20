package repl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import exception.ResponseException;
import petclient.PetClient;
import remote.NotificationHandler;
import webSocketMessages.Notification;

import static petclient.EscapeSequences.*;

public class Repl implements NotificationHandler {
    private final PetClient client;
    private final Map<String, Command> commands = new HashMap<>();

    public Repl(String serverUrl) throws ResponseException {
        client = new PetClient(serverUrl, this);

        commands.put("signin", new Command(1, "Expected: <yourname>", client::signIn));
        commands.put("rescue", new Command(2, "Expected: <name> <CAT|DOG|FROG|FISH>", client::rescuePet));
        commands.put("adopt", new Command(1, "Expected: <pet id>", client::adoptPet));
        commands.put("list", new Command(0, "", params -> client.listPets()));
        commands.put("adoptall", new Command(0, "", params -> client.adoptAllPets()));
        commands.put("signout", new Command(0, "", params -> client.signOut()));
        commands.put("quit", new Command(0, "", params -> "quit"));
    }

    public void run() {
        System.out.println(LOGO + " Welcome to the pet store. Sign in to start.");
        System.out.print(help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
                System.out.print(BLUE + result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    public String eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String name = (tokens.length > 0) ? tokens[0] : "";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);

            Command command = commands.get(name);
            if (command == null) {
                return help();
            }
            if (params.length < command.requiredParams()) {
                return command.usage();
            }
            return command.handler().run(params);
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
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

    public void notify(Notification notification) {
        System.out.println(RED + notification.message());
        printPrompt();
    }

    private void printPrompt() {
        System.out.print("\n" + RESET + ">>> " + GREEN);
    }

    @FunctionalInterface
    private interface CommandHandler {
        String run(String[] params) throws ResponseException;
    }

    private record Command(int requiredParams, String usage, CommandHandler handler) {
    }
}
