import petclient.PetClient;
import remote.RemoteGateway;
import repl.ConsoleView;
import repl.Repl;

import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) {
        String serverUrl = "http://localhost:8080";
        if (args.length == 1) {
            serverUrl = args[0];
        }

        try {
            // Use dependency inversion so that this is the only place that knows which concrete classes are used.
            var view = new ConsoleView(System.out);
            var remote = new RemoteGateway(serverUrl, view);
            var client = new PetClient(remote);
            new Repl(client, view).run(new Scanner(System.in));

        } catch (Throwable ex) {
            System.out.printf("Unable to start client: %s%n", ex.getMessage());
        }
    }

}
