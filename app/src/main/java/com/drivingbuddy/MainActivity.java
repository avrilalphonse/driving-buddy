package com.drivingbuddy;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.View;

import com.drivingbuddy.ui.auth.AuthViewModel;
import com.drivingbuddy.utils.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        TokenManager tokenManager = new TokenManager(this);
        if (tokenManager.getToken() == null) {
            Intent intent = new Intent(this, com.drivingbuddy.ui.auth.AuthActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.custom_toolbar);
        setSupportActionBar(toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            if (tokenManager.getToken() != null) {
                // User is logged in, navigate to home
                navController.navigate(R.id.homeFragment);
            }

            if (bottomNavigationView != null) {
                // Replace the simple setup with custom navigation to prevent fragment recreation
                bottomNavigationView.setOnItemSelectedListener(item -> {
                    // Get the current destination
                    NavDestination currentDestination = navController.getCurrentDestination();

                    // If we're already on this destination, don't navigate again
                    if (currentDestination != null && currentDestination.getId() == item.getItemId()) {
                        return true;
                    }

                    // Navigate with options to prevent fragment recreation
                    NavOptions navOptions = new NavOptions.Builder()
                            .setLaunchSingleTop(true)
                            .setRestoreState(true)
                            .setPopUpTo(navController.getGraph().getStartDestinationId(),
                                    false,
                                    true) // saveState
                            .build();

                    try {
                        navController.navigate(item.getItemId(), null, navOptions);
                        return true;
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                });

                // Handle reselection (optional - prevents accidental double taps)
                bottomNavigationView.setOnItemReselectedListener(item -> {
                    // Do nothing on reselection to prevent fragment recreation
                });
            }

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int destination_id = destination.getId();
                if (destination_id == R.id.loginFragment || destination_id == R.id.signupFragment) {
                    bottomNavigationView.setVisibility(View.GONE);
                    toolbar.setVisibility(View.VISIBLE);
                } else {
                    bottomNavigationView.setVisibility(View.VISIBLE);
                    toolbar.setVisibility(View.VISIBLE);

                    // overriding navigation selection for profile
                    if (destination_id == R.id.profileFragment) {
                        Menu menu = bottomNavigationView.getMenu();
                        menu.findItem(R.id.settingsFragment).setChecked(true);
                    }

                }
            });
        }
    }

    public void logout() {
        AuthViewModel authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        authViewModel.logout();

        Intent intent = new Intent(this, com.drivingbuddy.ui.auth.AuthActivity.class);
        startActivity(intent);
        finish();
    }
}