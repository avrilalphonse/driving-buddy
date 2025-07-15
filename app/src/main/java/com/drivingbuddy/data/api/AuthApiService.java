package com.drivingbuddy.data.api;

import com.drivingbuddy.data.model.LoginRequest;
import com.drivingbuddy.data.model.SignupRequest;
import com.drivingbuddy.data.model.AuthResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


public interface AuthApiService {

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest loginRequest);

    @POST("api/auth/signup")
    Call<AuthResponse> signup(@Body SignupRequest signupRequest);
}
