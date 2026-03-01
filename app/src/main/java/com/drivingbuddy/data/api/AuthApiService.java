package com.drivingbuddy.data.api;

import com.drivingbuddy.data.model.LoginRequest;
import com.drivingbuddy.data.model.SignupRequest;
import com.drivingbuddy.data.model.AuthResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import okhttp3.MultipartBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.Part;

public interface AuthApiService {

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest loginRequest);

    @POST("api/auth/signup")
    Call<AuthResponse> signup(@Body SignupRequest signupRequest);

    @GET("api/auth/me")
    Call<AuthResponse> getMe(@Header("Authorization") String authorization);

    @Multipart
    @POST("api/auth/me/photo")
    Call<AuthResponse> uploadProfilePhoto(@Header("Authorization") String authorization,
        @Part MultipartBody.Part photo
    );
}
