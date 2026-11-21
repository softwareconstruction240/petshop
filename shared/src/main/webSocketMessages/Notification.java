package webSocketMessages;

import com.google.gson.Gson;

public record Notification(Type type, String message) {
    private static final Gson GSON = new Gson();

    public enum Type {
        INFO,
        ARRIVAL,
        NOISE,
        DEPARTURE
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    public String toString() {
        return toJson();
    }
}
