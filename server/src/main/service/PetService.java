package service;

import dataaccess.DataAccess;
import model.*;
import exception.ResponseException;
import model.PetType;
import server.PetEventObserver;

import java.util.Collection;

public class PetService {

    private final DataAccess dataAccess;
    private final PetEventObserver petEventObserver;

    public PetService(DataAccess dataAccess, PetEventObserver petEventObserver) {
        this.dataAccess = dataAccess;
        this.petEventObserver = petEventObserver;
    }

    // Pet Shop is very simple.
    // A more complicated application would do the business logic in the service.

    public Pet addPet(Pet pet) throws ResponseException {
        if (pet.type() == PetType.DOG && pet.name().equals("fleas")) {
            throw new ResponseException(ResponseException.Code.ClientError, "Error: no dogs with fleas");
        }

        petEventObserver.makeNoise(pet);
        return dataAccess.addPet(pet);
    }

    public PetList listPets() throws ResponseException {
        return dataAccess.listPets();
    }

    public Pet getPet(int id) throws ResponseException {
        validateId(id);
        return dataAccess.getPet(id);
    }

    public void deletePet(Pet pet) throws ResponseException {
        validateId(pet.id());

        petEventObserver.makeNoise(pet);
        dataAccess.deletePet(pet.id());
    }

    public void deleteAllPets() throws ResponseException {
        Collection<Pet> pets = dataAccess.listPets();
        if (!pets.isEmpty()) {
            dataAccess.deleteAllPets();
        }
    }

    private void validateId(int id) throws ResponseException {
        if (id <= 0) {
            throw new ResponseException(ResponseException.Code.ClientError, "Error: invalid pet ID");
        }
    }
}
