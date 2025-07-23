package com.drivingbuddy.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.drivingbuddy.data.model.User;

public class TokenManager {
    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        this.prefs = context.getSharedPreferences(AuthPrefs.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString(AuthPrefs.TOKEN_KEY, token).apply();
    }

    public String getToken() {
        return prefs.getString(AuthPrefs.TOKEN_KEY, null);
    }

    public void saveUser(User user) {
        prefs.edit().putString("user_id", user.getId())
                .putString("user_name", user.getName())
                .putString("user_email", user.getEmail())
                .apply();
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
                .apply();
    }

    public void clearAll() {
        clearToken();
        clearUser();
    }
}
