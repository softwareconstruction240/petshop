package petclient;

import com.google.gson.Gson;
import model.*;
import exception.ResponseException;
import remote.NotificationObserver;
import remote.Remote;

public class PetClient {
    private String visitorName = null;
    private final Remote remote;
    private State state = State.SIGNEDOUT;


    public PetClient(String serverUrl, NotificationObserver notificationObserver) throws ResponseException {
        remote = new Remote(serverUrl, notificationObserver);
    }

    public boolean isSignedIn() {
        return state == State.SIGNEDIN;
    }

    public String signIn(String... params) throws ResponseException {
        visitorName = String.join("-", params);
        remote.enterPetShop(visitorName);
        state = State.SIGNEDIN;
        return String.format("You signed in as %s.", visitorName);
    }

    public String rescuePet(String... params) throws ResponseException {
        assertSignedIn();
        String name = params[0];
        PetType type = PetType.valueOf(params[1].toUpperCase());
        var pet = remote.addPet(new Pet(0, name, type));
        return String.format("You rescued %s. Assigned ID: %d", pet.name(), pet.id());
    }

    public String listPets() throws ResponseException {
        assertSignedIn();
        var result = new StringBuilder();
        var gson = new Gson();
        for (Pet pet : remote.listPets()) {
            result.append(gson.toJson(pet)).append('\n');
        }
        return result.toString();
    }

    public String adoptPet(String... params) throws ResponseException {
        assertSignedIn();
        try {
            int id = Integer.parseInt(params[0]);
            Pet pet = getPet(id);
            if (pet != null) {
                remote.deletePet(id);
                return String.format("%s says %s", pet.name(), pet.sound());
            }
        } catch (NumberFormatException ignored) {
        }
        throw new ResponseException(ResponseException.Code.ClientError, "Expected: <pet id>");
    }

    public String adoptAllPets() throws ResponseException {
        assertSignedIn();
        var buffer = new StringBuilder();
        for (Pet pet : remote.listPets()) {
            buffer.append(String.format("%s says %s%n", pet.name(), pet.sound()));
        }

        remote.deleteAllPets();
        return buffer.toString();
    }

    public String signOut() throws ResponseException {
        assertSignedIn();
        remote.leavePetShop(visitorName);
        state = State.SIGNEDOUT;
        return String.format("%s left the shop", visitorName);
    }

    private Pet getPet(int id) throws ResponseException {
        for (Pet pet : remote.listPets()) {
            if (pet.id() == id) {
                return pet;
            }
        }
        return null;
    }

    private void assertSignedIn() throws ResponseException {
        if (state == State.SIGNEDOUT) {
            throw new ResponseException(ResponseException.Code.ClientError, "You must sign in");
        }
    }

}
