
package com.example.sheherse.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sheherse.R;
import com.example.sheherse.model.TourListing;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TourListingAdapter
        extends RecyclerView.Adapter<TourListingAdapter.TourViewHolder> {

    public interface OnTourActionListener {
        void onTourClick(TourListing tour);
        void onDeleteClick(TourListing tour);
    }

    private final Context context;
    private List<TourListing> tours;
    private final boolean canDelete;
    private final OnTourActionListener actionListener;

    public TourListingAdapter(
            Context context,
            List<TourListing> tours,
            boolean canDelete,
            OnTourActionListener actionListener
    ) {
        this.context = context;
        this.tours = tours != null
                ? new ArrayList<>(tours)
                : new ArrayList<>();
        this.canDelete = canDelete;
        this.actionListener = actionListener;
    }

    public void updateTours(List<TourListing> newTours) {
        this.tours = newTours != null
                ? new ArrayList<>(newTours)
                : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TourViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tour, parent, false);

        return new TourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull TourViewHolder holder,
            int position
    ) {
        holder.bind(tours.get(position));
    }

    @Override
    public int getItemCount() {
        return tours.size();
    }

    class TourViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTourArea;
        private final TextView tvTourPrice;
        private final TextView tvTourTitle;
        private final TextView tvTourDescription;
        private final TextView tvTourDuration;
        private final TextView tvTourLanguages;
        private final TextView tvTourMeetingPoint;
        private final ImageButton btnDeleteTour;

        TourViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTourArea = itemView.findViewById(R.id.tvTourArea);
            tvTourPrice = itemView.findViewById(R.id.tvTourPrice);
            tvTourTitle = itemView.findViewById(R.id.tvTourTitle);
            tvTourDescription =
                    itemView.findViewById(R.id.tvTourDescription);
            tvTourDuration =
                    itemView.findViewById(R.id.tvTourDuration);
            tvTourLanguages =
                    itemView.findViewById(R.id.tvTourLanguages);
            tvTourMeetingPoint =
                    itemView.findViewById(R.id.tvTourMeetingPoint);
            btnDeleteTour =
                    itemView.findViewById(R.id.btnDeleteTour);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();

                if (position != RecyclerView.NO_POSITION
                        && actionListener != null) {
                    actionListener.onTourClick(tours.get(position));
                }
            });
        }

        void bind(TourListing tour) {
            tvTourArea.setText(safeText(tour.getArea(), "Mumbai"));
            tvTourTitle.setText(
                    safeText(tour.getTitle(), "Untitled tour")
            );
            tvTourDescription.setText(
                    safeText(tour.getDescription(), "")
            );

            tvTourPrice.setText(
                    String.format(
                            Locale.getDefault(),
                            "₹%d / person",
                            tour.getPricePerPerson()
                    )
            );

            tvTourDuration.setText(
                    String.format(
                            Locale.getDefault(),
                            "⏱ %d hours",
                            tour.getDurationHours()
                    )
            );

            List<String> languages = tour.getLanguages();
            String languageText = languages != null
                    && !languages.isEmpty()
                    ? android.text.TextUtils.join(", ", languages)
                    : "English, Hindi";

            tvTourLanguages.setText("🗣 " + languageText);
            tvTourMeetingPoint.setText(
                    "📍 " + safeText(tour.getMeetingPoint(), "To be confirmed")
            );

            btnDeleteTour.setVisibility(
                    canDelete ? View.VISIBLE : View.GONE
            );

            btnDeleteTour.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();

                if (position != RecyclerView.NO_POSITION
                        && actionListener != null) {
                    actionListener.onDeleteClick(tours.get(position));
                }
            });
        }

        private String safeText(String value, String fallback) {
            return value == null || value.trim().isEmpty()
                    ? fallback
                    : value.trim();
        }
    }
}
