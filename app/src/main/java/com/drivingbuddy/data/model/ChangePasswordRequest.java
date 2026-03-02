package com.drivingbuddy.data.model;

public class ChangePasswordRequest {
    private String newPassword;

    public ChangePasswordRequest(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPassword() { return newPassword; }
}