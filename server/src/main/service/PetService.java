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

    public Pet rescuePet(Pet pet) throws ResponseException {
        validatePet(pet);
        if (pet.type() == PetType.DOG && pet.name().equals("fleas")) {
            throw new ResponseException(ResponseException.Code.ClientError, "Error: no dogs with fleas");
        }

        Pet rescued = dataAccess.addPet(pet);
        petEventObserver.petRescued(rescued);
        return rescued;
    }

    public PetList listPets() throws ResponseException {
        return dataAccess.listPets();
    }

    public Pet getPet(int petId) throws ResponseException {
        validatePetId(petId);
        Pet pet = dataAccess.getPet(petId);
        if (pet == null) {
            throw new ResponseException(ResponseException.Code.NotFound, String.format("Error: pet %d not found", petId));
        }
        return pet;
    }

    public Pet adoptPet(int petId) throws ResponseException {
        Pet pet = getPet(petId);
        dataAccess.deletePet(petId);
        petEventObserver.petAdopted(pet);
        return pet;
    }

    public void adoptAllPets() throws ResponseException {
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

    private void validatePetId(int petId) throws ResponseException {
        if (petId <= 0) {
            throw new ResponseException(ResponseException.Code.ClientError, "Error: invalid pet ID");
        }
    }
}
