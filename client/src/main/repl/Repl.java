package repl;

import java.util.*;
import java.util.stream.Collectors;

import exception.ResponseException;
import model.Pet;
import model.PetType;
import petclient.PetClient;

public class Repl {
    private final PetClient client;
    private final ConsoleView view;
    private final CommandManager commandManager;
    private boolean running = true;

    public Repl(PetClient client, ConsoleView view) {
        this.client = client;
        this.view = view;
        this.commandManager = new CommandManager();
    }

    public void run(Scanner scanner) {
        view.welcome();
        while (running) {
            String line = scanner.nextLine();

            try {
                eval(line);
            } catch (Throwable e) {
                view.error(e.toString());
            }
        }
        view.goodbye();
    }

    public void eval(String input) {
        try {
            String[] tokens = input.trim().split("\\s+");
            String name = (tokens.length > 0) ? tokens[0].toLowerCase() : "";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            var command = commandManager.get(name);
            if (params.length < command.requiredParams()) {
                view.usage(command.usage());
            } else {
                view.success(command.handler().run(params));
            }
        } catch (ResponseException ex) {
            view.error(ex.getMessage());
        }
    }

    private class CommandManager {
        private final Map<String, Command> commands = new HashMap<>();

        private record Command(int requiredParams, String usage, CommandHandler handler) {
        }

        private static final String PET_TYPES = Arrays.stream(PetType.values())
                .map(Enum::name)
                .collect(Collectors.joining("|"));

        public CommandManager() {
            commands.put("signin", new Command(1, "Expected: <yourname>", this::signIn));
            commands.put("rescue", new Command(2, "Expected: <name> <" + PET_TYPES + ">", this::rescue));
            commands.put("adopt", new Command(1, "Expected: <pet id>", this::adopt));
            commands.put("list", new Command(0, "", params -> list()));
            commands.put("adoptall", new Command(0, "", params -> adoptAll()));
            commands.put("signout", new Command(0, "", params -> signOut()));
            commands.put("help", new Command(0, "", params -> help()));
            commands.put("quit", new Command(0, "", params -> {
                running = false;
                return "";
            }));
        }

        private String signIn(String[] params) throws ResponseException {
            client.signIn(String.join("-", params));
            return String.format("You signed in as %s.", client.getVisitorName());
        }

        private String rescue(String[] params) throws ResponseException {
            Pet pet = client.rescuePet(params[0], parsePetType(params[1]));
            return String.format("You rescued %s. Assigned ID: %d", pet.name(), pet.id());
        }

        private String adopt(String[] params) throws ResponseException {
            Pet pet = client.adoptPet(parsePetId(params[0]));
            return formatAdoption(pet);
        }

        private String list() throws ResponseException {
            List<Pet> pets = client.listPets();
            if (pets.isEmpty()) {
                return "No pets in the shop.";
            }
            var result = new StringBuilder();
            for (Pet pet : pets) {
                result.append(String.format("%4d  %-12s %s%n", pet.id(), pet.name(), pet.type()));
            }
            return result.toString();
        }

        private String adoptAll() throws ResponseException {
            var result = new StringBuilder();
            for (Pet pet : client.adoptAllPets()) {
                result.append(formatAdoption(pet)).append(System.lineSeparator());
            }
            return result.toString();
        }

        private String signOut() throws ResponseException {
            client.signOut();
            return String.format("%s left the shop", client.getVisitorName());
        }

        private static String formatAdoption(Pet pet) {
            return String.format("%s says %s", pet.name(), pet.sound());
        }

        private static int parsePetId(String text) throws ResponseException {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ex) {
                throw new ResponseException(ResponseException.Code.ClientError, "Expected: <pet id>");
            }
        }

        private static PetType parsePetType(String text) throws ResponseException {
            try {
                return PetType.valueOf(text.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new ResponseException(ResponseException.Code.ClientError, "Pet type must be one of " + PET_TYPES);
            }
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
                    - rescue <name> <%s>
                    - adoptAll
                    - signOut
                    - quit
                    """.formatted(PET_TYPES);
        }
    }
}
