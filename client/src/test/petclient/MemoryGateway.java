package petclient;

import exception.ResponseException;
import model.Pet;
import remote.PetShopGateway;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory stand-in for the server so PetClient can be tested without a network.
 */
class MemoryGateway implements PetShopGateway {
    final Map<Integer, Pet> pets = new LinkedHashMap<>();
    final List<String> visitors = new ArrayList<>();
    private int nextId = 1;

    @Override
    public void enterPetShop(String visitorName) {
        visitors.add(visitorName);
    }

    @Override
    public void leavePetShop(String visitorName) {
        visitors.remove(visitorName);
    }

    @Override
    public Pet rescuePet(Pet pet) {
        Pet rescued = pet.setId(nextId++);
        pets.put(rescued.id(), rescued);
        return rescued;
    }

    @Override
    public List<Pet> listPets() {
        return new ArrayList<>(pets.values());
    }

    @Override
    public void adoptPet(Pet pet) throws ResponseException {
        if (pets.remove(pet.id()) == null) {
            throw new ResponseException(ResponseException.Code.NotFound, "Error: pet " + pet.id() + " not found");
        }
    }

    @Override
    public void adoptAllPets() {
        pets.clear();
    }
}
