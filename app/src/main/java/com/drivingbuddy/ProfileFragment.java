package com.drivingbuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.drivingbuddy.ui.auth.AuthViewModel;

public class ProfileFragment extends Fragment {
    private AuthViewModel authViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_profile, container, false);
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        View backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });

        String userName = authViewModel.getUserName();
        String userEmail = authViewModel.getUserEmail();

        TextView name_text = view.findViewById(R.id.profile_name_display);
        TextView email_text = view.findViewById(R.id.profile_email_display);

        if (userName != null && !userName.isEmpty()) {
            name_text.setText(userName);

        } else {
            name_text.setText("Guest");
        }
        if (userEmail != null && !userEmail.isEmpty()) {
            email_text.setText(userEmail);

        } else {
            email_text.setText("No Email Found!");
        }

        return view;

    }
}
