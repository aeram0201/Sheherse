package com.example.sheherse;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.sheherse.data.AuthRepository;
import com.example.sheherse.model.AppUser;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class ProfileFragment extends Fragment {

    private TextView tvAvatarInitials;
    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private TextView tvProfileRole;

    private MaterialCardView cardRoleWorkspace;
    private ImageView ivWorkspaceIcon;
    private TextView tvWorkspaceTitle;
    private TextView tvWorkspaceSubtitle;
    private MaterialButton btnOpenWorkspace;

    private MaterialCardView btnSwitchExplorer;
    private ImageView ivExplorerCheck;
    private MaterialCardView btnSwitchGuide;
    private ImageView ivGuideCheck;
    private MaterialCardView btnSwitchOwner;
    private ImageView ivOwnerCheck;

    private MaterialButton btnSubmitGem;
    private MaterialButton btnLogout;

    private AuthRepository authRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authRepository = AuthRepository.getInstance(requireContext());

        initViews(view);
        setupListeners();
        updateUI();
    }

    private void initViews(View view) {
        tvAvatarInitials = view.findViewById(R.id.tvAvatarInitials);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvProfileRole = view.findViewById(R.id.tvProfileRole);

        cardRoleWorkspace = view.findViewById(R.id.cardRoleWorkspace);
        ivWorkspaceIcon = view.findViewById(R.id.ivWorkspaceIcon);
        tvWorkspaceTitle = view.findViewById(R.id.tvWorkspaceTitle);
        tvWorkspaceSubtitle = view.findViewById(R.id.tvWorkspaceSubtitle);
        btnOpenWorkspace = view.findViewById(R.id.btnOpenWorkspace);

        btnSwitchExplorer = view.findViewById(R.id.btnSwitchExplorer);
        ivExplorerCheck = view.findViewById(R.id.ivExplorerCheck);
        btnSwitchGuide = view.findViewById(R.id.btnSwitchGuide);
        ivGuideCheck = view.findViewById(R.id.ivGuideCheck);
        btnSwitchOwner = view.findViewById(R.id.btnSwitchOwner);
        ivOwnerCheck = view.findViewById(R.id.ivOwnerCheck);

        btnSubmitGem = view.findViewById(R.id.btnSubmitGem);
        btnLogout = view.findViewById(R.id.btnLogout);
    }

    private void setupListeners() {
        btnSwitchExplorer.setOnClickListener(v -> switchRole(AppUser.ROLE_EXPLORER));
        btnSwitchGuide.setOnClickListener(v -> switchRole(AppUser.ROLE_GUIDE));
        btnSwitchOwner.setOnClickListener(v -> switchRole(AppUser.ROLE_OWNER));

        btnSubmitGem.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CommunityActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Sign Out")
                    .setMessage("Are you sure you want to log out of SheherSe?")
                    .setPositiveButton("Sign Out", (dialog, which) -> {
                        authRepository.logout();
                        Toast.makeText(requireContext(), "Signed out.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(requireContext(), AuthActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        if (getActivity() != null) getActivity().finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void switchRole(String newRole) {
        authRepository.switchRole(newRole);
        AppUser user = authRepository.getCurrentUser();
        Toast.makeText(requireContext(), "Switched profile to " + user.getName() + " (" + user.getRoleDisplayName() + ")", Toast.LENGTH_SHORT).show();
        updateUI();
    }

    private void updateUI() {
        AppUser user = authRepository.getCurrentUser();
        if (user == null) return;

        tvAvatarInitials.setText(user.getAvatarInitials());
        tvProfileName.setText(user.getName());
        tvProfileEmail.setText(user.getEmail());
        tvProfileRole.setText(user.getRoleDisplayName());

        String currentRole = user.getRole();
        ivExplorerCheck.setVisibility(AppUser.ROLE_EXPLORER.equalsIgnoreCase(currentRole) ? View.VISIBLE : View.GONE);
        ivGuideCheck.setVisibility(AppUser.ROLE_GUIDE.equalsIgnoreCase(currentRole) ? View.VISIBLE : View.GONE);
        ivOwnerCheck.setVisibility(AppUser.ROLE_OWNER.equalsIgnoreCase(currentRole) ? View.VISIBLE : View.GONE);

        if (AppUser.ROLE_GUIDE.equalsIgnoreCase(currentRole)) {
            cardRoleWorkspace.setVisibility(View.VISIBLE);
            tvWorkspaceTitle.setText("Heritage Guide Control Center");
            tvWorkspaceSubtitle.setText("Manage walking tours, check booking requests and update rates.");
            btnOpenWorkspace.setText("Launch");
            btnOpenWorkspace.setOnClickListener(v -> startActivity(new Intent(requireContext(), GuideDashboardActivity.class)));
        } else if (AppUser.ROLE_OWNER.equalsIgnoreCase(currentRole)) {
            cardRoleWorkspace.setVisibility(View.VISIBLE);
            tvWorkspaceTitle.setText("Place Owner Control Center");
            tvWorkspaceSubtitle.setText("Manage opening hours, study facilities, and customer enquiries.");
            btnOpenWorkspace.setText("Launch");
            btnOpenWorkspace.setOnClickListener(v -> startActivity(new Intent(requireContext(), OwnerDashboardActivity.class)));
        } else {
            cardRoleWorkspace.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }
}
