package com.drivingbuddy.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.drivingbuddy.data.model.User;
import com.drivingbuddy.data.model.CarProfile;

public class TokenManager {
    private final SharedPreferences prefs;
    private static final String CAR_MAKE_KEY = "car_make";
    private static final String CAR_MODEL_KEY = "car_model";
    private static final String CAR_COLOR_NAME_KEY = "car_color_name";
    private static final String CAR_COLOR_HEX_KEY = "car_color_hex";
    private static final String USER_PROFILE_PHOTO_URL_KEY = "user_profile_picture_url";

    public TokenManager(Context context) {
        this.prefs = context.getSharedPreferences(AuthPrefs.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString(AuthPrefs.TOKEN_KEY, token).apply();
    }

    public String getToken() {
        return prefs.getString(AuthPrefs.TOKEN_KEY, null);
    }

    public String getProfilePictureUrl() {
        String url = prefs.getString(USER_PROFILE_PHOTO_URL_KEY, null);
        return (url != null && !url.isEmpty()) ? url : null;
    }

    public void saveUser(User user) {
        prefs.edit().putString("user_id", user.getId())
                .putString("user_name", user.getName())
                .putString("user_email", user.getEmail())
                .putString(USER_PROFILE_PHOTO_URL_KEY, user.getProfilePictureUrl() != null ? user.getProfilePictureUrl() : "")
                .apply();

        if (user.getCarDetails() != null) {
            CarProfile car = user.getCarDetails();
            saveCarProfile(car.getMake(), car.getModel(), car.getColorName(), car.getColorHex());
        }
    }

    private String getCarKey(String baseKey) {
        String userId = getUserId();
        return baseKey + "_" + userId;
    }

    public void saveCarProfile(String make, String model, String colorName, String colorHex) {
        String makeKey = getCarKey(CAR_MAKE_KEY);
        String modelKey = getCarKey(CAR_MODEL_KEY);
        String colorNameKey = getCarKey(CAR_COLOR_NAME_KEY);
        String colorHexKey = getCarKey(CAR_COLOR_HEX_KEY);

        prefs.edit()
            .putString(makeKey, make)
            .putString(modelKey, model)
            .putString(colorNameKey, colorName)
            .putString(colorHexKey, colorHex)
            .apply();
    }

    public com.drivingbuddy.data.model.CarProfile getCarProfile() {
        String makeKey = getCarKey(CAR_MAKE_KEY);
        String modelKey = getCarKey(CAR_MODEL_KEY);
        String colorNameKey = getCarKey(CAR_COLOR_NAME_KEY);
        String colorHexKey = getCarKey(CAR_COLOR_HEX_KEY);

        String make = prefs.getString(makeKey, null);
        String model = prefs.getString(modelKey, null);
        String colorName = prefs.getString(colorNameKey, null);
        String colorHex = prefs.getString(colorHexKey, null);
        if (make == null || model == null || colorName == null || colorHex == null) {
            return null;
        }
        return new com.drivingbuddy.data.model.CarProfile(make, model, colorName, colorHex);
    }

    public String getUserName() {
        return prefs.getString("user_name", null);
    }

    public String getUserEmail() {
        return prefs.getString("user_email", null);
    }

    public String getUserId() {
        return prefs.getString("user_id", null);
    }

    public void clearToken() {
        prefs.edit().remove(AuthPrefs.TOKEN_KEY).apply();
    }

    public void clearUser() {
        prefs.edit().remove("user_name")
                .remove("user_email")
                .remove("user_id")
                .remove(USER_PROFILE_PHOTO_URL_KEY)
                .remove(CAR_MAKE_KEY + "_" + getUserId())
                .remove(CAR_MODEL_KEY + "_" + getUserId())
                .remove(CAR_COLOR_NAME_KEY + "_" + getUserId())
                .remove(CAR_COLOR_HEX_KEY + "_" + getUserId())
                .apply();
    }

    public void clearAll() {
        clearToken();
        clearUser();
    }
}
