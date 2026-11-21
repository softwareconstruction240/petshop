package petclient;

import dataaccess.MemoryDataAccess;
import exception.ResponseException;
import model.Pet;
import model.PetType;
import org.junit.jupiter.api.*;
import remote.RemoteGateway;
import server.PetServer;
import server.ServerFacade;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PetClientTest {

    static private PetServer petServer;
    static PetClient client;
    static String url;

    @BeforeAll
    static void startServer() throws Exception {
        petServer = new PetServer(new MemoryDataAccess());
        petServer.run(0);
        url = "http://localhost:" + petServer.port();

        client = new PetClient(new RemoteGateway(url, notification -> {}));
        client.signIn("tester");
    }

    @AfterAll
    static void stopServer() {
        petServer.stop();
    }

    @BeforeEach
    void clear() throws Exception {
        client.adoptAllPets();
    }

    @Test
    void rescuePet() {
        Pet joe = assertDoesNotThrow(() -> client.rescuePet("joe", PetType.FISH));
        assertEquals("joe", joe.name());
        assertEquals(PetType.FISH, joe.type());

        Pet sally = assertDoesNotThrow(() -> client.rescuePet("sally", PetType.CAT));
        assertEquals("sally", sally.name());
        assertNotEquals(joe.id(), sally.id());
    }

    @Test
    void adoptPet() throws Exception {
        Pet joe = client.rescuePet("joe", PetType.FROG);
        Pet sally = client.rescuePet("sally", PetType.CAT);

        client.listPets();
        Pet adopted = assertDoesNotThrow(() -> client.adoptPet(1));
        assertEquals(joe, adopted);
        assertEquals(List.of(sally), client.listPets());
    }

    @Test
    void adoptAllPets() throws Exception {
        Pet joe = client.rescuePet("joe", PetType.ROCK);
        Pet pat = client.rescuePet("pat", PetType.CAT);

        List<Pet> adopted = assertDoesNotThrow(() -> client.adoptAllPets());
        assertEquals(List.of(joe, pat), adopted);
        assertTrue(client.listPets().isEmpty());
    }

    @Test
    void adoptNonexistentPet() throws Exception {
        client.rescuePet("joe", PetType.FROG);
        client.listPets();
        assertThrows(ResponseException.class, () -> client.adoptPet(933432));
    }

    @Test
    void adoptPetAdoptedByServerFails() throws Exception {
        Pet joe = client.rescuePet("joe", PetType.FROG);
        client.listPets();
        // Remove the pet behind the client's back, as another visitor would.
        new ServerFacade(url).adoptPet(joe.id());

        var ex = assertThrows(ResponseException.class, () -> client.adoptPet(1));
        assertEquals("joe has already been adopted", ex.getMessage());
    }

    @Test
    void listPets() throws Exception {
        Pet joe = client.rescuePet("joe", PetType.FISH);
        Pet sally = client.rescuePet("sally", PetType.FISH);

        List<Pet> pets = assertDoesNotThrow(() -> client.listPets());
        assertEquals(List.of(joe, sally), pets);
    }
}
