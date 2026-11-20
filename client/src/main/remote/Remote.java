package remote;

import exception.ResponseException;
import model.Pet;
import model.PetList;
import remote.websocket.WebSocketFacade;
import server.ServerFacade;

public class Remote {
    private final ServerFacade server;
    private final WebSocketFacade ws;

    public Remote(String serverUrl, NotificationObserver notificationObserver) throws ResponseException {
        server = new ServerFacade(serverUrl);
        ws = new WebSocketFacade(serverUrl, notificationObserver);
    }

    public void enterPetShop(String visitorName) throws ResponseException {
        ws.enterPetShop(visitorName);
    }

    public void leavePetShop(String visitorName) throws ResponseException {
        ws.leavePetShop(visitorName);
    }

    public Pet addPet(Pet pet) throws ResponseException {
        return server.addPet(pet);
    }

    public PetList listPets() throws ResponseException {
        return server.listPets();
    }

    public void deletePet(int id) throws ResponseException {
        server.deletePet(id);
    }

    public void deleteAllPets() throws ResponseException {
        server.deleteAllPets();
    }
}
