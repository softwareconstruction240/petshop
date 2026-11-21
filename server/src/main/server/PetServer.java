package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import exception.ResponseException;
import model.Pet;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import server.websocket.ConnectionManager;
import server.websocket.WebSocketHandler;
import service.PetService;

public class PetServer {
    // Used only to read request bodies. Responses are serialized by the model classes' toJson methods.
    private static final Gson GSON = new Gson();

    private final PetService service;
    private final WebSocketHandler webSocketHandler;
    private final Javalin httpHandler;

    public PetServer() {
        this(new MemoryDataAccess());
    }

    public PetServer(DataAccess dataAccess) {
        webSocketHandler = new WebSocketHandler(new ConnectionManager());
        service = new PetService(dataAccess, webSocketHandler);

        httpHandler = Javalin.create(config -> config.staticFiles.add("public"))
                .post("/pet", this::rescuePet)
                .get("/pet", this::listPets)
                .delete("/pet/{petId}", this::adoptPet)
                .delete("/pet", this::adoptAllPets)
                .exception(ResponseException.class, this::exceptionHandler)
                .exception(Exception.class, this::unexpectedExceptionHandler)
                .ws("/ws", ws -> {
                    ws.onConnect(webSocketHandler);
                    ws.onMessage(webSocketHandler);
                    ws.onClose(webSocketHandler);
                });
    }

    public PetServer run(int port) {
        httpHandler.start(port);
        return this;
    }

    public int port() {
        return httpHandler.port();
    }

    public void stop() {
        httpHandler.stop();
    }

    private void exceptionHandler(ResponseException ex, Context ctx) {
        sendError(ctx, ex.toHttpStatusCode(), ex);
    }

    /**
     * Every error leaves the server as a JSON ResponseException so clients can always parse it.
     * Javalin's own HTTP errors (such as 404 for an unknown route) keep their status.
     * Anything else is a bug: log the details and don't expose them to the client.
     */
    private void unexpectedExceptionHandler(Exception ex, Context ctx) {
        if (ex instanceof HttpResponseException httpEx) {
            var code = ResponseException.fromHttpStatusCode(httpEx.getStatus());
            sendError(ctx, httpEx.getStatus(), new ResponseException(code, "Error: " + httpEx.getMessage()));
            return;
        }
        ex.printStackTrace();
        sendError(ctx, 500, new ResponseException(ResponseException.Code.ServerError, "Error: internal server error"));
    }

    private void sendError(Context ctx, int status, ResponseException ex) {
        ctx.status(status);
        sendJson(ctx, ex.toJson());
    }

    private static void sendJson(Context ctx, String json) {
        ctx.contentType(ContentType.APPLICATION_JSON).result(json);
    }

    private void rescuePet(Context ctx) throws ResponseException {
        Pet pet = parseBody(ctx, Pet.class);
        sendJson(ctx, service.rescuePet(pet).toJson());
    }

    private void listPets(Context ctx) throws ResponseException {
        sendJson(ctx, service.listPets().toJson());
    }

    private void adoptPet(Context ctx) throws ResponseException {
        service.adoptPet(parsePetId(ctx.pathParam("petId")));
        ctx.status(204);
    }

    private void adoptAllPets(Context ctx) throws ResponseException {
        service.adoptAllPets();
        ctx.status(204);
    }

    private static <T> T parseBody(Context ctx, Class<T> type) throws ResponseException {
        // Gson returns null for an empty body, which the service rejects as a missing pet.
        try {
            return GSON.fromJson(ctx.body(), type);
        } catch (JsonSyntaxException ex) {
            // Malformed JSON is the client's mistake, not a server error.
            throw new ResponseException(ResponseException.Code.ClientError, "Error: malformed request body");
        }
    }

    private static int parsePetId(String text) throws ResponseException {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            throw new ResponseException(ResponseException.Code.ClientError, "Error: invalid pet ID");
        }
    }
}
