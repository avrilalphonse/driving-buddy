package com.drivingbuddy.data.model;

public class UpdateProfileRequest {
    private String name;
    private String email;

    public UpdateProfileRequest(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
}