package com.chat.app.models;

public class User {
    private String id, name, email, lastMessage, fileFromWho, lastSeen;

    public User(String id, String name, String email, String lastMessage, String fileFromWho, String lastSeen) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.fileFromWho = fileFromWho;
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

    public String getFileFromWho() {
        return fileFromWho;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getLastSeen() {
        return lastSeen;
    }
}
