package server;

import exception.ResponseException;
import model.*;
import org.junit.jupiter.api.*;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class PetServerTest {
    static private PetServer petServer;
    static ServerFacade server;

    @BeforeAll
    static void startServer() {
        petServer = new PetServer();
        petServer.run(0);
        var url = "http://localhost:" + petServer.port();
        server = new ServerFacade(url);
    }

    @AfterAll
    static void stopServer() {
        petServer.stop();
    }

    @BeforeEach
    void clear() {
        assertDoesNotThrow(() -> server.adoptAllPets());
    }

    @Test
    void rescuePet() {
        var joe = new Pet(0, "joe", PetType.CAT);
        Pet result = assertDoesNotThrow(() -> server.rescuePet(joe));
        assertPetEqual(joe, result);
    }

    @Test
    void adoptPet() throws Exception {
        var expected = new PetList();
        expected.add(server.rescuePet(new Pet(0, "sally", PetType.CAT)));

        Pet joe = server.rescuePet(new Pet(0, "joe", PetType.CAT));
        server.adoptPet(joe.id());

        PetList result = assertDoesNotThrow(() -> server.listPets());
        assertPetCollectionEqual(expected, result);
    }

    @Test
    void listPets() throws Exception {
        var expected = new PetList();
        expected.add(server.rescuePet(new Pet(0, "joe", PetType.CAT)));
        expected.add(server.rescuePet(new Pet(0, "sally", PetType.CAT)));

        var result = assertDoesNotThrow(() -> server.listPets());
        assertPetCollectionEqual(expected, result);
    }

    @Test
    void invalidPetRescue() {
        // Dogs with fleas are not allowed
        ResponseException error = Assertions.assertThrows(ResponseException.class,
                () -> server.rescuePet(new Pet(-1, "fleas", PetType.DOG)),
                "Inserting an invalid pet should throw an error");

        Assertions.assertEquals(ResponseException.Code.ClientError, error.code());
        Assertions.assertEquals("Error: no dogs with fleas", error.getMessage());
    }

    @Test
    void adoptNonexistentPet() {
        ResponseException error = assertThrows(ResponseException.class, () -> server.adoptPet(933432));
        assertEquals(ResponseException.Code.NotFound, error.code());
    }

    public static void assertPetEqual(Pet expected, Pet actual) {
        assertEquals(expected.name(), actual.name());
        assertEquals(expected.type(), actual.type());
    }

    public static void assertPetCollectionEqual(Collection<Pet> expected, Collection<Pet> actual) {
        Pet[] actualList = actual.toArray(new Pet[]{});
        Pet[] expectedList = expected.toArray(new Pet[]{});
        assertEquals(expectedList.length, actualList.length);
        for (var i = 0; i < actualList.length; i++) {
            assertPetEqual(expectedList[i], actualList[i]);
        }
    }
}
