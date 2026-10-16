package com.example.sheherse;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sheherse.adapter.PlaceAdapter;
import com.example.sheherse.data.PlaceRepository;
import com.example.sheherse.model.Place;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class SavedFragment extends Fragment {

    private TextView tvSavedCountSubtitle;
    private RecyclerView rvSavedPlaces;
    private View layoutEmptySaved;
    private MaterialButton btnExploreNow;

    private PlaceRepository placeRepository;
    private PlaceAdapter placeAdapter;
    private final List<Place> savedPlaces = new ArrayList<>();

    private HomeFragment.OnNavigationRequest navigationListener;

    public void setNavigationListener(HomeFragment.OnNavigationRequest listener) {
        this.navigationListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_saved, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        placeRepository = PlaceRepository.getInstance(requireContext());

        tvSavedCountSubtitle = view.findViewById(R.id.tvSavedCountSubtitle);
        rvSavedPlaces = view.findViewById(R.id.rvSavedPlaces);
        layoutEmptySaved = view.findViewById(R.id.layoutEmptySaved);
        btnExploreNow = view.findViewById(R.id.btnExploreNow);

        rvSavedPlaces.setLayoutManager(new LinearLayoutManager(requireContext()));
        placeAdapter = new PlaceAdapter(requireContext(), savedPlaces, place -> {
            Intent intent = new Intent(requireContext(), PlaceDetailActivity.class);
            intent.putExtra(PlaceDetailActivity.EXTRA_PLACE_ID, place.getId());
            startActivity(intent);
        });
        rvSavedPlaces.setAdapter(placeAdapter);

        btnExploreNow.setOnClickListener(v -> {
            if (navigationListener != null) {
                navigationListener.navigateToTab(R.id.nav_explore);
            }
        });

        loadSavedPlaces();
    }

    private void loadSavedPlaces() {
        savedPlaces.clear();
        savedPlaces.addAll(placeRepository.getSavedPlaces());

        if (savedPlaces.isEmpty()) {
            layoutEmptySaved.setVisibility(View.VISIBLE);
            rvSavedPlaces.setVisibility(View.GONE);
            tvSavedCountSubtitle.setText("0 spots curated in your collection");
        } else {
            layoutEmptySaved.setVisibility(View.GONE);
            rvSavedPlaces.setVisibility(View.VISIBLE);
            tvSavedCountSubtitle.setText(savedPlaces.size() + " spots curated in your personal collection");
            placeAdapter.updatePlaces(savedPlaces);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSavedPlaces();
    }
}
