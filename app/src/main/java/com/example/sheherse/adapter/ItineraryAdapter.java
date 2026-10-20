package com.example.sheherse.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sheherse.R;
import com.example.sheherse.model.ItineraryStop;
import com.example.sheherse.model.MenuItem;
import com.example.sheherse.model.Place;
import com.example.sheherse.model.TransitStep;
import com.example.sheherse.model.TravelLeg;
import com.example.sheherse.util.PlaceImages;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ItineraryAdapter extends RecyclerView.Adapter<ItineraryAdapter.StopViewHolder> {

    public interface OnStopActionListener {
        void onMoveUp(int position);
        void onMoveDown(int position);
        void onDelete(int position);
        void onReplace(int position);
        void onStopClick(ItineraryStop stop);
        void onStopDirectionsClick(ItineraryStop stop);
        void onLegDirectionsClick(TravelLeg leg, ItineraryStop fromStop);
    }

    private final Context context;
    private List<ItineraryStop> stops;
    private final OnStopActionListener actionListener;

    public ItineraryAdapter(
            Context context,
            List<ItineraryStop> stops,
            OnStopActionListener actionListener
    ) {
        this.context = context;
        this.stops = stops != null ? new ArrayList<>(stops) : new ArrayList<>();
        this.actionListener = actionListener;
    }

    public void updateStops(List<ItineraryStop> newStops) {
        this.stops = newStops != null ? new ArrayList<>(newStops) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_itinerary_stop, parent, false);
        return new StopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StopViewHolder holder, int position) {
        holder.bind(stops.get(position), position, stops.size());
    }

    @Override
    public int getItemCount() {
        return stops.size();
    }

    class StopViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvStopNumber;
        private final TextView tvStopTimeWindow;
        private final ImageButton btnStopDirections;
        private final ImageButton btnMoveUp;
        private final ImageButton btnMoveDown;
        private final ImageButton btnReplaceStop;
        private final ImageButton btnDeleteStop;

        private final ImageView ivStopPhoto;
        private final TextView tvStopName;
        private final TextView tvStopArea;
        private final TextView tvStopDuration;
        private final TextView tvStopCost;
        private final TextView tvStopOpeningBadge;
        private final TextView tvStopRationale;

        // Menu items section
        private final View layoutMenuSection;
        private final LinearLayout layoutMenuItemsContainer;
        private final TextView tvMenuUnavailableNotice;

        // Travel leg section
        private final View layoutTravelLeg;
        private final TextView tvLegModeBadge;
        private final TextView tvLegFareBadge;
        private final TextView tvTravelLegInfo;
        private final TextView tvTravelLegSteps;
        private final View btnLegDirections;

        public StopViewHolder(@NonNull View itemView) {
            super(itemView);

            tvStopNumber = itemView.findViewById(R.id.tvStopNumber);
            tvStopTimeWindow = itemView.findViewById(R.id.tvStopTimeWindow);
            btnStopDirections = itemView.findViewById(R.id.btnStopDirections);
            btnMoveUp = itemView.findViewById(R.id.btnMoveUp);
            btnMoveDown = itemView.findViewById(R.id.btnMoveDown);
            btnReplaceStop = itemView.findViewById(R.id.btnReplaceStop);
            btnDeleteStop = itemView.findViewById(R.id.btnDeleteStop);

            ivStopPhoto = itemView.findViewById(R.id.ivStopPhoto);
            tvStopName = itemView.findViewById(R.id.tvStopName);
            tvStopArea = itemView.findViewById(R.id.tvStopArea);
            tvStopDuration = itemView.findViewById(R.id.tvStopDuration);
            tvStopCost = itemView.findViewById(R.id.tvStopCost);
            tvStopOpeningBadge = itemView.findViewById(R.id.tvStopOpeningBadge);
            tvStopRationale = itemView.findViewById(R.id.tvStopRationale);

            layoutMenuSection = itemView.findViewById(R.id.layoutMenuSection);
            layoutMenuItemsContainer = itemView.findViewById(R.id.layoutMenuItemsContainer);
            tvMenuUnavailableNotice = itemView.findViewById(R.id.tvMenuUnavailableNotice);

            layoutTravelLeg = itemView.findViewById(R.id.layoutTravelLeg);
            tvLegModeBadge = itemView.findViewById(R.id.tvLegModeBadge);
            tvLegFareBadge = itemView.findViewById(R.id.tvLegFareBadge);
            tvTravelLegInfo = itemView.findViewById(R.id.tvTravelLegInfo);
            tvTravelLegSteps = itemView.findViewById(R.id.tvTravelLegSteps);
            btnLegDirections = itemView.findViewById(R.id.btnLegDirections);
        }

        public void bind(ItineraryStop stop, int position, int totalCount) {
            Place place = stop.getPlace();

            tvStopNumber.setText(String.valueOf(position + 1));
            tvStopName.setText(stop.getPlaceName());
            tvStopArea.setText("📍 " + stop.getPlaceArea());
            tvStopDuration.setText("⏱ " + stop.getDurationMinutes() + " mins");

            // Cost formatting
            int cost = stop.getEstimatedCost();
            if (cost == 0) {
                tvStopCost.setText("₹0 (Free)");
            } else {
                tvStopCost.setText("₹" + cost + (stop.isPriceEstimated() ? " (est.)" : ""));
            }

            // Opening Status Badge
            String openingStatus = stop.getOpeningStatus();
            if ("CONFIRMED_OPEN".equalsIgnoreCase(openingStatus)) {
                tvStopOpeningBadge.setVisibility(View.VISIBLE);
                tvStopOpeningBadge.setText("Open Today");
                tvStopOpeningBadge.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.status_verified_bg));
                tvStopOpeningBadge.setTextColor(ContextCompat.getColor(context, R.color.status_verified_text));
            } else if ("HOURS_UNKNOWN".equalsIgnoreCase(openingStatus)) {
                tvStopOpeningBadge.setVisibility(View.VISIBLE);
                tvStopOpeningBadge.setText("Hours Unverified");
                tvStopOpeningBadge.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.status_pending_bg));
                tvStopOpeningBadge.setTextColor(ContextCompat.getColor(context, R.color.status_pending_text));
            } else {
                tvStopOpeningBadge.setVisibility(View.GONE);
            }

            // Photo binding
            if (ivStopPhoto != null && place != null) {
                ivStopPhoto.setImageResource(PlaceImages.getPlaceImageRes(place));
            }

            // Time Window
            if (stop.getArrivalTime() != null && stop.getDepartureTime() != null) {
                tvStopTimeWindow.setVisibility(View.VISIBLE);
                tvStopTimeWindow.setText(stop.getArrivalTime() + " – " + stop.getDepartureTime());
            } else {
                tvStopTimeWindow.setVisibility(View.GONE);
            }

            // Recommendation Rationale
            String rationale = stop.getRecommendationReason();
            if (rationale != null && !rationale.isEmpty()) {
                tvStopRationale.setVisibility(View.VISIBLE);
                tvStopRationale.setText("💡 " + rationale);
            } else {
                tvStopRationale.setVisibility(View.GONE);
            }

            // Curated Menu Items for Food & Cafe Stops
            boolean isFoodOrCafe = place != null && ("Food".equalsIgnoreCase(place.getCategory())
                    || place.isAffordableFood()
                    || (place.getMenuItems() != null && !place.getMenuItems().isEmpty())
                    || "Cafe".equalsIgnoreCase(place.getCuisineType()));

            if (isFoodOrCafe && layoutMenuSection != null) {
                layoutMenuSection.setVisibility(View.VISIBLE);
                List<MenuItem> menuItems = place != null ? place.getMenuItems() : null;

                if (menuItems != null && !menuItems.isEmpty()) {
                    tvMenuUnavailableNotice.setVisibility(View.GONE);
                    layoutMenuItemsContainer.setVisibility(View.VISIBLE);
                    layoutMenuItemsContainer.removeAllViews();

                    int maxShow = Math.min(menuItems.size(), 3);
                    for (int m = 0; m < maxShow; m++) {
                        MenuItem item = menuItems.get(m);
                        TextView row = new TextView(context);
                        String dietIcon = "NON_VEG".equalsIgnoreCase(item.getDietaryType()) ? "🔴 Non-Veg"
                                : "BEVERAGE".equalsIgnoreCase(item.getDietaryType()) ? "☕ Beverage"
                                : "VEGAN".equalsIgnoreCase(item.getDietaryType()) ? "🌿 Vegan"
                                : "🟢 Veg";
                        String priceStr = item.getPriceInr() > 0 ? "₹" + item.getPriceInr() : "Price unlisted";

                        row.setText(String.format(Locale.getDefault(), "• %s — %s (%s)\n  %s",
                                item.getName(), priceStr, dietIcon, item.getDescription() != null ? item.getDescription() : item.getRecommendationReason()));
                        row.setTextSize(11f);
                        row.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
                        row.setPadding(0, 2, 0, 4);
                        layoutMenuItemsContainer.addView(row);
                    }
                } else {
                    layoutMenuItemsContainer.setVisibility(View.GONE);
                    tvMenuUnavailableNotice.setVisibility(View.VISIBLE);
                }
            } else if (layoutMenuSection != null) {
                layoutMenuSection.setVisibility(View.GONE);
            }

            // Travel Leg to next stop
            if (position < totalCount - 1) {
                layoutTravelLeg.setVisibility(View.VISIBLE);
                TravelLeg leg = stop.getTravelLegToNext();

                if (leg != null) {
                    String mode = leg.getRecommendedMode();
                    if ("LOCAL_TRAIN".equalsIgnoreCase(mode)) {
                        tvLegModeBadge.setText("🚆 LOCAL TRAIN");
                        tvLegModeBadge.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.forest_green_primary));
                    } else if ("METRO".equalsIgnoreCase(mode)) {
                        tvLegModeBadge.setText("🚇 METRO");
                        tvLegModeBadge.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.teal_secondary));
                    } else if ("TAXI_CAB".equalsIgnoreCase(mode)) {
                        tvLegModeBadge.setText("🚕 TAXI");
                        tvLegModeBadge.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.gold_dark));
                    } else if ("AUTO_RICKSHAW".equalsIgnoreCase(mode)) {
                        tvLegModeBadge.setText("🛺 AUTO");
                        tvLegModeBadge.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.gold_dark));
                    } else {
                        tvLegModeBadge.setText("🚶 SCENIC WALK");
                        tvLegModeBadge.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.teal_secondary));
                    }

                    if (leg.getFareInr() == 0) {
                        tvLegFareBadge.setText("Free (Walk)");
                    } else {
                        tvLegFareBadge.setText(String.format(Locale.getDefault(), "₹%d%s",
                                leg.getFareInr(), leg.isFareIsEstimated() ? " (est.)" : ""));
                    }

                    tvTravelLegInfo.setText(leg.getTransitSummary());

                    if (leg.getSteps() != null && !leg.getSteps().isEmpty()) {
                        StringBuilder stepsSb = new StringBuilder();
                        for (int sIdx = 0; sIdx < leg.getSteps().size(); sIdx++) {
                            TransitStep st = leg.getSteps().get(sIdx);
                            if (sIdx > 0) stepsSb.append("\n");
                            stepsSb.append("• ").append(st.getInstruction());
                        }
                        tvTravelLegSteps.setVisibility(View.VISIBLE);
                        tvTravelLegSteps.setText(stepsSb.toString());
                    } else {
                        tvTravelLegSteps.setVisibility(View.GONE);
                    }

                    btnLegDirections.setOnClickListener(v -> {
                        if (actionListener != null) actionListener.onLegDirectionsClick(leg, stop);
                    });
                } else {
                    // Fallback
                    tvLegModeBadge.setText("🚶 WALK");
                    tvLegFareBadge.setText("Free");
                    tvTravelLegInfo.setText(stop.getTravelTimeToNextMinutes() + " mins to next stop");
                    tvTravelLegSteps.setVisibility(View.GONE);
                }
            } else {
                layoutTravelLeg.setVisibility(View.GONE);
            }

            // Buttons state
            btnMoveUp.setEnabled(position > 0);
            btnMoveUp.setAlpha(position > 0 ? 1.0f : 0.3f);
            btnMoveDown.setEnabled(position < totalCount - 1);
            btnMoveDown.setAlpha(position < totalCount - 1 ? 1.0f : 0.3f);

            // Listeners
            itemView.setOnClickListener(v -> {
                if (actionListener != null) actionListener.onStopClick(stop);
            });
            btnStopDirections.setOnClickListener(v -> {
                if (actionListener != null) actionListener.onStopDirectionsClick(stop);
            });
            btnMoveUp.setOnClickListener(v -> {
                if (actionListener != null) actionListener.onMoveUp(position);
            });
            btnMoveDown.setOnClickListener(v -> {
                if (actionListener != null) actionListener.onMoveDown(position);
            });
            btnReplaceStop.setOnClickListener(v -> {
                if (actionListener != null) actionListener.onReplace(position);
            });
            btnDeleteStop.setOnClickListener(v -> {
                if (actionListener != null) actionListener.onDelete(position);
            });
        }
    }
}
