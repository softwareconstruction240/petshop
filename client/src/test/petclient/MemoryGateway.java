package petclient;

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
    public Pet addPet(Pet pet) {
        Pet added = pet.setId(nextId++);
        pets.put(added.id(), added);
        return added;
    }

    @Override
    public List<Pet> listPets() {
        return new ArrayList<>(pets.values());
    }

    @Override
    public void deletePet(int id) {
        pets.remove(id);
    }

    @Override
    public void deleteAllPets() {
        pets.clear();
    }
}
