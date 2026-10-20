package com.example.sheherse.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.sheherse.model.CostBreakdown;
import com.example.sheherse.model.Itinerary;
import com.example.sheherse.model.ItineraryStop;
import com.example.sheherse.model.MenuItem;
import com.example.sheherse.model.Place;
import com.example.sheherse.model.TransitStep;
import com.example.sheherse.model.TravelLeg;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlannerEngine {

    public interface PlanCallback {
        void onSuccess(Itinerary itinerary, boolean isFromBackend);
        void onError(String message);
    }

    public static class QuestionnaireData {
        public String date = "2026-11-15";
        public String startTime = "09:00";
        public String finishTime = "17:00";
        public boolean flexibleSchedule = false;

        public String startNeighbourhood = "Colaba / Fort";
        public double startLat = 18.9220;
        public double startLon = 72.8347;
        public String startPlaceId = null;

        public String finishNeighbourhood = null;
        public double finishLat = 0.0;
        public double finishLon = 0.0;
        public String finishPlaceId = null;

        public List<String> mustVisitPlaceIds = new ArrayList<>();
        public List<String> avoidPlaceIds = new ArrayList<>();

        public List<String> interests = new ArrayList<>();
        public String specificNotes = "";

        public int budgetInr = 500;
        public int groupSize = 1;
        public String foodPreference = "NO_PREFERENCE";
        public boolean includeFoodCosts = true;

        public String transportMode = "WALKING"; // WALKING, TRANSIT, DRIVING, TWO_WHEELER
        public String pacePreference = "BALANCED"; // RELAXED, BALANCED, PACKED
        public String experienceTier = "BALANCED"; // BUDGET, BALANCED, COMFORT
        public String travelerType = "SOLO"; // SOLO, FRIENDS, FAMILY, CHILDREN
        public List<String> accessibilityNeeds = new ArrayList<>();
    }

    private static PlannerEngine instance;
    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static final String PREF_NAME = "sheherse_planner_prefs";
    private static final String KEY_BACKEND_URL = "backend_url";
    private static final String DEFAULT_BACKEND_URL = "http://10.0.2.2:8000";

    private PlannerEngine(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized PlannerEngine getInstance(Context context) {
        if (instance == null) {
            instance = new PlannerEngine(context);
        }
        return instance;
    }

    public String getBackendUrl() {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_BACKEND_URL, DEFAULT_BACKEND_URL);
    }

    public void setBackendUrl(String url) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_BACKEND_URL, url).apply();
    }

    /**
     * Main entry point to plan an itinerary using questionnaire answers.
     * Tries the backend service first; if unreachable, falls back seamlessly to the
     * local constraint-satisfaction engine and reports this honestly.
     */
    public void generatePlan(QuestionnaireData data, PlanCallback callback) {
        executor.execute(() -> {
            try {
                // Try backend API first
                Itinerary backendResult = callBackendApi(data);
                if (backendResult != null) {
                    mainHandler.post(() -> callback.onSuccess(backendResult, true));
                    return;
                }
            } catch (Exception e) {
                // Backend unreachable or returned error, proceed to local engine
            }

            // Execute on-device local heuristic engine
            try {
                Itinerary localResult = planLocally(data);
                mainHandler.post(() -> callback.onSuccess(localResult, false));
            } catch (Exception ex) {
                Log.e("PlannerEngine", "Local planning failed", ex);
                mainHandler.post(() -> callback.onError("Couldn't generate your plan right now. Please try again."));
            }
        });
    }

    private Itinerary callBackendApi(QuestionnaireData data) throws Exception {
        String baseUrl = getBackendUrl();
        URL url = new URL(baseUrl + "/plan");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(4000);
        conn.setReadTimeout(6000);
        conn.setDoOutput(true);

        // Calculate available duration
        int startMin = parseTimeToMinutes(data.startTime);
        int finishMin = parseTimeToMinutes(data.finishTime);
        int duration = finishMin - startMin;
        if (duration <= 0) duration = 360; // 6 hours fallback

        JSONObject reqJson = new JSONObject();
        reqJson.put("date", data.date);
        reqJson.put("start_time", data.startTime);
        reqJson.put("finish_time", data.finishTime);
        reqJson.put("available_duration_minutes", duration);
        reqJson.put("flexible_schedule", data.flexibleSchedule);
        reqJson.put("budget_inr", data.budgetInr);
        reqJson.put("group_size", data.groupSize);
        reqJson.put("food_preference", data.foodPreference);
        reqJson.put("include_food_costs", data.includeFoodCosts);
        reqJson.put("specific_notes", data.specificNotes);
        reqJson.put("transport_mode", data.transportMode);
        reqJson.put("pace_preference", data.pacePreference);
        reqJson.put("experience_tier", data.experienceTier);
        reqJson.put("traveler_type", data.travelerType);

        if (data.startPlaceId != null && !data.startPlaceId.isEmpty()) {
            reqJson.put("start_place_id", data.startPlaceId);
        } else {
            JSONObject loc = new JSONObject();
            loc.put("latitude", data.startLat);
            loc.put("longitude", data.startLon);
            reqJson.put("start_location", loc);
        }

        if (data.finishPlaceId != null && !data.finishPlaceId.isEmpty()) {
            reqJson.put("finish_place_id", data.finishPlaceId);
        } else if (data.finishLat != 0.0 && data.finishLon != 0.0) {
            JSONObject floc = new JSONObject();
            floc.put("latitude", data.finishLat);
            floc.put("longitude", data.finishLon);
            reqJson.put("finish_location", floc);
        }

        JSONArray interestsArr = new JSONArray();
        for (String i : data.interests) interestsArr.put(i);
        reqJson.put("interests", interestsArr);

        JSONArray prefArr = new JSONArray();
        for (String p : data.mustVisitPlaceIds) prefArr.put(p);
        reqJson.put("preferred_place_ids", prefArr);

        JSONArray avoidArr = new JSONArray();
        for (String a : data.avoidPlaceIds) avoidArr.put(a);
        reqJson.put("avoid_place_ids", avoidArr);

        JSONArray accArr = new JSONArray();
        for (String acc : data.accessibilityNeeds) accArr.put(acc);
        reqJson.put("accessibility_needs", accArr);

        byte[] postBytes = reqJson.toString().getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(postBytes);
        }

        int code = conn.getResponseCode();
        if (code != 200) {
            return null;
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();

        return parseBackendResponse(sb.toString(), data);
    }

    private Itinerary parseBackendResponse(String jsonStr, QuestionnaireData qData) throws Exception {
        JSONObject root = new JSONObject(jsonStr);
        String id = root.optString("itinerary_id", "itin_" + System.currentTimeMillis());
        String title = root.optString("title", "My Mumbai Personalised Day");
        String rationale = root.optString("planning_rationale", "");

        JSONObject summary = root.getJSONObject("summary");
        int targetBudget = summary.optInt("budget_inr", qData.budgetInr);
        boolean costKnown = summary.optBoolean("cost_is_fully_known", true);
        boolean feasible = summary.optBoolean("is_feasible", true);
        int totalTravel = summary.optInt("total_travel_minutes", 0);
        int totalVisit = summary.optInt("total_visit_minutes", 0);

        Itinerary itinerary = new Itinerary(id, title, targetBudget, qData.interests);
        itinerary.setPlanningDate(qData.date);
        itinerary.setStartTime(qData.startTime);
        itinerary.setFinishTime(qData.finishTime);
        itinerary.setCostIsFullyKnown(costKnown);
        itinerary.setFeasible(feasible);
        itinerary.setTotalTravelMinutes(totalTravel);
        itinerary.setTotalVisitMinutes(totalVisit);
        itinerary.setPlanningRationale(rationale);

        // Parse cost breakdown if present
        JSONObject cbObj = summary.optJSONObject("cost_breakdown");
        if (cbObj != null) {
            CostBreakdown cb = new CostBreakdown(
                    cbObj.optInt("entry_fees_inr", 0),
                    cbObj.optInt("food_cost_inr", 0),
                    cbObj.optInt("transport_fare_inr", 0),
                    cbObj.optInt("total_cost_inr", 0),
                    cbObj.optBoolean("has_unknown_costs", false),
                    cbObj.optString("cost_completeness_note", "Fees sourced")
            );
            itinerary.setCostBreakdown(cb);
        }

        // Parse warnings
        JSONArray warningsArr = root.optJSONArray("warnings");
        if (warningsArr != null) {
            for (int i = 0; i < warningsArr.length(); i++) {
                JSONObject w = warningsArr.getJSONObject(i);
                itinerary.getWarnings().add(w.optString("message", ""));
            }
        }

        // Parse travel legs
        List<TravelLeg> parsedLegs = new ArrayList<>();
        JSONArray legsArr = root.optJSONArray("travel_legs");
        if (legsArr != null) {
            for (int l = 0; l < legsArr.length(); l++) {
                JSONObject legJson = legsArr.getJSONObject(l);
                TravelLeg leg = new TravelLeg();
                leg.setFromTitle(legJson.optString("from_title", "Origin"));
                leg.setToTitle(legJson.optString("to_title", "Destination"));
                leg.setFromPlaceId(legJson.optString("from_place_id", null));
                leg.setToPlaceId(legJson.optString("to_place_id", ""));
                leg.setMode(legJson.optString("mode", "WALKING"));
                leg.setRecommendedMode(legJson.optString("recommended_mode", "WALKING"));
                leg.setDistanceMeters(legJson.optInt("distance_meters", 500));
                leg.setDurationMinutes(legJson.optInt("duration_minutes", 10));
                leg.setEstimated(legJson.optBoolean("is_estimated", true));
                leg.setEstimationBasis(legJson.optString("estimation_basis", ""));
                leg.setRouteWarning(legJson.optString("route_warning", null));
                leg.setTransitAvailable(legJson.optBoolean("transit_available", true));
                leg.setTransitSummary(legJson.optString("transit_summary", "Direct walk"));
                leg.setBoardingStop(legJson.optString("boarding_stop", null));
                leg.setExitStop(legJson.optString("exit_stop", null));
                leg.setLineOrRoute(legJson.optString("line_or_route", null));
                leg.setWalkingDistanceMeters(legJson.optInt("walking_distance_meters", 0));
                leg.setWalkingDurationMinutes(legJson.optInt("walking_duration_minutes", 0));
                leg.setFareInr(legJson.optInt("fare_inr", 0));
                leg.setFareIsEstimated(legJson.optBoolean("fare_is_estimated", false));
                leg.setFareSource(legJson.optString("fare_source", ""));

                JSONArray stepsArr = legJson.optJSONArray("steps");
                if (stepsArr != null) {
                    List<TransitStep> steps = new ArrayList<>();
                    for (int sIdx = 0; sIdx < stepsArr.length(); sIdx++) {
                        JSONObject st = stepsArr.getJSONObject(sIdx);
                        steps.add(new TransitStep(
                                st.optString("step_type", "WALK"),
                                st.optString("instruction", ""),
                                st.optInt("duration_minutes", 5),
                                st.optInt("distance_meters", 0),
                                st.optString("line_name", null),
                                st.optString("boarding_stop", null),
                                st.optString("exit_stop", null),
                                st.optInt("num_stops", 0),
                                st.optInt("fare_inr", 0),
                                st.optBoolean("is_fare_estimated", false),
                                st.optString("source", null)
                        ));
                    }
                    leg.setSteps(steps);
                }
                parsedLegs.add(leg);
            }
        }
        itinerary.setTravelLegs(parsedLegs);

        // Parse stops
        PlaceRepository repo = PlaceRepository.getInstance(context);
        JSONArray stopsArr = root.getJSONArray("stops");
        for (int i = 0; i < stopsArr.length(); i++) {
            JSONObject s = stopsArr.getJSONObject(i);
            String stopId = s.optString("stop_id", "stop_" + i);
            String placeId = s.optString("place_id", "");
            Place place = repo.getPlaceById(placeId);
            if (place == null) {
                // construct fallback from JSON place object
                place = new Place(placeId, s.optJSONObject("place") != null ? s.optJSONObject("place").optString("name", "Stop") : "Stop", "", "Cultural", "Mumbai", "", 0, false, 60, 18.92, 72.83, new ArrayList<>(), new ArrayList<>(), "VERIFIED", false, false, false, "", "Moderate", 4.5f, "", "");
            }

            ItineraryStop stop = new ItineraryStop(stopId, place, i);
            stop.setArrivalTime(s.optString("arrival_time", ""));
            stop.setDepartureTime(s.optString("departure_time", ""));
            stop.setCustomDurationMinutes(s.optInt("duration_minutes", place.getDurationMinutes()));
            stop.setCustomCost(s.optInt("cost_inr", place.getEstimatedCost()));
            stop.setPriceEstimated(s.optBoolean("price_estimated", place.isPriceEstimated()));
            stop.setCostNote(s.optString("cost_note", ""));
            stop.setOpeningStatus(s.optString("opening_status", "CONFIRMED_OPEN"));
            stop.setRecommendationReason(s.optString("recommendation_reason", ""));
            stop.setCustomNote(s.optString("custom_note", ""));

            // Match travel leg to next stop if available
            for (TravelLeg leg : parsedLegs) {
                if (placeId.equals(leg.getFromPlaceId())) {
                    stop.setTravelTimeToNextMinutes(leg.getDurationMinutes());
                    stop.setTravelModeToNext(leg.getRecommendedMode());
                    stop.setTravelDistanceToNextMeters(leg.getDistanceMeters());
                    stop.setTravelLegToNext(leg);
                    break;
                }
            }

            itinerary.getStops().add(stop);
        }

        if (itinerary.getCostBreakdown() == null) {
            itinerary.setCostBreakdown(MumbaiTransitHelper.calculateCostBreakdown(itinerary));
        }

        return itinerary;
    }

    /**
     * Pure Java on-device deterministic constraint-satisfaction planner.
     * Executes when the backend is offline or unreachable, ensuring the user ALWAYS
     * gets a personalised, geographically sensible, time-budget-aware day plan.
     */
    public Itinerary planLocally(QuestionnaireData data) {
        PlaceRepository repo = PlaceRepository.getInstance(context);
        List<Place> allPlaces = repo.getAllPlaces();

        int startMin = parseTimeToMinutes(data.startTime);
        int finishMin = parseTimeToMinutes(data.finishTime);
        int totalAvailableMinutes = finishMin - startMin;
        if (totalAvailableMinutes <= 0) totalAvailableMinutes = 360;

        Itinerary itinerary = new Itinerary("itin_" + System.currentTimeMillis(), "Personalised " + data.startNeighbourhood + " Day Plan", data.budgetInr, data.interests);
        itinerary.setPlanningDate(data.date);
        itinerary.setStartTime(data.startTime);
        itinerary.setFinishTime(data.finishTime);
        itinerary.setCostIsFullyKnown(true);
        Log.d("PlannerEngine", "On-device plan generated (backend unavailable). URL: " + getBackendUrl());
        itinerary.setPlanningRationale("Personalised for " + data.travelerType.toLowerCase(Locale.ROOT) +
                " (" + data.pacePreference.toLowerCase(Locale.ROOT) + " pace) · ₹" + data.budgetInr + " budget");

        // Resolve starting coordinates
        double currentLat = data.startLat;
        double currentLon = data.startLon;
        if (data.startPlaceId != null) {
            Place sp = repo.getPlaceById(data.startPlaceId);
            if (sp != null) {
                currentLat = sp.getLatitude();
                currentLon = sp.getLongitude();
            }
        }

        double finishLat = data.finishLat;
        double finishLon = data.finishLon;
        if (data.finishPlaceId != null) {
            Place fp = repo.getPlaceById(data.finishPlaceId);
            if (fp != null) {
                finishLat = fp.getLatitude();
                finishLon = fp.getLongitude();
            }
        }

        // Filter eligible places
        Set<String> avoidSet = new HashSet<>(data.avoidPlaceIds);
        if (data.startPlaceId != null) avoidSet.add(data.startPlaceId);

        List<Place> candidates = new ArrayList<>();
        for (Place p : allPlaces) {
            if (avoidSet.contains(p.getId())) continue;
            if (p.getLatitude() == 0.0 && p.getLongitude() == 0.0) continue;
            // Check accessibility
            if (!data.accessibilityNeeds.isEmpty()) {
                boolean ok = true;
                for (String need : data.accessibilityNeeds) {
                    if ("Quiet".equalsIgnoreCase(need)) {
                        boolean isQuiet = "silent".equalsIgnoreCase(p.getQuietnessLevel())
                                || "quiet".equalsIgnoreCase(p.getQuietnessLevel())
                                || (p.getFacilities() != null && p.getFacilities().contains("Quiet"));
                        if (!isQuiet) ok = false;
                    } else if ("WiFi".equalsIgnoreCase(need)) {
                        boolean hasWifi = p.getFacilities() != null && p.getFacilities().contains("WiFi");
                        if (!hasWifi) ok = false;
                    } else if ("Charging".equalsIgnoreCase(need)) {
                        boolean hasCharging = p.getFacilities() != null && p.getFacilities().contains("Charging");
                        if (!hasCharging) ok = false;
                    }
                }
                if (!ok) continue;
            }
            candidates.add(p);
        }

        // Pacing duration multipliers
        float paceFactor = 1.0f;
        int bufferMinutes = 15;
        if ("RELAXED".equalsIgnoreCase(data.pacePreference)) {
            paceFactor = 1.35f;
            bufferMinutes = 25;
        } else if ("PACKED".equalsIgnoreCase(data.pacePreference)) {
            paceFactor = 0.85f;
            bufferMinutes = 10;
        }

        // Iterative greedy scheduling
        int currentMinutes = startMin;
        int remainingBudget = data.budgetInr;
        int endLimitMinutes = startMin + totalAvailableMinutes;
        Set<String> visitedIds = new HashSet<>(avoidSet);

        List<ItineraryStop> scheduledStops = new ArrayList<>();
        int orderIndex = 0;

        while (currentMinutes < endLimitMinutes && !candidates.isEmpty()) {
            final double cLat = currentLat;
            final double cLon = currentLon;
            final int remB = remainingBudget;
            final boolean isLaterInDay = (currentMinutes - startMin) > (totalAvailableMinutes * 0.5);

            // Score candidates
            Place bestPlace = null;
            double bestScore = -9999.0;
            String bestRationale = "";

            for (Place p : candidates) {
                if (visitedIds.contains(p.getId())) continue;

                double score = 0.0;
                List<String> reasons = new ArrayList<>();

                // Must-visit priority
                if (data.mustVisitPlaceIds.contains(p.getId())) {
                    score += 150.0;
                    reasons.add("Must-visit selection");
                }

                // Category match
                for (String interest : data.interests) {
                    if (interest.equalsIgnoreCase(p.getCategory())) {
                        score += 35.0;
                        reasons.add("Matches " + p.getCategory());
                    }
                    for (String tag : p.getTags()) {
                        if (tag.toLowerCase(Locale.ROOT).contains(interest.toLowerCase(Locale.ROOT))) {
                            score += 12.0;
                            reasons.add("Tag " + tag);
                            break;
                        }
                    }
                }

                // Specific note match
                if (data.specificNotes != null && !data.specificNotes.trim().isEmpty()) {
                    String[] tokens = data.specificNotes.toLowerCase(Locale.ROOT).split("\\s+");
                    String pText = (p.getName() + " " + p.getDescription() + " " + p.getArea() + " " + p.getInsiderTip()).toLowerCase(Locale.ROOT);
                    for (String t : tokens) {
                        if (t.length() >= 3 && pText.contains(t)) {
                            score += 20.0;
                            reasons.add("Matches wish: " + t);
                        }
                    }
                }

                // Food preference
                if ("Food".equalsIgnoreCase(p.getCategory())) {
                    String fDesc = (p.getDescription() + " " + p.getCuisineType()).toLowerCase(Locale.ROOT);
                    if ("VEGETARIAN".equalsIgnoreCase(data.foodPreference) || "VEGAN".equalsIgnoreCase(data.foodPreference)) {
                        if (fDesc.contains("kheema") || fDesc.contains("mutton") || fDesc.contains("meat")) {
                            score -= 30.0;
                        } else {
                            score += 15.0;
                            reasons.add("Vegetarian friendly");
                        }
                    }
                }

                // Traveler type
                if ("FAMILY".equalsIgnoreCase(data.travelerType) || "CHILDREN".equalsIgnoreCase(data.travelerType)) {
                    if ("Nature".equalsIgnoreCase(p.getCategory()) || "Historical".equalsIgnoreCase(p.getCategory())) {
                        score += 15.0;
                        reasons.add("Family friendly");
                    }
                }

                // Quality & verification
                score += p.getRating() * 8.0;
                if ("VERIFIED".equalsIgnoreCase(p.getVerificationStatus())) score += 5.0;
                if (p.isHiddenGem()) {
                    score += 12.0;
                    reasons.add("Curated hidden gem");
                }

                // Proximity penalty
                double distKm = haversineKm(cLat, cLon, p.getLatitude(), p.getLongitude());
                score -= Math.min(distKm * 2.0, 35.0);
                if (distKm <= 2.5) reasons.add(String.format(Locale.getDefault(), "Nearby (%.1f km)", distKm));

                // Destination convergence
                if (isLaterInDay && finishLat != 0.0 && finishLon != 0.0) {
                    double distToFin = haversineKm(finishLat, finishLon, p.getLatitude(), p.getLongitude());
                    if (distToFin <= 4.0) {
                        score += 20.0;
                        reasons.add("Close to return location");
                    } else {
                        score -= Math.min(distToFin * 1.2, 25.0);
                    }
                }

                // Cost feasibility
                if (p.getEstimatedCost() > remB) {
                    score -= 40.0; // Penalty if exceeds remaining budget
                }

                if (score > bestScore) {
                    bestScore = score;
                    bestPlace = p;
                    bestRationale = reasons.isEmpty() ? "Curated recommendation for " + p.getArea() : joinStrings(reasons, "; ");
                }
            }

            if (bestPlace == null) break;

            // Travel time calculation to this bestPlace
            int travelMin = estimateTravelMinutes(cLat, cLon, bestPlace.getLatitude(), bestPlace.getLongitude(), data.transportMode);
            int arrivalMin = currentMinutes + travelMin;
            int dwellMin = Math.round(bestPlace.getDurationMinutes() * paceFactor);
            int departureMin = arrivalMin + dwellMin;

            // Check if exceeds day limit
            if (departureMin > endLimitMinutes && !scheduledStops.isEmpty()) {
                break;
            }

            ItineraryStop stop = new ItineraryStop("stop_" + orderIndex, bestPlace, orderIndex);
            stop.setArrivalTime(formatMinutesToTime(arrivalMin));
            stop.setDepartureTime(formatMinutesToTime(departureMin));
            stop.setCustomDurationMinutes(dwellMin);
            stop.setCustomCost(bestPlace.getEstimatedCost() * Math.max(1, data.groupSize));
            stop.setPriceEstimated(bestPlace.isPriceEstimated());
            stop.setCostNote(bestPlace.getEstimatedCost() == 0 ? "Free entry" : "₹" + bestPlace.getEstimatedCost() + (bestPlace.isPriceEstimated() ? " (est.)" : ""));
            stop.setOpeningStatus("CONFIRMED_OPEN");
            stop.setRecommendationReason(bestRationale);
            stop.setCustomNote(bestPlace.getInsiderTip());

            if (!bestPlace.isPriceEstimated()) {
                // fully verified cost
            } else {
                itinerary.setCostIsFullyKnown(false);
            }

            scheduledStops.add(stop);
            visitedIds.add(bestPlace.getId());

            // Set travel leg on previous stop
            if (scheduledStops.size() > 1) {
                ItineraryStop prev = scheduledStops.get(scheduledStops.size() - 2);
                prev.setTravelTimeToNextMinutes(travelMin);
                prev.setTravelModeToNext(data.transportMode);
                prev.setTravelDistanceToNextMeters((int) (haversineKm(currentLat, currentLon, bestPlace.getLatitude(), bestPlace.getLongitude()) * 1000));
            }

            currentMinutes = departureMin + bufferMinutes;
            currentLat = bestPlace.getLatitude();
            currentLon = bestPlace.getLongitude();
            remainingBudget -= stop.getCustomCost();
            orderIndex++;
        }

        itinerary.setStops(scheduledStops);
        recalculateSchedule(itinerary, data.startTime, data.transportMode);
        return itinerary;
    }

    /**
     * Recalculates arrival, departure, travel times, and total cost after any user edit
     * (e.g. replacing a stop, removing a stop, moving up/down, or adding a stop).
     */
    public static void recalculateSchedule(Itinerary itinerary, String startTime, String transportMode) {
        if (itinerary == null || itinerary.getStops() == null || itinerary.getStops().isEmpty()) return;

        int currentMin = parseTimeToMinutes(startTime != null ? startTime : "09:00");
        int totalTravel = 0;
        int totalVisit = 0;

        List<TravelLeg> legs = new ArrayList<>();
        List<ItineraryStop> stops = itinerary.getStops();
        for (int i = 0; i < stops.size(); i++) {
            ItineraryStop stop = stops.get(i);
            stop.setOrderIndex(i);

            int dwell = stop.getCustomDurationMinutes();
            stop.setArrivalTime(formatMinutesToTime(currentMin));
            stop.setDepartureTime(formatMinutesToTime(currentMin + dwell));
            currentMin += dwell;
            totalVisit += dwell;

            if (i < stops.size() - 1) {
                ItineraryStop next = stops.get(i + 1);
                TravelLeg leg = MumbaiTransitHelper.planLeg(
                        stop.getPlace(),
                        next.getPlace(),
                        transportMode != null ? transportMode : "WALKING"
                );
                legs.add(leg);

                stop.setTravelLegToNext(leg);
                stop.setTravelTimeToNextMinutes(leg.getDurationMinutes());
                stop.setTravelModeToNext(leg.getRecommendedMode());
                stop.setTravelDistanceToNextMeters(leg.getDistanceMeters());

                totalTravel += leg.getDurationMinutes();
                currentMin += leg.getDurationMinutes();
            } else {
                stop.setTravelTimeToNextMinutes(0);
                stop.setTravelLegToNext(null);
            }
        }

        itinerary.setTravelLegs(legs);
        itinerary.setTotalTravelMinutes(totalTravel);
        itinerary.setTotalVisitMinutes(totalVisit);
        itinerary.setFinishTime(formatMinutesToTime(currentMin));
        itinerary.setCostBreakdown(MumbaiTransitHelper.calculateCostBreakdown(itinerary));
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private static int estimateTravelMinutes(double lat1, double lon1, double lat2, double lon2, String mode) {
        double km = haversineKm(lat1, lon1, lat2, lon2);
        double speedKmh = 4.5; // Walking
        if ("TRANSIT".equalsIgnoreCase(mode)) speedKmh = 18.0;
        else if ("DRIVING".equalsIgnoreCase(mode)) speedKmh = 22.0;
        else if ("TWO_WHEELER".equalsIgnoreCase(mode)) speedKmh = 28.0;

        int mins = (int) Math.round((km / speedKmh) * 60.0);
        return Math.max(5, mins); // Minimum 5 minutes
    }

    public static int parseTimeToMinutes(String hhmm) {
        try {
            String[] parts = hhmm.split(":");
            return Integer.parseInt(parts[0].trim()) * 60 + Integer.parseInt(parts[1].trim());
        } catch (Exception e) {
            return 9 * 60; // 09:00 default
        }
    }

    public static String formatMinutesToTime(int totalMinutes) {
        int h = (totalMinutes / 60) % 24;
        int m = totalMinutes % 60;
        String ampm = h >= 12 ? "PM" : "AM";
        int displayH = h % 12;
        if (displayH == 0) displayH = 12;
        return String.format(Locale.getDefault(), "%02d:%02d %s", displayH, m, ampm);
    }

    private static String joinStrings(List<String> list, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(delimiter);
            sb.append(list.get(i));
        }
        return sb.toString();
    }
}
