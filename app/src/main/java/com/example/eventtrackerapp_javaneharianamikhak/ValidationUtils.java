package com.example.eventtrackerapp_javaneharianamikhak;

import android.text.TextUtils;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for validation operations.
 * 
 * This class centralizes all input validation logic throughout the application,
 * following the Single Responsibility Principle. It provides comprehensive
 * validation for user inputs, ensuring data integrity and security.
 * 
 * Validation Categories:
 * - Event Validation: Event names, dates, times, and IDs
 * - User Validation: Usernames, passwords, and user IDs
 * - Contact Validation: Phone numbers and contact information
 * - Category Validation: Category names, colors, and IDs
 * - Date/Time Validation: Date formats, time formats, and future dates
 * 
 * Security Features:
 * - Input sanitization and trimming
 * - Length validation to prevent buffer overflows
 * - Format validation for dates and times
 * - Null and empty string checks
 * - ID validation for database integrity
 * 
 * Performance: All validation methods are optimized for speed and use
 * efficient string operations and regex patterns where appropriate.
 * 
 * @author Ariana Mikhak
 * @version 1.0
 * @since 2025
 */
public final class ValidationUtils {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy");
    
    // Prevent instantiation
    private ValidationUtils() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }
    
    /**
     * Validates event name.
     * @param eventName The event name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEventName(String eventName) {
        if (TextUtils.isEmpty(eventName)) {
            return false;
        }
        
        String trimmedName = eventName.trim();
        return trimmedName.length() >= EventConstants.MIN_EVENT_NAME_LENGTH && 
               trimmedName.length() <= EventConstants.MAX_EVENT_NAME_LENGTH;
    }
    
    /**
     * Validates event date format.
     * @param eventDate The event date to validate
     * @return true if valid date format, false otherwise
     */
    public static boolean isValidEventDate(String eventDate) {
        if (TextUtils.isEmpty(eventDate)) {
            return false;
        }
        
        try {
            LocalDate.parse(eventDate.trim(), DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
    /**
     * Validates event time format (optional).
     * @param eventTime The event time to validate
     * @return true if valid time format or empty, false otherwise
     */
    public static boolean isValidEventTime(String eventTime) {
        if (TextUtils.isEmpty(eventTime)) {
            return true; // Time is optional
        }
        
        // Basic time format validation (HH:MM AM/PM)
        String trimmedTime = eventTime.trim();
        return trimmedTime.matches("^(1[0-2]|0?[1-9]):[0-5][0-9]\\s?(AM|PM|am|pm)$");
    }
    
    /**
     * Validates username.
     * @param username The username to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidUsername(String username) {
        if (TextUtils.isEmpty(username)) {
            return false;
        }
        
        String trimmedUsername = username.trim();
        return trimmedUsername.length() >= 3 && trimmedUsername.length() <= 50;
    }
    
    /**
     * Validates password.
     * @param password The password to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (TextUtils.isEmpty(password)) {
            return false;
        }
        
        return password.length() >= 4;
    }
    
    /**
     * Validates phone number format.
     * @param phoneNumber The phone number to validate
     * @return true if valid format, false otherwise
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return false;
        }
        
        // Remove all non-digit characters
        String digitsOnly = phoneNumber.replaceAll("[^\\d]", "");
        return digitsOnly.length() >= 10 && digitsOnly.length() <= 15;
    }
    
    /**
     * Validates category name.
     * @param categoryName The category name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidCategoryName(String categoryName) {
        if (TextUtils.isEmpty(categoryName)) {
            return false;
        }
        
        String trimmedName = categoryName.trim();
        return trimmedName.length() >= 1 && trimmedName.length() <= 50;
    }
    
    /**
     * Validates category color format.
     * @param color The color to validate
     * @return true if valid hex color, false otherwise
     */
    public static boolean isValidColor(String color) {
        if (TextUtils.isEmpty(color)) {
            return false;
        }
        
        return color.matches("^#[0-9A-Fa-f]{6}$");
    }
    
    /**
     * Validates user ID.
     * @param userId The user ID to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidUserId(int userId) {
        return userId > EventConstants.INVALID_USER_ID;
    }
    
    /**
     * Validates event ID.
     * @param eventId The event ID to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEventId(int eventId) {
        return eventId > EventConstants.INVALID_EVENT_ID;
    }
    
    /**
     * Validates category ID.
     * @param categoryId The category ID to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidCategoryId(int categoryId) {
        return categoryId > EventConstants.INVALID_CATEGORY_ID;
    }
    
    /**
     * Checks if a date is in the future.
     * @param eventDate The event date to check
     * @return true if date is in the future, false otherwise
     */
    public static boolean isFutureDate(String eventDate) {
        if (!isValidEventDate(eventDate)) {
            return false;
        }
        
        try {
            LocalDate eventLocalDate = LocalDate.parse(eventDate.trim(), DATE_FORMATTER);
            LocalDate today = LocalDate.now();
            return !eventLocalDate.isBefore(today);
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
    /**
     * Gets error message for invalid event name.
     * @return Error message string
     */
    public static String getEventNameErrorMessage() {
        return "Event name must be between " + EventConstants.MIN_EVENT_NAME_LENGTH + 
               " and " + EventConstants.MAX_EVENT_NAME_LENGTH + " characters.";
    }
    
    /**
     * Gets error message for invalid event date.
     * @return Error message string
     */
    public static String getEventDateErrorMessage() {
        return "Please enter a valid date in MM/DD/YYYY format.";
    }
    
    /**
     * Gets error message for invalid event time.
     * @return Error message string
     */
    public static String getEventTimeErrorMessage() {
        return "Please enter a valid time in HH:MM AM/PM format.";
    }
} 