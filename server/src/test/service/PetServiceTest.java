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
    void rescuePet() throws ResponseException {
        var pet = new Pet(0, "joe", PetType.FISH);
        pet = service.rescuePet(pet);

        Collection<Pet> pets = service.listPets();
        assertEquals(1, pets.size());
        assertTrue(pets.contains(pet));
    }

    @Test
    void listPets() throws ResponseException {
        List<Pet> expected = new ArrayList<>();
        expected.add(service.rescuePet(new Pet(0, "joe", PetType.FISH)));
        expected.add(service.rescuePet(new Pet(0, "sally", PetType.CAT)));
        expected.add(service.rescuePet(new Pet(0, "fido", PetType.DOG)));

        Collection<Pet> actual = service.listPets();
        assertIterableEquals(expected, actual);
    }

    @Test
    void adoptPet() throws ResponseException {
        List<Pet> expected = new ArrayList<>();
        Pet pet = service.rescuePet(new Pet(0, "joe", PetType.FISH));
        expected.add(service.rescuePet(new Pet(0, "sally", PetType.CAT)));
        expected.add(service.rescuePet(new Pet(0, "fido", PetType.DOG)));

        assertEquals(pet, service.adoptPet(pet.id()));
        Collection<Pet> actual = service.listPets();
        assertIterableEquals(expected, actual);
    }

    @Test
    void adoptUnknownPet() {
        var ex = assertThrows(ResponseException.class, () -> service.adoptPet(999));
        assertEquals(ResponseException.Code.NotFound, ex.code());
        assertTrue(events.adopted.isEmpty());
    }

    @Test
    void adoptInvalidPetId() {
        var ex = assertThrows(ResponseException.class, () -> service.adoptPet(0));
        assertEquals(ResponseException.Code.ClientError, ex.code());
    }

    @Test
    void adoptAllPets() throws ResponseException {
        Pet joe = service.rescuePet(new Pet(0, "joe", PetType.FISH));
        Pet sally = service.rescuePet(new Pet(0, "sally", PetType.CAT));
        Pet fido = service.rescuePet(new Pet(0, "fido", PetType.DOG));

        assertEquals(List.of(joe, sally, fido), service.adoptAllPets());
        assertEquals(0, service.listPets().size());
        assertEquals(List.of(joe, sally, fido), events.adopted);
    }

    @Test
    void adoptAllWithNoPetsSendsNoEvents() throws ResponseException {
        assertTrue(service.adoptAllPets().isEmpty());
        assertTrue(events.adopted.isEmpty());
    }

    @Test
    void noDogsWithFleas() {
        assertThrows(ResponseException.class, () ->
                service.rescuePet(new Pet(0, "fleas", PetType.DOG)));
        assertTrue(events.rescued.isEmpty());
    }

    @Test
    void invalidPetsRejected() {
        for (Pet pet : new Pet[]{null, new Pet(0, null, PetType.CAT), new Pet(0, " ", PetType.CAT), new Pet(0, "joe", null)}) {
            var ex = assertThrows(ResponseException.class, () -> service.rescuePet(pet));
            assertEquals(ResponseException.Code.ClientError, ex.code());
        }
        assertTrue(events.rescued.isEmpty());
    }

    @Test
    void eventsReportWhatHappened() throws ResponseException {
        Pet betty = service.rescuePet(new Pet(0, "betty", PetType.DOG));
        service.adoptPet(betty.id());

        assertEquals(List.of(betty), events.rescued);
        assertEquals(List.of(betty), events.adopted);
    }

    private static class RecordingObserver implements PetEventObserver {
        final List<Pet> rescued = new ArrayList<>();
        final List<Pet> adopted = new ArrayList<>();

        @Override
        public void petRescued(Pet pet) {
            rescued.add(pet);
        }

        @Override
        public void petAdopted(Pet pet) {
            adopted.add(pet);
        }
    }
}
