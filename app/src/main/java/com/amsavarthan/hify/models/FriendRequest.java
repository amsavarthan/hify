package com.amsavarthan.hify.models;

/**
 * Created by amsavarthan on 11/3/18.
 */

public class FriendRequest extends UserId {

    private String id,username, name, email, image, token,timestamp;

    public FriendRequest() {
    }

    public FriendRequest(String id, String username, String name, String email, String image, String token, String timestamp) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.email = email;
        this.image = image;
        this.token = token;
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}