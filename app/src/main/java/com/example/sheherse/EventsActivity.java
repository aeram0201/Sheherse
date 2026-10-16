package com.example.sheherse;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sheherse.adapter.EventAdapter;
import com.example.sheherse.data.PlaceRepository;
import com.example.sheherse.model.Event;
import com.example.sheherse.model.Itinerary;
import com.example.sheherse.model.ItineraryStop;
import com.example.sheherse.model.Place;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventsActivity extends AppCompatActivity {

    private MaterialToolbar toolbarEvents;
    private RecyclerView rvEventsList;
    private EventAdapter eventAdapter;
    private PlaceRepository placeRepository;
    private List<Event> events = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        placeRepository = PlaceRepository.getInstance(this);

        toolbarEvents = findViewById(R.id.toolbarEvents);
        rvEventsList = findViewById(R.id.rvEventsList);

        toolbarEvents.setNavigationOnClickListener(v -> finish());

        rvEventsList.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(this, events, event -> showEventDetailDialog(event));
        rvEventsList.setAdapter(eventAdapter);

        loadEvents();
    }

    private void loadEvents() {
        events.clear();
        events.addAll(placeRepository.getAllEvents());
        eventAdapter.updateEvents(events);
    }

    private void showEventDetailDialog(Event event) {
        String costStr = event.getPrice() == 0 ? "Free Entry" : "₹" + event.getPrice() + " per person";
        String sampleNotice = event.isSample() ? "\n\n[Status: Sample demonstration event - dates and times are for prototype preview]" : "";

        String message = "📅 " + event.getDateTime() + "\n"
                + "📍 " + event.getVenue() + ", " + event.getArea() + "\n"
                + "💰 Cost: " + costStr + "\n"
                + "👤 Host: " + event.getHostName() + "\n\n"
                + event.getDescription()
                + sampleNotice;

        new AlertDialog.Builder(this)
                .setTitle(event.getTitle())
                .setMessage(message)
                .setPositiveButton("Add to My Day", (dialog, which) -> {
                    addEventToPlan(event);
                })
                .setNegativeButton("Close", null)
                .setNeutralButton("Share", (dialog, which) -> {
                    shareEvent(event);
                })
                .show();
    }

    private void addEventToPlan(Event event) {
        List<Itinerary> itineraries = placeRepository.getSavedItineraries();
        Itinerary target;
        if (itineraries.isEmpty()) {
            target = new Itinerary("itin_current", "My Mumbai Outing", 500, null);
        } else {
            target = itineraries.get(0);
        }

        // Check duplicates
        for (ItineraryStop stop : target.getStops()) {
            if (stop.getPlace() != null && stop.getPlace().getName().equalsIgnoreCase(event.getTitle())) {
                Toast.makeText(this, event.getTitle() + " is already in your day plan!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Create virtual place for event stop
        Place eventPlace = new Place(
                "event_stop_" + event.getId(),
                event.getTitle(),
                event.getDescription(),
                "Event",
                event.getArea(),
                event.getVenue(),
                event.getPrice(),
                false,
                90,
                event.getLatitude(),
                event.getLongitude(),
                Arrays.asList("Event", event.getCategory()),
                Arrays.asList("Seating"),
                "SAMPLE",
                false, false, false,
                "", "Moderate", 4.8f,
                event.getDateTime(),
                "Arrive 15 minutes early to meet the host: " + event.getHostName()
        );

        ItineraryStop stop = new ItineraryStop("stop_" + System.currentTimeMillis(), eventPlace, target.getStops().size());
        target.getStops().add(stop);
        placeRepository.saveItinerary(target);

        Toast.makeText(this, "Added '" + event.getTitle() + "' to My Day plan!", Toast.LENGTH_SHORT).show();
    }

    private void shareEvent(Event event) {
        String cost = event.getPrice() == 0 ? "Free" : "₹" + event.getPrice();
        String body = event.getTitle() + "\n"
                + "When: " + event.getDateTime() + "\n"
                + "Where: " + event.getVenue() + " (" + event.getArea() + ")\n"
                + "Cost: " + cost + "\n\n"
                + event.getDescription() + "\n\n"
                + "Discovered on SheherSe • Mumbai Discovery";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, event.getTitle());
        intent.putExtra(Intent.EXTRA_TEXT, body);
        startActivity(Intent.createChooser(intent, "Share Event"));
    }
}
