package com.example.sheherse;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sheherse.data.AuthRepository;
import com.example.sheherse.data.PlaceRepository;
import com.example.sheherse.model.AppUser;
import com.example.sheherse.model.CommunitySubmission;
import com.example.sheherse.model.Enquiry;
import com.example.sheherse.model.Itinerary;
import com.example.sheherse.model.ItineraryStop;
import com.example.sheherse.model.Place;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Locale;

public class PlaceDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PLACE_ID = "extra_place_id";

    private PlaceRepository placeRepository;
    private AuthRepository authRepository;
    private Place place;

    private MaterialToolbar toolbarDetail;
    private TextView tvDetailEmoji;
    private TextView tvDetailCategory;
    private FloatingActionButton fabSavePlace;
    private TextView tvDetailTitle;
    private TextView tvDetailArea;
    private TextView tvDetailCost;
    private TextView tvDetailHours;
    private TextView tvDetailQuietness;
    private View layoutFacilities;
    private TextView tvDetailWifi;
    private TextView tvDetailCharging;
    private TextView tvDetailSeating;
    private TextView tvDetailDescription;
    private TextView tvDetailTip;
    private TextView tvDetailAddress;
    private TextView tvDetailBestTime;

    private MaterialButton btnDetailDirections;
    private MaterialButton btnDetailShare;
    private MaterialButton btnDetailAddToPlan;
    private MaterialButton btnDetailEnquire;
    private MaterialButton btnDetailReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        placeRepository = PlaceRepository.getInstance(this);
        authRepository = AuthRepository.getInstance(this);

        String placeId = getIntent().getStringExtra(EXTRA_PLACE_ID);
        if (placeId == null || placeId.isEmpty()) {
            Toast.makeText(this, "Place not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        place = placeRepository.getPlaceById(placeId);
        if (place == null) {
            Toast.makeText(this, "Place record not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        bindPlaceData();
        setupListeners();
    }

    private void initViews() {
        toolbarDetail = findViewById(R.id.toolbarDetail);
        tvDetailEmoji = findViewById(R.id.tvDetailEmoji);
        tvDetailCategory = findViewById(R.id.tvDetailCategory);
        fabSavePlace = findViewById(R.id.fabSavePlace);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailArea = findViewById(R.id.tvDetailArea);
        tvDetailCost = findViewById(R.id.tvDetailCost);
        tvDetailHours = findViewById(R.id.tvDetailHours);
        tvDetailQuietness = findViewById(R.id.tvDetailQuietness);
        layoutFacilities = findViewById(R.id.layoutFacilities);
        tvDetailWifi = findViewById(R.id.tvDetailWifi);
        tvDetailCharging = findViewById(R.id.tvDetailCharging);
        tvDetailSeating = findViewById(R.id.tvDetailSeating);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvDetailTip = findViewById(R.id.tvDetailTip);
        tvDetailAddress = findViewById(R.id.tvDetailAddress);
        tvDetailBestTime = findViewById(R.id.tvDetailBestTime);

        btnDetailDirections = findViewById(R.id.btnDetailDirections);
        btnDetailShare = findViewById(R.id.btnDetailShare);
        btnDetailAddToPlan = findViewById(R.id.btnDetailAddToPlan);
        btnDetailEnquire = findViewById(R.id.btnDetailEnquire);
        btnDetailReport = findViewById(R.id.btnDetailReport);

        toolbarDetail.setNavigationOnClickListener(v -> finish());
    }

    private void bindPlaceData() {
        toolbarDetail.setTitle(place.getName());

        tvDetailTitle.setText(place.getName());
        tvDetailArea.setText("📍 " + place.getArea());
        tvDetailCategory.setText(place.getCategory() + " • " + place.getVerificationStatus());

        // Emoji category indicator
        String cat = place.getCategory() != null ? place.getCategory().toLowerCase() : "";
        if (cat.contains("food") || place.isAffordableFood()) {
            tvDetailEmoji.setText("☕");
        } else if (cat.contains("study") || place.isStudyWork()) {
            tvDetailEmoji.setText("📚");
        } else if (cat.contains("nature") || cat.contains("sunset")) {
            tvDetailEmoji.setText("🌊");
        } else if (cat.contains("market")) {
            tvDetailEmoji.setText("🌺");
        } else {
            tvDetailEmoji.setText("🏛️");
        }

        // Cost
        if (place.getEstimatedCost() == 0) {
            tvDetailCost.setText("₹0 (Free)");
        } else {
            String est = place.isPriceEstimated() ? " (Est.)" : "";
            tvDetailCost.setText(String.format(Locale.getDefault(), "₹%d%s", place.getEstimatedCost(), est));
        }

        // Hours & Quietness
        tvDetailHours.setText(place.getOpeningHours() != null && !place.getOpeningHours().isEmpty()
                ? place.getOpeningHours()
                : "Hours not verified");
        tvDetailQuietness.setText(place.getQuietnessLevel() != null && !place.getQuietnessLevel().isEmpty()
                ? place.getQuietnessLevel()
                : "Moderate");

        // Facilities
        List<String> facs = place.getFacilities();
        boolean hasWifi = facs != null && (facs.contains("WiFi") || facs.contains("wifi"));
        boolean hasCharging = facs != null && (facs.contains("Charging") || facs.contains("charging"));
        boolean hasSeating = facs != null && (facs.contains("Seating") || facs.contains("seating"));

        tvDetailWifi.setVisibility(hasWifi ? View.VISIBLE : View.GONE);
        tvDetailCharging.setVisibility(hasCharging ? View.VISIBLE : View.GONE);
        tvDetailSeating.setVisibility(hasSeating ? View.VISIBLE : View.GONE);

        // Description and Tips
        tvDetailDescription.setText(place.getDescription());
        tvDetailTip.setText(place.getInsiderTip() != null && !place.getInsiderTip().isEmpty()
                ? place.getInsiderTip()
                : "Best explored early morning or late afternoon for gentle sea breezes and golden hour light.");

        tvDetailAddress.setText(place.getAddress() != null && !place.getAddress().isEmpty()
                ? place.getAddress()
                : place.getArea() + ", Mumbai, Maharashtra");

        int duration = place.getDurationMinutes() > 0 ? place.getDurationMinutes() : 60;
        tvDetailBestTime.setText(String.format(Locale.getDefault(), "Suggested visit duration: %d minutes", duration));

        updateSaveIcon();
    }

    private void updateSaveIcon() {
        boolean isSaved = placeRepository.isPlaceSaved(place.getId());
        if (isSaved) {
            fabSavePlace.setImageResource(R.drawable.ic_heart_filled);
        } else {
            fabSavePlace.setImageResource(R.drawable.ic_heart_outline);
        }
    }

    private void setupListeners() {
        fabSavePlace.setOnClickListener(v -> {
            boolean nowSaved = placeRepository.toggleSavePlace(place.getId());
            updateSaveIcon();
            String msg = nowSaved ? "Saved to favourites!" : "Removed from favourites.";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        btnDetailDirections.setOnClickListener(v -> openDirections());

        btnDetailShare.setOnClickListener(v -> sharePlace());

        btnDetailAddToPlan.setOnClickListener(v -> addToPlan());

        btnDetailEnquire.setOnClickListener(v -> showEnquiryDialog());

        btnDetailReport.setOnClickListener(v -> showSuggestUpdateDialog());
    }

    private void addToPlan() {
        List<Itinerary> itineraries = placeRepository.getSavedItineraries();
        Itinerary target;
        if (itineraries.isEmpty()) {
            target = new Itinerary("itin_current", "My Mumbai Outing", 500, null);
        } else {
            target = itineraries.get(0);
        }

        // Prevent duplicates
        for (ItineraryStop stop : target.getStops()) {
            if (stop.getPlace() != null && stop.getPlace().getId().equals(place.getId())) {
                Toast.makeText(this, place.getName() + " is already in your day plan!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        ItineraryStop stop = new ItineraryStop("stop_" + System.currentTimeMillis(), place, target.getStops().size());
        target.getStops().add(stop);
        placeRepository.saveItinerary(target);

        Toast.makeText(this, "Added " + place.getName() + " to My Day plan!", Toast.LENGTH_SHORT).show();
    }

    private void openDirections() {
        String query = place.getName() + ", " + place.getAddress() + ", Mumbai";
        Uri mapUri;
        if (place.getLatitude() != 0.0 || place.getLongitude() != 0.0) {
            mapUri = Uri.parse("geo:" + place.getLatitude() + "," + place.getLongitude() + "?q=" + Uri.encode(query));
        } else {
            mapUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(query));
        }

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
        try {
            startActivity(mapIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No maps app available on device.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sharePlace() {
        String cost = place.getEstimatedCost() == 0 ? "Free" : "₹" + place.getEstimatedCost();
        String shareBody = place.getName() + " (" + place.getArea() + ")\n"
                + place.getDescription() + "\n\n"
                + "Estimated Cost: " + cost + "\n"
                + "Insider Tip: " + place.getInsiderTip() + "\n\n"
                + "Discovered on SheherSe • One Mumbai. Many ways to explore.";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, place.getName());
        intent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(intent, "Share Discovery"));
    }

    private void showEnquiryDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_enquiry, null);
        TextView tvTitle = dialogView.findViewById(R.id.tvEnquiryTargetTitle);
        TextInputEditText etName = dialogView.findViewById(R.id.etEnquiryName);
        TextInputEditText etEmail = dialogView.findViewById(R.id.etEnquiryEmail);
        TextInputEditText etPhone = dialogView.findViewById(R.id.etEnquiryPhone);
        TextInputEditText etDate = dialogView.findViewById(R.id.etEnquiryDate);
        TextInputEditText etParty = dialogView.findViewById(R.id.etEnquiryPartySize);
        TextInputEditText etMsg = dialogView.findViewById(R.id.etEnquiryMessage);

        AppUser currentUser = authRepository.getCurrentUser();
        if (currentUser != null) {
            etName.setText(currentUser.getName());
            etEmail.setText(currentUser.getEmail());
        }

        tvTitle.setText("Contact " + place.getName());

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Send Request", (dialog, which) -> {
                    String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                    String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
                    String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
                    String date = etDate.getText() != null ? etDate.getText().toString().trim() : "";
                    int party = 2;
                    try {
                        party = Integer.parseInt(etParty.getText().toString().trim());
                    } catch (Exception ignored) {}
                    String msg = etMsg.getText() != null ? etMsg.getText().toString().trim() : "";

                    Enquiry enquiry = new Enquiry(
                            "enq_" + System.currentTimeMillis(),
                            place.getOwnerId() != null ? place.getOwnerId() : "owner_kyani",
                            currentUser != null ? currentUser.getId() : "guest",
                            name,
                            email,
                            phone,
                            date,
                            party,
                            msg
                    );
                    placeRepository.addEnquiry(enquiry);
                    Toast.makeText(this, "Enquiry submitted to host successfully!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showSuggestUpdateDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_submit_gem, null);
        TextInputEditText etTitle = dialogView.findViewById(R.id.etSubmitTitle);
        TextInputEditText etArea = dialogView.findViewById(R.id.etSubmitArea);
        TextInputEditText etCat = dialogView.findViewById(R.id.etSubmitCategory);
        TextInputEditText etCost = dialogView.findViewById(R.id.etSubmitCost);
        TextInputEditText etDesc = dialogView.findViewById(R.id.etSubmitDescription);
        TextInputEditText etTip = dialogView.findViewById(R.id.etSubmitTip);

        etTitle.setText(place.getName());
        etArea.setText(place.getArea());
        etCat.setText(place.getCategory());
        etCost.setText(String.valueOf(place.getEstimatedCost()));
        etDesc.setText(place.getDescription());
        etTip.setText(place.getInsiderTip());

        new AlertDialog.Builder(this)
                .setTitle("Suggest Update / Correction")
                .setView(dialogView)
                .setPositiveButton("Submit for Review", (dialog, which) -> {
                    AppUser user = authRepository.getCurrentUser();
                    String desc = etDesc.getText() != null ? etDesc.getText().toString().trim() : "";
                    String tip = etTip.getText() != null ? etTip.getText().toString().trim() : "";

                    CommunitySubmission sub = new CommunitySubmission(
                            "sub_" + System.currentTimeMillis(),
                            user != null ? user.getId() : "user_anon",
                            user != null ? user.getName() : "Local Contributor",
                            CommunitySubmission.TYPE_TIP,
                            "Correction: " + place.getName(),
                            desc + " [Tip: " + tip + "]",
                            place.getArea(),
                            place.getCategory(),
                            CommunitySubmission.STATUS_PENDING,
                            0
                    );
                    placeRepository.addCommunitySubmission(sub);
                    Toast.makeText(this, "Correction submitted! Status: Pending Review.", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
