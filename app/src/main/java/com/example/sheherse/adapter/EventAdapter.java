package com.example.sheherse.adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sheherse.R;
import com.example.sheherse.model.Event;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventAdapter
        extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    private final Context context;
    private List<Event> events;
    private final OnEventClickListener clickListener;

    public EventAdapter(
            Context context,
            List<Event> events,
            OnEventClickListener clickListener
    ) {
        this.context = context;
        this.events = events != null
                ? new ArrayList<>(events)
                : new ArrayList<>();
        this.clickListener = clickListener;
    }

    public void updateEvents(List<Event> newEvents) {
        this.events = newEvents != null
                ? new ArrayList<>(newEvents)
                : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);

        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull EventViewHolder holder,
            int position
    ) {
        holder.bind(events.get(position));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvEventCategory;
        private final TextView tvEventPrice;
        private final TextView tvEventTitle;
        private final TextView tvEventDateTime;
        private final TextView tvEventVenue;
        private final TextView tvEventDescription;
        private final TextView tvEventHost;

        private final MaterialButton btnShareEvent;
        private final MaterialButton btnDirectionsEvent;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);

            tvEventCategory =
                    itemView.findViewById(R.id.tvEventCategory);

            tvEventPrice =
                    itemView.findViewById(R.id.tvEventPrice);

            tvEventTitle =
                    itemView.findViewById(R.id.tvEventTitle);

            tvEventDateTime =
                    itemView.findViewById(R.id.tvEventDateTime);

            tvEventVenue =
                    itemView.findViewById(R.id.tvEventVenue);

            tvEventDescription =
                    itemView.findViewById(R.id.tvEventDescription);

            tvEventHost =
                    itemView.findViewById(R.id.tvEventHost);

            btnShareEvent =
                    itemView.findViewById(R.id.btnShareEvent);

            btnDirectionsEvent =
                    itemView.findViewById(R.id.btnDirectionsEvent);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();

                if (position != RecyclerView.NO_POSITION
                        && clickListener != null) {
                    clickListener.onEventClick(events.get(position));
                }
            });
        }

        public void bind(Event event) {

            tvEventCategory.setText(
                    safeText(event.getCategory(), "Event")
            );

            tvEventTitle.setText(
                    safeText(event.getTitle(), "Untitled event")
            );

            tvEventDateTime.setText(
                    safeText(event.getDateTime(), "Date to be announced")
            );

            String venue = safeText(
                    event.getVenue(),
                    "Venue not specified"
            );

            String area = safeText(
                    event.getArea(),
                    "Mumbai"
            );

            tvEventVenue.setText(venue + ", " + area);

            tvEventDescription.setText(
                    safeText(event.getDescription(), "")
            );

            tvEventHost.setText(
                    "Hosted by "
                            + safeText(event.getHostName(), "Unknown host")
            );

            // Display event price.
            if (event.getPriceInINR() == 0) {
                tvEventPrice.setText("Free");
            } else {
                tvEventPrice.setText(
                        String.format(
                                Locale.getDefault(),
                                "₹%d",
                                event.getPriceInINR()
                        )
                );
            }

            // Share event details.
            btnShareEvent.setOnClickListener(
                    v -> shareEvent(event)
            );

            // Open directions to the event venue.
            btnDirectionsEvent.setOnClickListener(
                    v -> openDirections(event)
            );
        }

        private void shareEvent(Event event) {

            String cost;

            if (event.getPriceInINR() == 0) {
                cost = "Free";
            } else {
                cost = String.format(
                        Locale.getDefault(),
                        "₹%d",
                        event.getPriceInINR()
                );
            }

            String shareBody =
                    safeText(event.getTitle(), "Event") + "\n"
                            + "When: " + safeText(event.getDateTime(), "Not specified")
                            + "\n"
                            + "Where: " + safeText(event.getVenue(), "")
                            + " (" + safeText(event.getArea(), "Mumbai") + ")\n"
                            + "Cost: " + cost + "\n"
                            + "Details: " + safeText(event.getDescription(), "")
                            + "\n\n"
                            + "Discovered on Sheherse - Mumbai Explorer";

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");

            shareIntent.putExtra(
                    Intent.EXTRA_SUBJECT,
                    safeText(event.getTitle(), "Event")
            );

            shareIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    shareBody
            );

            context.startActivity(
                    Intent.createChooser(shareIntent, "Share Event")
            );
        }

        private void openDirections(Event event) {

            Uri mapUri;

            double latitude = event.getLatitude();
            double longitude = event.getLongitude();

            String venueLabel =
                    safeText(event.getVenue(), "Event venue")
                            + ", "
                            + safeText(event.getArea(), "Mumbai");

            if (latitude != 0.0 || longitude != 0.0) {

                mapUri = Uri.parse(
                        "geo:" + latitude + "," + longitude
                                + "?q=" + latitude + "," + longitude
                                + "(" + Uri.encode(venueLabel) + ")"
                );

            } else {

                String query = venueLabel + ", Mumbai";

                mapUri = Uri.parse(
                        "https://www.google.com/maps/search/?api=1&query="
                                + Uri.encode(query)
                );
            }

            Intent mapIntent = new Intent(
                    Intent.ACTION_VIEW,
                    mapUri
            );

            try {
                context.startActivity(mapIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(
                        context,
                        "No map application is available.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }

        private String safeText(String value, String fallback) {
            return value == null || value.trim().isEmpty()
                    ? fallback
                    : value.trim();
        }
    }
}
