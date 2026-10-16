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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ExploreFragment extends Fragment {

    private MaterialButton btnSurpriseMe;
    private EditText etSearch;
    private ImageButton btnClearSearch;

    private TextView tagQuickWifi;
    private TextView tagQuickFree;
    private TextView tagQuickSunset;
    private TextView tagQuickChai;
    private TextView tagQuickQuiet;

    private ChipGroup chipGroupCategory;
    private ChipGroup chipGroupArea;
    private TextView tvResultsCount;
    private TextView tvSortToggle;

    private RecyclerView rvExplorePlaces;
    private View layoutEmptyExplore;
    private MaterialButton btnResetFilters;

    private PlaceRepository placeRepository;
    private PlaceAdapter placeAdapter;
    private final List<Place> allPlaces = new ArrayList<>();
    private final List<Place> filteredPlaces = new ArrayList<>();

    private String selectedCategory = "ALL";
    private String selectedArea = "ALL";
    private String quickTag = "";
    private int sortMode = 0; // 0: Curated, 1: Rating, 2: Price Low->High

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
        setupQuickTags();
        setupChips();
        setupSortToggle();
        setupRecyclerView();
        loadAllPlaces();
    }

    private void initViews(View view) {
        btnSurpriseMe = view.findViewById(R.id.btnSurpriseMe);
        etSearch = view.findViewById(R.id.etSearch);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);

        tagQuickWifi = view.findViewById(R.id.tagQuickWifi);
        tagQuickFree = view.findViewById(R.id.tagQuickFree);
        tagQuickSunset = view.findViewById(R.id.tagQuickSunset);
        tagQuickChai = view.findViewById(R.id.tagQuickChai);
        tagQuickQuiet = view.findViewById(R.id.tagQuickQuiet);

        chipGroupCategory = view.findViewById(R.id.chipGroupCategory);
        chipGroupArea = view.findViewById(R.id.chipGroupArea);
        tvResultsCount = view.findViewById(R.id.tvResultsCount);
        tvSortToggle = view.findViewById(R.id.tvSortToggle);

        rvExplorePlaces = view.findViewById(R.id.rvExplorePlaces);
        layoutEmptyExplore = view.findViewById(R.id.layoutEmptyExplore);
        btnResetFilters = view.findViewById(R.id.btnResetFilters);

        btnSurpriseMe.setOnClickListener(v -> surpriseMe());
        btnResetFilters.setOnClickListener(v -> resetFilters());
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

    private void setupQuickTags() {
        View.OnClickListener tagClickListener = v -> {
            int id = v.getId();
            if (id == R.id.tagQuickWifi) {
                toggleQuickTag("wifi");
            } else if (id == R.id.tagQuickFree) {
                toggleQuickTag("free");
            } else if (id == R.id.tagQuickSunset) {
                toggleQuickTag("sunset");
            } else if (id == R.id.tagQuickChai) {
                toggleQuickTag("chai");
            } else if (id == R.id.tagQuickQuiet) {
                toggleQuickTag("quiet");
            }
        };

        tagQuickWifi.setOnClickListener(tagClickListener);
        tagQuickFree.setOnClickListener(tagClickListener);
        tagQuickSunset.setOnClickListener(tagClickListener);
        tagQuickChai.setOnClickListener(tagClickListener);
        tagQuickQuiet.setOnClickListener(tagClickListener);
    }

    private void toggleQuickTag(String tag) {
        if (quickTag.equals(tag)) {
            quickTag = "";
        } else {
            quickTag = tag;
        }
        applyFilters();
    }

    private void setupChips() {
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

        chipGroupArea.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipAreaSouth)) {
                selectedArea = "South";
            } else if (checkedIds.contains(R.id.chipAreaFort)) {
                selectedArea = "Fort";
            } else if (checkedIds.contains(R.id.chipAreaBandra)) {
                selectedArea = "Bandra";
            } else if (checkedIds.contains(R.id.chipAreaDadar)) {
                selectedArea = "Dadar";
            } else {
                selectedArea = "ALL";
            }
            applyFilters();
        });
    }

    private void setupSortToggle() {
        tvSortToggle.setOnClickListener(v -> {
            sortMode = (sortMode + 1) % 3;
            if (sortMode == 0) {
                tvSortToggle.setText("Sort: Curated ▾");
            } else if (sortMode == 1) {
                tvSortToggle.setText("Sort: Rating ★ ▾");
            } else {
                tvSortToggle.setText("Sort: Price (Low) ▾");
            }
            applyFilters();
        });
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
            if (!"ALL".equals(selectedCategory)) {
                if (selectedCategory.equalsIgnoreCase("Food") && !p.isAffordableFood() && !"Food".equalsIgnoreCase(p.getCategory())) {
                    continue;
                } else if (selectedCategory.equalsIgnoreCase("Study") && !p.isStudyWork() && !"Study".equalsIgnoreCase(p.getCategory())) {
                    continue;
                } else if (selectedCategory.equalsIgnoreCase("Nature") && !"Nature".equalsIgnoreCase(p.getCategory()) && !"Sunset".equalsIgnoreCase(p.getCategory())) {
                    continue;
                } else if (!selectedCategory.equalsIgnoreCase("Food") && !selectedCategory.equalsIgnoreCase("Study") && !selectedCategory.equalsIgnoreCase("Nature")) {
                    if (!selectedCategory.equalsIgnoreCase(p.getCategory())) {
                        continue;
                    }
                }
            }

            // Area check
            if (!"ALL".equals(selectedArea)) {
                String area = p.getArea() != null ? p.getArea().toLowerCase() : "";
                if (!area.contains(selectedArea.toLowerCase())) {
                    continue;
                }
            }

            // Quick tag check
            if (!quickTag.isEmpty()) {
                if ("wifi".equals(quickTag)) {
                    List<String> f = p.getFacilities();
                    if (f == null || (!f.contains("WiFi") && !f.contains("wifi"))) continue;
                } else if ("free".equals(quickTag)) {
                    if (p.getEstimatedCost() > 0) continue;
                } else if ("sunset".equals(quickTag)) {
                    if (!"Sunset".equalsIgnoreCase(p.getCategory()) && (p.getTags() == null || !p.getTags().contains("Sunset"))) continue;
                } else if ("chai".equals(quickTag)) {
                    if (!p.isAffordableFood()) continue;
                } else if ("quiet".equals(quickTag)) {
                    if (!"Silent".equalsIgnoreCase(p.getQuietnessLevel())) continue;
                }
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
        }

        tvResultsCount.setText("Showing " + filteredPlaces.size() + " Mumbai discoveries");

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
        quickTag = "";
        selectedCategory = "ALL";
        selectedArea = "ALL";
        chipGroupCategory.check(R.id.chipCatAll);
        chipGroupArea.check(R.id.chipAreaAll);
        sortMode = 0;
        tvSortToggle.setText("Sort: Curated ▾");
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
