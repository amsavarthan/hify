package com.amsavarthan.hify.models;

/**
 * Created by amsavarthan on 4/4/18.
 */

public class Post extends PostId {

    private String userId,name, timestamp, image, likes, favourites, description, color,username,userimage;

    public Post() {
    }

    public Post(String userId, String name, String timestamp, String image, String likes, String favourites, String description, String color, String username, String userimage) {
        this.userId = userId;
        this.name = name;
        this.timestamp = timestamp;
        this.image = image;
        this.likes = likes;
        this.favourites = favourites;
        this.description = description;
        this.color = color;
        this.username = username;
        this.userimage = userimage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserimage() {
        return userimage;
    }

    public void setUserimage(String userimage) {
        this.userimage = userimage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getFavourites() {
        return favourites;
    }

    public void setFavourites(String favourites) {
        this.favourites = favourites;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
