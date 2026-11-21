package petclient;

import model.*;
import exception.ResponseException;
import remote.PetShopGateway;

import java.util.List;

public class PetClient {
    private String visitorName = null;
    private final PetShopGateway remote;
    private State state = State.SIGNEDOUT;


    public PetClient(PetShopGateway remote) {
        this.remote = remote;
    }

    public boolean isSignedIn() {
        return state == State.SIGNEDIN;
    }

    public String getVisitorName() {
        return visitorName;
    }

    public void signIn(String visitorName) throws ResponseException {
        remote.enterPetShop(visitorName);
        this.visitorName = visitorName;
        state = State.SIGNEDIN;
    }

    public Pet rescuePet(String name, PetType type) throws ResponseException {
        assertSignedIn();
        return remote.addPet(new Pet(0, name, type));
    }

    public List<Pet> listPets() throws ResponseException {
        assertSignedIn();
        return remote.listPets();
    }

    public Pet adoptPet(int id) throws ResponseException {
        assertSignedIn();
        Pet pet = getPet(id);
        if (pet == null) {
            throw new ResponseException(ResponseException.Code.ClientError, String.format("Pet %d not found", id));
        }
        remote.deletePet(id);
        return pet;
    }

    public List<Pet> adoptAllPets() throws ResponseException {
        assertSignedIn();
        List<Pet> pets = remote.listPets();
        remote.deleteAllPets();
        return pets;
    }

    public void signOut() throws ResponseException {
        assertSignedIn();
        remote.leavePetShop(visitorName);
        state = State.SIGNEDOUT;
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
