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

import com.drivingbuddy.MainActivity;
import com.drivingbuddy.R;
import com.drivingbuddy.ui.auth.AuthViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {
    private NavController navController;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private AuthViewModel authViewModel;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        String userName = authViewModel.getUserName();

        TextView profile_title = view.findViewById(R.id.profile_title);
        TextView nameTextView = view.findViewById(R.id.profile_name);

        if (userName != null && !userName.isEmpty()) {
            profile_title.setText(userName + "'s Profile");
            nameTextView.setText(userName);
        } else {
            nameTextView.setText("Guest");
        }

        View profile = view.findViewById(R.id.profile_info_container);
        profile.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_settings_to_profile);
        });

        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

        // when logout is clicked, log the user out and return to the login screen
        View logout_btn = view.findViewById(R.id.logout_btn);
        logout_btn.setOnClickListener(v -> {
            ((MainActivity) getActivity()).logout();
        });

        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_nav);

        View dataCollectionContainer = view.findViewById(R.id.data_collection_container);
        dataCollectionContainer.setOnClickListener(v -> {
            navController.navigate(R.id.dataCollectionFragment);
        });

        View notificationsContainer = view.findViewById(R.id.notifications_container);
        notificationsContainer.setOnClickListener(v -> {
            navController.navigate(R.id.notificationsFragment);
        });

        View termsContainer = view.findViewById(R.id.permissions_container);
        termsContainer.setOnClickListener(v -> {
            navController.navigate(R.id.termsFragment);
        });

        return view;
    }
}