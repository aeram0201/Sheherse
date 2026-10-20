package com.example.sheherse;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sheherse.adapter.PlaceAdapter;
import com.example.sheherse.data.PlaceRepository;
import com.example.sheherse.model.Itinerary;
import com.example.sheherse.model.ItineraryStop;
import com.example.sheherse.model.Place;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CategoryListActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY_TYPE = "extra_category_type";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_SUBTITLE = "extra_subtitle";

    public static final String TYPE_HIDDEN_GEMS = "HIDDEN_GEMS";
    public static final String TYPE_STUDY_WORK = "STUDY_WORK";
    public static final String TYPE_AFFORDABLE_FOOD = "AFFORDABLE_FOOD";
    public static final String TYPE_FIRST_TIME = "FIRST_TIME";
    public static final String TYPE_HISTORICAL = "HISTORICAL";
    public static final String TYPE_CULTURAL = "CULTURAL";
    public static final String TYPE_NATURE = "NATURE";
    public static final String TYPE_ALL = "ALL";

    private MaterialToolbar toolbarCategory;
    private TextView tvCategorySubtitle;
    private TextView tvCategoryCount;
    private RecyclerView rvCategoryPlaces;
    private View layoutCategoryEmpty;

    private MaterialCardView cardFirstTimeTransitTips;
    private MaterialButton btnPlanFirstDay;
    private MaterialCardView cardHiddenGemsSubmitBanner;
    private MaterialButton btnSubmitGemFromCategory;

    private PlaceRepository placeRepository;
    private PlaceAdapter placeAdapter;
    private List<Place> filteredPlaces = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);

        placeRepository = PlaceRepository.getInstance(this);

        initViews();
        loadCategoryData();
    }

    private void initViews() {
        toolbarCategory = findViewById(R.id.toolbarCategory);
        tvCategorySubtitle = findViewById(R.id.tvCategorySubtitle);
        tvCategoryCount = findViewById(R.id.tvCategoryCount);
        rvCategoryPlaces = findViewById(R.id.rvCategoryPlaces);
        layoutCategoryEmpty = findViewById(R.id.layoutCategoryEmpty);

        cardFirstTimeTransitTips = findViewById(R.id.cardFirstTimeTransitTips);
        btnPlanFirstDay = findViewById(R.id.btnPlanFirstDay);
        cardHiddenGemsSubmitBanner = findViewById(R.id.cardHiddenGemsSubmitBanner);
        btnSubmitGemFromCategory = findViewById(R.id.btnSubmitGemFromCategory);

        if (btnPlanFirstDay != null) {
            btnPlanFirstDay.setOnClickListener(v -> {
                Itinerary itin = new Itinerary("itin_first_time_" + System.currentTimeMillis(), "First Time in Mumbai Essentials", 350, null);
                String[] presetIds = {"place_marine_drive", "place_kyani", "place_asiatic_steps", "place_horniman_circle"};
                int order = 0;
                for (String id : presetIds) {
                    Place p = placeRepository.getPlaceById(id);
                    if (p != null) {
                        itin.getStops().add(new ItineraryStop("stop_" + order, p, order));
                        order++;
                    }
                }
                placeRepository.saveItinerary(itin);
                Toast.makeText(this, "Loaded First Time in Mumbai trail into Planner!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }

        if (btnSubmitGemFromCategory != null) {
            btnSubmitGemFromCategory.setOnClickListener(v -> startActivity(new Intent(this, CommunityActivity.class)));
        }

        toolbarCategory.setNavigationOnClickListener(v -> finish());

        rvCategoryPlaces.setLayoutManager(new LinearLayoutManager(this));
        placeAdapter = new PlaceAdapter(this, filteredPlaces, place -> {
            Intent intent = new Intent(CategoryListActivity.this, PlaceDetailActivity.class);
            intent.putExtra(PlaceDetailActivity.EXTRA_PLACE_ID, place.getId());
            startActivity(intent);
        });
        rvCategoryPlaces.setAdapter(placeAdapter);
    }

    private void loadCategoryData() {
        String type = getIntent().getStringExtra(EXTRA_CATEGORY_TYPE);
        String customTitle = getIntent().getStringExtra(EXTRA_TITLE);
        String customSubtitle = getIntent().getStringExtra(EXTRA_SUBTITLE);

        if (type == null) type = TYPE_ALL;

        if (cardFirstTimeTransitTips != null) {
            cardFirstTimeTransitTips.setVisibility(TYPE_FIRST_TIME.equals(type) ? View.VISIBLE : View.GONE);
        }
        if (cardHiddenGemsSubmitBanner != null) {
            cardHiddenGemsSubmitBanner.setVisibility(TYPE_HIDDEN_GEMS.equals(type) ? View.VISIBLE : View.GONE);
        }

        filteredPlaces.clear();
        String title;
        String subtitle;

        if (TYPE_FIRST_TIME.equals(type)) {
            title = "First Time in Mumbai? • Welcome!";
            subtitle = "Essential must-visit landmarks, transit hacks, iconic food spots, and a day plan for first-time visitors to the city.";
            // Surface a curated mix: iconic, historical, sunset, and food spots
            for (Place p : placeRepository.getAllPlaces()) {
                String cat = p.getCategory();
                if ("Historical".equalsIgnoreCase(cat)
                        || "Cultural".equalsIgnoreCase(cat)
                        || "Sunset".equalsIgnoreCase(cat)
                        || ("Food".equalsIgnoreCase(cat) && "VERIFIED".equalsIgnoreCase(p.getVerificationStatus()))) {
                    filteredPlaces.add(p);
                }
            }
        } else if (TYPE_HIDDEN_GEMS.equals(type)) {
            title = "Hidden Gems • मुंबई की गुप्त गलियां";
            subtitle = "Lesser-known neighbourhood marvels, quiet heritage enclaves, and secluded corners away from usual tourist trails.";
            filteredPlaces.addAll(placeRepository.getHiddenGems());
        } else if (TYPE_STUDY_WORK.equals(type)) {
            title = "Study & Work Spaces • अध्ययन और काम";
            subtitle = "Quiet heritage libraries, breezy reading nooks, and student-friendly cafes equipped with Wi-Fi and power outlets.";
            filteredPlaces.addAll(placeRepository.getStudyWorkSpots());
        } else if (TYPE_AFFORDABLE_FOOD.equals(type)) {
            title = "Affordable Local Food • मुंबई का स्वाद";
            subtitle = "Legendary Irani chai, century-old bakeries, crispy choora vada pav, and coastal delicacies under ₹150.";
            filteredPlaces.addAll(placeRepository.getAffordableFoodSpots());
        } else if (TYPE_HISTORICAL.equals(type)) {
            title = "Colonial & Ancient Heritage";
            subtitle = "Victorian Gothic monuments, Silhara cisterns, and 19th-century hamlets narrating centuries of Bombay history.";
            for (Place p : placeRepository.getAllPlaces()) {
                if ("Historical".equalsIgnoreCase(p.getCategory())) filteredPlaces.add(p);
            }
        } else if (TYPE_CULTURAL.equals(type)) {
            title = "Arts & Cultural Hubs";
            subtitle = "Outdoor art districts, artisanal studios, and historic meeting places for artists and thinkers.";
            for (Place p : placeRepository.getAllPlaces()) {
                if ("Cultural".equalsIgnoreCase(p.getCategory())) filteredPlaces.add(p);
            }
        } else if (TYPE_NATURE.equals(type)) {
            title = "Coastal Vistas & Nature Trails";
            subtitle = "Arabian Sea promenades, migratory flamingo mudflats, and tropical rainforest escapes.";
            for (Place p : placeRepository.getAllPlaces()) {
                if ("Nature".equalsIgnoreCase(p.getCategory()) || "Sunset".equalsIgnoreCase(p.getCategory())) {
                    filteredPlaces.add(p);
                }
            }
        } else {
            title = "All Mumbai Discoveries";
            subtitle = "Curated insider guide to the historic, quiet, and affordable side of Mumbai.";
            filteredPlaces.addAll(placeRepository.getAllPlaces());
        }

        if (customTitle != null && !customTitle.isEmpty()) title = customTitle;
        if (customSubtitle != null && !customSubtitle.isEmpty()) subtitle = customSubtitle;

        toolbarCategory.setTitle(title);
        tvCategorySubtitle.setText(subtitle);
        tvCategoryCount.setText(filteredPlaces.size() + " verified spots found");

        if (filteredPlaces.isEmpty()) {
            layoutCategoryEmpty.setVisibility(View.VISIBLE);
            rvCategoryPlaces.setVisibility(View.GONE);
        } else {
            layoutCategoryEmpty.setVisibility(View.GONE);
            rvCategoryPlaces.setVisibility(View.VISIBLE);
            placeAdapter.updatePlaces(filteredPlaces);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (placeAdapter != null) {
            placeAdapter.notifyDataSetChanged();
        }
    }
}
