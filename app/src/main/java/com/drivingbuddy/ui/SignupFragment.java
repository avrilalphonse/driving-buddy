package com.drivingbuddy.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.text.TextUtils;
import android.widget.*;
import androidx.lifecycle.ViewModelProvider;
import com.drivingbuddy.R;
import com.drivingbuddy.ui.auth.AuthViewModel;

public class SignupFragment extends Fragment {

    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        EditText emailEditText = view.findViewById(R.id.email_signup);
        EditText nameEditText = view.findViewById(R.id.name_signup);
        EditText passwordEditText = view.findViewById(R.id.password_signup);
        Button signupButton = view.findViewById(R.id.signup_button);
        TextView goToLogin = view.findViewById(R.id.go_to_login);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        signupButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String name = nameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();

            if (TextUtils.isEmpty(name)) {
                nameEditText.setError("Name is required");
                nameEditText.requestFocus();
                return;
            }
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

            authViewModel.signup(email, name, password)
                    .observe(getViewLifecycleOwner(), response -> {
                        if (response != null && response.getUser() != null) {
                            Toast.makeText(getContext(), "Welcome " + response.getUser().getName(), Toast.LENGTH_SHORT).show();
                            NavHostFragment.findNavController(this)
                                    .navigate(R.id.action_signupFragment_to_loginFragment);
                        } else {
                            Toast.makeText(getContext(), "Signup failed", Toast.LENGTH_SHORT).show();

                            Log.e("Signup", "Signup response: " + response);
                        }
                    });
        });

        goToLogin.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_signupFragment_to_loginFragment)
        );
    }
}
