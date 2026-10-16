package com.example.sheherse;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sheherse.adapter.ItineraryAdapter;
import com.example.sheherse.data.PlaceRepository;
import com.example.sheherse.model.Itinerary;
import com.example.sheherse.model.ItineraryStop;
import com.example.sheherse.model.Place;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PlannerFragment extends Fragment {

    private TextView tvItineraryTitle;
    private ImageButton btnResetItinerary;
    private TextView tvSummaryStops;
    private TextView tvSummaryDuration;
    private TextView tvSummaryCost;

    private MaterialButton btnAddStop;
    private MaterialButton btnShareItinerary;
    private RecyclerView rvItineraryStops;

    private MaterialButton btnLoadPresetHeritage;
    private MaterialButton btnLoadPresetQuiet;
    private MaterialButton btnLoadPresetBandra;

    private PlaceRepository placeRepository;
    private ItineraryAdapter itineraryAdapter;
    private Itinerary currentItinerary;

    private final int MAX_DAY_MINUTES = 480; // 8 hours max recommended day plan
    private final int TARGET_BUDGET = 500;  // Target budget in INR

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_planner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        placeRepository = PlaceRepository.getInstance(requireContext());

        initViews(view);
        setupPresets();
        setupRecyclerView();
        loadCurrentItinerary();
    }

    private void initViews(View view) {
        tvItineraryTitle = view.findViewById(R.id.tvItineraryTitle);
        btnResetItinerary = view.findViewById(R.id.btnResetItinerary);
        tvSummaryStops = view.findViewById(R.id.tvSummaryStops);
        tvSummaryDuration = view.findViewById(R.id.tvSummaryDuration);
        tvSummaryCost = view.findViewById(R.id.tvSummaryCost);

        btnAddStop = view.findViewById(R.id.btnAddStop);
        btnShareItinerary = view.findViewById(R.id.btnShareItinerary);
        rvItineraryStops = view.findViewById(R.id.rvItineraryStops);

        btnLoadPresetHeritage = view.findViewById(R.id.btnLoadPresetHeritage);
        btnLoadPresetQuiet = view.findViewById(R.id.btnLoadPresetQuiet);
        btnLoadPresetBandra = view.findViewById(R.id.btnLoadPresetBandra);

        btnAddStop.setOnClickListener(v -> showAddStopDialog());

        btnResetItinerary.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Reset Plan")
                    .setMessage("Clear all stops from this day plan?")
                    .setPositiveButton("Clear", (dialog, which) -> {
                        if (currentItinerary != null) {
                            currentItinerary.getStops().clear();
                            placeRepository.saveItinerary(currentItinerary);
                            updateItineraryUI();
                            Toast.makeText(requireContext(), "Itinerary reset.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        btnShareItinerary.setOnClickListener(v -> shareItinerary());
    }

    private void setupPresets() {
        btnLoadPresetHeritage.setOnClickListener(v -> loadPreset(
                "South Bombay Heritage & Chai",
                TARGET_BUDGET,
                new String[]{"place_kyani", "place_asiatic_steps", "place_kitab_khana", "place_marine_drive"}
        ));

        btnLoadPresetQuiet.setOnClickListener(v -> loadPreset(
                "Quiet Corners & Coastal Solitude",
                0,
                new String[]{"place_banganga", "place_khotachiwadi", "place_central_library"}
        ));

        btnLoadPresetBandra.setOnClickListener(v -> loadPreset(
                "Bandra Bohemian & Sunset Walk",
                350,
                new String[]{"place_candies_bandra", "place_drip_cafe", "place_bandra_fort"}
        ));
    }

    private void loadPreset(String title, int budget, String[] placeIds) {
        currentItinerary = new Itinerary("itin_active", title, budget, null);
        int order = 0;
        for (String id : placeIds) {
            Place p = placeRepository.getPlaceById(id);
            if (p != null) {
                currentItinerary.getStops().add(new ItineraryStop("stop_" + order, p, order));
                order++;
            }
        }
        placeRepository.saveItinerary(currentItinerary);
        updateItineraryUI();
        Toast.makeText(requireContext(), "Loaded " + title + "!", Toast.LENGTH_SHORT).show();
    }

    private void setupRecyclerView() {
        rvItineraryStops.setLayoutManager(new LinearLayoutManager(requireContext()));
        itineraryAdapter = new ItineraryAdapter(requireContext(), new ArrayList<>(), new ItineraryAdapter.OnStopActionListener() {
            @Override
            public void onMoveUp(int position) {
                if (currentItinerary != null && position > 0) {
                    List<ItineraryStop> stops = currentItinerary.getStops();
                    Collections.swap(stops, position, position - 1);
                    reorderStops(stops);
                    placeRepository.saveItinerary(currentItinerary);
                    updateItineraryUI();
                }
            }

            @Override
            public void onMoveDown(int position) {
                if (currentItinerary != null && position < currentItinerary.getStops().size() - 1) {
                    List<ItineraryStop> stops = currentItinerary.getStops();
                    Collections.swap(stops, position, position + 1);
                    reorderStops(stops);
                    placeRepository.saveItinerary(currentItinerary);
                    updateItineraryUI();
                }
            }

            @Override
            public void onDelete(int position) {
                if (currentItinerary != null && position >= 0 && position < currentItinerary.getStops().size()) {
                    currentItinerary.getStops().remove(position);
                    reorderStops(currentItinerary.getStops());
                    placeRepository.saveItinerary(currentItinerary);
                    updateItineraryUI();
                    Toast.makeText(requireContext(), "Stop removed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        rvItineraryStops.setAdapter(itineraryAdapter);
    }

    private void reorderStops(List<ItineraryStop> stops) {
        for (int i = 0; i < stops.size(); i++) {
            stops.get(i).setOrderIndex(i);
        }
    }

    private void loadCurrentItinerary() {
        List<Itinerary> list = placeRepository.getSavedItineraries();
        if (list.isEmpty()) {
            loadPreset(
                    "My South Bombay Discovery Walk",
                    TARGET_BUDGET,
                    new String[]{"place_kyani", "place_asiatic_steps", "place_kitab_khana", "place_marine_drive"}
            );
        } else {
            currentItinerary = list.get(0);
            updateItineraryUI();
        }
    }

    private void updateItineraryUI() {
        if (currentItinerary == null) return;

        tvItineraryTitle.setText(currentItinerary.getTitle());

        List<ItineraryStop> stops = currentItinerary.getStops();
        tvSummaryStops.setText(stops.size() + " Stops");

        int totalMinutes = currentItinerary.calculateTotalDurationMinutes();
        float hours = totalMinutes / 60.0f;
        tvSummaryDuration.setText(String.format(Locale.getDefault(), "%.1f hrs", hours));

        int totalCost = currentItinerary.calculateTotalCost();
        tvSummaryCost.setText("₹" + totalCost);

        itineraryAdapter.updateStops(stops);

        // Budget & duration warning checks
        if (totalMinutes > MAX_DAY_MINUTES) {
            tvSummaryDuration.setTextColor(requireContext().getColor(R.color.vibrant_coral));
        } else {
            tvSummaryDuration.setTextColor(requireContext().getColor(R.color.teal_secondary));
        }

        if (currentItinerary.getTargetBudget() > 0 && totalCost > currentItinerary.getTargetBudget()) {
            tvSummaryCost.setTextColor(requireContext().getColor(R.color.vibrant_coral));
        } else {
            tvSummaryCost.setTextColor(requireContext().getColor(R.color.gold_dark));
        }
    }

    private void showAddStopDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_stop, null);
        Spinner spinnerPlaces = dialogView.findViewById(R.id.spinnerPlaces);
        TextInputEditText etDuration = dialogView.findViewById(R.id.etStopDuration);
        TextInputEditText etNote = dialogView.findViewById(R.id.etStopNote);

        List<Place> allPlaces = placeRepository.getAllPlaces();
        List<String> placeLabels = new ArrayList<>();
        for (Place p : allPlaces) {
            String cost = p.getEstimatedCost() == 0 ? "Free" : "₹" + p.getEstimatedCost();
            placeLabels.add(p.getName() + " (" + p.getArea() + " • " + cost + ")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                placeLabels
        );
        spinnerPlaces.setAdapter(adapter);

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Add Stop", (dialog, which) -> {
                    int selected = spinnerPlaces.getSelectedItemPosition();
                    if (selected >= 0 && selected < allPlaces.size()) {
                        Place selectedPlace = allPlaces.get(selected);

                        // Prevent duplicate
                        if (currentItinerary != null) {
                            for (ItineraryStop s : currentItinerary.getStops()) {
                                if (s.getPlace() != null && s.getPlace().getId().equals(selectedPlace.getId())) {
                                    Toast.makeText(requireContext(), selectedPlace.getName() + " is already in the itinerary!", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }

                            int duration = selectedPlace.getDurationMinutes() > 0 ? selectedPlace.getDurationMinutes() : 45;
                            try {
                                String dStr = etDuration.getText().toString().trim();
                                if (!dStr.isEmpty()) duration = Integer.parseInt(dStr);
                            } catch (Exception ignored) {}

                            String note = etNote.getText() != null ? etNote.getText().toString().trim() : "";

                            ItineraryStop newStop = new ItineraryStop(
                                    "stop_" + System.currentTimeMillis(),
                                    selectedPlace,
                                    currentItinerary.getStops().size()
                            );
                            newStop.setCustomDurationMinutes(duration);
                            newStop.setCustomNote(note);

                            currentItinerary.getStops().add(newStop);
                            placeRepository.saveItinerary(currentItinerary);
                            updateItineraryUI();
                            Toast.makeText(requireContext(), "Added " + selectedPlace.getName() + " to day plan!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void shareItinerary() {
        if (currentItinerary == null || currentItinerary.getStops().isEmpty()) {
            Toast.makeText(requireContext(), "Add some stops before sharing your itinerary!", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🗓️ ").append(currentItinerary.getTitle()).append("\n");
        sb.append("Est. Duration: ").append(String.format(Locale.getDefault(), "%.1f hrs", currentItinerary.calculateTotalDurationMinutes() / 60f)).append("\n");
        sb.append("Est. Cost: ₹").append(currentItinerary.calculateTotalCost()).append("\n\n");
        sb.append("Stops:\n");

        int count = 1;
        for (ItineraryStop stop : currentItinerary.getStops()) {
            sb.append(count).append(". ").append(stop.getPlaceName()).append(" (").append(stop.getPlaceArea()).append(")\n");
            sb.append("   ⏱ ").append(stop.getDurationMinutes()).append(" mins • ₹").append(stop.getEstimatedCost()).append("\n");
            if (stop.getCustomNote() != null && !stop.getCustomNote().isEmpty()) {
                sb.append("   Note: ").append(stop.getCustomNote()).append("\n");
            }
            count++;
        }

        sb.append("\nCreated with SheherSe • One Mumbai. Many ways to explore.");

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, currentItinerary.getTitle());
        intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(intent, "Share My Day Plan"));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCurrentItinerary();
    }
}
