package com.drivingbuddy.data.model;

public class UpdateCarDetailsRequest {
    private String make;
    private String model;
    private String colorName;
    private String colorHex;

    public UpdateCarDetailsRequest(String make, String model, String colorName, String colorHex) {
        this.make = make;
        this.model = model;
        this.colorName = colorName;
        this.colorHex = colorHex;
    }
}