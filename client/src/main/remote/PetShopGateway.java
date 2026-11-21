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

    Pet addPet(Pet pet) throws ResponseException;

    List<Pet> listPets() throws ResponseException;

    void deletePet(int id) throws ResponseException;

    void deleteAllPets() throws ResponseException;
}
