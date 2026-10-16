package com.example.sheherse;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sheherse.adapter.CommunitySubmissionAdapter;
import com.example.sheherse.data.AuthRepository;
import com.example.sheherse.data.PlaceRepository;
import com.example.sheherse.model.AppUser;
import com.example.sheherse.model.CommunitySubmission;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class CommunityActivity extends AppCompatActivity {

    private MaterialToolbar toolbarCommunity;
    private ChipGroup chipGroupCommunityFilter;
    private RecyclerView rvCommunitySubmissions;
    private ExtendedFloatingActionButton fabAddCommunitySpot;

    private PlaceRepository placeRepository;
    private AuthRepository authRepository;
    private CommunitySubmissionAdapter submissionAdapter;

    private final List<CommunitySubmission> displayList = new ArrayList<>();
    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        placeRepository = PlaceRepository.getInstance(this);
        authRepository = AuthRepository.getInstance(this);

        initViews();
        setupFilters();
        loadSubmissions();
    }

    private void initViews() {
        toolbarCommunity = findViewById(R.id.toolbarCommunity);
        chipGroupCommunityFilter = findViewById(R.id.chipGroupCommunityFilter);
        rvCommunitySubmissions = findViewById(R.id.rvCommunitySubmissions);
        fabAddCommunitySpot = findViewById(R.id.fabAddCommunitySpot);

        toolbarCommunity.setNavigationOnClickListener(v -> finish());

        rvCommunitySubmissions.setLayoutManager(new LinearLayoutManager(this));
        submissionAdapter = new CommunitySubmissionAdapter(this, displayList, (submission, position) -> {
            placeRepository.upvoteSubmission(submission.getId());
            submission.setUpvotes(submission.getUpvotes() + 1);
            submissionAdapter.notifyItemChanged(position);
            Toast.makeText(this, "Upvoted! Thanks for supporting community contributors.", Toast.LENGTH_SHORT).show();
        });
        rvCommunitySubmissions.setAdapter(submissionAdapter);

        fabAddCommunitySpot.setOnClickListener(v -> showSubmitGemDialog());
    }

    private void setupFilters() {
        chipGroupCommunityFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipFilterApproved)) {
                currentFilter = "APPROVED";
            } else if (checkedIds.contains(R.id.chipFilterPending)) {
                currentFilter = "PENDING";
            } else {
                currentFilter = "ALL";
            }
            loadSubmissions();
        });
    }

    private void loadSubmissions() {
        List<CommunitySubmission> all = placeRepository.getCommunitySubmissions();
        displayList.clear();

        for (CommunitySubmission s : all) {
            if ("ALL".equals(currentFilter)) {
                displayList.add(s);
            } else if ("APPROVED".equals(currentFilter) && CommunitySubmission.STATUS_APPROVED.equalsIgnoreCase(s.getStatus())) {
                displayList.add(s);
            } else if ("PENDING".equals(currentFilter) && CommunitySubmission.STATUS_PENDING.equalsIgnoreCase(s.getStatus())) {
                displayList.add(s);
            }
        }

        submissionAdapter.updateSubmissions(displayList);
    }

    private void showSubmitGemDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_submit_gem, null);
        TextInputEditText etTitle = dialogView.findViewById(R.id.etSubmitTitle);
        TextInputEditText etArea = dialogView.findViewById(R.id.etSubmitArea);
        TextInputEditText etCategory = dialogView.findViewById(R.id.etSubmitCategory);
        TextInputEditText etCost = dialogView.findViewById(R.id.etSubmitCost);
        TextInputEditText etDesc = dialogView.findViewById(R.id.etSubmitDescription);
        TextInputEditText etTip = dialogView.findViewById(R.id.etSubmitTip);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Submit for Review", (dialog, which) -> {
                    String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
                    String area = etArea.getText() != null ? etArea.getText().toString().trim() : "";
                    String category = etCategory.getText() != null ? etCategory.getText().toString().trim() : "Hidden Gem";
                    String desc = etDesc.getText() != null ? etDesc.getText().toString().trim() : "";
                    String tip = etTip.getText() != null ? etTip.getText().toString().trim() : "";

                    if (title.isEmpty()) {
                        Toast.makeText(this, "Please enter a place name.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AppUser currentUser = authRepository.getCurrentUser();
                    String authorId = currentUser != null ? currentUser.getId() : "user_guest";
                    String authorName = currentUser != null ? currentUser.getName() : "Local Contributor";

                    String fullDesc = desc;
                    if (!tip.isEmpty()) {
                        fullDesc += "\n💡 Insider Tip: " + tip;
                    }

                    CommunitySubmission newSub = new CommunitySubmission(
                            "comm_" + System.currentTimeMillis(),
                            authorId,
                            authorName,
                            CommunitySubmission.TYPE_PLACE,
                            title,
                            fullDesc,
                            area.isEmpty() ? "Mumbai" : area,
                            category.isEmpty() ? "Hidden Gem" : category,
                            CommunitySubmission.STATUS_PENDING,
                            0
                    );

                    placeRepository.addCommunitySubmission(newSub);
                    Toast.makeText(this, "Spot submitted! Marked as 'Pending Review' until moderator check.", Toast.LENGTH_LONG).show();

                    // Switch filter to pending so user sees their new submission
                    chipGroupCommunityFilter.check(R.id.chipFilterPending);
                    currentFilter = "PENDING";
                    loadSubmissions();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
