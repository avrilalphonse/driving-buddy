package com.drivingbuddy.ui.settings;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

import com.drivingbuddy.MainActivity;
import com.drivingbuddy.R;
import com.drivingbuddy.ui.auth.AuthViewModel;

public class SettingsFragment extends Fragment {
    private NavController navController;
    private AuthViewModel authViewModel;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        String userName = authViewModel.getUserName();

        TextView profile_title = view.findViewById(R.id.profile_title);
        TextView nameTextView = view.findViewById(R.id.profile_name);
        ImageView profileImage = view.findViewById(R.id.profile_image); 
        
        if (userName != null && !userName.isEmpty()) {
            profile_title.setText(userName + "'s Profile");
            nameTextView.setText(userName);
        } else {
            nameTextView.setText("Guest");
        }

        String photoUrl = authViewModel.getProfilePictureUrl();

        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(this)
                .load(photoUrl)
                .circleCrop()
                .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.ic_badge);
        }

        View profile = view.findViewById(R.id.profile_info_container);
        profile.setOnClickListener(v -> {
            navController.navigate(R.id.profileFragment);
        });

        authViewModel.getMe().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getUser() != null &&
                    response.getUser().getProfilePictureUrl() != null) {
                String url = response.getUser().getProfilePictureUrl();
                Glide.with(this).load(url).circleCrop().into(profileImage);
            }
        });

        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

        // when logout is clicked, log the user out and return to the login screen
        View logout_btn = view.findViewById(R.id.logout_btn);
        logout_btn.setOnClickListener(v -> {
            ((MainActivity) getActivity()).logout();
        });

        View dataCollectionContainer = view.findViewById(R.id.data_collection_container);
        dataCollectionContainer.setOnClickListener(v -> {
            navController.navigate(R.id.dataCollectionFragment);
        });

        View notificationsContainer = view.findViewById(R.id.notifications_container);
        notificationsContainer.setOnClickListener(v -> {
            navController.navigate(R.id.notificationsFragment);
        });

        View carDetailsContainer = view.findViewById(R.id.car_details_container);
        carDetailsContainer.setOnClickListener(v -> {
            navController.navigate(R.id.carDetailsFragment);
        });

        View termsContainer = view.findViewById(R.id.permissions_container);
        termsContainer.setOnClickListener(v -> {
            navController.navigate(R.id.termsFragment);
        });

        return view;
    }
}