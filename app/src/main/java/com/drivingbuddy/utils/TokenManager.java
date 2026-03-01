package com.drivingbuddy.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.drivingbuddy.data.model.User;

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
    }

    public void saveCarProfile(String make, String model, String colorName, String colorHex) {
        prefs.edit()
                .putString(CAR_MAKE_KEY, make)
                .putString(CAR_MODEL_KEY, model)
                .putString(CAR_COLOR_NAME_KEY, colorName)
                .putString(CAR_COLOR_HEX_KEY, colorHex)
                .apply();
    }

    public com.drivingbuddy.data.model.CarProfile getCarProfile() {
        String make = prefs.getString(CAR_MAKE_KEY, null);
        String model = prefs.getString(CAR_MODEL_KEY, null);
        String colorName = prefs.getString(CAR_COLOR_NAME_KEY, null);
        String colorHex = prefs.getString(CAR_COLOR_HEX_KEY, null);
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
                .apply();
    }

    public void clearAll() {
        clearToken();
        clearUser();
    }
}
