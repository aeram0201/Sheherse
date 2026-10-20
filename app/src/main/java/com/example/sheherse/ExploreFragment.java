package com.example.sheherse;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sheherse.adapter.PlaceAdapter;
import com.example.sheherse.data.PlaceRepository;
import com.example.sheherse.model.Place;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ExploreFragment extends Fragment {

    private MaterialButton btnSurpriseMe;
    private EditText etSearch;
    private ImageButton btnClearSearch;

    private ChipGroup chipGroupCategory;
    private View btnOpenFilterSheet;
    private TextView tvFilterLabel;
    private TextView tvFilterBadge;
    private View btnOpenSortDialog;
    private TextView tvSortToggle;
    private TextView tvResultsCount;
    private TextView btnClearAllFilters;

    private RecyclerView rvExplorePlaces;
    private View layoutEmptyExplore;
    private MaterialButton btnResetFilters;

    private PlaceRepository placeRepository;
    private PlaceAdapter placeAdapter;
    private final List<Place> allPlaces = new ArrayList<>();
    private final List<Place> filteredPlaces = new ArrayList<>();

    // Active Filter State
    private String selectedCategory = "ALL";
    private String selectedArea = "ALL";
    private String selectedBudget = "ALL"; // ALL, FREE, 100, 200
    private boolean filterWifi = false;
    private boolean filterCharging = false;
    private boolean filterQuiet = false;
    private boolean filterSunset = false;
    private boolean filterGemsOnly = false;
    private int sortMode = 0; // 0: Curated, 1: Rating, 2: Price Low->High, 3: Price High->Low

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_explore, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        placeRepository = PlaceRepository.getInstance(requireContext());

        initViews(view);
        setupSearch();
        setupCategoryChips();
        setupFilterSheetButton();
        setupSortDialogButton();
        setupRecyclerView();
        loadAllPlaces();
    }

    private void initViews(View view) {
        btnSurpriseMe = view.findViewById(R.id.btnSurpriseMe);
        etSearch = view.findViewById(R.id.etSearch);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);

        chipGroupCategory = view.findViewById(R.id.chipGroupCategory);
        btnOpenFilterSheet = view.findViewById(R.id.btnOpenFilterSheet);
        tvFilterLabel = view.findViewById(R.id.tvFilterLabel);
        tvFilterBadge = view.findViewById(R.id.tvFilterBadge);
        btnOpenSortDialog = view.findViewById(R.id.btnOpenSortDialog);
        tvSortToggle = view.findViewById(R.id.tvSortToggle);
        tvResultsCount = view.findViewById(R.id.tvResultsCount);
        btnClearAllFilters = view.findViewById(R.id.btnClearAllFilters);

        rvExplorePlaces = view.findViewById(R.id.rvExplorePlaces);
        layoutEmptyExplore = view.findViewById(R.id.layoutEmptyExplore);
        btnResetFilters = view.findViewById(R.id.btnResetFilters);

        btnSurpriseMe.setOnClickListener(v -> surpriseMe());
        btnResetFilters.setOnClickListener(v -> resetFilters());
        btnClearAllFilters.setOnClickListener(v -> resetFilters());
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            btnClearSearch.setVisibility(View.GONE);
        });
    }

    private void setupCategoryChips() {
        chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipCatCultural)) {
                selectedCategory = "Cultural";
            } else if (checkedIds.contains(R.id.chipCatHistorical)) {
                selectedCategory = "Historical";
            } else if (checkedIds.contains(R.id.chipCatFood)) {
                selectedCategory = "Food";
            } else if (checkedIds.contains(R.id.chipCatStudy)) {
                selectedCategory = "Study";
            } else if (checkedIds.contains(R.id.chipCatNature)) {
                selectedCategory = "Nature";
            } else {
                selectedCategory = "ALL";
            }
            applyFilters();
        });
    }

    private void setupFilterSheetButton() {
        btnOpenFilterSheet.setOnClickListener(v -> showFilterBottomSheet());
    }

    private void showFilterBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_explore_filters, null);
        dialog.setContentView(sheetView);

        ChipGroup sheetChipGroupArea = sheetView.findViewById(R.id.sheetChipGroupArea);
        ChipGroup sheetChipGroupBudget = sheetView.findViewById(R.id.sheetChipGroupBudget);
        Chip sheetChipWifi = sheetView.findViewById(R.id.sheetChipWifi);
        Chip sheetChipCharging = sheetView.findViewById(R.id.sheetChipCharging);
        Chip sheetChipQuiet = sheetView.findViewById(R.id.sheetChipQuiet);
        Chip sheetChipSunset = sheetView.findViewById(R.id.sheetChipSunset);
        Chip sheetChipGems = sheetView.findViewById(R.id.sheetChipGems);
        TextView btnSheetReset = sheetView.findViewById(R.id.btnSheetReset);
        MaterialButton btnSheetApply = sheetView.findViewById(R.id.btnSheetApply);

        // Pre-select current area
        if ("South".equalsIgnoreCase(selectedArea)) {
            sheetChipGroupArea.check(R.id.sheetChipAreaSouth);
        } else if ("Fort".equalsIgnoreCase(selectedArea)) {
            sheetChipGroupArea.check(R.id.sheetChipAreaFort);
        } else if ("Bandra".equalsIgnoreCase(selectedArea)) {
            sheetChipGroupArea.check(R.id.sheetChipAreaBandra);
        } else if ("Dadar".equalsIgnoreCase(selectedArea)) {
            sheetChipGroupArea.check(R.id.sheetChipAreaDadar);
        } else {
            sheetChipGroupArea.check(R.id.sheetChipAreaAll);
        }

        // Pre-select current budget
        if ("FREE".equalsIgnoreCase(selectedBudget)) {
            sheetChipGroupBudget.check(R.id.sheetChipBudgetFree);
        } else if ("100".equalsIgnoreCase(selectedBudget)) {
            sheetChipGroupBudget.check(R.id.sheetChipBudget100);
        } else if ("200".equalsIgnoreCase(selectedBudget)) {
            sheetChipGroupBudget.check(R.id.sheetChipBudget200);
        } else {
            sheetChipGroupBudget.check(R.id.sheetChipBudgetAll);
        }

        // Pre-select facilities
        sheetChipWifi.setChecked(filterWifi);
        sheetChipCharging.setChecked(filterCharging);
        sheetChipQuiet.setChecked(filterQuiet);
        sheetChipSunset.setChecked(filterSunset);
        sheetChipGems.setChecked(filterGemsOnly);

        // Reset inside sheet
        btnSheetReset.setOnClickListener(rv -> {
            sheetChipGroupArea.check(R.id.sheetChipAreaAll);
            sheetChipGroupBudget.check(R.id.sheetChipBudgetAll);
            sheetChipWifi.setChecked(false);
            sheetChipCharging.setChecked(false);
            sheetChipQuiet.setChecked(false);
            sheetChipSunset.setChecked(false);
            sheetChipGems.setChecked(false);
        });

        // Apply filters
        btnSheetApply.setOnClickListener(av -> {
            // Read area
            int areaId = sheetChipGroupArea.getCheckedChipId();
            if (areaId == R.id.sheetChipAreaSouth) selectedArea = "South";
            else if (areaId == R.id.sheetChipAreaFort) selectedArea = "Fort";
            else if (areaId == R.id.sheetChipAreaBandra) selectedArea = "Bandra";
            else if (areaId == R.id.sheetChipAreaDadar) selectedArea = "Dadar";
            else selectedArea = "ALL";

            // Read budget
            int budgetId = sheetChipGroupBudget.getCheckedChipId();
            if (budgetId == R.id.sheetChipBudgetFree) selectedBudget = "FREE";
            else if (budgetId == R.id.sheetChipBudget100) selectedBudget = "100";
            else if (budgetId == R.id.sheetChipBudget200) selectedBudget = "200";
            else selectedBudget = "ALL";

            // Read facilities
            filterWifi = sheetChipWifi.isChecked();
            filterCharging = sheetChipCharging.isChecked();
            filterQuiet = sheetChipQuiet.isChecked();
            filterSunset = sheetChipSunset.isChecked();
            filterGemsOnly = sheetChipGems.isChecked();

            dialog.dismiss();
            applyFilters();
        });

        dialog.show();
    }

    private void setupSortDialogButton() {
        View.OnClickListener sortClickListener = v -> showSortBottomSheet();
        btnOpenSortDialog.setOnClickListener(sortClickListener);
        tvSortToggle.setOnClickListener(sortClickListener);
    }

    private void showSortBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_explore_sort, null);
        dialog.setContentView(sheetView);

        ImageView ivCheckCurated = sheetView.findViewById(R.id.ivCheckCurated);
        ImageView ivCheckRating = sheetView.findViewById(R.id.ivCheckRating);
        ImageView ivCheckPriceLow = sheetView.findViewById(R.id.ivCheckPriceLow);
        ImageView ivCheckPriceHigh = sheetView.findViewById(R.id.ivCheckPriceHigh);

        ivCheckCurated.setVisibility(sortMode == 0 ? View.VISIBLE : View.GONE);
        ivCheckRating.setVisibility(sortMode == 1 ? View.VISIBLE : View.GONE);
        ivCheckPriceLow.setVisibility(sortMode == 2 ? View.VISIBLE : View.GONE);
        ivCheckPriceHigh.setVisibility(sortMode == 3 ? View.VISIBLE : View.GONE);

        sheetView.findViewById(R.id.sortOptionCurated).setOnClickListener(v -> {
            sortMode = 0;
            tvSortToggle.setText("Curated ▾");
            dialog.dismiss();
            applyFilters();
        });

        sheetView.findViewById(R.id.sortOptionRating).setOnClickListener(v -> {
            sortMode = 1;
            tvSortToggle.setText("Rating ★ ▾");
            dialog.dismiss();
            applyFilters();
        });

        sheetView.findViewById(R.id.sortOptionPriceLow).setOnClickListener(v -> {
            sortMode = 2;
            tvSortToggle.setText("Price: Low ▾");
            dialog.dismiss();
            applyFilters();
        });

        sheetView.findViewById(R.id.sortOptionPriceHigh).setOnClickListener(v -> {
            sortMode = 3;
            tvSortToggle.setText("Price: High ▾");
            dialog.dismiss();
            applyFilters();
        });

        dialog.show();
    }

    private void setupRecyclerView() {
        rvExplorePlaces.setLayoutManager(new LinearLayoutManager(requireContext()));
        placeAdapter = new PlaceAdapter(requireContext(), filteredPlaces, place -> {
            Intent intent = new Intent(requireContext(), PlaceDetailActivity.class);
            intent.putExtra(PlaceDetailActivity.EXTRA_PLACE_ID, place.getId());
            startActivity(intent);
        });
        rvExplorePlaces.setAdapter(placeAdapter);
    }

    private void loadAllPlaces() {
        allPlaces.clear();
        allPlaces.addAll(placeRepository.getAllPlaces());
        applyFilters();
    }

    private void applyFilters() {
        String query = etSearch.getText() != null ? etSearch.getText().toString().trim().toLowerCase() : "";
        filteredPlaces.clear();

        for (Place p : allPlaces) {
            // Category check
            if (!"ALL".equalsIgnoreCase(selectedCategory)) {
                if ("Food".equalsIgnoreCase(selectedCategory) && !p.isAffordableFood() && !"Food".equalsIgnoreCase(p.getCategory())) {
                    continue;
                } else if ("Study".equalsIgnoreCase(selectedCategory) && !p.isStudyWork() && !"Study".equalsIgnoreCase(p.getCategory())) {
                    continue;
                } else if ("Nature".equalsIgnoreCase(selectedCategory) && !"Nature".equalsIgnoreCase(p.getCategory()) && !"Sunset".equalsIgnoreCase(p.getCategory())) {
                    continue;
                } else if (!"Food".equalsIgnoreCase(selectedCategory) && !"Study".equalsIgnoreCase(selectedCategory) && !"Nature".equalsIgnoreCase(selectedCategory)) {
                    if (!selectedCategory.equalsIgnoreCase(p.getCategory())) {
                        continue;
                    }
                }
            }

            // Area check
            if (!"ALL".equalsIgnoreCase(selectedArea)) {
                String area = p.getArea() != null ? p.getArea().toLowerCase() : "";
                if (!area.contains(selectedArea.toLowerCase())) {
                    continue;
                }
            }

            // Budget check
            if (!"ALL".equalsIgnoreCase(selectedBudget)) {
                int cost = p.getEstimatedCost();
                if ("FREE".equalsIgnoreCase(selectedBudget) && cost > 0) continue;
                if ("100".equalsIgnoreCase(selectedBudget) && cost > 100) continue;
                if ("200".equalsIgnoreCase(selectedBudget) && cost > 200) continue;
            }

            // Facilities & Amenities
            if (filterWifi) {
                List<String> f = p.getFacilities();
                if (f == null || (!f.contains("WiFi") && !f.contains("wifi"))) continue;
            }
            if (filterCharging) {
                List<String> f = p.getFacilities();
                if (f == null || (!f.contains("Charging") && !f.contains("charging"))) continue;
            }
            if (filterQuiet) {
                if (!"Silent".equalsIgnoreCase(p.getQuietnessLevel()) && !"Quiet".equalsIgnoreCase(p.getQuietnessLevel())) {
                    List<String> f = p.getFacilities();
                    if (f == null || !f.contains("Quiet")) continue;
                }
            }
            if (filterSunset) {
                boolean isSunset = "Sunset".equalsIgnoreCase(p.getCategory()) ||
                        (p.getTags() != null && p.getTags().contains("Sunset"));
                if (!isSunset) continue;
            }
            if (filterGemsOnly) {
                if (!p.isHiddenGem()) continue;
            }

            // Search query check
            if (!query.isEmpty()) {
                boolean matches = false;
                if (p.getName() != null && p.getName().toLowerCase().contains(query)) matches = true;
                if (p.getArea() != null && p.getArea().toLowerCase().contains(query)) matches = true;
                if (p.getDescription() != null && p.getDescription().toLowerCase().contains(query)) matches = true;
                if (p.getCategory() != null && p.getCategory().toLowerCase().contains(query)) matches = true;
                if (p.getTags() != null) {
                    for (String t : p.getTags()) {
                        if (t != null && t.toLowerCase().contains(query)) {
                            matches = true;
                            break;
                        }
                    }
                }
                if (!matches) continue;
            }

            filteredPlaces.add(p);
        }

        // Apply Sorting
        if (sortMode == 1) {
            Collections.sort(filteredPlaces, (p1, p2) -> Float.compare(p2.getRating(), p1.getRating()));
        } else if (sortMode == 2) {
            Collections.sort(filteredPlaces, (p1, p2) -> Integer.compare(p1.getEstimatedCost(), p2.getEstimatedCost()));
        } else if (sortMode == 3) {
            Collections.sort(filteredPlaces, (p1, p2) -> Integer.compare(p2.getEstimatedCost(), p1.getEstimatedCost()));
        }

        // Update active filter badge count
        int activeFiltersCount = 0;
        if (!"ALL".equalsIgnoreCase(selectedArea)) activeFiltersCount++;
        if (!"ALL".equalsIgnoreCase(selectedBudget)) activeFiltersCount++;
        if (filterWifi) activeFiltersCount++;
        if (filterCharging) activeFiltersCount++;
        if (filterQuiet) activeFiltersCount++;
        if (filterSunset) activeFiltersCount++;
        if (filterGemsOnly) activeFiltersCount++;

        if (activeFiltersCount > 0) {
            tvFilterBadge.setText(String.valueOf(activeFiltersCount));
            tvFilterBadge.setVisibility(View.VISIBLE);
            btnClearAllFilters.setVisibility(View.VISIBLE);
        } else {
            tvFilterBadge.setVisibility(View.GONE);
            boolean hasNonDefault = !"ALL".equalsIgnoreCase(selectedCategory) || !query.isEmpty();
            btnClearAllFilters.setVisibility(hasNonDefault ? View.VISIBLE : View.GONE);
        }

        tvResultsCount.setText(filteredPlaces.size() + " places");

        if (filteredPlaces.isEmpty()) {
            layoutEmptyExplore.setVisibility(View.VISIBLE);
            rvExplorePlaces.setVisibility(View.GONE);
        } else {
            layoutEmptyExplore.setVisibility(View.GONE);
            rvExplorePlaces.setVisibility(View.VISIBLE);
            placeAdapter.updatePlaces(filteredPlaces);
        }
    }

    private void surpriseMe() {
        if (!allPlaces.isEmpty()) {
            int randomIndex = new Random().nextInt(allPlaces.size());
            Place randomPlace = allPlaces.get(randomIndex);
            Toast.makeText(requireContext(), "Surprise! Opening " + randomPlace.getName(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(requireContext(), PlaceDetailActivity.class);
            intent.putExtra(PlaceDetailActivity.EXTRA_PLACE_ID, randomPlace.getId());
            startActivity(intent);
        }
    }

    private void resetFilters() {
        etSearch.setText("");
        selectedCategory = "ALL";
        selectedArea = "ALL";
        selectedBudget = "ALL";
        filterWifi = false;
        filterCharging = false;
        filterQuiet = false;
        filterSunset = false;
        filterGemsOnly = false;
        chipGroupCategory.check(R.id.chipCatAll);
        sortMode = 0;
        tvSortToggle.setText("Curated ▾");
        applyFilters();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (placeAdapter != null) {
            placeAdapter.notifyDataSetChanged();
        }
    }
}
