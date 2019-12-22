package com.chat.app.models;

public class ChatMessage {
    private String objectId, fromId, toId, text, createdAt;

    public ChatMessage(String objectId, String fromId, String toId, String text, String createdAt) {
        this.objectId = objectId;
        this.toId = toId;
        this.fromId = fromId;
        this.text = text;
        this.createdAt = createdAt;
    }

    public String getObjectId() {
        return objectId;
    }

    public String getToId() {
        return toId;
    }

    public String getFromId() {
        return fromId;
    }

    public String getText() {
        return text;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
