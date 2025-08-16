package com.example.eventtrackerapp_javaneharianamikhak;

import android.util.Log;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for date and time parsing and validation.
 * Encapsulates all date/time logic to ensure consistency across the application.
 */
public final class DateTimeUtils {
    
    private static final String TAG = "DateTimeUtils";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER_12H = DateTimeFormatter.ofPattern("h:mm a");
    private static final DateTimeFormatter TIME_FORMATTER_24H = DateTimeFormatter.ofPattern("HH:mm");
    
    private DateTimeUtils() {
        // Prevent instantiation
    }
    
    /**
     * Parses a date string in M/d/yyyy format.
     * @param dateString The date string to parse
     * @return LocalDate if valid, null otherwise
     */
    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        
        try {
            return LocalDate.parse(dateString.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            Log.w(TAG, "Failed to parse date: " + dateString, e);
            return null;
        }
    }
    
    /**
     * Parses a time string in various formats (12-hour and 24-hour).
     * @param timeString The time string to parse
     * @return LocalTime if valid, null otherwise
     */
    public static LocalTime parseTime(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return null;
        }
        
        String cleanTime = timeString.trim().toLowerCase();
        
        try {
            // Try 12-hour format first (h:mm a)
            if (cleanTime.contains("am") || cleanTime.contains("pm")) {
                return LocalTime.parse(cleanTime, TIME_FORMATTER_12H);
            } else {
                // Try 24-hour format (HH:mm)
                return LocalTime.parse(cleanTime, TIME_FORMATTER_24H);
            }
        } catch (DateTimeParseException e) {
            Log.w(TAG, "Failed to parse time: " + timeString, e);
            return null;
        }
    }
    
    /**
     * Validates if a date string is in the correct format.
     * @param dateString The date string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidDate(String dateString) {
        return parseDate(dateString) != null;
    }
    
    /**
     * Validates if a time string is in the correct format.
     * @param timeString The time string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidTime(String timeString) {
        return parseTime(timeString) != null;
    }
    
    /**
     * Formats a LocalDate to the standard string format.
     * @param date The LocalDate to format
     * @return Formatted date string
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMATTER);
    }
    
    /**
     * Formats a LocalTime to 12-hour format.
     * @param time The LocalTime to format
     * @return Formatted time string
     */
    public static String formatTime12Hour(LocalTime time) {
        if (time == null) {
            return "";
        }
        return time.format(TIME_FORMATTER_12H);
    }
    
    /**
     * Checks if two events overlap in time.
     * @param event1 First event
     * @param event2 Second event
     * @return true if events overlap, false otherwise
     */
    public static boolean eventsOverlap(Event event1, Event event2) {
        if (event1.getTime() == null || event2.getTime() == null) {
            return false;
        }
        
        LocalTime time1 = parseTime(event1.getTime());
        LocalTime time2 = parseTime(event2.getTime());
        
        if (time1 == null || time2 == null) {
            return false;
        }
        
        long hoursDifference = Math.abs(java.time.temporal.ChronoUnit.HOURS.between(time1, time2));
        return hoursDifference < EventConstants.CONFLICT_THRESHOLD_HOURS;
    }
    
    /**
     * Gets the current time context (morning, afternoon, evening, night).
     * @return Time context string
     */
    public static String getCurrentTimeContext() {
        LocalTime now = LocalTime.now();
        int hour = now.getHour();
        
        if (hour >= EventConstants.MORNING_START_HOUR && hour < EventConstants.MORNING_END_HOUR) {
            return "morning";
        } else if (hour >= EventConstants.MORNING_END_HOUR && hour < EventConstants.AFTERNOON_END_HOUR) {
            return "afternoon";
        } else if (hour >= EventConstants.AFTERNOON_END_HOUR && hour < EventConstants.EVENING_END_HOUR) {
            return "evening";
        } else {
            return "night";
        }
    }
    
    /**
     * Checks if a date is in the past.
     * @param dateString The date string to check
     * @return true if the date is in the past, false otherwise
     */
    public static boolean isPastDate(String dateString) {
        LocalDate date = parseDate(dateString);
        if (date == null) {
            return false;
        }
        return date.isBefore(LocalDate.now());
    }
    
    /**
     * Checks if a date is today.
     * @param dateString The date string to check
     * @return true if the date is today, false otherwise
     */
    public static boolean isToday(String dateString) {
        LocalDate date = parseDate(dateString);
        if (date == null) {
            return false;
        }
        return date.equals(LocalDate.now());
    }
    
    /**
     * Gets the number of days until a date.
     * @param dateString The date string to check
     * @return Number of days until the date (negative if past)
     */
    public static long getDaysUntil(String dateString) {
        LocalDate date = parseDate(dateString);
        if (date == null) {
            return Long.MAX_VALUE;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), date);
    }
} 