package com.example.sheherse;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.sheherse.data.AuthRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements HomeFragment.OnNavigationRequest {

    private BottomNavigationView bottomNavigation;
    private AuthRepository authRepository;

    private HomeFragment homeFragment;
    private ExploreFragment exploreFragment;
    private SavedFragment savedFragment;
    private PlannerFragment plannerFragment;
    private ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authRepository = AuthRepository.getInstance(this);

        // If not logged in, redirect to AuthActivity
        if (!authRepository.isLoggedIn()) {
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        View rootView = findViewById(R.id.main_root);
        ViewCompat.setOnApplyWindowInsetsListener(
                rootView,
                (view, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );
                    view.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            0
                    );
                    return insets;
                }
        );

        initFragments();
        setupBottomNavigation();

        if (savedInstanceState == null) {
            loadFragment(homeFragment);
        }
    }

    private void initFragments() {
        homeFragment = new HomeFragment();
        homeFragment.setNavigationListener(this);

        exploreFragment = new ExploreFragment();

        savedFragment = new SavedFragment();
        savedFragment.setNavigationListener(this);

        plannerFragment = new PlannerFragment();

        profileFragment = new ProfileFragment();
    }

    private void setupBottomNavigation() {
        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    loadFragment(homeFragment);
                    return true;
                } else if (itemId == R.id.nav_explore) {
                    loadFragment(exploreFragment);
                    return true;
                } else if (itemId == R.id.nav_saved) {
                    loadFragment(savedFragment);
                    return true;
                } else if (itemId == R.id.nav_planner) {
                    loadFragment(plannerFragment);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    loadFragment(profileFragment);
                    return true;
                }

                return false;
            });
        }
    }

    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    public void navigateToTab(int menuItemId) {
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(menuItemId);
        }
    }
}
