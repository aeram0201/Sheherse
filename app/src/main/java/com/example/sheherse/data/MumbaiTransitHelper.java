package com.example.sheherse.data;

import com.example.sheherse.model.CostBreakdown;
import com.example.sheherse.model.Itinerary;
import com.example.sheherse.model.ItineraryStop;
import com.example.sheherse.model.Place;
import com.example.sheherse.model.TransitStep;
import com.example.sheherse.model.TravelLeg;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MumbaiTransitHelper {

    private static class Station {
        String name;
        double lat;
        double lon;
        int seq;

        Station(String name, double lat, double lon, int seq) {
            this.name = name;
            this.lat = lat;
            this.lon = lon;
            this.seq = seq;
        }
    }

    private static final Station[] WESTERN_LINE = {
            new Station("Churchgate Railway Station", 18.9322, 72.8264, 1),
            new Station("Marine Lines Railway Station", 18.9436, 72.8234, 2),
            new Station("Charni Road Railway Station", 18.9515, 72.8184, 3),
            new Station("Grant Road Railway Station", 18.9634, 72.8159, 4),
            new Station("Mumbai Central Railway Station", 18.9696, 72.8194, 5),
            new Station("Mahalaxmi Railway Station", 18.9827, 72.8242, 6),
            new Station("Lower Parel Railway Station", 18.9953, 72.8306, 7),
            new Station("Prabhadevi Railway Station", 19.0069, 72.8347, 8),
            new Station("Dadar Western Railway Station", 19.0178, 72.8432, 9),
            new Station("Matunga Road Railway Station", 19.0284, 72.8441, 10),
            new Station("Mahim Junction Railway Station", 19.0408, 72.8466, 11),
            new Station("Bandra Railway Station", 19.0544, 72.8402, 12),
            new Station("Khar Road Railway Station", 19.0698, 72.8378, 13),
            new Station("Santacruz Railway Station", 19.0818, 72.8385, 14),
            new Station("Vile Parle Railway Station", 19.0988, 72.8439, 15),
            new Station("Andheri Railway Station", 19.1197, 72.8464, 16)
    };

    private static final Station[] CENTRAL_LINE = {
            new Station("CSMT (Chhatrapati Shivaji Maharaj Terminus)", 18.9401, 72.8354, 1),
            new Station("Masjid Railway Station", 18.9526, 72.8399, 2),
            new Station("Sandhurst Road Railway Station", 18.9614, 72.8398, 3),
            new Station("Byculla Railway Station", 18.9774, 72.8329, 4),
            new Station("Chinchpokli Railway Station", 18.9888, 72.8335, 5),
            new Station("Currey Road Railway Station", 18.9950, 72.8340, 6),
            new Station("Parel Railway Station", 19.0084, 72.8386, 7),
            new Station("Dadar Central Railway Station", 19.0182, 72.8435, 8),
            new Station("Matunga Central Railway Station", 19.0270, 72.8550, 9),
            new Station("Sion Railway Station", 19.0392, 72.8624, 10),
            new Station("Kurla Railway Station", 19.0657, 72.8793, 11),
            new Station("Ghatkopar Railway Station", 19.0864, 72.9081, 12)
    };

    private static final Station[] METRO_LINE_1 = {
            new Station("Versova Metro Station", 19.1317, 72.8172, 1),
            new Station("D.N. Nagar Metro Station", 19.1255, 72.8278, 2),
            new Station("Azad Nagar Metro Station", 19.1264, 72.8363, 3),
            new Station("Andheri Metro Station", 19.1197, 72.8464, 4),
            new Station("Western Express Highway Metro Station", 19.1158, 72.8569, 5),
            new Station("Chakala (J.B. Nagar) Metro Station", 19.1121, 72.8665, 6),
            new Station("Airport Road Metro Station", 19.1098, 72.8744, 7),
            new Station("Marol Naka Metro Station", 19.1082, 72.8821, 8),
            new Station("Saki Naka Metro Station", 19.1037, 72.8887, 9),
            new Station("Ghatkopar Metro Station", 19.0864, 72.9081, 10)
    };

    public static double haversineM(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private static Station findNearestStation(double lat, double lon, Station[] stations, double maxMeters) {
        Station best = null;
        double bestDist = Double.MAX_VALUE;
        for (Station s : stations) {
            double d = haversineM(lat, lon, s.lat, s.lon);
            if (d < bestDist && d <= maxMeters) {
                bestDist = d;
                best = s;
            }
        }
        return best;
    }

    public static int calculateLocalTrainFare(double roadKm) {
        if (roadKm <= 10.0) return 5;
        if (roadKm <= 20.0) return 10;
        if (roadKm <= 30.0) return 15;
        return 20;
    }

    public static int calculateMetroFare(int stops) {
        if (stops <= 3) return 10;
        if (stops <= 6) return 20;
        if (stops <= 9) return 30;
        return 40;
    }

    public static int calculateTaxiFare(double roadKm) {
        if (roadKm <= 1.5) return 28;
        return (int) Math.round(28.0 + (roadKm - 1.5) * 18.66);
    }

    public static int calculateAutoFare(double roadKm) {
        if (roadKm <= 1.5) return 23;
        return (int) Math.round(23.0 + (roadKm - 1.5) * 15.33);
    }

    public static TravelLeg planLeg(Place fromPlace, Place toPlace, String requestedMode) {
        String fromTitle = fromPlace != null ? fromPlace.getName() : "Origin";
        String toTitle = toPlace != null ? toPlace.getName() : "Destination";
        String fromId = fromPlace != null ? fromPlace.getId() : null;
        String toId = toPlace != null ? toPlace.getId() : "";

        double lat1 = fromPlace != null ? fromPlace.getLatitude() : 18.92;
        double lon1 = fromPlace != null ? fromPlace.getLongitude() : 72.83;
        double lat2 = toPlace != null ? toPlace.getLatitude() : 18.93;
        double lon2 = toPlace != null ? toPlace.getLongitude() : 72.83;

        double straightM = haversineM(lat1, lon1, lat2, lon2);
        int roadM = (int) Math.round(straightM * 1.35);
        double roadKm = roadM / 1000.0;

        TravelLeg leg = new TravelLeg(fromTitle, toTitle, fromId, toId, requestedMode, roadM, 15);
        leg.setEstimated(true);

        // Case 1: Short walk (<= 1200m)
        if (straightM <= 1200 || ("WALKING".equalsIgnoreCase(requestedMode) && straightM <= 1800)) {
            int walkMin = Math.max((int) Math.ceil((roadKm / 4.5) * 60.0), 2);
            leg.setRecommendedMode("WALKING");
            leg.setDurationMinutes(walkMin);
            leg.setDistanceMeters(roadM);
            leg.setTransitAvailable(true);
            leg.setTransitSummary(String.format(Locale.getDefault(), "Scenic walk (%dm · ~%d mins)", roadM, walkMin));
            leg.setWalkingDistanceMeters(roadM);
            leg.setWalkingDurationMinutes(walkMin);
            leg.setFareInr(0);
            leg.setFareIsEstimated(false);
            leg.setFareSource("Free pedestrian walking route");

            List<TransitStep> steps = new ArrayList<>();
            steps.add(new TransitStep("WALK", "Walk " + roadM + "m to " + toTitle, walkMin, roadM,
                    null, null, null, 0, 0, false, "Urban pedestrian route (~4.5 km/h)"));
            leg.setSteps(steps);
            return leg;
        }

        // Case 2: Western Railway corridor
        Station w1 = findNearestStation(lat1, lon1, WESTERN_LINE, 1600.0);
        Station w2 = findNearestStation(lat2, lon2, WESTERN_LINE, 1600.0);
        if (w1 != null && w2 != null && w1.seq != w2.seq) {
            int stops = Math.abs(w2.seq - w1.seq);
            int trainMin = Math.max((int) Math.round(stops * 2.5), 4);
            int walk1M = (int) Math.round(haversineM(lat1, lon1, w1.lat, w1.lon) * 1.25);
            int walk1Min = Math.max((int) Math.ceil((walk1M / 1000.0 / 4.5) * 60.0), 2);
            int walk2M = (int) Math.round(haversineM(lat2, lon2, w2.lat, w2.lon) * 1.25);
            int walk2Min = Math.max((int) Math.ceil((walk2M / 1000.0 / 4.5) * 60.0), 2);
            int totalMin = walk1Min + 5 + trainMin + walk2Min;
            int fare = calculateLocalTrainFare(roadKm);

            leg.setRecommendedMode("LOCAL_TRAIN");
            leg.setLineOrRoute("Western Line");
            leg.setBoardingStop(w1.name);
            leg.setExitStop(w2.name);
            leg.setDurationMinutes(totalMin);
            leg.setDistanceMeters(roadM);
            leg.setTransitAvailable(true);
            leg.setTransitSummary(String.format(Locale.getDefault(), "Western Line Local: %s → %s (%d stops · ₹%d)",
                    w1.name, w2.name, stops, fare));
            leg.setWalkingDistanceMeters(walk1M + walk2M);
            leg.setWalkingDurationMinutes(walk1Min + walk2Min);
            leg.setFareInr(fare);
            leg.setFareIsEstimated(false);
            leg.setFareSource("Indian Railways Mumbai Suburban Tariff (2nd Class)");

            List<TransitStep> steps = new ArrayList<>();
            steps.add(new TransitStep("WALK", "Walk " + walk1M + "m to " + w1.name + " (~" + walk1Min + " mins)",
                    walk1Min, walk1M, null, null, null, 0, 0, false, "Pedestrian route"));
            steps.add(new TransitStep("LOCAL_TRAIN", "Board Western Line Local train to " + w2.name + " (" + stops + " stops, ~" + trainMin + " mins)",
                    trainMin + 5, roadM, "Western Line", w1.name, w2.name, stops, fare, false,
                    "Indian Railways Suburban Tariff"));
            steps.add(new TransitStep("WALK", "Exit " + w2.name + " and walk " + walk2M + "m to " + toTitle,
                    walk2Min, walk2M, null, null, null, 0, 0, false, "Pedestrian route"));
            leg.setSteps(steps);
            return leg;
        }

        // Case 3: Central Railway corridor
        Station c1 = findNearestStation(lat1, lon1, CENTRAL_LINE, 1600.0);
        Station c2 = findNearestStation(lat2, lon2, CENTRAL_LINE, 1600.0);
        if (c1 != null && c2 != null && c1.seq != c2.seq) {
            int stops = Math.abs(c2.seq - c1.seq);
            int trainMin = Math.max((int) Math.round(stops * 2.8), 4);
            int walk1M = (int) Math.round(haversineM(lat1, lon1, c1.lat, c1.lon) * 1.25);
            int walk1Min = Math.max((int) Math.ceil((walk1M / 1000.0 / 4.5) * 60.0), 2);
            int walk2M = (int) Math.round(haversineM(lat2, lon2, c2.lat, c2.lon) * 1.25);
            int walk2Min = Math.max((int) Math.ceil((walk2M / 1000.0 / 4.5) * 60.0), 2);
            int totalMin = walk1Min + 5 + trainMin + walk2Min;
            int fare = calculateLocalTrainFare(roadKm);

            leg.setRecommendedMode("LOCAL_TRAIN");
            leg.setLineOrRoute("Central Line");
            leg.setBoardingStop(c1.name);
            leg.setExitStop(c2.name);
            leg.setDurationMinutes(totalMin);
            leg.setDistanceMeters(roadM);
            leg.setTransitAvailable(true);
            leg.setTransitSummary(String.format(Locale.getDefault(), "Central Line Local: %s → %s (%d stops · ₹%d)",
                    c1.name, c2.name, stops, fare));
            leg.setWalkingDistanceMeters(walk1M + walk2M);
            leg.setWalkingDurationMinutes(walk1Min + walk2Min);
            leg.setFareInr(fare);
            leg.setFareIsEstimated(false);
            leg.setFareSource("Indian Railways Mumbai Suburban Tariff (2nd Class)");

            List<TransitStep> steps = new ArrayList<>();
            steps.add(new TransitStep("WALK", "Walk " + walk1M + "m to " + c1.name + " (~" + walk1Min + " mins)",
                    walk1Min, walk1M, null, null, null, 0, 0, false, "Pedestrian route"));
            steps.add(new TransitStep("LOCAL_TRAIN", "Board Central Line Local to " + c2.name + " (" + stops + " stops, ~" + trainMin + " mins)",
                    trainMin + 5, roadM, "Central Line", c1.name, c2.name, stops, fare, false,
                    "Indian Railways Suburban Tariff"));
            steps.add(new TransitStep("WALK", "Exit " + c2.name + " and walk " + walk2M + "m to " + toTitle,
                    walk2Min, walk2M, null, null, null, 0, 0, false, "Pedestrian route"));
            leg.setSteps(steps);
            return leg;
        }

        // Case 4: Metro Line 1 corridor
        Station m1 = findNearestStation(lat1, lon1, METRO_LINE_1, 1200.0);
        Station m2 = findNearestStation(lat2, lon2, METRO_LINE_1, 1200.0);
        if (m1 != null && m2 != null && m1.seq != m2.seq) {
            int stops = Math.abs(m2.seq - m1.seq);
            int metroMin = Math.max((int) Math.round(stops * 2.2), 3);
            int walk1M = (int) Math.round(haversineM(lat1, lon1, m1.lat, m1.lon) * 1.2);
            int walk1Min = Math.max((int) Math.ceil((walk1M / 1000.0 / 4.5) * 60.0), 2);
            int walk2M = (int) Math.round(haversineM(lat2, lon2, m2.lat, m2.lon) * 1.2);
            int walk2Min = Math.max((int) Math.ceil((walk2M / 1000.0 / 4.5) * 60.0), 2);
            int fare = calculateMetroFare(stops);

            leg.setRecommendedMode("METRO");
            leg.setLineOrRoute("Metro Line 1");
            leg.setBoardingStop(m1.name);
            leg.setExitStop(m2.name);
            leg.setDurationMinutes(walk1Min + 4 + metroMin + walk2Min);
            leg.setDistanceMeters(roadM);
            leg.setTransitAvailable(true);
            leg.setTransitSummary(String.format(Locale.getDefault(), "Metro Line 1: %s → %s (%d stops · ₹%d)",
                    m1.name, m2.name, stops, fare));
            leg.setWalkingDistanceMeters(walk1M + walk2M);
            leg.setWalkingDurationMinutes(walk1Min + walk2Min);
            leg.setFareInr(fare);
            leg.setFareIsEstimated(false);
            leg.setFareSource("Mumbai Metro One Official Fare Chart");

            List<TransitStep> steps = new ArrayList<>();
            steps.add(new TransitStep("WALK", "Walk " + walk1M + "m to " + m1.name, walk1Min, walk1M, null, null, null, 0, 0, false, "Walk"));
            steps.add(new TransitStep("METRO", "Take Metro Line 1 to " + m2.name + " (" + stops + " stops)", metroMin + 4, roadM, "Metro Line 1", m1.name, m2.name, stops, fare, false, "Metro One"));
            steps.add(new TransitStep("WALK", "Exit " + m2.name + " and walk to " + toTitle, walk2Min, walk2M, null, null, null, 0, 0, false, "Walk"));
            leg.setSteps(steps);
            return leg;
        }

        // Case 5: Public transit details unavailable for this specific pair
        boolean isSuburbs = lat1 > 19.05 && lat2 > 19.05;
        int driveMin = Math.max((int) Math.ceil((roadKm / 18.0) * 60.0) + 3, 5);
        int walkMin = Math.max((int) Math.ceil((roadKm / 4.5) * 60.0), 15);
        int fareEst = isSuburbs ? calculateAutoFare(roadKm) : calculateTaxiFare(roadKm);
        String fareSrc = isSuburbs ? "MMRTA Suburban Auto Tariff (Est. ₹23 base + ₹15.33/km)"
                : "MMRTA Official Taxi Tariff (Est. ₹28 base + ₹18.66/km)";

        leg.setRecommendedMode(isSuburbs ? "AUTO_RICKSHAW" : "TAXI_CAB");
        leg.setTransitAvailable(false);
        leg.setTransitSummary("Public transport details unavailable for this leg");
        leg.setDurationMinutes(driveMin);
        leg.setDistanceMeters(roadM);
        leg.setWalkingDistanceMeters(roadM);
        leg.setWalkingDurationMinutes(walkMin);
        leg.setFareInr(fareEst);
        leg.setFareIsEstimated(true);
        leg.setFareSource(fareSrc);

        List<TransitStep> steps = new ArrayList<>();
        steps.add(new TransitStep(isSuburbs ? "AUTO" : "TAXI",
                (isSuburbs ? "Auto-rickshaw" : "Kaali-Peeli Taxi") + String.format(Locale.getDefault(), ": ~%d mins (%.1f km, est. ₹%d)", driveMin, roadKm, fareEst),
                driveMin, roadM, null, null, null, 0, fareEst, true, fareSrc));
        steps.add(new TransitStep("WALK",
                String.format(Locale.getDefault(), "Alternative: %d min walk (%.1f km) or tap to open live transit in Google Maps", walkMin, roadKm),
                walkMin, roadM, null, null, null, 0, 0, false, "Pedestrian estimate"));
        leg.setSteps(steps);

        return leg;
    }

    public static CostBreakdown calculateCostBreakdown(Itinerary itinerary) {
        if (itinerary == null || itinerary.getStops() == null) {
            return new CostBreakdown(0, 0, 0, 0, false, "No stops scheduled");
        }

        int entryTotal = 0;
        int foodTotal = 0;
        int otherStopTotal = 0;
        boolean hasUnknown = false;

        for (ItineraryStop s : itinerary.getStops()) {
            Place p = s.getPlace();
            int cost = s.getCustomCost();
            if (s.isPriceEstimated()) hasUnknown = true;

            if (p != null) {
                String cat = p.getCategory();
                if ("Historical".equalsIgnoreCase(cat) || "Cultural".equalsIgnoreCase(cat) || "Nature".equalsIgnoreCase(cat)) {
                    entryTotal += cost;
                } else if ("Food".equalsIgnoreCase(cat) || p.isAffordableFood()) {
                    foodTotal += cost;
                } else {
                    otherStopTotal += cost;
                }
            } else {
                otherStopTotal += cost;
            }
        }

        int transportTotal = 0;
        if (itinerary.getTravelLegs() != null) {
            for (TravelLeg leg : itinerary.getTravelLegs()) {
                transportTotal += leg.getFareInr();
                if (leg.isFareIsEstimated()) hasUnknown = true;
            }
        }

        int grandTotal = entryTotal + foodTotal + otherStopTotal + transportTotal;
        String note = String.format(Locale.getDefault(),
                "Breakdown: Entry ₹%d + Food ₹%d + Transit ₹%d = ₹%d",
                entryTotal, foodTotal, transportTotal, grandTotal);

        return new CostBreakdown(entryTotal, foodTotal, transportTotal, grandTotal, hasUnknown, note);
    }
}
