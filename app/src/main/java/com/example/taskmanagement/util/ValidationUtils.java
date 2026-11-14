package com.example.taskmanagement.util;

import android.text.TextUtils;
import android.util.Patterns;

/**
 * Input validation utility methods.
 */
public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isValidUsername(String username) {
        if (isEmpty(username)) return false;
        String trimmed = username.trim();
        return trimmed.length() >= 3 && trimmed.length() <= 30;
    }

    public static boolean isValidPassword(String password) {
        if (isEmpty(password)) return false;
        return password.length() >= 4;
    }

    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) return true; // Email can be optional
        return Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    public static boolean isValidTaskTitle(String title) {
        if (isEmpty(title)) return false;
        return title.trim().length() >= 2;
    }
}
