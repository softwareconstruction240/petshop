package service;

import dataaccess.MemoryDataAccess;
import exception.ResponseException;
import model.Pet;
import model.PetType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PetServiceTest {
    private RecordingObserver events;
    private PetService service;

    @BeforeEach
    void setUp() {
        events = new RecordingObserver();
        service = new PetService(new MemoryDataAccess(), events);
    }

    @Test
    void addPet() throws ResponseException {
        var pet = new Pet(0, "joe", PetType.FISH);
        pet = service.addPet(pet);

        Collection<Pet> pets = service.listPets();
        assertEquals(1, pets.size());
        assertTrue(pets.contains(pet));
    }

    @Test
    void listPets() throws ResponseException {
        List<Pet> expected = new ArrayList<>();
        expected.add(service.addPet(new Pet(0, "joe", PetType.FISH)));
        expected.add(service.addPet(new Pet(0, "sally", PetType.CAT)));
        expected.add(service.addPet(new Pet(0, "fido", PetType.DOG)));

        Collection<Pet> actual = service.listPets();
        assertIterableEquals(expected, actual);
    }

    @Test
    void deletePet() throws ResponseException {
        List<Pet> expected = new ArrayList<>();
        Pet pet = service.addPet(new Pet(0, "joe", PetType.FISH));
        expected.add(service.addPet(new Pet(0, "sally", PetType.CAT)));
        expected.add(service.addPet(new Pet(0, "fido", PetType.DOG)));

        assertEquals(pet, service.deletePet(pet.id()));
        Collection<Pet> actual = service.listPets();
        assertIterableEquals(expected, actual);
    }

    @Test
    void deleteUnknownPet() {
        var ex = assertThrows(ResponseException.class, () -> service.deletePet(999));
        assertEquals(ResponseException.Code.NotFound, ex.code());
        assertTrue(events.adopted.isEmpty());
    }

    @Test
    void deleteInvalidId() {
        var ex = assertThrows(ResponseException.class, () -> service.deletePet(0));
        assertEquals(ResponseException.Code.ClientError, ex.code());
    }

    @Test
    void deleteAllPets() throws ResponseException {
        service.addPet(new Pet(0, "joe", PetType.FISH));
        service.addPet(new Pet(0, "sally", PetType.CAT));
        service.addPet(new Pet(0, "fido", PetType.DOG));

        service.deleteAllPets();
        assertEquals(0, service.listPets().size());
    }

    @Test
    void noDogsWithFleas() {
        assertThrows(ResponseException.class, () ->
                service.addPet(new Pet(0, "fleas", PetType.DOG)));
        assertTrue(events.added.isEmpty());
    }

    @Test
    void invalidPetsRejected() {
        for (Pet pet : new Pet[]{null, new Pet(0, null, PetType.CAT), new Pet(0, " ", PetType.CAT), new Pet(0, "joe", null)}) {
            var ex = assertThrows(ResponseException.class, () -> service.addPet(pet));
            assertEquals(ResponseException.Code.ClientError, ex.code());
        }
        assertTrue(events.added.isEmpty());
    }

    @Test
    void eventsReportWhatHappened() throws ResponseException {
        Pet betty = service.addPet(new Pet(0, "betty", PetType.DOG));
        service.deletePet(betty.id());

        assertEquals(List.of(betty), events.added);
        assertEquals(List.of(betty), events.adopted);
    }

    private static class RecordingObserver implements PetEventObserver {
        final List<Pet> added = new ArrayList<>();
        final List<Pet> adopted = new ArrayList<>();

        @Override
        public void petAdded(Pet pet) {
            added.add(pet);
        }

        @Override
        public void petAdopted(Pet pet) {
            adopted.add(pet);
        }
    }
}
