package com.example.taskmanagement.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Utility functions for date parsing, formatting, and comparison.
 */
public final class DateUtils {

    public static final String FORMAT_DATABASE = "yyyy-MM-dd";
    public static final String FORMAT_DISPLAY = "dd MMM yyyy";
    public static final String FORMAT_DISPLAY_FULL = "EEEE, dd MMMM yyyy";

    private DateUtils() {
        // Prevent instantiation
    }

    /**
     * Get today's date in yyyy-MM-dd format.
     */
    public static String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATABASE, Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Format a date string from yyyy-MM-dd to a human-readable display string (e.g. 14 Nov 2026).
     */
    public static String formatDisplayDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return "-";
        }
        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat(FORMAT_DATABASE, Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat(FORMAT_DISPLAY, Locale.getDefault());
            Date date = dbFormat.parse(dateStr);
            return date != null ? displayFormat.format(date) : dateStr;
        } catch (ParseException e) {
            return dateStr;
        }
    }

    /**
     * Format a date string to a full display string with day name.
     */
    public static String formatFullDate(Date date) {
        if (date == null) return "-";
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DISPLAY_FULL, Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Parse date string in yyyy-MM-dd format to Date object.
     */
    public static Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat(FORMAT_DATABASE, Locale.getDefault());
            return dbFormat.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Check if a date string in yyyy-MM-dd format is strictly in the past (before today's midnight).
     */
    public static boolean isPastDate(String dateStr) {
        Date target = parseDate(dateStr);
        if (target == null) return false;

        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        todayCal.set(Calendar.SECOND, 0);
        todayCal.set(Calendar.MILLISECOND, 0);

        Calendar targetCal = Calendar.getInstance();
        targetCal.setTime(target);
        targetCal.set(Calendar.HOUR_OF_DAY, 0);
        targetCal.set(Calendar.MINUTE, 0);
        targetCal.set(Calendar.SECOND, 0);
        targetCal.set(Calendar.MILLISECOND, 0);

        return targetCal.before(todayCal);
    }

    /**
     * Compare two date strings (yyyy-MM-dd).
     * Returns true if dueDate is before startDate.
     */
    public static boolean isDueDateBeforeStartDate(String startDateStr, String dueDateStr) {
        Date start = parseDate(startDateStr);
        Date due = parseDate(dueDateStr);
        if (start == null || due == null) return false;
        return due.before(start);
    }

    /**
     * Calculate remaining days until due date.
     * Negative value means overdue.
     */
    public static long getDaysRemaining(String dueDateStr) {
        Date due = parseDate(dueDateStr);
        if (due == null) return 0;

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        long diff = due.getTime() - today.getTimeInMillis();
        return diff / (1000 * 60 * 60 * 24);
    }
}
