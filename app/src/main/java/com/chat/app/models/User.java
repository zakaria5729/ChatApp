package com.chat.app.models;

public class User {
    private String id, name, email, lastMessage, lastSeen;

    public User(String id, String name, String email, String lastMessage, String lastSeen) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.lastMessage = lastMessage;
        this.lastSeen = lastSeen;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getLastSeen() {
        return lastSeen;
    }
}
