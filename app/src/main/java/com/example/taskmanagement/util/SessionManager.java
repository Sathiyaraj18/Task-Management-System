package com.example.taskmanagement.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.taskmanagement.model.User;

/**
 * Manages user authentication session using SharedPreferences.
 */
public class SessionManager {

    private static final String PREF_NAME = "task_management_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_EMAIL = "email";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = prefs.edit();
    }

    /**
     * Create login session for authenticated user.
     */
    public void createLoginSession(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, user.getId());
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_FULL_NAME, user.getFullName());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.apply();
    }

    /**
     * Check if user is currently logged in.
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Get the logged-in user id.
     */
    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    /**
     * Get the logged-in username.
     */
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    /**
     * Get the logged-in user's full name.
     */
    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, getUsername());
    }

    /**
     * Get current user details.
     */
    public User getCurrentUser() {
        if (!isLoggedIn()) return null;
        User user = new User();
        user.setId(getUserId());
        user.setUsername(getUsername());
        user.setFullName(getFullName());
        user.setEmail(prefs.getString(KEY_EMAIL, ""));
        return user;
    }

    /**
     * Clear session and logout user.
     */
    public void logoutUser() {
        editor.clear();
        editor.apply();
    }
}
