package dataaccess;

import model.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Jetty serves requests on multiple threads, so every method is synchronized.
 * A LinkedHashMap keeps pets in the order they were added.
 */
public class MemoryDataAccess implements DataAccess {
    private int nextId = 1;
    final private Map<Integer, Pet> pets = new LinkedHashMap<>();

    public synchronized Pet addPet(Pet pet) {
        pet = new Pet(nextId++, pet.name(), pet.type());

        pets.put(pet.id(), pet);
        return pet;
    }

    public synchronized PetList listPets() {
        return new PetList(pets.values());
    }


    public synchronized Pet getPet(int id) {
        return pets.get(id);
    }

    public synchronized void deletePet(Integer id) {
        pets.remove(id);
    }

    public synchronized void deleteAllPets() {
        pets.clear();
    }
}
