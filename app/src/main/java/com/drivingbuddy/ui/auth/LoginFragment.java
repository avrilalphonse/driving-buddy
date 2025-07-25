package com.drivingbuddy.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.drivingbuddy.MainActivity;
import com.drivingbuddy.R;
import android.text.TextUtils;
import android.widget.*;
import androidx.lifecycle.ViewModelProvider;

public class LoginFragment extends Fragment {

    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        EditText emailEditText = view.findViewById(R.id.email_input);
        EditText passwordEditText = view.findViewById(R.id.password_input);
        Button loginButton = view.findViewById(R.id.login_button);
        TextView goToSignup = view.findViewById(R.id.go_to_signup);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        loginButton.setOnClickListener(v -> {
            if (emailEditText == null || passwordEditText == null) {
                Toast.makeText(getContext(), "Unexpected error: Login views not found", Toast.LENGTH_SHORT).show();
                return;
            }

            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();

            if (TextUtils.isEmpty(email)) {
                emailEditText.setError("Email is required");
                emailEditText.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                passwordEditText.setError("Password is required");
                passwordEditText.requestFocus();
                return;
            }

            authViewModel.login(email, password).observe(getViewLifecycleOwner(), authResponse -> {
                if (authResponse != null && authResponse.getUser() != null) {
                    String name = authResponse.getUser().getName();
                    Toast.makeText(getContext(), "Login successful. Welcome " + name, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(requireActivity(), MainActivity.class);
                    startActivity(intent);
                    requireActivity().finish();
                } else {
                    Toast.makeText(getContext(), "Login failed. Please check credentials.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        goToSignup.setOnClickListener(v ->
                NavHostFragment.findNavController(LoginFragment.this)
                        .navigate(R.id.action_loginFragment_to_signupFragment)
        );
    }

}