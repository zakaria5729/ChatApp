package com.chat.app.models;

public class ChatMessage {
    private String fromId, toId, message;

    public ChatMessage(String fromId, String toId, String message) {
        this.toId = toId;
        this.fromId = fromId;
        this.message = message;
    }

    public String getToId() {
        return toId;
    }

    public String getFromId() {
        return fromId;
    }

    public String getMessage() {
        return message;
    }
}
