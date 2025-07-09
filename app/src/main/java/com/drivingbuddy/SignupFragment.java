package com.drivingbuddy;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SignupFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        TextView goToLogin = view.findViewById(R.id.go_to_login);
        goToLogin.setOnClickListener(v ->
                NavHostFragment.findNavController(SignupFragment.this)
                        .navigate(R.id.action_signupFragment_to_loginFragment)
        );

        return view;
    }
}
