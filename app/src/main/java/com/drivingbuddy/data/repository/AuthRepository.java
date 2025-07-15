package com.drivingbuddy.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.drivingbuddy.data.api.ApiClient;
import com.drivingbuddy.data.api.AuthApiService;
import com.drivingbuddy.data.model.AuthResponse;
import com.drivingbuddy.data.model.LoginRequest;
import com.drivingbuddy.data.model.SignupRequest;
import com.drivingbuddy.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.annotation.NonNull;

import java.io.IOException;

public class AuthRepository {

    private final AuthApiService apiService;
    private final TokenManager tokenManager;

    public AuthRepository(Context context) {
        this.apiService = ApiClient.getAuthApiService();
        this.tokenManager = new TokenManager(context);
    }

    public LiveData<AuthResponse> signup(String email, String name, String password) {
        MutableLiveData<AuthResponse> liveData = new MutableLiveData<>();
        apiService.signup(new SignupRequest(email, name, password))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            tokenManager.saveToken(response.body().getToken());
                            liveData.setValue(response.body());
                        } else {
                            try {
                                if (response.errorBody() != null) {
                                    Log.e("Signup Error", "Error: " + response.errorBody().string());
                                } else {
                                    Log.e("Signup Error", "Unknown error with no error body");
                                }
                            } catch (IOException e) {
                                Log.e("Signup Error", "IOException: " + e.getMessage());
                            }

                            liveData.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                        Log.e("Signup Failure", "Exception: " + t.getMessage(), t);
                        liveData.setValue(null);
                    }
                });

        return liveData;
    }

    public LiveData<AuthResponse> login(String email, String password) {
        MutableLiveData<AuthResponse> liveData = new MutableLiveData<>();
        apiService.login(new LoginRequest(email, password))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            tokenManager.saveToken(response.body().getToken());
                            liveData.setValue(response.body());
                        } else {
                            liveData.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                        liveData.setValue(null);
                    }
                });

        return liveData;
    }

    public String getToken() {
        return tokenManager.getToken();
    }
}