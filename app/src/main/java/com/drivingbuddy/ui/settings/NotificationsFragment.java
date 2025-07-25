package com.drivingbuddy.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.drivingbuddy.R;

public class NotificationsFragment extends Fragment {
    public NotificationsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        TextView title = view.findViewById(R.id.settings_toolbar_title);
        title.setText(getString(R.string.notifs_title));

        ImageButton backBtn = view.findViewById(R.id.toolbar_back);
        ImageButton menuBtn = view.findViewById(R.id.toolbar_menu);

        backBtn.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.settingsFragment));
        menuBtn.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), menuBtn);
            MenuInflater menuInflater = popup.getMenuInflater();
            menuInflater.inflate(R.menu.settings_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_notif) {
                    return true;
                } else if (id == R.id.menu_data_collection) {
                    Navigation.findNavController(view).navigate(R.id.dataCollectionFragment);
                    return true;
                } else if (id == R.id.menu_perms) {
                    Navigation.findNavController(view).navigate(R.id.termsFragment);
                    return true;
                } else if (id == R.id.menu_profile) {
                    Navigation.findNavController(view).navigate(R.id.profileFragment);
                    return true;
                } else {
                    return false;
                }
            });
            popup.show();
        });

        // rmr switch state using SharedPreferences
        SwitchCompat pushSwitch = view.findViewById(R.id.switch_push_notifications);
        SwitchCompat emailSwitch = view.findViewById(R.id.switch_email_notifications);
        SharedPreferences prefs = requireContext().getSharedPreferences("notification_prefs", Context.MODE_PRIVATE);
        pushSwitch.setChecked(prefs.getBoolean("push_enabled", true));
        emailSwitch.setChecked(prefs.getBoolean("email_enabled", true));
        pushSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
            prefs.edit().putBoolean("push_enabled", isChecked).apply()
        );
        emailSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
            prefs.edit().putBoolean("email_enabled", isChecked).apply()
        );

        return view;
    }
}
