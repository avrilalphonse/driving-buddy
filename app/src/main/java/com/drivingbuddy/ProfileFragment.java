package com.drivingbuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
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
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView title = view.findViewById(R.id.settings_toolbar_title);
        title.setText(getString(R.string.profile_title));

        ImageButton backBtn = view.findViewById(R.id.toolbar_back);
        ImageButton menuBtn = view.findViewById(R.id.toolbar_menu);

        backBtn.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.settingsFragment));
        menuBtn.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), menuBtn);
            MenuInflater menuInflater = popup.getMenuInflater();
            menuInflater.inflate(R.menu.settings_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_profile) {
                    return true;
                } else if (id == R.id.menu_notif) {
                    Navigation.findNavController(view).navigate(R.id.notificationsFragment);
                    return true;
                } else if (id == R.id.menu_perms) {
                    Navigation.findNavController(view).navigate(R.id.termsFragment);
                    return true;
                } else if (id == R.id.menu_data_collection) {
                    Navigation.findNavController(view).navigate(R.id.dataCollectionFragment);
                    return true;
                } else {
                    return false;
                }
            });
            popup.show();
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
