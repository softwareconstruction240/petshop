package service;

import dataaccess.DataAccess;
import model.*;
import exception.ResponseException;

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
        validatePet(pet);
        if (pet.type() == PetType.DOG && pet.name().equals("fleas")) {
            throw new ResponseException(ResponseException.Code.ClientError, "Error: no dogs with fleas");
        }

        Pet added = dataAccess.addPet(pet);
        petEventObserver.petAdded(added);
        return added;
    }

    public PetList listPets() throws ResponseException {
        return dataAccess.listPets();
    }

    public Pet getPet(int id) throws ResponseException {
        validateId(id);
        Pet pet = dataAccess.getPet(id);
        if (pet == null) {
            throw new ResponseException(ResponseException.Code.NotFound, String.format("Error: pet %d not found", id));
        }
        return pet;
    }

    public Pet deletePet(int id) throws ResponseException {
        Pet pet = getPet(id);
        dataAccess.deletePet(id);
        petEventObserver.petAdopted(pet);
        return pet;
    }

    public void deleteAllPets() throws ResponseException {
        dataAccess.deleteAllPets();
    }

    private void validatePet(Pet pet) throws ResponseException {
        if (pet == null) {
            throw new ResponseException(ResponseException.Code.ClientError, "Error: missing pet");
        }
        if (pet.name() == null || pet.name().isBlank()) {
            throw new ResponseException(ResponseException.Code.ClientError, "Error: pet name is required");
        }
        if (pet.type() == null) {
            throw new ResponseException(ResponseException.Code.ClientError, "Error: pet type is required");
        }
    }

    private void validateId(int id) throws ResponseException {
        if (id <= 0) {
            throw new ResponseException(ResponseException.Code.ClientError, "Error: invalid pet ID");
        }
    }
}
