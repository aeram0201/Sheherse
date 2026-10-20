package com.example.sheherse.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.sheherse.model.Itinerary;
import com.example.sheherse.model.ItineraryStop;
import com.example.sheherse.model.Place;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ItineraryMapManager {

    public interface OnMapInteractionListener {
        void onMarkerSelected(String placeId, int orderIndex);
        void onLegSelected(int fromIndex, int toIndex);
        void onMapReady();
        void onMapFailed(String reason);
    }

    private final WebView webView;
    private final OnMapInteractionListener listener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isMapLoaded = false;
    private Itinerary pendingItinerary = null;

    public ItineraryMapManager(WebView webView, OnMapInteractionListener listener) {
        this.webView = webView;
        this.listener = listener;
        initWebView();
    }

    private void initWebView() {
        if (webView == null) return;

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                isMapLoaded = true;
                if (pendingItinerary != null) {
                    renderItinerary(pendingItinerary);
                    pendingItinerary = null;
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (listener != null) {
                    listener.onMapFailed(description);
                }
            }
        });

        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void onMarkerClick(String placeId, int orderIndex) {
                mainHandler.post(() -> {
                    if (listener != null) listener.onMarkerSelected(placeId, orderIndex);
                });
            }

            @JavascriptInterface
            public void onLegClick(int fromIndex, int toIndex) {
                mainHandler.post(() -> {
                    if (listener != null) listener.onLegSelected(fromIndex, toIndex);
                });
            }

            @JavascriptInterface
            public void onMapReady() {
                mainHandler.post(() -> {
                    if (listener != null) listener.onMapReady();
                });
            }

            @JavascriptInterface
            public void onMapError(String msg) {
                mainHandler.post(() -> {
                    if (listener != null) listener.onMapFailed(msg);
                });
            }
        }, "AndroidBridge");

        webView.loadUrl("file:///android_asset/map/itinerary_map.html");
    }

    public void renderItinerary(Itinerary itinerary) {
        if (itinerary == null || itinerary.getStops() == null) return;

        if (!isMapLoaded) {
            this.pendingItinerary = itinerary;
            return;
        }

        try {
            JSONObject root = new JSONObject();
            JSONArray stopsArr = new JSONArray();

            for (int i = 0; i < itinerary.getStops().size(); i++) {
                ItineraryStop s = itinerary.getStops().get(i);
                Place p = s.getPlace();
                if (p == null || (p.getLatitude() == 0.0 && p.getLongitude() == 0.0)) continue;

                JSONObject obj = new JSONObject();
                obj.put("orderIndex", i);
                obj.put("placeId", p.getId());
                obj.put("name", s.getPlaceName());
                obj.put("area", s.getPlaceArea());
                obj.put("lat", p.getLatitude());
                obj.put("lon", p.getLongitude());

                String timeWindow = "";
                if (s.getArrivalTime() != null && s.getDepartureTime() != null) {
                    timeWindow = s.getArrivalTime() + " - " + s.getDepartureTime();
                }
                obj.put("timeWindow", timeWindow);

                String costText = s.getEstimatedCost() == 0 ? "Free entry" : "₹" + s.getEstimatedCost();
                obj.put("costText", costText);

                stopsArr.put(obj);
            }

            root.put("stops", stopsArr);

            String jsonPayload = root.toString().replace("'", "\\'");
            webView.evaluateJavascript("renderItinerary('" + jsonPayload + "');", null);
        } catch (Exception e) {
            if (listener != null) listener.onMapFailed(e.getMessage());
        }
    }

    public static void openFullItineraryInGoogleMaps(Context context, Itinerary itinerary) {
        if (itinerary == null || itinerary.getStops() == null || itinerary.getStops().isEmpty()) {
            Toast.makeText(context, "No stops in itinerary to navigate.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Place> validPlaces = new ArrayList<>();
        for (ItineraryStop stop : itinerary.getStops()) {
            if (stop.getPlace() != null && (stop.getPlace().getLatitude() != 0.0 || stop.getPlace().getLongitude() != 0.0)) {
                validPlaces.add(stop.getPlace());
            }
        }

        if (validPlaces.isEmpty()) {
            Toast.makeText(context, "No GPS coordinates available for route stops.", Toast.LENGTH_SHORT).show();
            return;
        }

        Place origin = validPlaces.get(0);
        Place destination = validPlaces.get(validPlaces.size() - 1);

        StringBuilder uriBuilder = new StringBuilder("https://www.google.com/maps/dir/?api=1");
        uriBuilder.append("&origin=").append(origin.getLatitude()).append(",").append(origin.getLongitude());
        uriBuilder.append("&destination=").append(destination.getLatitude()).append(",").append(destination.getLongitude());

        if (validPlaces.size() > 2) {
            uriBuilder.append("&waypoints=");
            for (int i = 1; i < validPlaces.size() - 1; i++) {
                if (i > 1) uriBuilder.append("|");
                Place wp = validPlaces.get(i);
                uriBuilder.append(wp.getLatitude()).append(",").append(wp.getLongitude());
            }
        }
        uriBuilder.append("&travelmode=walking");

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriBuilder.toString()));
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "Google Maps is not installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public static void openLegInGoogleMaps(Context context, Place origin, Place destination) {
        if (origin == null || destination == null) {
            Toast.makeText(context, "Location coordinates unavailable for directions.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uri = String.format(Locale.US,
                "https://www.google.com/maps/dir/?api=1&origin=%f,%f&destination=%f,%f&travelmode=walking",
                origin.getLatitude(), origin.getLongitude(),
                destination.getLatitude(), destination.getLongitude());

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "No map application is available.", Toast.LENGTH_SHORT).show();
        }
    }

    public static void openSinglePlaceDirections(Context context, Place place) {
        if (place == null) return;
        Uri gmmIntentUri;
        if (place.getLatitude() != 0.0 || place.getLongitude() != 0.0) {
            gmmIntentUri = Uri.parse("geo:" + place.getLatitude() + "," + place.getLongitude() + "?q=" + Uri.encode(place.getName() + ", Mumbai"));
        } else {
            gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(place.getName() + " " + place.getArea() + " Mumbai"));
        }
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        try {
            context.startActivity(mapIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "No map application is available.", Toast.LENGTH_SHORT).show();
        }
    }
}
