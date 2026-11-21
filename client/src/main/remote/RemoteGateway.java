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
    public Pet addPet(Pet pet) throws ResponseException {
        return server.addPet(pet);
    }

    @Override
    public List<Pet> listPets() throws ResponseException {
        return server.listPets();
    }

    @Override
    public void deletePet(int id) throws ResponseException {
        server.deletePet(id);
    }

    @Override
    public void deleteAllPets() throws ResponseException {
        server.deleteAllPets();
    }
}
