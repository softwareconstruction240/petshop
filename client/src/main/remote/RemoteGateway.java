package remote;

import exception.ResponseException;
import model.Pet;
import remote.websocket.WebSocketFacade;
import server.ServerFacade;

import java.util.List;

/**
 * Production gateway: pet operations go over HTTP, shop presence goes over the WebSocket.
 */
public class RemoteGateway implements PetShopGateway {
    private final ServerFacade server;
    private final WebSocketFacade ws;

    public RemoteGateway(String serverUrl, NotificationObserver notificationObserver) throws ResponseException {
        server = new ServerFacade(serverUrl);
        ws = new WebSocketFacade(serverUrl, notificationObserver);
    }

    @Override
    public void enterPetShop(String visitorName) throws ResponseException {
        ws.enterPetShop(visitorName);
    }

    @Override
    public void leavePetShop(String visitorName) throws ResponseException {
        ws.leavePetShop(visitorName);
    }

    @Override
    public Pet rescuePet(Pet pet) throws ResponseException {
        return server.rescuePet(pet);
    }

    @Override
    public List<Pet> listPets() throws ResponseException {
        return server.listPets();
    }

    @Override
    public void adoptPet(Pet pet) throws ResponseException {
        server.adoptPet(pet.id());
    }

    @Override
    public void adoptAllPets() throws ResponseException {
        server.adoptAllPets();
    }
}
