package server;

import exception.ResponseException;
import model.Pet;

public interface PetEventObserver {
    void makeNoise(Pet pet) throws ResponseException;
}
