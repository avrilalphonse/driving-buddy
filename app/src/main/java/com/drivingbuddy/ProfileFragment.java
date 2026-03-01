package com.drivingbuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.net.Uri;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.drivingbuddy.ui.auth.AuthViewModel;

public class ProfileFragment extends Fragment {
    private AuthViewModel authViewModel;

    private final ActivityResultLauncher<String> imagePickerLauncher =
        registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                uploadImageFromUri(uri);
            }
        });

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

        ImageView avatar = view.findViewById(R.id.profile_avatar);
        ImageButton avatarChange = view.findViewById(R.id.profile_avatar_change);

        String photoUrl = authViewModel.getProfilePictureUrl();
        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(this)
                    .load(photoUrl)
                    .circleCrop()
                    .into(avatar);
        } else {
            avatar.setImageResource(R.drawable.ic_badge);
        }

        avatarChange.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*")
        );

        return view;

    }

    private void uploadImageFromUri(Uri uri) {
        try {
            File tempFile = File.createTempFile("profile_photo", ".jpg", requireContext().getCacheDir());
            try (InputStream in = requireContext().getContentResolver().openInputStream(uri);
                 FileOutputStream out = new FileOutputStream(tempFile)) {

                if (in == null) return;

                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            }

            authViewModel.uploadProfilePhoto(tempFile)
                    .observe(getViewLifecycleOwner(), response -> {
                        if (response != null && response.getUser() != null &&
                                response.getUser().getProfilePictureUrl() != null) {
                            String newUrl = response.getUser().getProfilePictureUrl();
                            ImageView avatar = requireView().findViewById(R.id.profile_avatar);
                            Glide.with(this)
                                    .load(newUrl)
                                    .circleCrop()
                                    .into(avatar);
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
