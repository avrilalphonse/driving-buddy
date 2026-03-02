package com.drivingbuddy.data.model;

public class CarProfile {
    private String make;
    private String model;
    private String colorName;
    private String colorHex;

    public CarProfile() {
    }

    public CarProfile(String make, String model, String colorName, String colorHex) {
        this.make = make;
        this.model = model;
        this.colorName = colorName;
        this.colorHex = colorHex;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public String getColorName() {
        return colorName;
    }

    public String getColorHex() {
        return colorHex;
    }
}