package com.example.sheherse;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sheherse.adapter.EnquiryAdapter;
import com.example.sheherse.data.AuthRepository;
import com.example.sheherse.data.PlaceRepository;
import com.example.sheherse.model.AppUser;
import com.example.sheherse.model.Enquiry;
import com.example.sheherse.model.Place;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class OwnerDashboardActivity extends AppCompatActivity {

    private MaterialToolbar toolbarOwner;
    private TextInputEditText etOwnerHours;
    private TextInputEditText etOwnerCost;
    private MaterialCheckBox cbOwnerWifi;
    private MaterialCheckBox cbOwnerCharging;
    private MaterialCheckBox cbOwnerSeating;
    private MaterialButton btnSaveOwnerUpdates;
    private RecyclerView rvOwnerEnquiries;

    private PlaceRepository placeRepository;
    private AuthRepository authRepository;
    private EnquiryAdapter enquiryAdapter;
    private final List<Enquiry> enquiriesList = new ArrayList<>();
    private Place managedPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_dashboard);

        placeRepository = PlaceRepository.getInstance(this);
        authRepository = AuthRepository.getInstance(this);

        initViews();
        loadPlaceData();
        loadEnquiries();
    }

    private void initViews() {
        toolbarOwner = findViewById(R.id.toolbarOwner);
        etOwnerHours = findViewById(R.id.etOwnerHours);
        etOwnerCost = findViewById(R.id.etOwnerCost);
        cbOwnerWifi = findViewById(R.id.cbOwnerWifi);
        cbOwnerCharging = findViewById(R.id.cbOwnerCharging);
        cbOwnerSeating = findViewById(R.id.cbOwnerSeating);
        btnSaveOwnerUpdates = findViewById(R.id.btnSaveOwnerUpdates);
        rvOwnerEnquiries = findViewById(R.id.rvOwnerEnquiries);

        toolbarOwner.setNavigationOnClickListener(v -> finish());

        rvOwnerEnquiries.setLayoutManager(new LinearLayoutManager(this));
        enquiryAdapter = new EnquiryAdapter(this, enquiriesList);
        rvOwnerEnquiries.setAdapter(enquiryAdapter);

        btnSaveOwnerUpdates.setOnClickListener(v -> saveUpdates());
    }

    private void loadPlaceData() {
        managedPlace = placeRepository.getPlaceById("place_kyani");
        if (managedPlace == null) {
            managedPlace = placeRepository.getPlaceById("place_kitab_khana");
        }

        if (managedPlace != null) {
            toolbarOwner.setTitle("Managing: " + managedPlace.getName());
            etOwnerHours.setText(managedPlace.getOpeningHours());
            etOwnerCost.setText(String.valueOf(managedPlace.getEstimatedCost()));

            List<String> facs = managedPlace.getFacilities();
            cbOwnerWifi.setChecked(facs != null && (facs.contains("WiFi") || facs.contains("wifi")));
            cbOwnerCharging.setChecked(facs != null && (facs.contains("Charging") || facs.contains("charging")));
            cbOwnerSeating.setChecked(facs != null && (facs.contains("Seating") || facs.contains("seating")));
        }
    }

    private void saveUpdates() {
        if (managedPlace == null) return;

        String hours = etOwnerHours.getText() != null ? etOwnerHours.getText().toString().trim() : "";
        int cost = 100;
        try {
            cost = Integer.parseInt(etOwnerCost.getText().toString().trim());
        } catch (Exception ignored) {}

        managedPlace.setOpeningHours(hours);
        managedPlace.setEstimatedCost(cost);
        managedPlace.setPriceEstimated(false);

        List<String> facs = new ArrayList<>();
        if (cbOwnerWifi.isChecked()) facs.add("WiFi");
        if (cbOwnerCharging.isChecked()) facs.add("Charging");
        if (cbOwnerSeating.isChecked()) facs.add("Seating");
        facs.add("AC");
        managedPlace.setFacilities(facs);

        placeRepository.updatePlace(managedPlace);
        Toast.makeText(this, "Live details updated across SheherSe discovery feeds!", Toast.LENGTH_SHORT).show();
    }

    private void loadEnquiries() {
        enquiriesList.clear();
        String ownerId = "owner_kyani";
        AppUser user = authRepository.getCurrentUser();
        if (user != null && AppUser.ROLE_OWNER.equals(user.getRole())) {
            ownerId = user.getId();
        }

        List<Enquiry> list = placeRepository.getEnquiriesForRecipient(ownerId);
        if (list.isEmpty()) {
            list = placeRepository.getEnquiriesForRecipient("owner_kyani");
        }
        if (list.isEmpty()) {
            list.add(new Enquiry(
                    "enq_owner_1",
                    ownerId,
                    "student_sameer",
                    "Sameer Khan",
                    "sameer.student@mumbaiuniv.ac.in",
                    "+91 98331 45678",
                    "Tomorrow, 4:00 PM",
                    4,
                    "Can we reserve a quiet corner table with power sockets for a study group session?"
            ));
        }
        enquiriesList.addAll(list);
        enquiryAdapter.updateEnquiries(enquiriesList);
    }
}
