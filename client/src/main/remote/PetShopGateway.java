package remote;

import exception.ResponseException;
import model.Pet;

import java.util.List;

/**
 * The operations the client needs from the pet shop, independent of how they are carried out
 * (HTTP and WebSocket in production, in memory for tests).
 */
public interface PetShopGateway {
    void enterPetShop(String visitorName) throws ResponseException;

    void leavePetShop(String visitorName) throws ResponseException;

    Pet rescuePet(Pet pet) throws ResponseException;

    List<Pet> listPets() throws ResponseException;

    void adoptPet(Pet pet) throws ResponseException;

    void adoptAllPets() throws ResponseException;
}
