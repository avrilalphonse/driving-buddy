package com.drivingbuddy.utils;

import android.content.Context;
import android.content.SharedPreferences;

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

    public void clearToken() {
        prefs.edit().remove(AuthPrefs.TOKEN_KEY).apply();
    }

}
