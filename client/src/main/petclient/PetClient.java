package petclient;

import model.*;
import exception.ResponseException;
import remote.PetShopGateway;

import java.util.List;

public class PetClient {
    private String visitorName = null;
    private final PetShopGateway remote;
    private State state = State.SIGNEDOUT;
    private List<Pet> listedPets = List.of();


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
        return remote.rescuePet(new Pet(0, name, type));
    }

    /**
     * Returns the current pets and remembers them, so that adoptPet can refer to a pet
     * by its position in the list the user was shown.
     */
    public List<Pet> listPets() throws ResponseException {
        assertSignedIn();
        listedPets = List.copyOf(remote.listPets());
        return listedPets;
    }

    /**
     * Adopts the pet at the given 1-based position in the most recent listPets result.
     * The remembered list isn't changed by adopting, so positions keep matching what the
     * user saw until they list again.
     */
    public Pet adoptPet(int position) throws ResponseException {
        assertSignedIn();
        if (listedPets.isEmpty()) {
            throw new ResponseException(ResponseException.Code.ClientError, "List the pets before adopting one");
        }
        if (position < 1 || position > listedPets.size()) {
            throw new ResponseException(ResponseException.Code.ClientError, "Pet position must match something in the list");
        }

        Pet pet = listedPets.get(position - 1);
        try {
            remote.adoptPet(pet);
        } catch (ResponseException ex) {
            if (ex.code() == ResponseException.Code.NotFound) {
                throw new ResponseException(ResponseException.Code.ClientError,
                        String.format("%s has already been adopted", pet.name()));
            }
            throw ex;
        }
        return pet;
    }

    public List<Pet> adoptAllPets() throws ResponseException {
        assertSignedIn();
        List<Pet> pets = remote.listPets();
        remote.adoptAllPets();
        listedPets = List.of();
        return pets;
    }

    public void signOut() throws ResponseException {
        assertSignedIn();
        remote.leavePetShop(visitorName);
        state = State.SIGNEDOUT;
        listedPets = List.of();
    }

    private void assertSignedIn() throws ResponseException {
        if (state == State.SIGNEDOUT) {
            throw new ResponseException(ResponseException.Code.ClientError, "You must sign in");
        }
    }

}
