package com.example.sheherse;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sheherse.data.AuthRepository;
import com.example.sheherse.model.AppUser;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AuthActivity extends AppCompatActivity {

    private boolean isRegisterMode = false;
    private AuthRepository authRepository;

    private MaterialButton btnTabLogin;
    private MaterialButton btnTabRegister;
    private TextView tvAuthTitle;
    private TextView tvAuthSubtitle;
    private TextView tvAuthError;

    private TextInputLayout tilAuthName;
    private TextInputEditText etAuthName;
    private TextInputLayout tilAuthEmail;
    private TextInputEditText etAuthEmail;
    private TextInputLayout tilAuthPassword;
    private TextInputEditText etAuthPassword;
    private TextInputLayout tilAuthConfirmPassword;
    private TextInputEditText etAuthConfirmPassword;

    private View layoutRoleSelection;
    private Spinner spinnerRole;
    private MaterialCheckBox cbTerms;
    private TextView tvForgotPassword;
    private MaterialButton btnAuthSubmit;

    private static final String[] ROLES_DISPLAY = {
            "Explorer / Tourist",
            "Student / Freelancer",
            "Mumbai Resident",
            "Local Business Owner",
            "Event Organizer"
    };

    private static final String[] ROLES_INTERNAL = {
            AppUser.ROLE_EXPLORER,
            AppUser.ROLE_STUDENT,
            AppUser.ROLE_RESIDENT,
            AppUser.ROLE_OWNER,
            AppUser.ROLE_ORGANIZER
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        authRepository = AuthRepository.getInstance(this);

        initViews();
        setupListeners();
        updateFormMode(false);
    }

    private void initViews() {
        btnTabLogin = findViewById(R.id.btnTabLogin);
        btnTabRegister = findViewById(R.id.btnTabRegister);
        tvAuthTitle = findViewById(R.id.tvAuthTitle);
        tvAuthSubtitle = findViewById(R.id.tvAuthSubtitle);
        tvAuthError = findViewById(R.id.tvAuthError);

        tilAuthName = findViewById(R.id.tilAuthName);
        etAuthName = findViewById(R.id.etAuthName);
        tilAuthEmail = findViewById(R.id.tilAuthEmail);
        etAuthEmail = findViewById(R.id.etAuthEmail);
        tilAuthPassword = findViewById(R.id.tilAuthPassword);
        etAuthPassword = findViewById(R.id.etAuthPassword);
        tilAuthConfirmPassword = findViewById(R.id.tilAuthConfirmPassword);
        etAuthConfirmPassword = findViewById(R.id.etAuthConfirmPassword);

        layoutRoleSelection = findViewById(R.id.layoutRoleSelection);
        spinnerRole = findViewById(R.id.spinnerRole);
        cbTerms = findViewById(R.id.cbTerms);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnAuthSubmit = findViewById(R.id.btnAuthSubmit);

        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                ROLES_DISPLAY
        );
        spinnerRole.setAdapter(roleAdapter);
    }

    private void setupListeners() {
        btnTabLogin.setOnClickListener(v -> updateFormMode(false));
        btnTabRegister.setOnClickListener(v -> updateFormMode(true));

        btnAuthSubmit.setOnClickListener(v -> handleAuthSubmit());

        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());

        // Instant demo logins
        findViewById(R.id.btnDemoExplorer).setOnClickListener(v -> launchDemo(AppUser.ROLE_EXPLORER));
        findViewById(R.id.btnDemoStudent).setOnClickListener(v -> launchDemo(AppUser.ROLE_STUDENT));
        findViewById(R.id.btnDemoResident).setOnClickListener(v -> launchDemo(AppUser.ROLE_RESIDENT));
        findViewById(R.id.btnDemoGuide).setOnClickListener(v -> launchDemo(AppUser.ROLE_GUIDE));
        findViewById(R.id.btnDemoOwner).setOnClickListener(v -> launchDemo(AppUser.ROLE_OWNER));
        findViewById(R.id.btnDemoOrganizer).setOnClickListener(v -> launchDemo(AppUser.ROLE_ORGANIZER));
    }

    private void updateFormMode(boolean register) {
        this.isRegisterMode = register;
        tvAuthError.setVisibility(View.GONE);

        if (register) {
            btnTabRegister.setBackgroundTintList(getColorStateList(R.color.forest_green_primary));
            btnTabRegister.setTextColor(getColor(R.color.white));
            btnTabLogin.setBackgroundTintList(getColorStateList(android.R.color.transparent));
            btnTabLogin.setTextColor(getColor(R.color.text_secondary));

            tvAuthTitle.setText("Create SheherSe Account");
            tvAuthSubtitle.setText("Join fellow Mumbai explorers, students, residents and local businesses.");

            tilAuthName.setVisibility(View.VISIBLE);
            tilAuthConfirmPassword.setVisibility(View.VISIBLE);
            layoutRoleSelection.setVisibility(View.VISIBLE);
            cbTerms.setVisibility(View.VISIBLE);
            tvForgotPassword.setVisibility(View.GONE);
            btnAuthSubmit.setText("Create Account");
        } else {
            btnTabLogin.setBackgroundTintList(getColorStateList(R.color.forest_green_primary));
            btnTabLogin.setTextColor(getColor(R.color.white));
            btnTabRegister.setBackgroundTintList(getColorStateList(android.R.color.transparent));
            btnTabRegister.setTextColor(getColor(R.color.text_secondary));

            tvAuthTitle.setText("Welcome Back");
            tvAuthSubtitle.setText("Sign in to save spots, plan your day, and discover Mumbai.");

            tilAuthName.setVisibility(View.GONE);
            tilAuthConfirmPassword.setVisibility(View.GONE);
            layoutRoleSelection.setVisibility(View.GONE);
            cbTerms.setVisibility(View.GONE);
            tvForgotPassword.setVisibility(View.VISIBLE);
            btnAuthSubmit.setText("Sign In");
        }
    }

    private void handleAuthSubmit() {
        tvAuthError.setVisibility(View.GONE);

        String email = etAuthEmail.getText() != null ? etAuthEmail.getText().toString().trim() : "";
        String password = etAuthPassword.getText() != null ? etAuthPassword.getText().toString().trim() : "";

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Please enter a valid email address.");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters long.");
            return;
        }

        if (isRegisterMode) {
            String name = etAuthName.getText() != null ? etAuthName.getText().toString().trim() : "";
            String confirmPassword = etAuthConfirmPassword.getText() != null ? etAuthConfirmPassword.getText().toString().trim() : "";

            if (name.isEmpty()) {
                showError("Please enter your full name.");
                return;
            }

            if (!password.equals(confirmPassword)) {
                showError("Passwords do not match. Please re-enter.");
                return;
            }

            if (!cbTerms.isChecked()) {
                showError("Please acknowledge the SheherSe community terms to continue.");
                return;
            }

            int selectedRoleIndex = spinnerRole.getSelectedItemPosition();
            String role = (selectedRoleIndex >= 0 && selectedRoleIndex < ROLES_INTERNAL.length)
                    ? ROLES_INTERNAL[selectedRoleIndex]
                    : AppUser.ROLE_EXPLORER;

            AppUser user = authRepository.register(name, email, password, role);
            if (user != null) {
                Toast.makeText(this, "Welcome to SheherSe, " + user.getName() + "!", Toast.LENGTH_SHORT).show();
                proceedToMain();
            } else {
                showError("Failed to register. Please try again.");
            }
        } else {
            boolean success = authRepository.login(email, password);
            if (success) {
                AppUser user = authRepository.getCurrentUser();
                Toast.makeText(this, "Welcome back, " + user.getName() + "!", Toast.LENGTH_SHORT).show();
                proceedToMain();
            } else {
                showError("Invalid credentials. You can also use the 1-Tap Demo buttons below.");
            }
        }
    }

    private void launchDemo(String role) {
        AppUser user = authRepository.loginDemo(role);
        Toast.makeText(this, "Logged in as " + user.getName() + " (" + user.getRoleDisplayName() + ")", Toast.LENGTH_SHORT).show();
        proceedToMain();
    }

    private void proceedToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        tvAuthError.setText(message);
        tvAuthError.setVisibility(View.VISIBLE);
    }

    private void showForgotPasswordDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Password Recovery")
                .setMessage("SheherSe is currently running in local prototype mode. Real-world email password recovery requires backend cloud services. For immediate testing, you can sign in with any email and password or use the 1-Tap Demo Access options below.")
                .setPositiveButton("Understood", null)
                .show();
    }
}
