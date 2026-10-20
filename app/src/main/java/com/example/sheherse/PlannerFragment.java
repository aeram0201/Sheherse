package com.example.sheherse;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import com.example.sheherse.data.PlannerEngine;
import com.example.sheherse.model.CostBreakdown;
import com.example.sheherse.model.Itinerary;
import com.example.sheherse.model.ItineraryStop;
import com.example.sheherse.model.Place;
import com.example.sheherse.model.TravelLeg;
import com.example.sheherse.util.ItineraryMapManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PlannerFragment extends Fragment {

    private MaterialCardView cardLaunchWizard;
    private MaterialButton btnOpenWizard;

    private TextView tvItineraryTitle;
    private TextView tvItineraryDateWindow;
    private TextView tvPlanningRationale;
    private View layoutWarnings;
    private TextView tvWarningsText;

    private ImageButton btnResetItinerary;
    private TextView tvSummaryStops;
    private TextView tvSummaryDuration;
    private TextView tvSummaryCost;

    private MaterialButton btnAddStop;
    private MaterialButton btnShareItinerary;
    private MaterialButton btnRecalculateRoute;
    private MaterialButton btnSaveItinerary;
    private RecyclerView rvItineraryStops;

    // Map views
    private WebView webViewItineraryMap;
    private View layoutMapFallback;
    private MaterialButton btnOpenGoogleMapsFull;
    private MaterialButton btnFallbackDirections;
    private ItineraryMapManager itineraryMapManager;

    // Budget breakdown views
    private TextView tvBreakdownEntryFees;
    private TextView tvBreakdownFood;
    private TextView tvBreakdownTransport;
    private TextView tvBreakdownTotal;
    private TextView tvBreakdownTargetBudget;
    private TextView tvBreakdownRemaining;
    private TextView tvCostCompletenessNote;
    private LinearLayout layoutBudgetExceededBanner;
    private TextView tvBudgetExceededAdvice;

    private View cardPresetHeritage;
    private View cardPresetQuiet;
    private View cardPresetBandra;

    private PlaceRepository placeRepository;
    private ItineraryAdapter itineraryAdapter;
    private Itinerary currentItinerary;

    private final int MAX_DAY_MINUTES = 600; // 10 hours max recommended day plan
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
        setupWizardBanner();
        setupPresets(view);
        setupRecyclerView();
        loadCurrentItinerary();
    }

    private void initViews(View view) {
        cardLaunchWizard = view.findViewById(R.id.cardLaunchWizard);
        btnOpenWizard = view.findViewById(R.id.btnOpenWizard);

        tvItineraryTitle = view.findViewById(R.id.tvItineraryTitle);
        tvItineraryDateWindow = view.findViewById(R.id.tvItineraryDateWindow);
        tvPlanningRationale = view.findViewById(R.id.tvPlanningRationale);
        layoutWarnings = view.findViewById(R.id.layoutWarnings);
        tvWarningsText = view.findViewById(R.id.tvWarningsText);

        btnResetItinerary = view.findViewById(R.id.btnResetItinerary);
        tvSummaryStops = view.findViewById(R.id.tvSummaryStops);
        tvSummaryDuration = view.findViewById(R.id.tvSummaryDuration);
        tvSummaryCost = view.findViewById(R.id.tvSummaryCost);

        btnAddStop = view.findViewById(R.id.btnAddStop);
        btnShareItinerary = view.findViewById(R.id.btnShareItinerary);
        btnRecalculateRoute = view.findViewById(R.id.btnRecalculateRoute);
        btnSaveItinerary = view.findViewById(R.id.btnSaveItinerary);
        rvItineraryStops = view.findViewById(R.id.rvItineraryStops);

        // Map views
        webViewItineraryMap = view.findViewById(R.id.webViewItineraryMap);
        layoutMapFallback = view.findViewById(R.id.layoutMapFallback);
        btnOpenGoogleMapsFull = view.findViewById(R.id.btnOpenGoogleMapsFull);
        btnFallbackDirections = view.findViewById(R.id.btnFallbackDirections);

        // Budget breakdown views
        tvBreakdownEntryFees = view.findViewById(R.id.tvBreakdownEntryFees);
        tvBreakdownFood = view.findViewById(R.id.tvBreakdownFood);
        tvBreakdownTransport = view.findViewById(R.id.tvBreakdownTransport);
        tvBreakdownTotal = view.findViewById(R.id.tvBreakdownTotal);
        tvBreakdownTargetBudget = view.findViewById(R.id.tvBreakdownTargetBudget);
        tvBreakdownRemaining = view.findViewById(R.id.tvBreakdownRemaining);
        tvCostCompletenessNote = view.findViewById(R.id.tvCostCompletenessNote);
        layoutBudgetExceededBanner = view.findViewById(R.id.layoutBudgetExceededBanner);
        tvBudgetExceededAdvice = view.findViewById(R.id.tvBudgetExceededAdvice);

        // Initialize map manager
        if (webViewItineraryMap != null) {
            itineraryMapManager = new ItineraryMapManager(webViewItineraryMap,
                    new ItineraryMapManager.OnMapInteractionListener() {
                        @Override
                        public void onMarkerSelected(String placeId, int orderIndex) {
                            if (currentItinerary == null) return;
                            List<ItineraryStop> stops = currentItinerary.getStops();
                            if (orderIndex >= 0 && orderIndex < stops.size()) {
                                ItineraryStop stop = stops.get(orderIndex);
                                if (stop.getPlace() != null) {
                                    Intent intent = new Intent(requireContext(), PlaceDetailActivity.class);
                                    intent.putExtra(PlaceDetailActivity.EXTRA_PLACE_ID, stop.getPlace().getId());
                                    startActivity(intent);
                                }
                            }
                        }

                        @Override
                        public void onLegSelected(int fromIndex, int toIndex) {
                            if (currentItinerary == null) return;
                            List<ItineraryStop> stops = currentItinerary.getStops();
                            if (fromIndex >= 0 && fromIndex < stops.size()) {
                                ItineraryStop fromStop = stops.get(fromIndex);
                                TravelLeg leg = fromStop.getTravelLegToNext();
                                if (leg != null) {
                                    showLegDetailsDialog(leg, fromStop);
                                }
                            }
                        }

                        @Override
                        public void onMapReady() {
                            if (webViewItineraryMap != null) webViewItineraryMap.setVisibility(View.VISIBLE);
                            if (layoutMapFallback != null) layoutMapFallback.setVisibility(View.GONE);
                        }

                        @Override
                        public void onMapFailed(String reason) {
                            if (webViewItineraryMap != null) webViewItineraryMap.setVisibility(View.GONE);
                            if (layoutMapFallback != null) layoutMapFallback.setVisibility(View.VISIBLE);
                        }
                    });
        }

        btnAddStop.setOnClickListener(v -> showAddStopDialog());

        if (btnOpenGoogleMapsFull != null) {
            btnOpenGoogleMapsFull.setOnClickListener(v -> {
                if (currentItinerary != null) {
                    ItineraryMapManager.openFullItineraryInGoogleMaps(requireContext(), currentItinerary);
                }
            });
        }

        if (btnFallbackDirections != null) {
            btnFallbackDirections.setOnClickListener(v -> {
                if (currentItinerary != null) {
                    ItineraryMapManager.openFullItineraryInGoogleMaps(requireContext(), currentItinerary);
                }
            });
        }

        if (btnRecalculateRoute != null) {
            btnRecalculateRoute.setOnClickListener(v -> {
                if (currentItinerary != null) {
                    PlannerEngine.recalculateSchedule(currentItinerary, currentItinerary.getStartTime(), "WALKING");
                    placeRepository.saveItinerary(currentItinerary);
                    updateItineraryUI();
                    Toast.makeText(requireContext(), "Route recalculated.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnSaveItinerary != null) {
            btnSaveItinerary.setOnClickListener(v -> {
                if (currentItinerary != null) {
                    placeRepository.saveItinerary(currentItinerary);
                    Toast.makeText(requireContext(), "Plan saved to My Plans.", Toast.LENGTH_SHORT).show();
                }
            });
        }

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

    private void setupWizardBanner() {
        View.OnClickListener launchListener = v -> {
            startActivity(new Intent(requireContext(), PlannerWizardActivity.class));
        };

        if (cardLaunchWizard != null) cardLaunchWizard.setOnClickListener(launchListener);
        if (btnOpenWizard != null) btnOpenWizard.setOnClickListener(launchListener);
    }

    private void setupPresets(View view) {
        cardPresetHeritage = view.findViewById(R.id.cardPresetHeritage);
        cardPresetQuiet = view.findViewById(R.id.cardPresetQuiet);
        cardPresetBandra = view.findViewById(R.id.cardPresetBandra);

        if (cardPresetHeritage != null) {
            cardPresetHeritage.setOnClickListener(v -> loadPreset(
                    "South Bombay Heritage & Chai",
                    TARGET_BUDGET,
                    new String[]{"place_kyani", "place_asiatic_steps", "place_kitab_khana", "place_marine_drive"}
            ));
        }

        if (cardPresetQuiet != null) {
            cardPresetQuiet.setOnClickListener(v -> loadPreset(
                    "Quiet Corners & Coastal Solitude",
                    0,
                    new String[]{"place_banganga", "place_khotachiwadi", "place_central_library"}
            ));
        }

        if (cardPresetBandra != null) {
            cardPresetBandra.setOnClickListener(v -> loadPreset(
                    "Bandra Bohemian & Sunset Walk",
                    350,
                    new String[]{"place_candies_bandra", "place_drip_cafe", "place_bandra_fort"}
            ));
        }
    }

    private void loadPreset(String title, int budget, String[] placeIds) {
        currentItinerary = new Itinerary("itin_active", title, budget, null);
        currentItinerary.setPlanningDate("2026-11-15");
        currentItinerary.setStartTime("09:00");
        currentItinerary.setPlanningRationale("Curated Mumbai Discovery Walk. Optimized for scenic walking pace.");

        int order = 0;
        for (String id : placeIds) {
            Place p = placeRepository.getPlaceById(id);
            if (p != null) {
                currentItinerary.getStops().add(new ItineraryStop("stop_" + order, p, order));
                order++;
            }
        }

        PlannerEngine.recalculateSchedule(currentItinerary, "09:00", "WALKING");
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
                    PlannerEngine.recalculateSchedule(currentItinerary, currentItinerary.getStartTime(), "WALKING");
                    placeRepository.saveItinerary(currentItinerary);
                    updateItineraryUI();
                }
            }

            @Override
            public void onMoveDown(int position) {
                if (currentItinerary != null && position < currentItinerary.getStops().size() - 1) {
                    List<ItineraryStop> stops = currentItinerary.getStops();
                    Collections.swap(stops, position, position + 1);
                    PlannerEngine.recalculateSchedule(currentItinerary, currentItinerary.getStartTime(), "WALKING");
                    placeRepository.saveItinerary(currentItinerary);
                    updateItineraryUI();
                }
            }

            @Override
            public void onDelete(int position) {
                if (currentItinerary != null && position >= 0 && position < currentItinerary.getStops().size()) {
                    currentItinerary.getStops().remove(position);
                    PlannerEngine.recalculateSchedule(currentItinerary, currentItinerary.getStartTime(), "WALKING");
                    placeRepository.saveItinerary(currentItinerary);
                    updateItineraryUI();
                    Toast.makeText(requireContext(), "Stop removed. Schedule recalculated.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onReplace(int position) {
                showReplaceStopDialog(position);
            }

            @Override
            public void onStopClick(ItineraryStop stop) {
                if (stop.getPlace() != null) {
                    Intent intent = new Intent(requireContext(), PlaceDetailActivity.class);
                    intent.putExtra(PlaceDetailActivity.EXTRA_PLACE_ID, stop.getPlace().getId());
                    startActivity(intent);
                }
            }

            @Override
            public void onStopDirectionsClick(ItineraryStop stop) {
                if (stop.getPlace() != null) {
                    ItineraryMapManager.openSinglePlaceDirections(requireContext(), stop.getPlace());
                } else {
                    Toast.makeText(requireContext(), "Location unavailable for this stop.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLegDirectionsClick(TravelLeg leg, ItineraryStop fromStop) {
                if (fromStop == null) return;
                // Determine the next stop for destination
                if (currentItinerary != null) {
                    List<ItineraryStop> stops = currentItinerary.getStops();
                    int fromIdx = stops.indexOf(fromStop);
                    if (fromIdx >= 0 && fromIdx < stops.size() - 1) {
                        ItineraryStop toStop = stops.get(fromIdx + 1);
                        Place origin = fromStop.getPlace();
                        Place dest = toStop.getPlace();
                        ItineraryMapManager.openLegInGoogleMaps(requireContext(), origin, dest);
                    }
                }
            }
        });
        rvItineraryStops.setAdapter(itineraryAdapter);
    }

    private void showReplaceStopDialog(int position) {
        if (currentItinerary == null || position < 0 || position >= currentItinerary.getStops().size()) return;
        ItineraryStop stopToReplace = currentItinerary.getStops().get(position);
        Place currentPlace = stopToReplace.getPlace();

        Set<String> alreadyInPlan = new HashSet<>();
        for (ItineraryStop s : currentItinerary.getStops()) {
            if (s.getPlace() != null) alreadyInPlan.add(s.getPlace().getId());
        }

        List<Place> allPlaces = placeRepository.getAllPlaces();
        List<Place> alternatives = new ArrayList<>();

        // Prefer same category or nearby
        for (Place p : allPlaces) {
            if (alreadyInPlan.contains(p.getId())) continue;
            if (currentPlace != null && p.getCategory().equalsIgnoreCase(currentPlace.getCategory())) {
                alternatives.add(p);
            }
        }
        // If few, add other verified spots
        if (alternatives.size() < 4) {
            for (Place p : allPlaces) {
                if (!alreadyInPlan.contains(p.getId()) && !alternatives.contains(p)) {
                    alternatives.add(p);
                }
            }
        }

        String[] altLabels = new String[alternatives.size()];
        for (int i = 0; i < alternatives.size(); i++) {
            Place p = alternatives.get(i);
            String cost = p.getEstimatedCost() == 0 ? "Free" : "₹" + p.getEstimatedCost();
            altLabels[i] = p.getName() + " (" + p.getArea() + " • " + cost + ")";
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Replace " + (currentPlace != null ? currentPlace.getName() : "Stop"))
                .setItems(altLabels, (dialog, which) -> {
                    Place newPlace = alternatives.get(which);
                    ItineraryStop newStop = new ItineraryStop("stop_" + position, newPlace, position);
                    newStop.setRecommendationReason("Replaced by you • " + newPlace.getCategory() + " spot");
                    currentItinerary.getStops().set(position, newStop);

                    PlannerEngine.recalculateSchedule(currentItinerary, currentItinerary.getStartTime(), "WALKING");
                    placeRepository.saveItinerary(currentItinerary);
                    updateItineraryUI();
                    Toast.makeText(requireContext(), "Replaced with " + newPlace.getName() + "! Schedule recalculated.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
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

        // Date and Time window subtitle
        String dateStr = currentItinerary.getPlanningDate() != null ? currentItinerary.getPlanningDate() : "Today";
        String sTime = currentItinerary.getStartTime() != null ? currentItinerary.getStartTime() : "09:00 AM";
        String fTime = currentItinerary.getFinishTime() != null ? currentItinerary.getFinishTime() : "06:00 PM";
        tvItineraryDateWindow.setText("📅 " + dateStr + " • " + sTime + " – " + fTime);

        // Rationale text
        String rationale = currentItinerary.getPlanningRationale();
        if (rationale != null && !rationale.isEmpty()) {
            tvPlanningRationale.setVisibility(View.VISIBLE);
            tvPlanningRationale.setText("💡 " + rationale);
        } else {
            tvPlanningRationale.setVisibility(View.GONE);
        }

        // Warnings banner
        List<String> warnings = currentItinerary.getWarnings();
        if (warnings != null && !warnings.isEmpty()) {
            layoutWarnings.setVisibility(View.VISIBLE);
            StringBuilder wsb = new StringBuilder();
            for (int i = 0; i < warnings.size(); i++) {
                if (i > 0) wsb.append("\n• ");
                else wsb.append("• ");
                wsb.append(warnings.get(i));
            }
            tvWarningsText.setText(wsb.toString());
        } else if (!currentItinerary.isCostIsFullyKnown()) {
            layoutWarnings.setVisibility(View.VISIBLE);
            tvWarningsText.setText("Some stops include estimated snack or transit costs. Opening hours verified.");
        } else {
            layoutWarnings.setVisibility(View.GONE);
        }

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

        // --- Render map ---
        if (itineraryMapManager != null) {
            itineraryMapManager.renderItinerary(currentItinerary);
        }

        // --- Budget breakdown card ---
        updateBudgetBreakdown(totalCost);
    }

    private void updateBudgetBreakdown(int totalCost) {
        CostBreakdown breakdown = currentItinerary.getCostBreakdown();

        if (breakdown != null) {
            // Entry fees
            if (tvBreakdownEntryFees != null) {
                int fees = breakdown.getEntryFeesInr();
                tvBreakdownEntryFees.setText(fees == 0 ? "₹0 (Free entry)" : "₹" + fees);
            }
            // Food
            if (tvBreakdownFood != null) {
                int food = breakdown.getFoodCostInr();
                tvBreakdownFood.setText(food == 0 ? "₹0" : "₹" + food);
            }
            // Transport
            if (tvBreakdownTransport != null) {
                int transport = breakdown.getTransportFareInr();
                tvBreakdownTransport.setText(transport == 0 ? "₹0" : "₹" + transport);
            }
            // Completeness note
            if (tvCostCompletenessNote != null && breakdown.getCostCompletenessNote() != null) {
                tvCostCompletenessNote.setText(breakdown.getCostCompletenessNote());
            }
        } else {
            // Compute simple breakdown from stops when no detailed breakdown available
            int entryFees = 0, food = 0;
            for (ItineraryStop stop : currentItinerary.getStops()) {
                String cat = stop.getPlace() != null ? stop.getPlace().getCategory() : "";
                int cost = stop.getEstimatedCost();
                if (cat.equalsIgnoreCase("Food") || cat.equalsIgnoreCase("Cafe")
                        || cat.equalsIgnoreCase("Restaurant") || cat.equalsIgnoreCase("Affordable Eats")) {
                    food += cost;
                } else {
                    entryFees += cost;
                }
            }
            if (tvBreakdownEntryFees != null) tvBreakdownEntryFees.setText(entryFees == 0 ? "₹0 (Free entry)" : "₹" + entryFees);
            if (tvBreakdownFood != null) tvBreakdownFood.setText(food == 0 ? "₹0" : "₹" + food);
            if (tvBreakdownTransport != null) tvBreakdownTransport.setText("—");
        }

        // Total
        if (tvBreakdownTotal != null) {
            tvBreakdownTotal.setText("Total Est. Cost: ₹" + totalCost);
        }

        // Target budget row
        int budget = currentItinerary.getTargetBudget();
        if (tvBreakdownTargetBudget != null) {
            if (budget > 0) {
                tvBreakdownTargetBudget.setVisibility(View.VISIBLE);
                tvBreakdownTargetBudget.setText("Your Budget: ₹" + budget);
            } else {
                tvBreakdownTargetBudget.setVisibility(View.GONE);
            }
        }

        // Remaining
        if (tvBreakdownRemaining != null) {
            if (budget > 0) {
                int remaining = budget - totalCost;
                if (remaining >= 0) {
                    tvBreakdownRemaining.setText("Remaining: ₹" + remaining);
                    tvBreakdownRemaining.setBackgroundTintList(
                            requireContext().getColorStateList(R.color.forest_green_pale));
                    tvBreakdownRemaining.setTextColor(requireContext().getColor(R.color.forest_green_primary));
                } else {
                    tvBreakdownRemaining.setText("Over by: ₹" + Math.abs(remaining));
                    tvBreakdownRemaining.setBackgroundTintList(
                            requireContext().getColorStateList(R.color.vibrant_coral_pale));
                    tvBreakdownRemaining.setTextColor(requireContext().getColor(R.color.vibrant_coral));
                }
            } else {
                tvBreakdownRemaining.setVisibility(View.GONE);
            }
        }

        // Budget exceeded banner
        if (layoutBudgetExceededBanner != null) {
            boolean overBudget = budget > 0 && totalCost > budget;
            layoutBudgetExceededBanner.setVisibility(overBudget ? View.VISIBLE : View.GONE);
            if (overBudget && tvBudgetExceededAdvice != null) {
                int over = totalCost - budget;
                tvBudgetExceededAdvice.setText("Your plan is ₹" + over + " over budget. Try removing a paid stop or choosing a free alternative.");
            }
        }
    }

    /** Shows a simple bottom dialog with the transit leg details. */
    private void showLegDetailsDialog(TravelLeg leg, ItineraryStop fromStop) {
        if (leg == null || getContext() == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("From: ").append(fromStop.getPlaceName()).append("\n\n");
        sb.append("Mode: ").append(leg.getRecommendedMode() != null ? leg.getRecommendedMode() : "Walking").append("\n");
        if (leg.getLineOrRoute() != null && !leg.getLineOrRoute().isEmpty()) {
            sb.append("Line: ").append(leg.getLineOrRoute()).append("\n");
        }
        if (leg.getBoardingStop() != null && !leg.getBoardingStop().isEmpty()) {
            sb.append("Board at: ").append(leg.getBoardingStop()).append("\n");
        }
        if (leg.getExitStop() != null && !leg.getExitStop().isEmpty()) {
            sb.append("Exit at: ").append(leg.getExitStop()).append("\n");
        }
        sb.append("Duration: ").append(leg.getDurationMinutes()).append(" mins\n");
        if (leg.getFareInr() > 0) {
            sb.append("Fare: ₹").append(leg.getFareInr());
            if (leg.isFareIsEstimated()) sb.append(" (est.)");
            sb.append("\n");
        } else {
            sb.append("Fare: Free\n");
        }
        if (leg.getTransitSummary() != null && !leg.getTransitSummary().isEmpty()) {
            sb.append("\n").append(leg.getTransitSummary());
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("🚇 Transit Details")
                .setMessage(sb.toString())
                .setPositiveButton("Directions in Maps", (d, w) -> {
                    if (currentItinerary != null) {
                        List<ItineraryStop> stops = currentItinerary.getStops();
                        int idx = stops.indexOf(fromStop);
                        if (idx >= 0 && idx < stops.size() - 1) {
                            ItineraryMapManager.openLegInGoogleMaps(
                                    requireContext(), fromStop.getPlace(), stops.get(idx + 1).getPlace());
                        }
                    }
                })
                .setNegativeButton("Close", null)
                .show();
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
                            newStop.setRecommendationReason("Added manually by you");

                            currentItinerary.getStops().add(newStop);
                            PlannerEngine.recalculateSchedule(currentItinerary, currentItinerary.getStartTime(), "WALKING");
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
        if (currentItinerary.getPlanningDate() != null) {
            sb.append("Date: ").append(currentItinerary.getPlanningDate()).append("\n");
        }
        sb.append("Duration: ").append(String.format(Locale.getDefault(), "%.1f hrs", currentItinerary.calculateTotalDurationMinutes() / 60f)).append("\n");
        sb.append("Budget: ₹").append(currentItinerary.calculateTotalCost()).append("\n\n");
        sb.append("Route Timeline:\n");

        int count = 1;
        for (ItineraryStop stop : currentItinerary.getStops()) {
            String timeStr = stop.getArrivalTime() != null ? "[" + stop.getArrivalTime() + " - " + stop.getDepartureTime() + "] " : "";
            sb.append(count).append(". ").append(timeStr).append(stop.getPlaceName()).append(" (").append(stop.getPlaceArea()).append(")\n");
            sb.append("   ⏱ ").append(stop.getDurationMinutes()).append(" mins • ₹").append(stop.getEstimatedCost()).append("\n");
            if (stop.getRecommendationReason() != null && !stop.getRecommendationReason().isEmpty()) {
                sb.append("   💡 ").append(stop.getRecommendationReason()).append("\n");
            }
            if (stop.getTravelTimeToNextMinutes() > 0) {
                sb.append("   ➡️ ").append(stop.getTravelTimeToNextMinutes()).append(" mins to next stop\n");
            }
            count++;
        }

        sb.append("\nCreated with SheherSe • Discover Mumbai, From Mumbai.");

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
