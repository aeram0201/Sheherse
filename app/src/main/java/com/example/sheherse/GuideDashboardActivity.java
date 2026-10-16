package com.example.sheherse;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sheherse.adapter.EnquiryAdapter;
import com.example.sheherse.adapter.TourListingAdapter;
import com.example.sheherse.data.AuthRepository;
import com.example.sheherse.data.PlaceRepository;
import com.example.sheherse.model.AppUser;
import com.example.sheherse.model.Enquiry;
import com.example.sheherse.model.TourListing;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuideDashboardActivity extends AppCompatActivity {

    private MaterialToolbar toolbarGuide;
    private MaterialButton btnAddNewTour;
    private RecyclerView rvGuideTours;
    private RecyclerView rvGuideEnquiries;

    private PlaceRepository placeRepository;
    private AuthRepository authRepository;

    private TourListingAdapter tourAdapter;
    private EnquiryAdapter enquiryAdapter;

    private final List<TourListing> toursList = new ArrayList<>();
    private final List<Enquiry> enquiriesList = new ArrayList<>();
    private String guideId = "guide_farhan";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_dashboard);

        placeRepository = PlaceRepository.getInstance(this);
        authRepository = AuthRepository.getInstance(this);

        AppUser user = authRepository.getCurrentUser();
        if (user != null && AppUser.ROLE_GUIDE.equals(user.getRole())) {
            guideId = user.getId();
        }

        initViews();
        loadDashboardData();
    }

    private void initViews() {
        toolbarGuide = findViewById(R.id.toolbarGuide);
        btnAddNewTour = findViewById(R.id.btnAddNewTour);
        rvGuideTours = findViewById(R.id.rvGuideTours);
        rvGuideEnquiries = findViewById(R.id.rvGuideEnquiries);

        toolbarGuide.setNavigationOnClickListener(v -> finish());

        rvGuideTours.setLayoutManager(new LinearLayoutManager(this));
        tourAdapter = new TourListingAdapter(this, toursList, true, new TourListingAdapter.OnTourActionListener() {
            @Override
            public void onTourClick(TourListing tour) {
                Toast.makeText(GuideDashboardActivity.this, tour.getTitle() + " - ₹" + tour.getPricePerPerson(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(TourListing tour) {
                placeRepository.deleteTour(tour.getId());
                loadDashboardData();
                Toast.makeText(GuideDashboardActivity.this, "Tour removed.", Toast.LENGTH_SHORT).show();
            }
        });
        rvGuideTours.setAdapter(tourAdapter);

        rvGuideEnquiries.setLayoutManager(new LinearLayoutManager(this));
        enquiryAdapter = new EnquiryAdapter(this, enquiriesList);
        rvGuideEnquiries.setAdapter(enquiryAdapter);

        btnAddNewTour.setOnClickListener(v -> showAddTourDialog());
    }

    private void loadDashboardData() {
        toursList.clear();
        toursList.addAll(placeRepository.getTourListings());
        tourAdapter.updateTours(toursList);

        enquiriesList.clear();
        List<Enquiry> allEnquiries = placeRepository.getEnquiriesForRecipient(guideId);
        if (allEnquiries.isEmpty()) {
            allEnquiries = placeRepository.getEnquiriesForRecipient("guide_farhan");
        }
        if (allEnquiries.isEmpty()) {
            allEnquiries.add(new Enquiry(
                    "enq_demo_1",
                    guideId,
                    "user_arjun",
                    "Arjun Mehta",
                    "arjun.explorer@gmail.com",
                    "+91 98201 12345",
                    "This Saturday, 8:30 AM",
                    2,
                    "Looking forward to the South Mumbai Art Deco Walk! Are cameras permitted?"
            ));
        }
        enquiriesList.addAll(allEnquiries);
        enquiryAdapter.updateEnquiries(enquiriesList);
    }

    private void showAddTourDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_tour, null);
        TextInputEditText etTitle = dialogView.findViewById(R.id.etTourTitle);
        TextInputEditText etDesc = dialogView.findViewById(R.id.etTourDescription);
        TextInputEditText etDuration = dialogView.findViewById(R.id.etTourDuration);
        TextInputEditText etPrice = dialogView.findViewById(R.id.etTourPrice);
        TextInputEditText etMeeting = dialogView.findViewById(R.id.etTourMeetingPoint);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Publish Tour", (dialog, which) -> {
                    String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
                    String desc = etDesc.getText() != null ? etDesc.getText().toString().trim() : "";
                    String durationStr = etDuration.getText() != null ? etDuration.getText().toString().trim() : "2";
                    int duration = 2;
                    try {
                        duration = Integer.parseInt(durationStr.replaceAll("[^0-9]", ""));
                    } catch (Exception ignored) {}

                    int price = 400;
                    try {
                        price = Integer.parseInt(etPrice.getText().toString().trim());
                    } catch (Exception ignored) {}

                    String meeting = etMeeting.getText() != null ? etMeeting.getText().toString().trim() : "South Mumbai";

                    TourListing newTour = new TourListing(
                            "tour_" + System.currentTimeMillis(),
                            guideId,
                            "Farhan Merchant",
                            title.isEmpty() ? "Mumbai Heritage Trail" : title,
                            desc,
                            price,
                            duration,
                            meeting,
                            Arrays.asList("English", "Hindi"),
                            "South Mumbai",
                            4.9f
                    );
                    placeRepository.addTour(newTour);
                    loadDashboardData();
                    Toast.makeText(this, "Walking Tour published successfully!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
