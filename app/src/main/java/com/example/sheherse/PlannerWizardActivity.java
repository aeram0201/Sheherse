package com.example.sheherse;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sheherse.data.PlaceRepository;
import com.example.sheherse.data.PlannerEngine;
import com.example.sheherse.model.Itinerary;
import com.example.sheherse.model.Place;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PlannerWizardActivity extends AppCompatActivity {

    public static final String EXTRA_GENERATED_ITINERARY = "extra_generated_itinerary";

    private MaterialToolbar toolbarWizard;
    private TextView tvStepNumber;
    private TextView tvStepTitle;
    private LinearProgressIndicator progressBarWizard;

    // 5 step container views
    private LinearLayout step1Layout;
    private LinearLayout step2Layout;
    private LinearLayout step3Layout;
    private LinearLayout step4Layout;
    private LinearLayout step5Layout;

    private MaterialButton btnWizardBack;
    private MaterialButton btnWizardNext;

    // Step 1 controls
    private MaterialButton btnPickDate;
    private MaterialButton btnPickStartTime;
    private MaterialButton btnPickFinishTime;
    private TextView tvDurationSummary;
    private SwitchMaterial switchFlexibleSchedule;
    private Calendar selectedDate = Calendar.getInstance();
    private int startHour = 9;
    private int startMinute = 0;
    private int finishHour = 18;
    private int finishMinute = 0;

    // Step 2 controls
    private Spinner spinnerStartNeighbourhood;
    private EditText etCustomStartLocation;
    private Spinner spinnerFinishNeighbourhood;
    private MaterialButton btnSelectMustVisitPlaces;
    private final List<String> selectedMustVisitIds = new ArrayList<>();

    // Step 3 controls
    private ChipGroup chipGroupInterests;
    private EditText etSpecificNotes;

    // Step 4 controls
    private ChipGroup chipGroupBudgetPresets;
    private View layoutCustomBudget;
    private EditText etCustomBudget;
    private TextView tvGroupSizeCount;
    private MaterialButton btnDecGroupSize;
    private MaterialButton btnIncGroupSize;
    private int groupSize = 1;
    private ChipGroup chipGroupFoodPreference;
    private SwitchMaterial switchIncludeFoodCosts;

    // Step 5 controls
    private ChipGroup chipGroupTransport;
    private ChipGroup chipGroupPace;
    private ChipGroup chipGroupTravelerType;
    private ChipGroup chipGroupAccessibility;

    private int currentStep = 1;
    private final int TOTAL_STEPS = 5;

    // Neighbourhood data
    private static final String[] NEIGHBOURHOODS = {
            "Colaba / Fort (South Mumbai)",
            "Marine Drive / Nariman Point",
            "Malabar Hill / Walkeshwar",
            "Girgaon / Khotachiwadi",
            "Dadar (Central Mumbai)",
            "Bandra West (Suburbs)",
            "Juhu / Andheri West",
            "BKC (Bandra Kurla Complex)",
            "Borivali / SGNP (North Mumbai)",
            "Powai (Central Suburbs)"
    };

    private static final double[][] NEIGHBOURHOOD_COORDS = {
            {18.9220, 72.8347},
            {18.9438, 72.8231},
            {18.9452, 72.7937},
            {18.9547, 72.8183},
            {19.0189, 72.8427},
            {19.0558, 72.8285},
            {19.1075, 72.8263},
            {19.0665, 72.8687},
            {19.2064, 72.9065},
            {19.1176, 72.9060}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planner_wizard);

        initViews();
        setupStep1();
        setupStep2();
        setupStep3();
        setupStep4();
        setupStep5();
        setupNavigation();
        updateStepUI();
    }

    private void initViews() {
        toolbarWizard = findViewById(R.id.toolbarWizard);
        tvStepNumber = findViewById(R.id.tvStepNumber);
        tvStepTitle = findViewById(R.id.tvStepTitle);
        progressBarWizard = findViewById(R.id.progressBarWizard);

        step1Layout = findViewById(R.id.step1Layout);
        step2Layout = findViewById(R.id.step2Layout);
        step3Layout = findViewById(R.id.step3Layout);
        step4Layout = findViewById(R.id.step4Layout);
        step5Layout = findViewById(R.id.step5Layout);

        btnWizardBack = findViewById(R.id.btnWizardBack);
        btnWizardNext = findViewById(R.id.btnWizardNext);

        toolbarWizard.setNavigationOnClickListener(v -> finish());
        toolbarWizard.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_backend_settings) {
                showBackendSettingsDialog();
                return true;
            }
            return false;
        });
    }

    private void setupStep1() {
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickStartTime = findViewById(R.id.btnPickStartTime);
        btnPickFinishTime = findViewById(R.id.btnPickFinishTime);
        tvDurationSummary = findViewById(R.id.tvDurationSummary);
        switchFlexibleSchedule = findViewById(R.id.switchFlexibleSchedule);

        // Default tomorrow
        selectedDate.add(Calendar.DAY_OF_YEAR, 1);
        updateDateButton();

        btnPickDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, month);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateButton();
            }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnPickStartTime.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                startHour = hourOfDay;
                startMinute = minute;
                updateTimeButtons();
            }, startHour, startMinute, false).show();
        });

        btnPickFinishTime.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                finishHour = hourOfDay;
                finishMinute = minute;
                updateTimeButtons();
            }, finishHour, finishMinute, false).show();
        });

        updateTimeButtons();
    }

    private void updateDateButton() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault());
        btnPickDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void updateTimeButtons() {
        btnPickStartTime.setText(formatTime(startHour, startMinute));
        btnPickFinishTime.setText(formatTime(finishHour, finishMinute));

        int durationMin = (finishHour * 60 + finishMinute) - (startHour * 60 + startMinute);
        if (durationMin <= 0) {
            tvDurationSummary.setText("⚠️ Finish time must be later than start time!");
            tvDurationSummary.setTextColor(getColor(R.color.vibrant_coral));
        } else {
            float hours = durationMin / 60.0f;
            tvDurationSummary.setText(String.format(Locale.getDefault(), "⏱ Total available window: %.1f hours (%d mins)", hours, durationMin));
            tvDurationSummary.setTextColor(getColor(R.color.teal_secondary));
        }
    }

    private String formatTime(int h, int m) {
        String ampm = h >= 12 ? "PM" : "AM";
        int displayH = h % 12;
        if (displayH == 0) displayH = 12;
        return String.format(Locale.getDefault(), "%02d:%02d %s", displayH, m, ampm);
    }

    private void setupStep2() {
        spinnerStartNeighbourhood = findViewById(R.id.spinnerStartNeighbourhood);
        etCustomStartLocation = findViewById(R.id.etCustomStartLocation);
        spinnerFinishNeighbourhood = findViewById(R.id.spinnerFinishNeighbourhood);
        btnSelectMustVisitPlaces = findViewById(R.id.btnSelectMustVisitPlaces);

        ArrayAdapter<String> startAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, NEIGHBOURHOODS);
        spinnerStartNeighbourhood.setAdapter(startAdapter);

        List<String> finishOptions = new ArrayList<>();
        finishOptions.add("Flexible / Same as start area");
        for (String n : NEIGHBOURHOODS) finishOptions.add(n);
        ArrayAdapter<String> finishAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, finishOptions);
        spinnerFinishNeighbourhood.setAdapter(finishAdapter);

        btnSelectMustVisitPlaces.setOnClickListener(v -> showMustVisitPicker());
    }

    private void showMustVisitPicker() {
        PlaceRepository repo = PlaceRepository.getInstance(this);
        List<Place> places = repo.getAllPlaces();
        String[] names = new String[places.size()];
        boolean[] checked = new boolean[places.size()];

        for (int i = 0; i < places.size(); i++) {
            names[i] = places.get(i).getName() + " (" + places.get(i).getArea() + ")";
            checked[i] = selectedMustVisitIds.contains(places.get(i).getId());
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Must-Visit Places")
                .setMultiChoiceItems(names, checked, (dialog, which, isChecked) -> {
                    String id = places.get(which).getId();
                    if (isChecked && !selectedMustVisitIds.contains(id)) {
                        selectedMustVisitIds.add(id);
                    } else if (!isChecked) {
                        selectedMustVisitIds.remove(id);
                    }
                })
                .setPositiveButton("Done", (dialog, which) -> {
                    btnSelectMustVisitPlaces.setText(selectedMustVisitIds.size() + " must-visit spot(s) selected");
                })
                .show();
    }

    private void setupStep3() {
        chipGroupInterests = findViewById(R.id.chipGroupInterests);
        etSpecificNotes = findViewById(R.id.etSpecificNotes);

        // Pre-select popular interests
        findViewById(R.id.chipInterestHeritage).performClick();
        findViewById(R.id.chipInterestSunset).performClick();
        findViewById(R.id.chipInterestFood).performClick();
    }

    private void setupStep4() {
        chipGroupBudgetPresets = findViewById(R.id.chipGroupBudgetPresets);
        layoutCustomBudget = findViewById(R.id.layoutCustomBudget);
        etCustomBudget = findViewById(R.id.etCustomBudget);
        tvGroupSizeCount = findViewById(R.id.tvGroupSizeCount);
        btnDecGroupSize = findViewById(R.id.btnDecGroupSize);
        btnIncGroupSize = findViewById(R.id.btnIncGroupSize);
        chipGroupFoodPreference = findViewById(R.id.chipGroupFoodPreference);
        switchIncludeFoodCosts = findViewById(R.id.switchIncludeFoodCosts);

        chipGroupBudgetPresets.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipBudgetCustom)) {
                layoutCustomBudget.setVisibility(View.VISIBLE);
            } else {
                layoutCustomBudget.setVisibility(View.GONE);
            }
        });

        btnDecGroupSize.setOnClickListener(v -> {
            if (groupSize > 1) {
                groupSize--;
                tvGroupSizeCount.setText(String.valueOf(groupSize));
            }
        });

        btnIncGroupSize.setOnClickListener(v -> {
            if (groupSize < 20) {
                groupSize++;
                tvGroupSizeCount.setText(String.valueOf(groupSize));
            }
        });
    }

    private void setupStep5() {
        chipGroupTransport = findViewById(R.id.chipGroupTransport);
        chipGroupPace = findViewById(R.id.chipGroupPace);
        chipGroupTravelerType = findViewById(R.id.chipGroupTravelerType);
        chipGroupAccessibility = findViewById(R.id.chipGroupAccessibility);
    }

    private void setupNavigation() {
        btnWizardBack.setOnClickListener(v -> {
            if (currentStep > 1) {
                currentStep--;
                updateStepUI();
            }
        });

        btnWizardNext.setOnClickListener(v -> {
            if (validateCurrentStep()) {
                if (currentStep < TOTAL_STEPS) {
                    currentStep++;
                    updateStepUI();
                } else {
                    generatePlan();
                }
            }
        });
    }

    private boolean validateCurrentStep() {
        if (currentStep == 1) {
            int durationMin = (finishHour * 60 + finishMinute) - (startHour * 60 + startMinute);
            if (durationMin <= 0) {
                Toast.makeText(this, "Finish time must be after start time.", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (durationMin < 45) {
                Toast.makeText(this, "Please provide at least 45 minutes for a visit plan.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private void updateStepUI() {
        step1Layout.setVisibility(currentStep == 1 ? View.VISIBLE : View.GONE);
        step2Layout.setVisibility(currentStep == 2 ? View.VISIBLE : View.GONE);
        step3Layout.setVisibility(currentStep == 3 ? View.VISIBLE : View.GONE);
        step4Layout.setVisibility(currentStep == 4 ? View.VISIBLE : View.GONE);
        step5Layout.setVisibility(currentStep == 5 ? View.VISIBLE : View.GONE);

        btnWizardBack.setVisibility(currentStep == 1 ? View.GONE : View.VISIBLE);
        btnWizardNext.setText(currentStep == TOTAL_STEPS ? "Generate My Day ✨" : "Continue →");

        progressBarWizard.setProgress(currentStep);
        tvStepNumber.setText("STEP " + currentStep + " OF " + TOTAL_STEPS);

        switch (currentStep) {
            case 1:
                tvStepTitle.setText("Date & Available Time");
                break;
            case 2:
                tvStepTitle.setText("Starting Point & Route");
                break;
            case 3:
                tvStepTitle.setText("Interests & Experiences");
                break;
            case 4:
                tvStepTitle.setText("Budget & Food");
                break;
            case 5:
                tvStepTitle.setText("Travel Style & Pacing");
                break;
        }
    }

    private void generatePlan() {
        PlannerEngine.QuestionnaireData data = new PlannerEngine.QuestionnaireData();

        // 1. Date & Times
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        data.date = sdf.format(selectedDate.getTime());
        data.startTime = String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute);
        data.finishTime = String.format(Locale.getDefault(), "%02d:%02d", finishHour, finishMinute);
        data.flexibleSchedule = switchFlexibleSchedule.isChecked();

        // 2. Start & Finish
        int startIdx = spinnerStartNeighbourhood.getSelectedItemPosition();
        if (startIdx >= 0 && startIdx < NEIGHBOURHOOD_COORDS.length) {
            data.startNeighbourhood = NEIGHBOURHOODS[startIdx];
            data.startLat = NEIGHBOURHOOD_COORDS[startIdx][0];
            data.startLon = NEIGHBOURHOOD_COORDS[startIdx][1];
        }

        int finishIdx = spinnerFinishNeighbourhood.getSelectedItemPosition();
        if (finishIdx > 0 && (finishIdx - 1) < NEIGHBOURHOOD_COORDS.length) {
            data.finishNeighbourhood = NEIGHBOURHOODS[finishIdx - 1];
            data.finishLat = NEIGHBOURHOOD_COORDS[finishIdx - 1][0];
            data.finishLon = NEIGHBOURHOOD_COORDS[finishIdx - 1][1];
        }

        data.mustVisitPlaceIds = new ArrayList<>(selectedMustVisitIds);

        // 3. Interests
        List<String> interests = new ArrayList<>();
        if (isChipChecked(R.id.chipInterestHeritage)) interests.add("Historical");
        if (isChipChecked(R.id.chipInterestHiddenGems)) interests.add("Hidden Gems");
        if (isChipChecked(R.id.chipInterestNature)) interests.add("Nature");
        if (isChipChecked(R.id.chipInterestSunset)) interests.add("Sunset");
        if (isChipChecked(R.id.chipInterestFood)) interests.add("Food");
        if (isChipChecked(R.id.chipInterestCafes)) interests.add("Study");
        if (isChipChecked(R.id.chipInterestArt)) interests.add("Cultural");
        if (isChipChecked(R.id.chipInterestQuiet)) interests.add("Quiet");
        if (isChipChecked(R.id.chipInterestStudy)) interests.add("Study");
        if (isChipChecked(R.id.chipInterestTourist)) interests.add("Sunset");
        if (interests.isEmpty()) interests.add("Historical");
        data.interests = interests;
        data.specificNotes = etSpecificNotes.getText() != null ? etSpecificNotes.getText().toString().trim() : "";

        // 4. Budget & Food
        int budget = 500;
        int checkedBudgetChip = chipGroupBudgetPresets.getCheckedChipId();
        if (checkedBudgetChip == R.id.chipBudgetFree) budget = 0;
        else if (checkedBudgetChip == R.id.chipBudget250) budget = 250;
        else if (checkedBudgetChip == R.id.chipBudget500) budget = 500;
        else if (checkedBudgetChip == R.id.chipBudget1000) budget = 1000;
        else if (checkedBudgetChip == R.id.chipBudgetCustom) {
            try {
                budget = Integer.parseInt(etCustomBudget.getText().toString().trim());
            } catch (Exception ignored) {}
        }
        data.budgetInr = budget;
        data.groupSize = groupSize;

        int foodChip = chipGroupFoodPreference.getCheckedChipId();
        if (foodChip == R.id.chipFoodVeg) data.foodPreference = "VEGETARIAN";
        else if (foodChip == R.id.chipFoodNonVeg) data.foodPreference = "NON_VEGETARIAN";
        else if (foodChip == R.id.chipFoodVegan) data.foodPreference = "VEGAN";
        else data.foodPreference = "NO_PREFERENCE";
        data.includeFoodCosts = switchIncludeFoodCosts.isChecked();

        // 5. Travel & Pace
        int transChip = chipGroupTransport.getCheckedChipId();
        if (transChip == R.id.chipTransportTransit) data.transportMode = "TRANSIT";
        else if (transChip == R.id.chipTransportDriving) data.transportMode = "DRIVING";
        else if (transChip == R.id.chipTransportBike) data.transportMode = "TWO_WHEELER";
        else data.transportMode = "WALKING";

        int paceChip = chipGroupPace.getCheckedChipId();
        if (paceChip == R.id.chipPaceRelaxed) data.pacePreference = "RELAXED";
        else if (paceChip == R.id.chipPacePacked) data.pacePreference = "PACKED";
        else data.pacePreference = "BALANCED";

        int travelerChip = chipGroupTravelerType.getCheckedChipId();
        if (travelerChip == R.id.chipTravelerFriends) data.travelerType = "FRIENDS";
        else if (travelerChip == R.id.chipTravelerFamily) data.travelerType = "FAMILY";
        else if (travelerChip == R.id.chipTravelerChildren) data.travelerType = "CHILDREN";
        else data.travelerType = "SOLO";

        List<String> acc = new ArrayList<>();
        if (isChipChecked(R.id.chipAccSeating)) acc.add("Seating");
        if (isChipChecked(R.id.chipAccRestroom)) acc.add("Restroom");
        if (isChipChecked(R.id.chipAccQuiet)) acc.add("Quiet");
        if (isChipChecked(R.id.chipAccAC)) acc.add("AC");
        if (isChipChecked(R.id.chipAccWiFi)) acc.add("WiFi");
        data.accessibilityNeeds = acc;

        // Show generating dialog
        AlertDialog progressDialog = new AlertDialog.Builder(this)
                .setTitle("Personalising Your Mumbai Itinerary")
                .setMessage("Synthesising opening hours, geographic routes, and curated local secrets...")
                .setCancelable(false)
                .create();
        progressDialog.show();

        PlannerEngine.getInstance(this).generatePlan(data, new PlannerEngine.PlanCallback() {
            @Override
            public void onSuccess(Itinerary itinerary, boolean isFromBackend) {
                progressDialog.dismiss();
                // Save to repository
                PlaceRepository.getInstance(PlannerWizardActivity.this).saveItinerary(itinerary);

                Toast.makeText(PlannerWizardActivity.this, "✨ Your personalised plan is ready!", Toast.LENGTH_SHORT).show();

                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(String message) {
                progressDialog.dismiss();
                Log.e("PlannerWizard", "Plan generation failed: " + message);
                new AlertDialog.Builder(PlannerWizardActivity.this)
                        .setTitle("Couldn't Create Your Plan")
                        .setMessage("Something went wrong while building your itinerary. Please check your connection and try again.")
                        .setPositiveButton("Try Again", (d, w) -> generatePlan())
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }

    private boolean isChipChecked(int chipId) {
        Chip chip = findViewById(chipId);
        return chip != null && chip.isChecked();
    }

    private void showBackendSettingsDialog() {
        PlannerEngine engine = PlannerEngine.getInstance(this);
        EditText input = new EditText(this);
        input.setText(engine.getBackendUrl());
        input.setHint("Planner service URL");

        new AlertDialog.Builder(this)
                .setTitle("Advanced Settings")
                .setMessage("Configure the AI planner service endpoint. Leave as default unless advised by support.")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String url = input.getText().toString().trim();
                    if (!url.isEmpty()) {
                        engine.setBackendUrl(url);
                        Log.d("PlannerWizard", "Backend URL updated to: " + url);
                        Toast.makeText(this, "Settings saved.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Reset Default", (dialog, which) -> {
                    engine.setBackendUrl("http://10.0.2.2:8000");
                    Toast.makeText(this, "Restored to default.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
