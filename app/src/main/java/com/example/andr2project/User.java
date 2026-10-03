package com.example.andr2project;

import com.google.firebase.firestore.GeoPoint;

public class User {
    public String name, email, imgUrl;
    public GeoPoint location;
    public Team team;
    public Boolean isLoggedIn;

    public User() {
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.location = new GeoPoint(0, 0);
        this.team = Team.NONE;
        this.isLoggedIn = false;
    }

    public User(String name, String email, double latitude, double longitude, Team team, Boolean isLoggedIn) {
        this.name = name;
        this.email = email;
        this.location = new GeoPoint(latitude, longitude);
        this.team = team;
        this.isLoggedIn = isLoggedIn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public Boolean getLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(Boolean loggedIn) {
        isLoggedIn = loggedIn;
    }
}
