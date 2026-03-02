package com.drivingbuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.core.content.res.ResourcesCompat;

import com.drivingbuddy.ui.auth.AuthViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

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

        view.findViewById(R.id.edit_name_button).setOnClickListener(v -> showEditNameDialog(name_text));
        view.findViewById(R.id.edit_email_button).setOnClickListener(v -> showEditEmailDialog(email_text));
        view.findViewById(R.id.change_password_button).setOnClickListener(v -> showChangePasswordDialog());

        return view;

    }

    private void showEditNameDialog(TextView nameDisplay) {
        TextInputLayout til = new TextInputLayout(requireContext());
        TextInputEditText input = new TextInputEditText(requireContext());

        til.setHintEnabled(false);
        input.setHint("Name");
        til.addView(input);

        String currentName = authViewModel.getUserName();
        if (!TextUtils.isEmpty(currentName)) input.setText(currentName);
        input.setSingleLine(true);

        int side = (int) (24 * getResources().getDisplayMetrics().density);
        int topBottom = (int) (8 * getResources().getDisplayMetrics().density);

        FrameLayout container = new FrameLayout(requireContext());
        container.setPadding(side, topBottom, side, topBottom);
        container.addView(til);

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Name")
            .setView(container)
            .setPositiveButton("Save", (dialog, which) -> {
                String newName = input.getText().toString().trim();
                if (TextUtils.isEmpty(newName)) {
                    Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                String currentEmail = authViewModel.getUserEmail();
                if (currentEmail == null) currentEmail = "";

                authViewModel.updateProfile(newName, currentEmail)
                    .observe(getViewLifecycleOwner(), authResponse -> {
                        if (authResponse != null && authResponse.getUser() != null) {
                            nameDisplay.setText(authResponse.getUser().getName());
                            Toast.makeText(requireContext(), "Name updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show();
                        }
                    });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showEditEmailDialog(TextView emailDisplay) {
        TextInputLayout til = new TextInputLayout(requireContext());
        TextInputEditText input = new TextInputEditText(requireContext());

        til.setHintEnabled(false);
        input.setHint("Email");
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        til.addView(input);

        String currentEmail = authViewModel.getUserEmail();
        if (!TextUtils.isEmpty(currentEmail)) input.setText(currentEmail);
        input.setSingleLine(true);

        int side = (int) (24 * getResources().getDisplayMetrics().density);
        int topBottom = (int) (8 * getResources().getDisplayMetrics().density);

        FrameLayout container = new FrameLayout(requireContext());
        container.setPadding(side, topBottom, side, topBottom);
        container.addView(til); 

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Email")
            .setView(container)
            .setPositiveButton("Save", (dialog, which) -> {
                String newEmail = input.getText().toString().trim();
                if (TextUtils.isEmpty(newEmail)) {
                    Toast.makeText(requireContext(), "Email cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                String currentName = authViewModel.getUserName();
                if (currentName == null) currentName = "";

                authViewModel.updateProfile(currentName, newEmail)
                    .observe(getViewLifecycleOwner(), authResponse -> {
                        if (authResponse != null && authResponse.getUser() != null) {
                            emailDisplay.setText(authResponse.getUser().getEmail());
                            Toast.makeText(requireContext(), "Email updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Update failed (email may be in use)", Toast.LENGTH_SHORT).show();
                        }
                    });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showChangePasswordDialog() {
        int passwordInputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.topMargin = (int) (12 * getResources().getDisplayMetrics().density);

        TextInputLayout newPassTil = new TextInputLayout(requireContext());
        newPassTil.setHint("New password");
        TextInputEditText newPasswordInput = new TextInputEditText(requireContext());
        newPasswordInput.setInputType(passwordInputType);
        newPasswordInput.setTypeface(
            ResourcesCompat.getFont(requireContext(), R.font.my_font)
        );
        newPassTil.addView(newPasswordInput);

        TextInputLayout confirmTil = new TextInputLayout(requireContext());
        confirmTil.setHint("Confirm new password");
        TextInputEditText confirmPass = new TextInputEditText(requireContext());
        confirmPass.setInputType(passwordInputType);
        confirmPass.setTypeface(
            ResourcesCompat.getFont(requireContext(), R.font.my_font)
        );
        confirmTil.addView(confirmPass);
        confirmTil.setLayoutParams(lp);

        layout.addView(newPassTil);
        layout.addView(confirmTil);

        int side = (int) (24 * getResources().getDisplayMetrics().density);
        int topBottom = (int) (8 * getResources().getDisplayMetrics().density);

        FrameLayout container = new FrameLayout(requireContext());
        container.setPadding(side, topBottom, side, topBottom);
        container.addView(layout); 

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change Password")
            .setView(container)
            .setPositiveButton("Save", (dialog, which) -> {
                String newPass = newPasswordInput.getText().toString();
                String confirm = confirmPass.getText().toString();

                if (TextUtils.isEmpty(newPass)) {
                    Toast.makeText(requireContext(), "Enter new password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newPass.length() < 6) {
                    Toast.makeText(requireContext(), "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!newPass.equals(confirm)) {
                    Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                authViewModel.changePassword(newPass)
                    .observe(getViewLifecycleOwner(), authResponse -> {
                        Toast.makeText(requireContext(),
                                authResponse != null ? "Password updated" : "Password update failed",
                                Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
