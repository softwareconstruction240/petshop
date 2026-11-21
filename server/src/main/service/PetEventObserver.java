package service;

import model.Pet;

/**
 * Tells interested parties what happened to a pet. PetService owns this interface because it
 * describes what the service needs. Outer layers (such as the WebSocket handler) implement it,
 * so the service never depends on them.
 *
 * Events describe what happened, not how to react. Each observer decides what to do.
 */
public interface PetEventObserver {
    void petRescued(Pet pet);

    void petAdopted(Pet pet);
}
