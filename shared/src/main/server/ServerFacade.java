package server;

import com.google.gson.Gson;
import exception.ResponseException;
import model.*;

import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public Pet rescuePet(Pet pet) throws ResponseException {
        var request = buildRequest("POST", "/pet", pet);
        var response = sendRequest(request);
        return handleResponse(response, Pet.class);
    }

    public void adoptPet(int petId) throws ResponseException {
        var path = String.format("/pet/%s", petId);
        var request = buildRequest("DELETE", path, null);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    public void adoptAllPets() throws ResponseException {
        var request = buildRequest("DELETE", "/pet", null);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    public PetList listPets() throws ResponseException {
        var request = buildRequest("GET", "/pet", null);
        var response = sendRequest(request);
        return handleResponse(response, PetList.class);
    }

    private HttpRequest buildRequest(String method, String path, Object body) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }
        return request.build();
    }

    private BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {
        var status = response.statusCode();
        if (!isSuccessful(status)) {
            throw errorFrom(response);
        }

        if (responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }

        return null;
    }

    /**
     * Prefer the server's JSON error. Fall back to the status code when the body is
     * empty or isn't one of our errors (for example, a proxy's HTML error page).
     */
    private ResponseException errorFrom(HttpResponse<String> response) {
        var status = response.statusCode();
        try {
            return ResponseException.fromJson(response.body());
        } catch (RuntimeException ignored) {
            return new ResponseException(ResponseException.fromHttpStatusCode(status), "other failure: " + status);
        }
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
