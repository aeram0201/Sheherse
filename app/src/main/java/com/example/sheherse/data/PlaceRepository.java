package com.example.sheherse.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.sheherse.model.CommunitySubmission;
import com.example.sheherse.model.Enquiry;
import com.example.sheherse.model.Event;
import com.example.sheherse.model.Itinerary;
import com.example.sheherse.model.MenuItem;
import com.example.sheherse.model.Place;
import com.example.sheherse.model.TourListing;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlaceRepository {
    private static final String PREF_NAME = "sheherse_places_prefs";
    private static final String KEY_SAVED_IDS = "saved_place_ids";
    private static final String KEY_CUSTOM_PLACES = "custom_places_json";
    private static final String KEY_ITINERARIES = "saved_itineraries_json";
    private static final String KEY_COMMUNITY = "community_submissions_json";
    private static final String KEY_TOURS = "tour_listings_json";
    private static final String KEY_ENQUIRIES = "enquiries_json";

    private static PlaceRepository instance;
    private final SharedPreferences prefs;
    private final Gson gson;

    private final Map<String, Place> placesMap = new LinkedHashMap<>();
    private final List<Event> eventsList = new ArrayList<>();
    private final List<TourListing> toursList = new ArrayList<>();
    private final List<CommunitySubmission> communityList = new ArrayList<>();
    private final List<Enquiry> enquiriesList = new ArrayList<>();
    private final Set<String> savedPlaceIds = new HashSet<>();
    private final List<Itinerary> savedItineraries = new ArrayList<>();

    private PlaceRepository(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        initSeedData();
        loadPersistedData();
    }

    public static synchronized PlaceRepository getInstance(Context context) {
        if (instance == null) {
            instance = new PlaceRepository(context);
        }
        return instance;
    }

    private void initSeedData() {
        // 1. Sassoon Docks
        addSeedPlace(new Place(
                "place_sassoon_docks",
                "Sassoon Docks & Art Project",
                "One of the oldest wet docks in Mumbai, bustling with fishing trawlers at dawn and transformed by massive street art murals created by the St+art India Foundation.",
                "Cultural",
                "Colaba, South Mumbai",
                "Sassoon Dock Road, Colaba, Mumbai 400005",
                0, false, 90,
                18.9137, 72.8225,
                Arrays.asList("Street Art", "Fishermen", "Photography", "Historic"),
                Arrays.asList("Seating", "Restroom"),
                "VERIFIED",
                true, false, false,
                "", "Lively", 4.6f,
                "5:00 AM – 7:00 PM",
                "Arrive around 6:30 AM to see the lively Koli fish auctions followed by the vibrant outdoor murals in morning light."
        ));

        // 2. Banganga Tank
        addSeedPlace(new Place(
                "place_banganga",
                "Banganga Tank & Walkeshwar",
                "An ancient freshwater cistern dating back to the 12th-century Silhara dynasty, encircled by old temples, stone steps, and quiet alleyways echoing ancient mythology.",
                "Historical",
                "Malabar Hill, South Mumbai",
                "Teen Batti, Walkeshwar, Malabar Hill, Mumbai 400006",
                0, false, 60,
                18.9452, 72.7937,
                Arrays.asList("Heritage", "Cistern", "Quiet", "Temples"),
                Arrays.asList("Seating", "Quiet"),
                "VERIFIED",
                true, true, false,
                "", "Silent", 4.7f,
                "6:00 AM – 8:00 PM",
                "Feed the resident geese and sit on the east steps for an oasis of absolute calm amidst the city hustle."
        ));

        // 3. Khotachiwadi
        addSeedPlace(new Place(
                "place_khotachiwadi",
                "Khotachiwadi Heritage Village",
                "A protected heritage hamlet featuring 19th-century Portuguese-style wooden cottages with verandahs, exterior staircases, and vibrant painted facades tucked inside Girgaon.",
                "Historical",
                "Girgaon, South Mumbai",
                "Khotachiwadi, Girgaon, Mumbai 400004",
                0, false, 60,
                18.9547, 72.8183,
                Arrays.asList("Architecture", "Portuguese", "Heritage", "Walk"),
                Arrays.asList("Seating", "Quiet"),
                "VERIFIED",
                true, false, false,
                "", "Silent", 4.5f,
                "Open 24 hours (respect resident privacy)",
                "Visit 47-A gallery for contemporary design exhibitions set inside a restored vintage home."
        ));

        // 4. David Sassoon Library
        addSeedPlace(new Place(
                "place_sassoon_library",
                "David Sassoon Library & Reading Room",
                "A striking Venetian Gothic heritage library built in 1870, surrounded by a lush green garden courtyard in the heart of Kala Ghoda, ideal for quiet reading and study.",
                "Study",
                "Kala Ghoda, Fort",
                "152, Mahatma Gandhi Road, Kala Ghoda, Fort, Mumbai 400001",
                50, true, 120,
                18.9287, 72.8322,
                Arrays.asList("Study", "Library", "Heritage", "Garden"),
                Arrays.asList("WiFi", "Charging", "Seating", "Quiet", "AC"),
                "VERIFIED",
                false, true, false,
                "", "Silent", 4.8f,
                "8:00 AM – 9:00 PM",
                "Day passes are available for students and researchers. The garden courtyard is breezy and perfect for study sessions."
        ));

        // 5. Kitab Khana
        addSeedPlace(new Place(
                "place_kitab_khana",
                "Kitab Khana & Somethings Gourmet Cafe",
                "A magnificent 150-year-old colonial building housing Mumbai's most beloved independent bookstore with high ceilings, teakwood shelves, and a peaceful boutique in-house cafe.",
                "Study",
                "Fort, South Mumbai",
                "Somaiya Bhavan, 45/47, MG Road, Flora Fountain, Fort, Mumbai 400001",
                200, true, 90,
                18.9328, 72.8329,
                Arrays.asList("Bookstore", "Cafe", "Study", "Colonial"),
                Arrays.asList("WiFi", "Charging", "Seating", "AC", "Restroom"),
                "VERIFIED",
                false, true, true,
                "Cafe", "Moderate", 4.9f,
                "10:30 AM – 7:30 PM",
                "Try their fresh cinnamon rolls and herbal tea while studying at the corner mezzanine desks."
        ));

        // 6. Asiatic Society Library
        addSeedPlace(new Place(
                "place_asiatic_steps",
                "Asiatic Society Library Steps & Town Hall",
                "Neoclassical architectural marvel with sweeping stone steps overlooking Horniman Circle. A favorite meeting spot for students, readers, photographers, and thinkers.",
                "Historical",
                "Fort, South Mumbai",
                "Town Hall, Shahid Bhagat Singh Rd, Fort, Mumbai 400023",
                0, false, 45,
                18.9317, 72.8358,
                Arrays.asList("Neoclassical", "Town Hall", "Steps", "Sunset"),
                Arrays.asList("Seating", "Restroom"),
                "VERIFIED",
                false, true, false,
                "", "Moderate", 4.7f,
                "10:00 AM – 5:00 PM (Steps open 24/7)",
                "Sit on the highest steps just before sunset to watch the vintage red BEST buses circle Horniman Circle."
        ));

        // 7. Kyani & Co.
        Place kyani = new Place(
                "place_kyani",
                "Kyani & Co. Bakery & Irani Chai",
                "Operating continuously since 1904, this legendary Irani cafe serves crisp Bun Maska, hot spicy Kheema Pav, caramel custard, and cutting chai under vintage glass mirrors.",
                "Food",
                "Marine Lines, South Mumbai",
                "Jermahal Estate, 657, JSS Rd, Marine Lines, Mumbai 400002",
                80, true, 45,
                18.9431, 72.8272,
                Arrays.asList("Irani Cafe", "Bun Maska", "Budget Food", "Heritage"),
                Arrays.asList("Seating", "Restroom"),
                "VERIFIED",
                false, false, true,
                "Parsi/Irani", "Lively", 4.7f,
                "7:00 AM – 8:30 PM",
                "Dip fresh brun pav into hot Irani chai and don't miss the Parsi mawa cake from the bakery counter."
        );
        kyani.setOwnerId("owner_kyani");
        addSeedPlace(kyani);

        // 8. Britannia & Co.
        addSeedPlace(new Place(
                "place_britannia",
                "Britannia & Co. Restaurant",
                "Historic Parsi-Irani restaurant in the charming colonial Ballard Estate, celebrated worldwide for its iconic Berry Pulao, Sali Boti, and chilled Raspberry Soda.",
                "Food",
                "Ballard Estate, Fort",
                "Wakefield House, 11, Sprott Rd, Ballard Estate, Fort, Mumbai 400001",
                280, true, 60,
                18.9365, 72.8402,
                Arrays.asList("Berry Pulao", "Parsi", "Heritage", "Ballard Estate"),
                Arrays.asList("Seating", "Restroom"),
                "VERIFIED",
                false, false, true,
                "Parsi/Irani", "Moderate", 4.6f,
                "11:30 AM – 4:00 PM (Closed Sundays)",
                "Order the Mutton Berry Pulao with sweet Iranian berries and wash it down with Duke's raspberry soda."
        ));

        // 9. Ashok Vada Pav
        addSeedPlace(new Place(
                "place_ashok_vada_pav",
                "Ashok Vada Pav (Kirti College)",
                "Mumbai's benchmark vada pav stall running for over 40 years. Renowned for its golden crispy 'choora' (batter flakes) blended into secret spicy chutneys.",
                "Food",
                "Prabhadevi, Central Mumbai",
                "Kashinath Dhuru Marg, Off Veer Savarkar Marg, Prabhadevi, Mumbai 400028",
                35, false, 20,
                19.0163, 72.8306,
                Arrays.asList("Vada Pav", "Street Food", "Iconic", "Budget"),
                Arrays.asList(),
                "VERIFIED",
                false, false, true,
                "Vada Pav & Chaat", "Lively", 4.9f,
                "11:00 AM – 9:00 PM",
                "Ask for extra crispy 'choora' with garlic chutney for the signature crunch."
        ));

        // 10. Cannon Pav Bhaji
        addSeedPlace(new Place(
                "place_cannon_pav_bhaji",
                "Cannon Pav Bhaji (CST)",
                "Situated right across the Gothic facade of CSMT terminus, serving piping-hot Amul butter pav bhaji with caramelized onions and tangy lemons to commuters since 1975.",
                "Food",
                "CST Station, Fort",
                "Dadabhai Naoroji Rd, Opposite CST Station, Mumbai 400001",
                120, false, 30,
                18.9406, 72.8354,
                Arrays.asList("Pav Bhaji", "Butter", "Street Food", "CST"),
                Arrays.asList("Seating"),
                "VERIFIED",
                false, false, true,
                "Vada Pav & Chaat", "Lively", 4.5f,
                "7:00 AM – 11:00 PM",
                "Pair with a fresh glass of chilled mosambi (sweet lime) juice from the next-door stall."
        ));

        // 11. Guru Kripa
        addSeedPlace(new Place(
                "place_guru_kripa",
                "Guru Kripa Samosa & Chole",
                "Mumbai's supreme samosa haven in the historic Sindhi colony of Sion. Famous for huge, piping hot samosas drowned in tangy slow-cooked chole and sweet tamarind sauce.",
                "Food",
                "Sion, Central Mumbai",
                "40, Guru Kripa Building, Road No 24, Sion West, Mumbai 400022",
                60, false, 30,
                19.0397, 72.8624,
                Arrays.asList("Samosa", "Chole", "Sindhi", "Budget Food"),
                Arrays.asList("Seating", "Restroom"),
                "VERIFIED",
                false, false, true,
                "Vada Pav & Chaat", "Lively", 4.7f,
                "8:00 AM – 10:30 PM",
                "They supply samosas to single-screen cinema halls across Mumbai! Get the Samosa Chole with crushed onions."
        ));

        // 12. Dadar Flower Market
        addSeedPlace(new Place(
                "place_dadar_flowers",
                "Dadar Flower Market (Phool Galli)",
                "A mesmerizing kaleidoscope of marigolds, jasmine, roses, and orchids sold by thousands of vendors right beneath the Dadar station flyover at break of dawn.",
                "Market",
                "Dadar West",
                "Senapati Bapat Marg, Dadar West, Mumbai 400028",
                0, false, 60,
                19.0189, 72.8427,
                Arrays.asList("Flowers", "Dawn", "Sensory", "Photography"),
                Arrays.asList(),
                "VERIFIED",
                true, false, false,
                "", "Lively", 4.6f,
                "4:00 AM – 9:00 AM (Peak dawn)",
                "Bring your camera between 5:30 AM and 6:30 AM when the morning dew glimmers on mountains of orange marigolds."
        ));

        // 13. Sewri Mangrove Park
        addSeedPlace(new Place(
                "place_sewri_mangroves",
                "Sewri Mangrove Park & Mudflats",
                "Expansive mudflats and mangrove forest on the eastern seafront where tens of thousands of migratory Lesser and Greater Flamingos gather against industrial dock cranes.",
                "Nature",
                "Sewri, East Mumbai",
                "Sewri Fort Road, Sewri, Mumbai 400015",
                0, false, 90,
                18.9984, 72.8601,
                Arrays.asList("Flamingos", "Nature", "Mudflats", "Birdwatching"),
                Arrays.asList("Quiet"),
                "SAMPLE",
                true, false, false,
                "", "Silent", 4.4f,
                "Sunrise to Sunset (Nov – May best for Flamingos)",
                "Check the Mumbai tide tables: low tide exposes mudflats where flamingos feed closest to the shore."
        ));

        // 14. Gilbert Hill
        addSeedPlace(new Place(
                "place_gilbert_hill",
                "Gilbert Hill Volcanic Monolith",
                "A sheer 200-foot column of black basalt rock created 66 million years ago from Mesozoic volcanic activity, predating human civilization with twin temples atop.",
                "Nature",
                "Andheri West, Suburbs",
                "Sagar City, Andheri West, Mumbai 400058",
                0, false, 60,
                19.1207, 72.8398,
                Arrays.asList("Geology", "Ancient", "Panoramic View", "Volcano"),
                Arrays.asList("Seating", "Quiet"),
                "VERIFIED",
                true, false, false,
                "", "Silent", 4.3f,
                "6:00 AM – 7:00 PM",
                "Climb the steep stone staircase for an incredible 360-degree sunset panorama over Mumbai's western suburban skyline."
        ));

        // 15. Candies Bandra
        addSeedPlace(new Place(
                "place_candies_bandra",
                "Candies (Pali Hill)",
                "A multi-tiered Portuguese villa turned into a sprawling artsy student sanctuary with mosaic courtyards, cozy reading nooks, and affordable bakery bites.",
                "Study",
                "Bandra West, Suburbs",
                "5AA Mac Ronells, Next to Learner's Academy, Pali Hill, Bandra West, Mumbai 400050",
                250, true, 120,
                19.0631, 72.8291,
                Arrays.asList("Cafe", "Study Spot", "Bandra", "WiFi"),
                Arrays.asList("WiFi", "Charging", "Seating", "Restroom", "AC"),
                "VERIFIED",
                false, true, true,
                "Cafe", "Moderate", 4.6f,
                "8:30 AM – 10:00 PM",
                "Head to the rooftop terrace early on weekdays for fast Wi-Fi and discounted post-8PM pastry specials."
        ));

        // 16. Marine Drive Promenade
        addSeedPlace(new Place(
                "place_marine_drive",
                "Marine Drive ('Queen\\'s Necklace')",
                "A 3.6-kilometer-long arc along the Arabian Sea lined with Art Deco buildings and tetrapods, Mumbai's iconic spot to catch the sea breeze and listen to the waves.",
                "Sunset",
                "South Mumbai",
                "Netaji Subhash Chandra Bose Road, Mumbai 400020",
                0, false, 90,
                18.9438, 72.8231,
                Arrays.asList("Sea Promenade", "Art Deco", "Sunset", "Iconic"),
                Arrays.asList("Seating"),
                "VERIFIED",
                false, false, false,
                "", "Lively", 4.9f,
                "Open 24 hours",
                "Sit on the promenade wall near Churchgate at dusk when the streetlights flick on in golden curves."
        ));

        // 17. Drip Cafe & Roastery (Bandra)
        addSeedPlace(new Place(
                "place_drip_cafe",
                "Drip Specialty Cafe & Roastery",
                "Modern independent coffee house in Bandra catering to freelancers and remote coders with fast 100 Mbps Wi-Fi, abundant charging sockets, and artisanal pour-overs.",
                "Study",
                "Bandra West, Suburbs",
                "Chapel Road, Ranwar, Bandra West, Mumbai 400050",
                220, true, 150,
                19.0558, 72.8285,
                Arrays.asList("Coffee", "Study Spot", "WiFi", "Work"),
                Arrays.asList("WiFi", "Charging", "Seating", "AC", "Quiet"),
                "VERIFIED",
                false, true, true,
                "Cafe", "Silent", 4.7f,
                "8:00 AM – 10:30 PM",
                "The quiet loft upstairs has dedicated single-study desks equipped with surge-protected universal power plugs."
        ));

        // 18. Horniman Circle Garden & Arcades
        addSeedPlace(new Place(
                "place_horniman_circle",
                "Horniman Circle Garden & Colonial Arcades",
                "A circular public park dating back to 1869 framed by magnificent colonnaded neo-classical crescent buildings, quiet stone paths, and shady banyan trees.",
                "Historical",
                "Fort, South Mumbai",
                "Horniman Circle, Kala Ghoda, Fort, Mumbai 400001",
                0, false, 45,
                18.9324, 72.8365,
                Arrays.asList("Heritage", "Garden", "Quiet Walk", "Colonial"),
                Arrays.asList("Seating", "Quiet"),
                "VERIFIED",
                true, true, false,
                "", "Silent", 4.6f,
                "9:00 AM – 8:00 PM",
                "Sit on the heritage iron benches during lunch hours to read away from traffic sounds while watching flocks of pigeons."
        ));

        // 19. Maharashtra Central Library Reading Room
        addSeedPlace(new Place(
                "place_central_library",
                "Maharashtra Central Library (Town Hall)",
                "One of India's premier archival libraries with soaring vaulted ceilings, rows of oak reading tables, and access to over 800,000 preserved manuscripts.",
                "Study",
                "Fort, South Mumbai",
                "Town Hall, Shahid Bhagat Singh Rd, Fort, Mumbai 400023",
                0, false, 180,
                18.9319, 72.8360,
                Arrays.asList("Library", "Study Room", "Silent", "Academic"),
                Arrays.asList("Charging", "Seating", "Quiet", "Restroom"),
                "VERIFIED",
                false, true, false,
                "", "Silent", 4.8f,
                "10:00 AM – 6:00 PM (Closed 2nd & 4th Saturdays)",
                "Carry a valid college or government ID to obtain free daily visitor study passes for the ground floor silent reading hall."
        ));

        // 20. Cafe Military
        addSeedPlace(new Place(
                "place_cafe_military",
                "Cafe Military (Irani & Parsi)",
                "Unpretentious Irani establishment established in 1933 in the banking heart of Fort. Renowned for slow-cooked Akuri, Mutton Sali Boti, caramel custard, and cutting chai.",
                "Food",
                "Fort, South Mumbai",
                "Ali Chamber, Tamind Lane, Fort, Mumbai 400001",
                140, true, 45,
                18.9311, 72.8336,
                Arrays.asList("Irani", "Parsi", "Budget Food", "Keema"),
                Arrays.asList("Seating"),
                "VERIFIED",
                true, false, true,
                "Parsi/Irani", "Lively", 4.6f,
                "8:30 AM – 9:30 PM (Closed Sundays)",
                "Their Friday Mutton Dhansak served with brown caramelized rice is considered one of the best budget meals in town."
        ));

        // 21. B. Merwan & Co.
        addSeedPlace(new Place(
                "place_b_merwan",
                "B. Merwan & Co. (Grant Road)",
                "Founded in 1914 opposite Grant Road station, this legendary bakery is world-renowned for piping-hot morning mawa cakes that sell out within 30 minutes of opening.",
                "Food",
                "Grant Road, South Mumbai",
                "Ali Mahomed Bldg, Opposite Grant Road Station East, Mumbai 400007",
                40, false, 25,
                18.9632, 72.8164,
                Arrays.asList("Mawa Cake", "Irani Chai", "Historic Bakery", "Budget"),
                Arrays.asList(),
                "VERIFIED",
                false, false, true,
                "Parsi/Irani", "Lively", 4.9f,
                "7:00 AM – 6:00 PM",
                "Reach by 7:15 AM sharp to secure fresh oven-warm Mawa Cakes before the office commuters clean out the shelves."
        ));

        // 22. Sardar Refreshments
        addSeedPlace(new Place(
                "place_sardar_pav_bhaji",
                "Sardar Refreshments (Tardeo)",
                "The undisputed king of decadent buttery Pav Bhaji in Mumbai since 1966. Prepared on a giant cast-iron tawa with melted Amul butter pools and toasted soft pav.",
                "Food",
                "Tardeo, Central Mumbai",
                "166-A, Tardeo Road, Junction, Tardeo, Mumbai 400034",
                160, false, 35,
                18.9696, 72.8169,
                Arrays.asList("Pav Bhaji", "Butter", "Late Night", "Iconic"),
                Arrays.asList("Seating"),
                "VERIFIED",
                false, false, true,
                "Vada Pav & Chaat", "Lively", 4.5f,
                "12:00 PM – 2:00 AM",
                "Ask for the 'Cheese Pav Bhaji' with extra roasted lemon pav for a legendary late-night treat."
        ));

        // 23. Pancham Puriwala
        addSeedPlace(new Place(
                "place_pancham_puriwala",
                "Pancham Puriwala (VT)",
                "Serving golden, crispy puris with spicy pumpkin bhaji and slow-simmered chole since the 1840s, predating the construction of the Victoria Terminus across the road.",
                "Food",
                "Fort / VT, South Mumbai",
                "8/10, Perin Nariman St, Ballard Estate, Fort, Mumbai 400001",
                75, false, 30,
                18.9398, 72.8362,
                Arrays.asList("Puri Bhaji", "Historic", "Budget Meal", "Oldest"),
                Arrays.asList("Seating"),
                "VERIFIED",
                false, false, true,
                "North Indian / Chaat", "Lively", 4.7f,
                "8:30 AM – 11:00 PM",
                "Try the Pancham Thali with masala puris (beetroot, spinach, and methi) and chilled shrikhand."
        ));

        // 24. Kanheri Caves
        addSeedPlace(new Place(
                "place_kanheri_caves",
                "Kanheri Caves (Sanjay Gandhi National Park)",
                "A mesmerizing complex of over 100 basalt rock-cut Buddhist prayer halls and monk cells dating from the 1st century BCE, nestled deep inside Mumbai's tropical rainforest.",
                "Historical",
                "Borivali East, Suburbs",
                "Sanjay Gandhi National Park, Borivali East, Mumbai 400066",
                85, false, 180,
                19.2064, 72.9065,
                Arrays.asList("Ancient", "Caves", "Buddhist", "Rainforest", "Trek"),
                Arrays.asList("Seating", "Quiet", "Restroom"),
                "VERIFIED",
                true, false, false,
                "", "Silent", 4.8f,
                "7:30 AM – 5:00 PM (Closed Mondays)",
                "Rent a bicycle at the national park entrance gate to cycle the 6km canopy-covered forest trail up to the cave entrance."
        ));

        // 25. Castella de Aguada (Bandra Fort)
        addSeedPlace(new Place(
                "place_bandra_fort",
                "Castella de Aguada (Bandra Fort)",
                "17th-century Portuguese watchtower fort sitting at the tip of Land's End, offering panoramic amphitheatre views of the Arabian Sea and the Bandra-Worli Sea Link.",
                "Sunset",
                "Bandra West, Suburbs",
                "Byramji Jeejeebhoy Road, Bandstand, Bandra West, Mumbai 400050",
                0, false, 75,
                19.0435, 72.8189,
                Arrays.asList("Portuguese Fort", "Sea Link", "Sunset", "Free"),
                Arrays.asList("Seating", "Quiet"),
                "VERIFIED",
                true, false, false,
                "", "Moderate", 4.7f,
                "6:00 AM – 7:30 PM",
                "The stone steps facing the sea link offer Mumbai's best free vantage point when the bridge lights illuminate at dusk."
        ));

        // 26. Maharashtra Nature Park (Mahim)
        addSeedPlace(new Place(
                "place_nature_park",
                "Maharashtra Nature Park (Mahim)",
                "An incredible 37-acre ecological wonder converted from a municipal garbage dumping ground into a lush woodland with 14,000 trees, butterflies, and quiet walking paths.",
                "Nature",
                "Dharavi / Mahim, Central Mumbai",
                "Near Dharavi Bus Depot, Sion Bandra Link Road, Mumbai 400017",
                20, false, 90,
                19.0538, 72.8596,
                Arrays.asList("Ecology", "Forest", "Birdwatching", "Quiet"),
                Arrays.asList("Seating", "Quiet", "Restroom"),
                "VERIFIED",
                true, true, false,
                "", "Silent", 4.6f,
                "8:30 AM – 5:00 PM",
                "The canopy reading benches near the butterfly garden are secluded and perfect for focused reading away from traffic noise."
        ));

        // 27. The Bombay Paperie
        addSeedPlace(new Place(
                "place_bombay_paperie",
                "The Bombay Paperie (Heritage Paper Studio)",
                "A rare artisanal haven dedicated to the ancient craft of 100% tree-free handmade cotton paper, operating inside a quaint heritage alley in the Fort financial district.",
                "Cultural",
                "Fort, South Mumbai",
                "59, Bombay Mutual Terrace, Fort, Mumbai 400001",
                0, false, 45,
                18.9333, 72.8341,
                Arrays.asList("Handmade Paper", "Artisan", "Heritage", "Art"),
                Arrays.asList("AC", "Quiet"),
                "VERIFIED",
                true, false, false,
                "", "Silent", 4.5f,
                "10:30 AM – 7:00 PM",
                "Watch live demonstrations of cotton rag pulping and pick up handmade leather-bound explorer travel journals."
        ));

        // 28. Title Waves Bookstore
        addSeedPlace(new Place(
                "place_title_waves",
                "Title Waves Bookstore & Reading Lounge",
                "Sprawling 8,000 sq ft boutique independent bookstore in Bandra with dedicated quiet reading pods, charging plugs, and an in-house espresso coffee bar.",
                "Study",
                "Bandra West, Suburbs",
                "St. Paul's Media Complex, 24th Road, Bandra West, Mumbai 400050",
                150, true, 120,
                19.0601, 72.8329,
                Arrays.asList("Bookstore", "Study", "WiFi", "Quiet"),
                Arrays.asList("WiFi", "Charging", "Seating", "Quiet", "AC", "Restroom"),
                "VERIFIED",
                false, true, false,
                "", "Silent", 4.8f,
                "10:00 AM – 9:00 PM",
                "The back acoustic reading booths have personal reading lamps and high-speed Wi-Fi, perfect for study and coding sessions."
        ));

        // 29. Dosti House / US Consulate Library
        addSeedPlace(new Place(
                "place_dosti_house",
                "Dosti House (BKC)",
                "A state-of-the-art public cultural library and knowledge center in Bandra-Kurla Complex with ergonomic study desks, high-speed Wi-Fi, and extensive research databases.",
                "Study",
                "BKC, Suburbs",
                "C-49, G Block, Bandra Kurla Complex, Bandra East, Mumbai 400051",
                0, false, 180,
                19.0665, 72.8687,
                Arrays.asList("Modern Library", "Coworking", "WiFi", "AC"),
                Arrays.asList("WiFi", "Charging", "Seating", "Quiet", "AC", "Restroom"),
                "VERIFIED",
                false, true, false,
                "", "Silent", 4.9f,
                "9:00 AM – 5:00 PM (Mon-Sat)",
                "Free access upon presenting government photo ID. Unmatched internet speed and zero background noise."
        ));

        // 30. Jai Hind Gandhi Memorial Library
        addSeedPlace(new Place(
                "place_jai_hind_library",
                "Mani Bhavan & Gandhi Memorial Study Room",
                "Historic headquarters of Mahatma Gandhi in Mumbai during 1917–1934, housing a peaceful 20,000-volume research library and quiet terrace.",
                "Study",
                "Gamdevi, South Mumbai",
                "19, Laburnum Road, Gamdevi, Mumbai 400007",
                0, false, 90,
                18.9602, 72.8123,
                Arrays.asList("Gandhi Memorial", "Quiet Study", "Heritage", "Serene"),
                Arrays.asList("Seating", "Quiet", "Restroom"),
                "VERIFIED",
                true, true, false,
                "", "Silent", 4.8f,
                "9:30 AM – 6:00 PM",
                "Tucked inside the shaded leafy avenue of Laburnum Road; sit on the first floor study library for deep focus."
        ));

        // --- SEED EVENTS (10 Records) ---
        eventsList.add(new Event(
                "event_kala_ghoda",
                "Kala Ghoda Heritage & Architecture Walk",
                "Join heritage enthusiasts as we decode the Victorian Gothic, Indo-Saracenic, and Art Deco landmarks of Mumbai's premier art district.",
                "Heritage",
                "Kala Ghoda, Fort",
                "Outside David Sassoon Library",
                "Sunday, 8:00 AM",
                "8:00 AM – 10:00 AM",
                0,
                "Farhan Merchant",
                true
        ));

        eventsList.add(new Event(
                "event_flower_market",
                "Dadar Dawn Flower Market Photography Trail",
                "Capture the intense sensory rush of fresh blossoms, vibrant colors, and morning portraits under the Dadar flyover.",
                "Walk",
                "Dadar West",
                "Dadar Station Platform 1 Exit",
                "Saturday, 6:00 AM",
                "6:00 AM – 7:30 AM",
                0,
                "Mumbai Photo Guild",
                true
        ));

        eventsList.add(new Event(
                "event_marine_drive_jam",
                "Sunset Acoustic & Drum Jam",
                "Open-air acoustic music session by the sea. Bring your guitar, djembe, or simply sit on the tetrapods and enjoy the sunset vibes.",
                "Music",
                "Marine Drive",
                "Promenade near Air India Building",
                "Saturday, 5:30 PM",
                "5:30 PM – 7:30 PM",
                0,
                "Acoustic Mumbai",
                true
        ));

        eventsList.add(new Event(
                "event_bandra_art",
                "Bandra Street Art & Ranwar Village Heritage Stroll",
                "Explore the hidden graffiti alleys, Bollywood portraits, and quaint Indo-Portuguese cottages of Bandra West.",
                "Art",
                "Bandra West",
                "Ranwar Village Square, Bandra",
                "Sunday, 4:00 PM",
                "4:00 PM – 6:00 PM",
                200,
                "Art Deco Mumbai",
                true
        ));

        eventsList.add(new Event(
                "event_khotachiwadi_walk",
                "Khotachiwadi Living Heritage & Architecture Walk",
                "Guided historical walk through 19th-century wooden verandahs, vintage chapels, and local resident storytelling in Girgaon.",
                "Heritage",
                "Girgaon, South Mumbai",
                "Outside 47-A Gallery, Khotachiwadi",
                "Saturday, 9:00 AM",
                "9:00 AM – 10:30 AM",
                150,
                "Kavita Deshmukh",
                true
        ));

        eventsList.add(new Event(
                "event_fort_art_deco",
                "Oval Maidan & UNESCO Art Deco Evening Trail",
                "A twilight architectural exploration of the world's second-largest collection of Art Deco buildings along Oval Maidan.",
                "Heritage",
                "Churchgate / Fort",
                "Eros Cinema Entrance, Churchgate",
                "Friday, 6:00 PM",
                "6:00 PM – 8:00 PM",
                0,
                "Natasha Batra",
                true
        ));

        eventsList.add(new Event(
                "event_sassoon_fishermen",
                "Sassoon Docks Koli Culture & Dawn Photo Walk",
                "Witness the early morning fish auctions, colorful Koli wooden trawlers, and mural graffiti with local photographers.",
                "Photography",
                "Colaba, South Mumbai",
                "Sassoon Dock Gate No. 1",
                "Sunday, 6:30 AM",
                "6:30 AM – 8:30 AM",
                0,
                "Farhan Merchant",
                true
        ));

        eventsList.add(new Event(
                "event_banganga_sunset",
                "Banganga Ancient Cistern Acoustic Sunset Ragas",
                "Sit on the 12th-century stone steps as evening lamps light up across the holy water tank accompanied by serene flute and sitar.",
                "Music",
                "Malabar Hill",
                "Banganga East Steps, Walkeshwar",
                "Sunday, 5:30 PM",
                "5:30 PM – 7:00 PM",
                0,
                "Walkeshwar Cultural Society",
                true
        ));

        eventsList.add(new Event(
                "event_csmt_illumination",
                "CSMT Terminus & Ballard Estate Night Walk",
                "Explore the dramatic floodlit Victorian Gothic gargoyles of Victoria Terminus and quiet cobbled streets of Ballard Estate.",
                "Heritage",
                "Fort / CST",
                "CSMT Viewing Gallery, Opposite Cannon",
                "Saturday, 8:00 PM",
                "8:00 PM – 9:30 PM",
                100,
                "Bombay Heritage Guild",
                true
        ));

        eventsList.add(new Event(
                "event_kanheri_nature",
                "Kanheri Rainforest Birding & Forest Trail",
                "Morning trek through the Sanjay Gandhi National Park forest spotting parakeets, hornbills, and deer up to ancient Buddhist caves.",
                "Nature",
                "Borivali East",
                "SGNP Main Gate, Borivali",
                "Sunday, 7:00 AM",
                "7:00 AM – 10:30 AM",
                120,
                "Wild Mumbai Trust",
                true
        ));

        // --- SEED TOURS (Guide Farhan) ---
        toursList.add(new TourListing(
                "tour_farhan_heritage",
                "guide_farhan",
                "Farhan Merchant",
                "Forgotten South Mumbai: Opium Trade to Art Deco",
                "An intimate 3-hour walking masterclass unraveling how 19th-century trade barons, cotton mills, and architects shaped South Bombay. Includes heritage tea break.",
                450, 3,
                "Asiatic Library Steps, Fort",
                Arrays.asList("English", "Hindi", "Marathi"),
                "South Mumbai & Fort",
                4.9f
        ));

        toursList.add(new TourListing(
                "tour_farhan_food",
                "guide_farhan",
                "Farhan Merchant",
                "Secret Street Food & Irani Trail",
                "Taste 7 handpicked culinary legends across CST, Fort, and Marine Lines, from spicy choora vada pav to mawa cakes and cutting chai.",
                350, 2,
                "Opposite CSMT Station, Cannon Pav Bhaji",
                Arrays.asList("English", "Hindi"),
                "CST & Marine Lines",
                4.8f
        ));

        // --- SEED COMMUNITY SUBMISSIONS (10 Records) ---
        communityList.add(new CommunitySubmission(
                "comm_1",
                "user_riya",
                "Riya Sen",
                CommunitySubmission.TYPE_PLACE,
                "Portuguese St. John the Baptist Ruins (SEEPZ)",
                "Abandoned 16th-century church ruins hidden inside the industrial SEEPZ complex in Andheri East, overgrown with ancient banyan roots like Angkor Wat.",
                "Andheri East",
                "Historical",
                CommunitySubmission.STATUS_APPROVED,
                24
        ));

        communityList.add(new CommunitySubmission(
                "comm_2",
                "user_tanmay",
                "Tanmay Joshi",
                CommunitySubmission.TYPE_TIP,
                "Free Sunset Spot: Bandra Fort Amphitheatre",
                "Instead of crowded Bandstand, climb inside Castella de Aguada (Bandra Fort) for an unobstructed view of the Bandra-Worli Sea Link lit up at 7 PM.",
                "Bandra West",
                "Sunset",
                CommunitySubmission.STATUS_APPROVED,
                38
        ));

        communityList.add(new CommunitySubmission(
                "comm_3",
                "user_meera",
                "Meera K.",
                CommunitySubmission.TYPE_TIP,
                "Best Quiet Corner at David Sassoon Library",
                "The second-floor balcony overlooking the garden is breezy, shaded, and has power sockets under table #4.",
                "Fort",
                "Study",
                CommunitySubmission.STATUS_APPROVED,
                19
        ));

        communityList.add(new CommunitySubmission(
                "comm_4",
                "user_farooq",
                "Farooq Merchant",
                CommunitySubmission.TYPE_PLACE,
                "Yazdani Bakery & Restaurant (Horniman Circle)",
                "Historic 1950s Parsi bakery renowned for authentic crusty brun maska, warm apple pie, ginger biscuits, and chai served on vintage communal benches.",
                "Fort",
                "Food",
                CommunitySubmission.STATUS_APPROVED,
                31
        ));

        communityList.add(new CommunitySubmission(
                "comm_5",
                "user_shreya",
                "Shreya Rao",
                CommunitySubmission.TYPE_PLACE,
                "Worli Koliwada Village & Sea Jetty Sunset",
                "800-year-old fishing village with narrow lanes, local fish fry stalls, and an open pier offering a dramatic view of the Bandra-Worli Sea Link.",
                "Worli, Central Mumbai",
                "Sunset",
                CommunitySubmission.STATUS_APPROVED,
                17
        ));

        communityList.add(new CommunitySubmission(
                "comm_6",
                "user_vikram",
                "Vikram Chawla",
                CommunitySubmission.TYPE_TIP,
                "Asiatic Library Sunday Morning Urban Sketchers",
                "Every Sunday at 8 AM, amateur artists sit on the Asiatic Town Hall steps to sketch old buildings. Free to join and great creative company!",
                "Fort",
                "Cultural",
                CommunitySubmission.STATUS_APPROVED,
                15
        ));

        communityList.add(new CommunitySubmission(
                "comm_7",
                "user_ananya",
                "Ananya Dixit",
                CommunitySubmission.TYPE_PLACE,
                "Jimmy Boy Parsi Dining (Fort)",
                "Celebrated for serving authentic Parsi Lagan Nu Bhonu (wedding feast) on a real banana leaf at budget prices, plus Duke's Raspberry Soda.",
                "Fort",
                "Food",
                CommunitySubmission.STATUS_APPROVED,
                22
        ));

        communityList.add(new CommunitySubmission(
                "comm_8",
                "user_rohit",
                "Rohit Nair",
                CommunitySubmission.TYPE_TIP,
                "Dadar Flower Market Photography Secret",
                "Go to the pedestrian footbridge over platform 1 around 6:15 AM to shoot wide angle pictures looking down at the marigold sellers without bumping into baskets.",
                "Dadar West",
                "Market",
                CommunitySubmission.STATUS_APPROVED,
                28
        ));

        communityList.add(new CommunitySubmission(
                "comm_9",
                "user_zoya",
                "Zoya Siddiqui",
                CommunitySubmission.TYPE_PLACE,
                "Colaba Causeway Antique Book Vendors Alley",
                "Tucked behind the cinema lane, old paper vendors sell vintage first-edition paperbacks, National Geographics from the 1960s, and vintage Mumbai maps for under ₹100.",
                "Colaba",
                "Hidden Gem",
                CommunitySubmission.STATUS_APPROVED,
                14
        ));

        communityList.add(new CommunitySubmission(
                "comm_10",
                "user_arjun",
                "Arjun Mehta",
                CommunitySubmission.TYPE_PLACE,
                "Hidden Rooftop Deck at Horniman Circle",
                "Old terrace stairwell in the lane behind Starbucks that offers an unobstructed view of the St. Thomas Cathedral spire.",
                "Fort",
                "Hidden Gem",
                CommunitySubmission.STATUS_PENDING,
                5
        ));

        attachCuratedMenus();
    }

    private void attachCuratedMenus() {
        Place kyani = placesMap.get("place_kyani");
        if (kyani != null) {
            kyani.setMenuItems(Arrays.asList(
                    new MenuItem("Bun Maska & Irani Chai", 60, true, "VEGETARIAN", "Fresh crusty bun with salted Amul butter paired with slow-brewed sweet Irani cutting chai.", "Signature breakfast combo served continuously since 1904.", "Kyani & Co. Heritage Menu (Curated 2026)"),
                    new MenuItem("Parsi Mawa Cake", 35, true, "VEGETARIAN", "Cardamom-scented dense sponge cake baked fresh in heritage ovens.", "Iconic Mumbai takeaway cake.", "Kyani & Co. Heritage Menu (Curated 2026)"),
                    new MenuItem("Chicken Farcha", 160, true, "NON_VEG", "Spiced fried chicken coated in crispy egg-laced batter.", "Quintessential Parsi feast classic.", "Kyani & Co. Heritage Menu (Curated 2026)"),
                    new MenuItem("Kheema Pav", 140, true, "NON_VEG", "Spicy minced mutton gravy served with hot ladi pav.", "Beloved spicy Mumbai morning staple.", "Kyani & Co. Heritage Menu (Curated 2026)")
            ));
        }

        Place britannia = placesMap.get("place_britannia");
        if (britannia != null) {
            britannia.setMenuItems(Arrays.asList(
                    new MenuItem("Mutton Berry Pulao", 450, true, "NON_VEG", "Fragrant basmati rice crowned with Iranian barberries (zereshk) and slow-braised mutton.", "Legendary dish created by late Mr. Boman Kohinoor.", "Britannia & Co. Menu (Curated 2026)"),
                    new MenuItem("Veg Berry Pulao", 350, true, "VEGETARIAN", "Barberry rice tossed with paneer, spiced vegetables, and cashews.", "Rich vegetarian adaptation of the Persian classic.", "Britannia & Co. Menu (Curated 2026)"),
                    new MenuItem("Sali Boti with Pav", 380, true, "NON_VEG", "Sweet and tangy mutton curry blanketed with crisp potato shoestrings.", "Authentic Parsi sweet-sour flavor profile.", "Britannia & Co. Menu (Curated 2026)"),
                    new MenuItem("Caramel Custard", 120, true, "VEGETARIAN", "Steamed golden egg custard dripping with amber caramel sauce.", "The gold standard of Mumbai caramel custards.", "Britannia & Co. Menu (Curated 2026)")
            ));
        }

        Place kitabKhana = placesMap.get("place_kitab_khana");
        if (kitabKhana != null) {
            kitabKhana.setMenuItems(Arrays.asList(
                    new MenuItem("Artisanal Cappuccino & Biscotti", 180, true, "BEVERAGE", "Freshly roasted Arabica espresso with velvety milk foam and house biscotti.", "Perfect pairing while reading in the high-ceilinged bookstore.", "Somethings Gourmet Cafe Menu (Curated 2026)"),
                    new MenuItem("Grilled Pesto & Bocconcini Panini", 240, true, "VEGETARIAN", "Basil pesto, fresh bocconcini cheese, and ripe tomato pressed in sourdough.", "Wholesome, quiet light lunch in Fort.", "Somethings Gourmet Cafe Menu (Curated 2026)")
            ));
        }

        Place candies = placesMap.get("place_candies_bandra");
        if (candies != null) {
            candies.setMenuItems(Arrays.asList(
                    new MenuItem("Macaroni & Sweet Corn Salad", 120, true, "VEGETARIAN", "Chilled pasta salad with crunchy capsicum, corn, and house cream dressing.", "Bandra youth staple since the 1990s.", "Candies Pali Hill Menu (Curated 2026)"),
                    new MenuItem("Chicken Mini Burgers (Plate)", 150, true, "NON_VEG", "Spiced chicken patties in miniature sesame buns with mint slaw.", "Fast, satisfying snack in the sprawling villa courtyard.", "Candies Pali Hill Menu (Curated 2026)"),
                    new MenuItem("Blended Iced Mocha", 130, true, "BEVERAGE", "Thick blended iced chocolate espresso shake with dark chocolate drizzle.", "Refreshing cold coffee for Mumbai afternoons.", "Candies Pali Hill Menu (Curated 2026)")
            ));
        }

        Place guruKripa = placesMap.get("place_guru_kripa");
        if (guruKripa != null) {
            guruKripa.setMenuItems(Arrays.asList(
                    new MenuItem("Samosa with Chole", 60, true, "VEGETARIAN", "Two crushed crispy samosas drenched in thick spiced Sindhi chole with tamarind chutney.", "Supplies samosas to famous Mumbai cinema halls for decades.", "Guru Kripa Menu (Curated 2026)"),
                    new MenuItem("Dal Pakwan", 70, true, "VEGETARIAN", "Crispy cumin crackers served with spicy chana dal and chopped onion chutney.", "Traditional authentic Sindhi breakfast.", "Guru Kripa Menu (Curated 2026)")
            ));
        }

        Place ashok = placesMap.get("place_ashok_vada_pav");
        if (ashok != null) {
            ashok.setMenuItems(Collections.singletonList(
                    new MenuItem("Kirti College Special Vada Pav", 30, true, "VEGETARIAN", "Hot potato vada packed into soft pav with spicy red garlic chutney and crisp besan chura.", "Frequently voted Mumbai's number one street vada pav.", "Ashok Stall Tariff (Curated 2026)")
            ));
        }

        Place cannon = placesMap.get("place_cannon_pav_bhaji");
        if (cannon != null) {
            cannon.setMenuItems(Collections.singletonList(
                    new MenuItem("Amul Butter Pav Bhaji", 140, true, "VEGETARIAN", "Spiced tomato vegetable mash topped with melted butter, chopped onions, and lemon with two pavs.", "Legendary quick meal opposite CSMT station.", "Cannon Refreshments Menu (Curated 2026)")
            ));
        }

        Place sardar = placesMap.get("place_sardar_pav_bhaji");
        if (sardar != null) {
            sardar.setMenuItems(Collections.singletonList(
                    new MenuItem("Cheese Butter Pav Bhaji", 220, true, "VEGETARIAN", "Ultra-buttery vegetable mash smothered in shredded cheese and hot buttered pavs.", "Famous for its decadent, unapologetic butter slab.", "Sardar Refreshments Menu (Curated 2026)")
            ));
        }

        Place pancham = placesMap.get("place_pancham_puriwala");
        if (pancham != null) {
            pancham.setMenuItems(Collections.singletonList(
                    new MenuItem("Pancham Special Thali (5 Puris)", 160, true, "VEGETARIAN", "Five assorted puris (palak, beetroot, masala, sada) served with aloo bhaji, chole, and raita.", "Historic establishment operating near VT since 1848.", "Pancham Puriwala Menu (Curated 2026)")
            ));
        }
    }

    private void addSeedPlace(Place p) {
        placesMap.put(p.getId(), p);
    }

    private void loadPersistedData() {
        // Saved Place IDs
        Set<String> saved = prefs.getStringSet(KEY_SAVED_IDS, null);
        if (saved != null) {
            savedPlaceIds.addAll(saved);
        } else {
            // Default 2 favourites
            savedPlaceIds.add("place_sassoon_docks");
            savedPlaceIds.add("place_kyani");
        }

        // Custom places
        String customJson = prefs.getString(KEY_CUSTOM_PLACES, null);
        if (customJson != null) {
            Type type = new TypeToken<List<Place>>() {}.getType();
            List<Place> customList = gson.fromJson(customJson, type);
            if (customList != null) {
                for (Place p : customList) {
                    placesMap.put(p.getId(), p);
                }
            }
        }

        // Saved Itineraries
        String itinJson = prefs.getString(KEY_ITINERARIES, null);
        if (itinJson != null) {
            Type type = new TypeToken<List<Itinerary>>() {}.getType();
            List<Itinerary> list = gson.fromJson(itinJson, type);
            if (list != null) {
                savedItineraries.addAll(list);
            }
        }

        // Community submissions
        String commJson = prefs.getString(KEY_COMMUNITY, null);
        if (commJson != null) {
            Type type = new TypeToken<List<CommunitySubmission>>() {}.getType();
            List<CommunitySubmission> list = gson.fromJson(commJson, type);
            if (list != null && !list.isEmpty()) {
                communityList.clear();
                communityList.addAll(list);
            }
        }

        // Tours
        String tourJson = prefs.getString(KEY_TOURS, null);
        if (tourJson != null) {
            Type type = new TypeToken<List<TourListing>>() {}.getType();
            List<TourListing> list = gson.fromJson(tourJson, type);
            if (list != null && !list.isEmpty()) {
                toursList.clear();
                toursList.addAll(list);
            }
        }

        // Enquiries
        String enqJson = prefs.getString(KEY_ENQUIRIES, null);
        if (enqJson != null) {
            Type type = new TypeToken<List<Enquiry>>() {}.getType();
            List<Enquiry> list = gson.fromJson(enqJson, type);
            if (list != null) {
                enquiriesList.addAll(list);
            }
        }
    }

    public List<Place> getAllPlaces() {
        return new ArrayList<>(placesMap.values());
    }

    public Place getPlaceById(String id) {
        return placesMap.get(id);
    }

    public List<Place> getHiddenGems() {
        List<Place> list = new ArrayList<>();
        for (Place p : placesMap.values()) {
            if (p.isHiddenGem()) list.add(p);
        }
        return list;
    }

    public List<Place> getStudyWorkSpots() {
        List<Place> list = new ArrayList<>();
        for (Place p : placesMap.values()) {
            if (p.isStudyWork()) list.add(p);
        }
        return list;
    }

    public List<Place> getAffordableFoodSpots() {
        List<Place> list = new ArrayList<>();
        for (Place p : placesMap.values()) {
            if (p.isAffordableFood()) list.add(p);
        }
        return list;
    }

    public List<Place> getSavedPlaces() {
        List<Place> list = new ArrayList<>();
        for (String id : savedPlaceIds) {
            Place p = placesMap.get(id);
            if (p != null) list.add(p);
        }
        return list;
    }

    public boolean isPlaceSaved(String placeId) {
        return savedPlaceIds.contains(placeId);
    }

    public boolean toggleSavePlace(String placeId) {
        boolean nowSaved;
        if (savedPlaceIds.contains(placeId)) {
            savedPlaceIds.remove(placeId);
            nowSaved = false;
        } else {
            savedPlaceIds.add(placeId);
            nowSaved = true;
        }
        prefs.edit().putStringSet(KEY_SAVED_IDS, new HashSet<>(savedPlaceIds)).apply();
        return nowSaved;
    }

    public void addCustomPlace(Place place) {
        if (place == null) return;
        placesMap.put(place.getId(), place);
        saveCustomPlaces();
    }

    public void updatePlace(Place place) {
        if (place == null) return;
        placesMap.put(place.getId(), place);
        saveCustomPlaces();
    }

    private void saveCustomPlaces() {
        List<Place> customs = new ArrayList<>();
        for (Place p : placesMap.values()) {
            if (!p.getId().startsWith("place_")) {
                customs.add(p);
            }
        }
        prefs.edit().putString(KEY_CUSTOM_PLACES, gson.toJson(customs)).apply();
    }

    // Events
    public List<Event> getAllEvents() {
        return new ArrayList<>(eventsList);
    }

    public void addEvent(Event event) {
        if (event != null) {
            eventsList.add(0, event);
        }
    }

    // Itineraries
    public List<Itinerary> getSavedItineraries() {
        return new ArrayList<>(savedItineraries);
    }

    public void saveItinerary(Itinerary itinerary) {
        if (itinerary == null) return;
        int found = -1;
        for (int i = 0; i < savedItineraries.size(); i++) {
            if (savedItineraries.get(i).getId().equals(itinerary.getId())) {
                found = i;
                break;
            }
        }
        if (found >= 0) {
            savedItineraries.set(found, itinerary);
        } else {
            savedItineraries.add(0, itinerary);
        }
        prefs.edit().putString(KEY_ITINERARIES, gson.toJson(savedItineraries)).apply();
    }

    public void deleteItinerary(String id) {
        for (int i = 0; i < savedItineraries.size(); i++) {
            if (savedItineraries.get(i).getId().equals(id)) {
                savedItineraries.remove(i);
                break;
            }
        }
        prefs.edit().putString(KEY_ITINERARIES, gson.toJson(savedItineraries)).apply();
    }

    // Community Submissions
    public List<CommunitySubmission> getCommunitySubmissions() {
        return new ArrayList<>(communityList);
    }

    public void addCommunitySubmission(CommunitySubmission sub) {
        if (sub != null) {
            communityList.add(0, sub);
            prefs.edit().putString(KEY_COMMUNITY, gson.toJson(communityList)).apply();
        }
    }

    public void upvoteSubmission(String id) {
        for (CommunitySubmission sub : communityList) {
            if (sub.getId().equals(id)) {
                sub.setUpvotes(sub.getUpvotes() + 1);
                break;
            }
        }
        prefs.edit().putString(KEY_COMMUNITY, gson.toJson(communityList)).apply();
    }

    public void updateSubmissionStatus(String id, String status) {
        for (CommunitySubmission sub : communityList) {
            if (sub.getId().equals(id)) {
                sub.setStatus(status);
                break;
            }
        }
        prefs.edit().putString(KEY_COMMUNITY, gson.toJson(communityList)).apply();
    }

    // Tours
    public List<TourListing> getTourListings() {
        return new ArrayList<>(toursList);
    }

    public List<TourListing> getToursByGuide(String guideId) {
        List<TourListing> list = new ArrayList<>();
        for (TourListing t : toursList) {
            if (t.getGuideId().equals(guideId)) {
                list.add(t);
            }
        }
        return list;
    }

    public void addTour(TourListing tour) {
        if (tour != null) {
            toursList.add(0, tour);
            prefs.edit().putString(KEY_TOURS, gson.toJson(toursList)).apply();
        }
    }

    public void deleteTour(String tourId) {
        for (int i = 0; i < toursList.size(); i++) {
            if (toursList.get(i).getId().equals(tourId)) {
                toursList.remove(i);
                break;
            }
        }
        prefs.edit().putString(KEY_TOURS, gson.toJson(toursList)).apply();
    }

    // Enquiries
    public List<Enquiry> getEnquiriesForRecipient(String recipientId) {
        List<Enquiry> list = new ArrayList<>();
        for (Enquiry e : enquiriesList) {
            if (recipientId != null && recipientId.equals(e.getRecipientId())) {
                list.add(e);
            }
        }
        return list;
    }

    public void addEnquiry(Enquiry enquiry) {
        if (enquiry != null) {
            enquiriesList.add(0, enquiry);
            prefs.edit().putString(KEY_ENQUIRIES, gson.toJson(enquiriesList)).apply();
        }
    }
}
