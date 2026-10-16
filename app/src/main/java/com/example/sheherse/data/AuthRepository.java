package com.example.sheherse.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.sheherse.model.AppUser;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class AuthRepository {
    private static final String PREF_NAME = "sheherse_auth_prefs";
    private static final String KEY_CURRENT_USER = "current_user";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_ONBOARDING_DONE = "onboarding_done";
    private static final String KEY_REGISTERED_USERS = "registered_users_map";

    private static AuthRepository instance;
    private final SharedPreferences prefs;
    private final Gson gson;
    private AppUser currentUser;

    private AuthRepository(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        loadSession();
    }

    public static synchronized AuthRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AuthRepository(context);
        }
        return instance;
    }

    private void loadSession() {
        String userJson = prefs.getString(KEY_CURRENT_USER, null);
        if (userJson != null) {
            currentUser = gson.fromJson(userJson, AppUser.class);
        }
    }

    public boolean isOnboardingCompleted() {
        return prefs.getBoolean(KEY_ONBOARDING_DONE, false);
    }

    public void setOnboardingCompleted(boolean completed) {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, completed).apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && currentUser != null;
    }

    public AppUser getCurrentUser() {
        if (currentUser == null) {
            // Default to demo explorer for instant evaluation if needed
            return loginDemo(AppUser.ROLE_EXPLORER);
        }
        return currentUser;
    }

    public void setCurrentUser(AppUser user) {
        this.currentUser = user;
        if (user != null) {
            prefs.edit()
                    .putBoolean(KEY_IS_LOGGED_IN, true)
                    .putString(KEY_CURRENT_USER, gson.toJson(user))
                    .apply();
        } else {
            prefs.edit()
                    .putBoolean(KEY_IS_LOGGED_IN, false)
                    .remove(KEY_CURRENT_USER)
                    .apply();
        }
    }

    public AppUser loginDemo(String role) {
        AppUser demoUser;
        if (AppUser.ROLE_GUIDE.equalsIgnoreCase(role)) {
            demoUser = new AppUser(
                    "guide_farhan",
                    "Farhan Merchant",
                    "farhan@mumbaiguides.com",
                    AppUser.ROLE_GUIDE,
                    true,
                    "Certified South Mumbai & Art Deco heritage walk expert (8+ yrs experience)."
            );
        } else if (AppUser.ROLE_OWNER.equalsIgnoreCase(role)) {
            demoUser = new AppUser(
                    "owner_kyani",
                    "Pooja Shetty",
                    "pooja@kyanicafe.com",
                    AppUser.ROLE_OWNER,
                    true,
                    "Proprietor at Kyani & Co. Bakery & Chai, preserving heritage since 1904."
            );
        } else if (AppUser.ROLE_STUDENT.equalsIgnoreCase(role)) {
            demoUser = new AppUser(
                    "student_sameer",
                    "Sameer Khan",
                    "sameer.student@mumbaiuniv.ac.in",
                    AppUser.ROLE_STUDENT,
                    true,
                    "Architecture student searching for quiet reading rooms, fast Wi-Fi & budget Irani snacks."
            );
        } else if (AppUser.ROLE_RESIDENT.equalsIgnoreCase(role)) {
            demoUser = new AppUser(
                    "resident_kavita",
                    "Kavita Deshmukh",
                    "kavita.girgaon@gmail.com",
                    AppUser.ROLE_RESIDENT,
                    true,
                    "Lifelong Girgaon resident sharing authentic neighbourhood tales and hidden coastal alleys."
            );
        } else if (AppUser.ROLE_ORGANIZER.equalsIgnoreCase(role)) {
            demoUser = new AppUser(
                    "organizer_natasha",
                    "Natasha Batra",
                    "natasha@bombayheritage.org",
                    AppUser.ROLE_ORGANIZER,
                    true,
                    "Cultural curator coordinating Kala Ghoda open-air walks and suburban photo walks."
            );
        } else {
            demoUser = new AppUser(
                    "user_arjun",
                    "Arjun Mehta",
                    "arjun.explorer@gmail.com",
                    AppUser.ROLE_EXPLORER,
                    true,
                    "First-time Mumbai explorer keen on discovering colonial architecture and coastal sunsets."
            );
        }
        setCurrentUser(demoUser);
        return demoUser;
    }

    public boolean login(String email, String password) {
        if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
            return false;
        }

        String lowerEmail = email.trim().toLowerCase();

        // Check if demo email
        if (lowerEmail.contains("guide")) {
            loginDemo(AppUser.ROLE_GUIDE);
            return true;
        } else if (lowerEmail.contains("owner")) {
            loginDemo(AppUser.ROLE_OWNER);
            return true;
        } else if (lowerEmail.contains("student")) {
            loginDemo(AppUser.ROLE_STUDENT);
            return true;
        } else if (lowerEmail.contains("resident")) {
            loginDemo(AppUser.ROLE_RESIDENT);
            return true;
        } else if (lowerEmail.contains("organizer")) {
            loginDemo(AppUser.ROLE_ORGANIZER);
            return true;
        }

        // Custom user login
        String name = email.split("@")[0];
        if (!name.isEmpty()) {
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
        } else {
            name = "Mumbai Explorer";
        }

        AppUser user = new AppUser(
                "user_" + Math.abs(lowerEmail.hashCode()),
                name,
                lowerEmail,
                AppUser.ROLE_EXPLORER,
                false,
                "Explorer discovering Mumbai."
        );
        setCurrentUser(user);
        return true;
    }

    public AppUser register(String name, String email, String password, String role) {
        if (name == null || email == null || name.trim().isEmpty() || email.trim().isEmpty()) return null;
        String finalRole = role != null ? role : AppUser.ROLE_EXPLORER;
        String lowerEmail = email.trim().toLowerCase();
        String id = "user_" + Math.abs(lowerEmail.hashCode());

        String bio;
        if (AppUser.ROLE_GUIDE.equals(finalRole)) {
            bio = "Certified Mumbai Tour Guide.";
        } else if (AppUser.ROLE_OWNER.equals(finalRole)) {
            bio = "Local Business Establishment Owner.";
        } else if (AppUser.ROLE_STUDENT.equals(finalRole)) {
            bio = "Student & Freelancer studying in Mumbai.";
        } else if (AppUser.ROLE_RESIDENT.equals(finalRole)) {
            bio = "Proud Mumbai Resident and local recommender.";
        } else if (AppUser.ROLE_ORGANIZER.equals(finalRole)) {
            bio = "Event Organizer hosting cultural experiences.";
        } else {
            bio = "Mumbai city explorer.";
        }

        AppUser user = new AppUser(
                id,
                name.trim(),
                lowerEmail,
                finalRole,
                false,
                bio
        );
        setCurrentUser(user);
        return user;
    }

    public void switchRole(String newRole) {
        if (currentUser != null) {
            currentUser.setRole(newRole);
            if (currentUser.isDemo()) {
                loginDemo(newRole);
            } else {
                setCurrentUser(currentUser);
            }
        } else {
            loginDemo(newRole);
        }
    }

    public void logout() {
        currentUser = null;
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .remove(KEY_CURRENT_USER)
                .apply();
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return password;
        }
    }
}
