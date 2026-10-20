package com.example.sheherse;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sheherse.adapter.PlaceAdapter;
import com.example.sheherse.data.AuthRepository;
import com.example.sheherse.data.PlaceRepository;
import com.example.sheherse.model.AppUser;
import com.example.sheherse.model.Itinerary;
import com.example.sheherse.model.ItineraryStop;
import com.example.sheherse.model.Place;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public interface OnNavigationRequest {
        void navigateToTab(int menuItemId);
    }

    private TextView tvGreeting;
    private TextView tvUserRoleBadge;
    private TextView tvLiveMumbaiTicker;

    private MaterialCardView cardRoleAction;
    private ImageView ivRoleIcon;
    private TextView tvRoleActionTitle;
    private TextView tvRoleActionSubtitle;
    private MaterialButton btnOpenRoleDashboard;

    // Six module cards
    private MaterialCardView cardExploreModule;
    private MaterialCardView cardFirstTime;
    private MaterialCardView cardHiddenGems;
    private MaterialCardView cardStudyWork;
    private MaterialCardView cardAffordableFood;
    private MaterialCardView cardEvents;
    private MaterialCardView cardCommunity;

    // AI Planner banner
    private MaterialCardView cardAiPlanner;

    // Trail cards
    private MaterialCardView cardTrailStudent;
    private MaterialCardView cardTrailDeco;
    private MaterialCardView cardTrailBandra;

    // Slang card
    private MaterialCardView cardBambaiyaHack;
    private TextView tvSlangWord;
    private TextView tvSlangMeaning;

    // Navigation helpers (now invisible but kept for safety)
    private TextView tvExploreAllMoods;
    private TextView tvOpenPlanner;
    private TextView tvViewAllPlaces;

    private ChipGroup chipGroupHomeFilter;
    private RecyclerView rvFeaturedPlaces;

    private PlaceRepository placeRepository;
    private AuthRepository authRepository;
    private PlaceAdapter placeAdapter;
    private final List<Place> featuredList = new ArrayList<>();

    private OnNavigationRequest navigationListener;

    public void setNavigationListener(OnNavigationRequest listener) {
        this.navigationListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        placeRepository = PlaceRepository.getInstance(requireContext());
        authRepository = AuthRepository.getInstance(requireContext());

        initViews(view);
        setupRoleBanner();
        setupModuleCards();
        setupAiPlanner();
        setupTrailCards();
        setupSlangCard();
        setupFilters();
        setupRecyclerView();
        loadFeaturedPlaces("ALL");
    }

    private void initViews(View view) {
        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvUserRoleBadge = view.findViewById(R.id.tvUserRoleBadge);
        tvLiveMumbaiTicker = view.findViewById(R.id.tvLiveMumbaiTicker);

        cardRoleAction = view.findViewById(R.id.cardRoleAction);
        ivRoleIcon = view.findViewById(R.id.ivRoleIcon);
        tvRoleActionTitle = view.findViewById(R.id.tvRoleActionTitle);
        tvRoleActionSubtitle = view.findViewById(R.id.tvRoleActionSubtitle);
        btnOpenRoleDashboard = view.findViewById(R.id.btnOpenRoleDashboard);

        cardExploreModule = view.findViewById(R.id.cardExploreModule);
        cardFirstTime = view.findViewById(R.id.cardFirstTime);
        cardHiddenGems = view.findViewById(R.id.cardHiddenGems);
        cardStudyWork = view.findViewById(R.id.cardStudyWork);
        cardAffordableFood = view.findViewById(R.id.cardAffordableFood);
        cardEvents = view.findViewById(R.id.cardEvents);
        cardCommunity = view.findViewById(R.id.cardCommunity);

        cardAiPlanner = view.findViewById(R.id.cardAiPlanner);

        cardTrailStudent = view.findViewById(R.id.cardTrailStudent);
        cardTrailDeco = view.findViewById(R.id.cardTrailDeco);
        cardTrailBandra = view.findViewById(R.id.cardTrailBandra);
        cardBambaiyaHack = view.findViewById(R.id.cardBambaiyaHack);
        tvSlangWord = view.findViewById(R.id.tvSlangWord);
        tvSlangMeaning = view.findViewById(R.id.tvSlangMeaning);

        tvExploreAllMoods = view.findViewById(R.id.tvExploreAllMoods);
        tvOpenPlanner = view.findViewById(R.id.tvOpenPlanner);
        tvViewAllPlaces = view.findViewById(R.id.tvViewAllPlaces);

        chipGroupHomeFilter = view.findViewById(R.id.chipGroupHomeFilter);
        rvFeaturedPlaces = view.findViewById(R.id.rvFeaturedPlaces);

        if (tvLiveMumbaiTicker != null) tvLiveMumbaiTicker.setSelected(true);
    }

    private void setupRoleBanner() {
        AppUser user = authRepository.getCurrentUser();
        if (user != null) {
            tvGreeting.setText("Namaste, " + user.getName() + "! Where does your curiosity lead today?");
            tvUserRoleBadge.setText(user.getRoleDisplayName());

            if (AppUser.ROLE_GUIDE.equals(user.getRole())) {
                cardRoleAction.setVisibility(View.VISIBLE);
                tvRoleActionTitle.setText("Heritage Tour Guide Control Hub");
                tvRoleActionSubtitle.setText("Manage your walking tours and respond to traveller enquiries.");
                btnOpenRoleDashboard.setText("Open Hub");
                btnOpenRoleDashboard.setOnClickListener(v -> startActivity(new Intent(requireContext(), GuideDashboardActivity.class)));
            } else if (AppUser.ROLE_OWNER.equals(user.getRole())) {
                cardRoleAction.setVisibility(View.VISIBLE);
                tvRoleActionTitle.setText("Place Owner Control Hub");
                tvRoleActionSubtitle.setText("Update your opening hours, study facilities, and booking requests.");
                btnOpenRoleDashboard.setText("Manage");
                btnOpenRoleDashboard.setOnClickListener(v -> startActivity(new Intent(requireContext(), OwnerDashboardActivity.class)));
            } else {
                cardRoleAction.setVisibility(View.GONE);
            }
        }
    }

    private void setupModuleCards() {
        if (cardExploreModule != null)
            cardExploreModule.setOnClickListener(v -> {
                if (navigationListener != null) navigationListener.navigateToTab(R.id.nav_explore);
            });

        if (cardFirstTime != null)
            cardFirstTime.setOnClickListener(v -> openCategory(CategoryListActivity.TYPE_FIRST_TIME));

        if (cardHiddenGems != null)
            cardHiddenGems.setOnClickListener(v -> openCategory(CategoryListActivity.TYPE_HIDDEN_GEMS));

        if (cardStudyWork != null)
            cardStudyWork.setOnClickListener(v -> openCategory(CategoryListActivity.TYPE_STUDY_WORK));

        if (cardAffordableFood != null)
            cardAffordableFood.setOnClickListener(v -> openCategory(CategoryListActivity.TYPE_AFFORDABLE_FOOD));

        if (cardEvents != null)
            cardEvents.setOnClickListener(v -> startActivity(new Intent(requireContext(), EventsActivity.class)));

        if (cardCommunity != null)
            cardCommunity.setOnClickListener(v -> startActivity(new Intent(requireContext(), CommunityActivity.class)));

        if (tvExploreAllMoods != null)
            tvExploreAllMoods.setOnClickListener(v -> {
                if (navigationListener != null) navigationListener.navigateToTab(R.id.nav_explore);
            });

        if (tvOpenPlanner != null)
            tvOpenPlanner.setOnClickListener(v -> {
                if (navigationListener != null) navigationListener.navigateToTab(R.id.nav_planner);
            });

        if (tvViewAllPlaces != null)
            tvViewAllPlaces.setOnClickListener(v -> {
                if (navigationListener != null) navigationListener.navigateToTab(R.id.nav_explore);
            });
    }

    private void setupAiPlanner() {
        if (cardAiPlanner != null) {
            cardAiPlanner.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), PlannerWizardActivity.class));
            });
        }
    }

    private void openCategory(String categoryType) {
        Intent intent = new Intent(requireContext(), CategoryListActivity.class);
        intent.putExtra(CategoryListActivity.EXTRA_CATEGORY_TYPE, categoryType);
        startActivity(intent);
    }

    private void setupTrailCards() {
        if (cardTrailStudent != null)
            cardTrailStudent.setOnClickListener(v -> loadTrailPreset(
                    "Student SoBo Day Out",
                    250,
                    new String[]{"place_kyani", "place_asiatic_steps", "place_kitab_khana", "place_marine_drive"}
            ));

        if (cardTrailDeco != null)
            cardTrailDeco.setOnClickListener(v -> loadTrailPreset(
                    "Fort Art Deco & UNESCO Trail",
                    150,
                    new String[]{"place_horniman_circle", "place_central_library", "place_britannia", "place_sassoon_docks"}
            ));

        if (cardTrailBandra != null)
            cardTrailBandra.setOnClickListener(v -> loadTrailPreset(
                    "Bandra Bohemian & Sunset Trail",
                    300,
                    new String[]{"place_candies_bandra", "place_drip_cafe", "place_bandra_fort"}
            ));
    }

    private void loadTrailPreset(String title, int budget, String[] placeIds) {
        Itinerary itin = new Itinerary("itin_preset_" + System.currentTimeMillis(), title, budget, null);
        int order = 0;
        for (String id : placeIds) {
            Place p = placeRepository.getPlaceById(id);
            if (p != null) {
                itin.getStops().add(new ItineraryStop("stop_" + order, p, order));
                order++;
            }
        }
        placeRepository.saveItinerary(itin);
        Toast.makeText(requireContext(), "Loaded '" + title + "' into Planner!", Toast.LENGTH_SHORT).show();
        if (navigationListener != null) navigationListener.navigateToTab(R.id.nav_planner);
    }

    private void setupSlangCard() {
        if (cardBambaiyaHack != null) {
            cardBambaiyaHack.setOnClickListener(v ->
                    new AlertDialog.Builder(requireContext())
                            .setTitle("📖 Mumbai Street Slang Guide")
                            .setMessage("• 'Kadak Single' — Strong cutting chai with extra boiled milk and cardamom.\n\n"
                                    + "• 'Brun Maska' — Crispy hard-crust bread slit open and stuffed with cold salted butter.\n\n"
                                    + "• 'Chinchpokli / VT side' — Way to indicate South Bombay direction on local trains.\n\n"
                                    + "• 'Bindaas' — Worry-free Mumbai attitude.\n\n"
                                    + "• 'Khau Galli' — Food street loaded with street eats like Dosa, Frankie & Chaat.")
                            .setPositiveButton("Bindaas!", null)
                            .show()
            );
        }
    }

    private void setupFilters() {
        if (chipGroupHomeFilter == null) return;
        chipGroupHomeFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipHomeBudget)) {
                loadFeaturedPlaces("BUDGET");
            } else if (checkedIds.contains(R.id.chipHomeStudy)) {
                loadFeaturedPlaces("STUDY");
            } else if (checkedIds.contains(R.id.chipHomeSunset)) {
                loadFeaturedPlaces("SUNSET");
            } else {
                loadFeaturedPlaces("ALL");
            }
        });
    }

    private void setupRecyclerView() {
        if (rvFeaturedPlaces == null) return;
        rvFeaturedPlaces.setLayoutManager(new LinearLayoutManager(requireContext()));
        placeAdapter = new PlaceAdapter(requireContext(), featuredList, place -> {
            Intent intent = new Intent(requireContext(), PlaceDetailActivity.class);
            intent.putExtra(PlaceDetailActivity.EXTRA_PLACE_ID, place.getId());
            startActivity(intent);
        });
        rvFeaturedPlaces.setAdapter(placeAdapter);
    }

    private void loadFeaturedPlaces(String filter) {
        featuredList.clear();
        List<Place> all = placeRepository.getAllPlaces();

        switch (filter) {
            case "BUDGET":
                for (Place p : all) {
                    if (p.getEstimatedCost() >= 0 && p.getEstimatedCost() <= 100) {
                        featuredList.add(p);
                    }
                }
                break;
            case "STUDY":
                for (Place p : all) {
                    if (p.isStudyWork() || "Study".equalsIgnoreCase(p.getCategory())) {
                        featuredList.add(p);
                    }
                }
                break;
            case "SUNSET":
                for (Place p : all) {
                    if ("Sunset".equalsIgnoreCase(p.getCategory())
                            || (p.getTags() != null && (p.getTags().contains("Sunset") || p.getTags().contains("Sea Promenade")))) {
                        featuredList.add(p);
                    }
                }
                break;
            default:
                featuredList.addAll(all);
                break;
        }

        if (placeAdapter != null) placeAdapter.updatePlaces(featuredList);
    }
}
