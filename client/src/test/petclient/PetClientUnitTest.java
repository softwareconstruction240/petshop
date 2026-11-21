package petclient;

import exception.ResponseException;
import model.Pet;
import model.PetType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests PetClient's own rules against an in-memory gateway. No server is required.
 */
class PetClientUnitTest {
    private MemoryGateway gateway;
    private PetClient client;

    @BeforeEach
    void setUp() {
        gateway = new MemoryGateway();
        client = new PetClient(gateway);
    }

    @Test
    void signInEntersShop() throws Exception {
        client.signIn("tester");

        assertTrue(client.isSignedIn());
        assertEquals("tester", client.getVisitorName());
        assertEquals(List.of("tester"), gateway.visitors);
    }

    @Test
    void signOutLeavesShop() throws Exception {
        client.signIn("tester");
        client.signOut();

        assertFalse(client.isSignedIn());
        assertTrue(gateway.visitors.isEmpty());
    }

    @Test
    void operationsRequireSignIn() {
        assertThrows(ResponseException.class, () -> client.rescuePet("joe", PetType.DOG));
        assertThrows(ResponseException.class, () -> client.listPets());
        assertThrows(ResponseException.class, () -> client.adoptPet(1));
        assertThrows(ResponseException.class, () -> client.adoptAllPets());
        assertThrows(ResponseException.class, () -> client.signOut());
        assertTrue(gateway.pets.isEmpty());
    }

    @Test
    void adoptPetRemovesOnlyThatPet() throws Exception {
        client.signIn("tester");
        Pet joe = client.rescuePet("joe", PetType.FROG);
        Pet sally = client.rescuePet("sally", PetType.CAT);

        client.listPets();
        assertEquals(joe, client.adoptPet(1));
        assertEquals(List.of(sally), client.listPets());
    }

    @Test
    void positionsMatchListShownUntilListedAgain() throws Exception {
        client.signIn("tester");
        Pet joe = client.rescuePet("joe", PetType.FROG);
        Pet sally = client.rescuePet("sally", PetType.CAT);

        client.listPets();
        assertEquals(joe, client.adoptPet(1));
        assertEquals(sally, client.adoptPet(2));
        assertTrue(gateway.pets.isEmpty());
    }

    @Test
    void adoptBeforeListingFails() throws Exception {
        client.signIn("tester");
        client.rescuePet("joe", PetType.FROG);

        var ex = assertThrows(ResponseException.class, () -> client.adoptPet(1));
        assertEquals(ResponseException.Code.ClientError, ex.code());
        assertEquals(1, gateway.pets.size());
    }

    @Test
    void adoptOutOfRangePositionFails() throws Exception {
        client.signIn("tester");
        client.rescuePet("joe", PetType.FROG);
        client.listPets();

        for (int position : new int[]{0, 2, -1}) {
            var ex = assertThrows(ResponseException.class, () -> client.adoptPet(position));
            assertEquals(ResponseException.Code.ClientError, ex.code());
        }
        assertEquals(1, gateway.pets.size());
    }

    @Test
    void adoptAlreadyAdoptedPetFails() throws Exception {
        client.signIn("tester");
        client.rescuePet("joe", PetType.FROG);
        client.listPets();
        client.adoptPet(1);

        var ex = assertThrows(ResponseException.class, () -> client.adoptPet(1));
        assertEquals(ResponseException.Code.ClientError, ex.code());
        assertEquals("joe has already been adopted", ex.getMessage());
    }

    @Test
    void adoptAllReturnsEveryPet() throws Exception {
        client.signIn("tester");
        Pet joe = client.rescuePet("joe", PetType.ROCK);
        Pet pat = client.rescuePet("pat", PetType.CAT);

        assertEquals(List.of(joe, pat), client.adoptAllPets());
        assertTrue(gateway.pets.isEmpty());
    }
}
