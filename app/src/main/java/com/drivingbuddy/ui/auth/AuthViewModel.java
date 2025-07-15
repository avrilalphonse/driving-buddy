package com.drivingbuddy.ui.auth;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import com.drivingbuddy.data.model.AuthResponse;
import com.drivingbuddy.data.repository.AuthRepository;

public class AuthViewModel extends AndroidViewModel{

    private final AuthRepository authRepository;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application.getApplicationContext());
    }

    public LiveData<AuthResponse> signup(String email, String name, String password) {
        return authRepository.signup(email, name, password);
    }

    public LiveData<AuthResponse> login(String email, String password) {
        return authRepository.login(email, password);
    }

    public String getToken() {
        return authRepository.getToken();
    }

}