package com.drivingbuddy.data.api;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2:3000/";
    private static Retrofit retrofit;

    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                Request originalRequest = chain.request();
                Request newRequest = originalRequest.newBuilder()
                        .header("Content-Type", "application/json")
                        .build();
                return chain.proceed(newRequest);
            })
            .build();

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static AuthApiService getAuthApiService() {
        return getClient().create(AuthApiService.class);
    }
}
