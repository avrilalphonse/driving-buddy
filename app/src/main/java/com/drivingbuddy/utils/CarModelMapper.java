package com.drivingbuddy.utils;

public class CarModelMapper {
    private CarModelMapper() {
    }

    public static String getModelAsset(String make, String model) {
        if ("Civic".equals(model)) {
            return "models/whitehondacivic.glb";
        }
        if ("Corolla Cross".equals(model)) {
            return "models/greytoyotacorollacross.glb";
        }
        if ("Mini Cooper".equals(model) || "Cooper".equals(model)) {
            return "models/blackminicooper.glb";
        }
        if ("SUV".equals(model)) {
            return "models/greysuv.glb";
        }
        if ("Sedan".equals(model)) {
            return "models/bluesedan.glb";
        }
        return "models/bluesedan.glb";
    }
}
