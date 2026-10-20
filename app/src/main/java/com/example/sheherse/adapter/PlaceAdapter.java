package com.example.sheherse.adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sheherse.R;
import com.example.sheherse.data.PlaceRepository;
import com.example.sheherse.model.Place;
import com.example.sheherse.util.PlaceImages;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlaceAdapter
        extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    public interface OnPlaceClickListener {
        void onPlaceClick(Place place);
    }

    private final Context context;
    private List<Place> places;
    private final OnPlaceClickListener clickListener;
    private final PlaceRepository repository;

    public PlaceAdapter(
            Context context,
            List<Place> places,
            OnPlaceClickListener clickListener
    ) {
        this.context = context;
        this.places = places != null
                ? new ArrayList<>(places)
                : new ArrayList<>();
        this.clickListener = clickListener;
        this.repository = PlaceRepository.getInstance(context);
    }

    public void updatePlaces(List<Place> newPlaces) {
        this.places = newPlaces != null
                ? new ArrayList<>(newPlaces)
                : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place, parent, false);

        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PlaceViewHolder holder,
            int position
    ) {
        holder.bind(places.get(position));
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    class PlaceViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivPlaceHero;
        private final TextView tvPlaceCategory;
        private final TextView tvPlaceStatusBadge;
        private final ImageButton btnSavePlace;
        private final TextView tvPlaceName;
        private final TextView tvPlaceArea;
        private final TextView tvPlaceRating;
        private final TextView tvPlaceDescription;
        private final LinearLayout layoutFacilities;
        private final TextView chipWifi;
        private final TextView chipCharging;
        private final TextView chipQuiet;
        private final TextView tvDurationTag;
        private final TextView tvPlaceCost;
        private final TextView tvEstimatedBadge;
        private final MaterialButton btnSharePlace;
        private final MaterialButton btnDirectionsPlace;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);

            ivPlaceHero =
                    itemView.findViewById(R.id.ivPlaceHero);
            tvPlaceCategory =
                    itemView.findViewById(R.id.tvPlaceCategory);
            tvPlaceStatusBadge =
                    itemView.findViewById(R.id.tvPlaceStatusBadge);
            btnSavePlace =
                    itemView.findViewById(R.id.btnSavePlace);
            tvPlaceName =
                    itemView.findViewById(R.id.tvPlaceName);
            tvPlaceArea =
                    itemView.findViewById(R.id.tvPlaceArea);
            tvPlaceRating =
                    itemView.findViewById(R.id.tvPlaceRating);
            tvPlaceDescription =
                    itemView.findViewById(R.id.tvPlaceDescription);
            layoutFacilities =
                    itemView.findViewById(R.id.layoutFacilities);
            chipWifi =
                    itemView.findViewById(R.id.chipWifi);
            chipCharging =
                    itemView.findViewById(R.id.chipCharging);
            chipQuiet =
                    itemView.findViewById(R.id.chipQuiet);
            tvDurationTag =
                    itemView.findViewById(R.id.tvDurationTag);
            tvPlaceCost =
                    itemView.findViewById(R.id.tvPlaceCost);
            tvEstimatedBadge =
                    itemView.findViewById(R.id.tvEstimatedBadge);
            btnSharePlace =
                    itemView.findViewById(R.id.btnSharePlace);
            btnDirectionsPlace =
                    itemView.findViewById(R.id.btnDirectionsPlace);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();

                if (position != RecyclerView.NO_POSITION
                        && clickListener != null) {
                    clickListener.onPlaceClick(places.get(position));
                }
            });
        }

        public void bind(Place place) {

            // Hero image
            if (ivPlaceHero != null) {
                ivPlaceHero.setImageResource(PlaceImages.getPlaceImageRes(place));
            }

            tvPlaceName.setText(safeText(place.getName(), "Unnamed place"));
            tvPlaceArea.setText(safeText(place.getArea(), "Mumbai"));
            tvPlaceCategory.setText(
                    safeText(place.getCategory(), "Place")
            );

            tvPlaceRating.setText(
                    String.format(
                            Locale.getDefault(),
                            "%.1f",
                            place.getRating()
                    )
            );

            tvPlaceDescription.setText(
                    safeText(place.getDescription(), "")
            );

            // Verification status badge
            String status = place.getVerificationStatus();

            if (status != null && !status.trim().isEmpty()) {
                tvPlaceStatusBadge.setVisibility(View.VISIBLE);
                tvPlaceStatusBadge.setText(status);

                if (status.equalsIgnoreCase("VERIFIED")) {
                    tvPlaceStatusBadge.setBackgroundColor(
                            ContextCompat.getColor(
                                    context,
                                    R.color.status_verified
                            )
                    );
                    tvPlaceStatusBadge.setTextColor(
                            ContextCompat.getColor(
                                    context,
                                    R.color.status_verified_text
                            )
                    );

                } else if (status.equalsIgnoreCase("COMMUNITY")) {
                    tvPlaceStatusBadge.setBackgroundColor(
                            ContextCompat.getColor(
                                    context,
                                    R.color.status_community
                            )
                    );
                    tvPlaceStatusBadge.setTextColor(
                            ContextCompat.getColor(
                                    context,
                                    R.color.status_community_text
                            )
                    );

                } else {
                    tvPlaceStatusBadge.setBackgroundColor(
                            ContextCompat.getColor(
                                    context,
                                    R.color.status_sample
                            )
                    );
                    tvPlaceStatusBadge.setTextColor(
                            ContextCompat.getColor(
                                    context,
                                    R.color.status_sample_text
                            )
                    );
                }

            } else {
                tvPlaceStatusBadge.setVisibility(View.GONE);
            }

            // Saved / favourite state
            boolean isSaved = repository.isPlaceSaved(place.getId());
            updateHeartIcon(isSaved);

            btnSavePlace.setOnClickListener(v -> {
                boolean nowSaved =
                        repository.toggleSavePlace(place.getId());

                updateHeartIcon(nowSaved);

                String message = nowSaved
                        ? "Saved to favourites"
                        : "Removed from favourites";

                Toast.makeText(
                        context,
                        message,
                        Toast.LENGTH_SHORT
                ).show();
            });

            // Cost and estimated-price badge
            if (place.getEstimatedCost() == 0) {
                tvPlaceCost.setText("Free");
                tvEstimatedBadge.setVisibility(View.GONE);
            } else {
                tvPlaceCost.setText(
                        String.format(
                                Locale.getDefault(),
                                "₹%d",
                                place.getEstimatedCost()
                        )
                );

                tvEstimatedBadge.setVisibility(
                        place.isPriceEstimated()
                                ? View.VISIBLE
                                : View.GONE
                );
            }

            // Duration
            if (place.getDurationMinutes() > 0) {
                tvDurationTag.setText(
                        String.format(
                                Locale.getDefault(),
                                "• %d mins",
                                place.getDurationMinutes()
                        )
                );
                tvDurationTag.setVisibility(View.VISIBLE);
            } else {
                tvDurationTag.setVisibility(View.GONE);
            }

            // Facilities
            List<String> facilities = place.getFacilities();

            boolean hasWifi = false;
            boolean hasCharging = false;
            boolean hasQuiet = false;

            if (facilities != null) {
                for (String facility : facilities) {
                    if (facility == null) {
                        continue;
                    }

                    String value = facility.toLowerCase(Locale.ROOT);

                    if (value.equals("free wi-fi")
                            || value.equals("wi-fi")
                            || value.equals("wifi")) {
                        hasWifi = true;
                    }

                    if (value.contains("charging")
                            || value.contains("socket")) {
                        hasCharging = true;
                    }

                    if (value.contains("quiet")
                            || value.contains("silent")) {
                        hasQuiet = true;
                    }
                }
            }

            chipWifi.setVisibility(
                    hasWifi ? View.VISIBLE : View.GONE
            );

            chipCharging.setVisibility(
                    hasCharging ? View.VISIBLE : View.GONE
            );

            chipQuiet.setVisibility(
                    hasQuiet ? View.VISIBLE : View.GONE
            );

            // Share place
            btnSharePlace.setOnClickListener(v -> sharePlace(place));

            // Directions
            btnDirectionsPlace.setOnClickListener(
                    v -> openDirections(place)
            );
        }

        private void sharePlace(Place place) {
            String cost = place.getEstimatedCost() == 0
                    ? "Free"
                    : String.format(
                    Locale.getDefault(),
                    "₹%d",
                    place.getEstimatedCost()
            );

            String shareBody =
                    safeText(place.getName(), "Place")
                            + " - "
                            + safeText(place.getArea(), "Mumbai")
                            + "\n"
                            + safeText(place.getDescription(), "")
                            + "\nCost: "
                            + cost
                            + "\nInsider Tip: "
                            + safeText(place.getInsiderTip(), "Not available")
                            + "\n\nDiscovered on Sheherse - Mumbai Explorer";

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, place.getName());
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

            context.startActivity(
                    Intent.createChooser(shareIntent, "Share Place")
            );
        }

        private void openDirections(Place place) {
            String query = safeText(place.getName(), "Place")
                    + ", "
                    + safeText(place.getAddress(), "")
                    + ", "
                    + safeText(place.getArea(), "Mumbai");

            Uri mapUri;

            if (place.getLatitude() != 0.0
                    || place.getLongitude() != 0.0) {

                mapUri = Uri.parse(
                        "geo:" + place.getLatitude()
                                + "," + place.getLongitude()
                                + "?q=" + Uri.encode(query)
                );

            } else {
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

        private void updateHeartIcon(boolean isSaved) {
            if (isSaved) {
                btnSavePlace.setImageResource(R.drawable.ic_heart_filled);
                btnSavePlace.setColorFilter(
                        ContextCompat.getColor(
                                context,
                                R.color.status_rejected_text
                        )
                );
            } else {
                btnSavePlace.setImageResource(R.drawable.ic_heart_outline);
                btnSavePlace.setColorFilter(
                        ContextCompat.getColor(
                                context,
                                R.color.text_secondary
                        )
                );
            }
        }

        private String safeText(String value, String fallback) {
            return value == null || value.trim().isEmpty()
                    ? fallback
                    : value.trim();
        }
    }
}
