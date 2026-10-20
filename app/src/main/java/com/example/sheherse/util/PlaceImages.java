package com.example.sheherse.util;

import com.example.sheherse.R;
import com.example.sheherse.model.Place;

/**
 * Utility class mapping Place IDs and categories to real photographic drawable resources.
 * This delivers authentic Mumbai destination photography for every single place card
 * and detail screen across the app without modifying Place.java models or breaking
 * SharedPreferences / Firebase serialization.
 */
public final class PlaceImages {

    private PlaceImages() {
        // No instances
    }

    /**
     * Returns an authentic photograph drawable resource ID for the given Place.
     * Maps each known place ID to its bespoke real photograph, falling back to
     * category-specific photography for user-created or unknown places.
     */
    public static int getPlaceImageRes(Place place) {
        if (place == null) return R.drawable.photo_hero_gateway;

        String id = place.getId();
        if (id != null) {
            switch (id) {
                // 1. Sassoon Docks
                case "place_sassoon_docks":
                    return R.drawable.photo_sassoon_docks;

                // 2. Banganga Tank
                case "place_banganga":
                    return R.drawable.photo_banganga;

                // 3. Khotachiwadi Heritage Village
                case "place_khotachiwadi":
                    return R.drawable.photo_khotachiwadi;

                // 4. David Sassoon Library
                case "place_sassoon_library":
                    return R.drawable.photo_sassoon_library;

                // 5. Kitab Khana Bookstore
                case "place_kitab_khana":
                    return R.drawable.photo_kitab_khana;

                // 6. Asiatic Society Library Steps
                case "place_asiatic_steps":
                    return R.drawable.photo_asiatic_steps;

                // 7. Kyani & Co. Irani Cafe
                case "place_kyani":
                    return R.drawable.photo_kyani;

                // 8. Britannia & Co.
                case "place_britannia":
                    return R.drawable.photo_britannia;

                // 9. Ashok Vada Pav
                case "place_ashok_vada_pav":
                    return R.drawable.photo_ashok_vada_pav;

                // 10. Cannon Pav Bhaji (CST)
                case "place_cannon_pav_bhaji":
                    return R.drawable.photo_cannon_pav_bhaji;

                // 11. Guru Kripa Samosa & Chole
                case "place_guru_kripa":
                    return R.drawable.photo_guru_kripa;

                // 12. Dadar Flower Market
                case "place_dadar_flowers":
                    return R.drawable.photo_dadar_flowers;

                // 13. Sewri Mangrove Park (Flamingos)
                case "place_sewri_mangroves":
                    return R.drawable.photo_sewri_mangroves;

                // 14. Gilbert Hill Basalt Monolith
                case "place_gilbert_hill":
                    return R.drawable.photo_gilbert_hill;

                // 15. Candies Bandra
                case "place_candies_bandra":
                    return R.drawable.photo_candies_bandra;

                // 16. Marine Drive Promenade
                case "place_marine_drive":
                    return R.drawable.photo_marine_drive;

                // 17. Drip Cafe & Roastery
                case "place_drip_cafe":
                    return R.drawable.photo_drip_cafe;

                // 18. Horniman Circle Garden & Arcades
                case "place_horniman_circle":
                    return R.drawable.photo_horniman_circle;

                // 19. Maharashtra Central Library
                case "place_central_library":
                    return R.drawable.photo_central_library;

                // 20. Cafe Military (Irani & Parsi)
                case "place_cafe_military":
                    return R.drawable.photo_cafe_military;

                // 21. B. Merwan & Co. Bakery
                case "place_b_merwan":
                    return R.drawable.photo_b_merwan;

                // 22. Sardar Refreshments Pav Bhaji
                case "place_sardar_pav_bhaji":
                    return R.drawable.photo_sardar_pav_bhaji;

                // 23. Pancham Puriwala
                case "place_pancham_puriwala":
                    return R.drawable.photo_pancham_puriwala;

                // 24. Kanheri Caves (SGNP)
                case "place_kanheri_caves":
                    return R.drawable.photo_kanheri_caves;

                // 25. Castella de Aguada (Bandra Fort)
                case "place_bandra_fort":
                    return R.drawable.photo_bandra_fort;

                // 26. Maharashtra Nature Park
                case "place_nature_park":
                    return R.drawable.photo_nature_park;

                // 27. The Bombay Paperie
                case "place_bombay_paperie":
                    return R.drawable.photo_bombay_paperie;

                // 28. Title Waves Bookstore
                case "place_title_waves":
                    return R.drawable.photo_title_waves;

                // 29. Dosti House / US Consulate Library
                case "place_dosti_house":
                    return R.drawable.photo_dosti_house;

                // 30. Mani Bhavan Gandhi Memorial
                case "place_jai_hind_library":
                    return R.drawable.photo_jai_hind_library;

                default:
                    break;
            }
        }
        return getCategoryImageRes(place.getCategory());
    }

    /**
     * Returns a real photograph drawable resource ID for a given category string.
     */
    public static int getCategoryImageRes(String category) {
        if (category == null) return R.drawable.photo_hero_gateway;
        switch (category.toLowerCase(java.util.Locale.ROOT)) {
            case "study":
                return R.drawable.photo_sassoon_library;
            case "food":
                return R.drawable.photo_ashok_vada_pav;
            case "nature":
                return R.drawable.photo_sewri_mangroves;
            case "sunset":
                return R.drawable.photo_marine_drive;
            case "market":
                return R.drawable.photo_dadar_flowers;
            case "cultural":
                return R.drawable.photo_sassoon_docks;
            case "historical":
                return R.drawable.photo_asiatic_steps;
            default:
                return R.drawable.photo_hero_gateway;
        }
    }

    /**
     * Returns a hero module photograph for the given module type constant.
     * Uses iconic real photography of Mumbai landmarks.
     */
    public static int getModuleHeroRes(String moduleType) {
        if (moduleType == null) return R.drawable.photo_hero_gateway;
        switch (moduleType.toUpperCase(java.util.Locale.ROOT)) {
            case "EXPLORE":
                return R.drawable.photo_hero_gateway;
            case "FIRST_TIME":
                return R.drawable.photo_hero_csmt;
            case "HIDDEN_GEMS":
                return R.drawable.photo_banganga;
            case "STUDY_WORK":
                return R.drawable.photo_sassoon_library;
            case "AFFORDABLE_FOOD":
                return R.drawable.photo_cannon_pav_bhaji;
            case "EVENTS":
                return R.drawable.photo_dadar_flowers;
            case "AI_PLANNER":
                return R.drawable.photo_hero_sealink;
            default:
                return R.drawable.photo_hero_gateway;
        }
    }
}
