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
import com.example.sheherse.model.ItineraryStop;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ItineraryAdapter
        extends RecyclerView.Adapter<ItineraryAdapter.StopViewHolder> {

    public interface OnStopActionListener {
        void onMoveUp(int position);
        void onMoveDown(int position);
        void onDelete(int position);
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
        this.stops = stops != null
                ? new ArrayList<>(stops)
                : new ArrayList<>();
        this.actionListener = actionListener;
    }

    public void updateStops(List<ItineraryStop> newStops) {
        this.stops = newStops != null
                ? new ArrayList<>(newStops)
                : new ArrayList<>();

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StopViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_itinerary_stop, parent, false);

        return new StopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull StopViewHolder holder,
            int position
    ) {
        holder.bind(stops.get(position), position, stops.size());
    }

    @Override
    public int getItemCount() {
        return stops.size();
    }

    class StopViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvStopNumber;
        private final TextView tvStopName;
        private final TextView tvStopArea;
        private final TextView tvStopDuration;
        private final TextView tvStopCost;

        private final ImageButton btnMoveUp;
        private final ImageButton btnMoveDown;
        private final ImageButton btnDeleteStop;

        public StopViewHolder(@NonNull View itemView) {
            super(itemView);

            tvStopNumber = itemView.findViewById(R.id.tvStopNumber);
            tvStopName = itemView.findViewById(R.id.tvStopName);
            tvStopArea = itemView.findViewById(R.id.tvStopArea);
            tvStopDuration = itemView.findViewById(R.id.tvStopDuration);
            tvStopCost = itemView.findViewById(R.id.tvStopCost);

            btnMoveUp = itemView.findViewById(R.id.btnMoveUp);
            btnMoveDown = itemView.findViewById(R.id.btnMoveDown);
            btnDeleteStop = itemView.findViewById(R.id.btnDeleteStop);
        }

        public void bind(
                ItineraryStop stop,
                int position,
                int totalCount
        ) {
            tvStopNumber.setText(
                    String.valueOf(position + 1)
            );

            tvStopName.setText(stop.getPlaceName());
            tvStopArea.setText(stop.getPlaceArea());

            tvStopDuration.setText(
                    String.format(
                            Locale.getDefault(),
                            "⏱ %d mins",
                            stop.getDurationMinutes()
                    )
            );

            int cost = stop.getEstimatedCost();

            if (cost == 0) {
                tvStopCost.setText("₹0 (Free)");
            } else {
                tvStopCost.setText(
                        String.format(
                                Locale.getDefault(),
                                "₹%d",
                                cost
                        )
                );
            }

            btnMoveUp.setVisibility(
                    position > 0 ? View.VISIBLE : View.INVISIBLE
            );

            btnMoveDown.setVisibility(
                    position < totalCount - 1
                            ? View.VISIBLE
                            : View.INVISIBLE
            );

            btnMoveUp.setOnClickListener(v -> {
                int currentPosition = getBindingAdapterPosition();

                if (currentPosition != RecyclerView.NO_POSITION
                        && actionListener != null) {
                    actionListener.onMoveUp(currentPosition);
                }
            });

            btnMoveDown.setOnClickListener(v -> {
                int currentPosition = getBindingAdapterPosition();

                if (currentPosition != RecyclerView.NO_POSITION
                        && actionListener != null) {
                    actionListener.onMoveDown(currentPosition);
                }
            });

            btnDeleteStop.setOnClickListener(v -> {
                int currentPosition = getBindingAdapterPosition();

                if (currentPosition != RecyclerView.NO_POSITION
                        && actionListener != null) {
                    actionListener.onDelete(currentPosition);
                }
            });
        }
    }
}
