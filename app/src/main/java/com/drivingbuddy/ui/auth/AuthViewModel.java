package com.drivingbuddy.ui.auth;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import com.drivingbuddy.data.model.AuthResponse;
import com.drivingbuddy.data.repository.AuthRepository;
import com.drivingbuddy.data.model.CarProfile;

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

    public String getUserName() {
        return authRepository.getUserName();
    }

    public String getUserEmail() {
        return authRepository.getUserEmail();
    }

    public String getUserId() {
        return authRepository.getUserId();
    }

    public LiveData<AuthResponse> getMe() {
        return authRepository.getMe();
    }

    public LiveData<AuthResponse> uploadProfilePhoto(java.io.File imageFile) {
        return authRepository.uploadProfilePhoto(imageFile);
    }

    public String getProfilePictureUrl() {
        return authRepository.getProfilePictureUrl();
    }

    public LiveData<AuthResponse> updateProfile(String name, String email) {
        return authRepository.updateProfile(name, email);
    }

    public LiveData<AuthResponse> changePassword(String newPassword) {
        return authRepository.changePassword(newPassword);
    }
    
    public CarProfile getCarProfile() {
        return authRepository.getCarProfile();
    }

    public LiveData<AuthResponse> updateCarDetails(String make, String model, String colorName, String colorHex) {
        return authRepository.updateCarDetails(make, model, colorName, colorHex);
    }

    public void logout() {
        authRepository.logout();
    }
}