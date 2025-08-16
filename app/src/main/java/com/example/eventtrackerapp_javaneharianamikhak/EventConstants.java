package com.example.eventtrackerapp_javaneharianamikhak;

/**
 * Constants used throughout the EventTrackerApp.
 * 
 * This class centralizes all magic numbers, strings, and configuration values
 * used throughout the application. This follows the DRY (Don't Repeat Yourself)
 * principle and makes the codebase more maintainable and less error-prone.
 * 
 * Categories:
 * - Database Constants: Version numbers, table names, column names
 * - Request Codes: Activity result codes for navigation
 * - Time Constants: Hour-based time classifications
 * - UI Constants: Standard dimensions and spacing
 * - Validation Constants: Input validation rules
 * - Error Messages: User-friendly error messages
 * - Success Messages: User feedback messages
 * - Default Values: Application defaults
 * - AI Constants: AI service configuration
 * - File Provider: File sharing configuration
 * 
 * Benefits:
 * - Eliminates magic numbers and strings throughout codebase
 * - Centralized configuration management
 * - Easier maintenance and updates
 * - Improved code readability
 * - Reduced risk of typos and inconsistencies
 * 
 * @author Ariana Mikhak
 * @version 1.0
 * @since 2025
 */
public final class EventConstants {
    
    // Database Constants
    public static final int INVALID_USER_ID = -1;
    public static final int INVALID_EVENT_ID = -1;
    public static final int INVALID_CATEGORY_ID = -1;
    public static final int DATABASE_VERSION = 6;
    
    // Request Codes
    public static final int ADD_EVENT_REQUEST = 1;
    public static final int EDIT_EVENT_REQUEST = 2;
    public static final int SMS_PERMISSION_REQUEST = 3;
    
    // Time Constants
    public static final int MORNING_START_HOUR = 5;
    public static final int MORNING_END_HOUR = 12;
    public static final int AFTERNOON_END_HOUR = 17;
    public static final int EVENING_END_HOUR = 21;
    public static final int CONFLICT_THRESHOLD_HOURS = 2;
    
    // UI Constants
    public static final int DEFAULT_MARGIN_DP = 16;
    public static final int DEFAULT_PADDING_DP = 12;
    public static final int TOOLBAR_HEIGHT_DP = 56;
    
    // Validation Constants
    public static final int MIN_EVENT_NAME_LENGTH = 1;
    public static final int MAX_EVENT_NAME_LENGTH = 100;
    
    // CSV Export Constants
    public static final String CSV_HEADER = "Event ID,Event Name,Date,Time,Category";
    public static final String CSV_FILENAME = "events_export.csv";
    public static final String EXPORT_DIRECTORY = "exports";
    
    // Error Messages
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid credentials format.";
    public static final String ERROR_DATABASE = "Database error occurred. Please try again.";
    public static final String ERROR_EXPORT_FAILED = "Failed to export events: ";
    public static final String ERROR_NO_EVENTS = "No events to sort";
    public static final String ERROR_EMPTY_EVENT_NAME = "Event name cannot be empty";
    public static final String ERROR_EMPTY_EVENT_DATE = "Event date cannot be empty";
    public static final String ERROR_INVALID_DATE_FORMAT = "Invalid event date format";
    public static final String ERROR_TIME_CONFLICT = "Time conflict detected";
    public static final String ERROR_DATABASE_OPERATION = "Database operation failed";
    
    // Success Messages
    public static final String SUCCESS_EVENT_ADDED = "Event added successfully!";
    public static final String SUCCESS_EVENT_UPDATED = "Event updated successfully!";
    public static final String SUCCESS_EVENT_DELETED = "Event deleted successfully.";
    public static final String SUCCESS_EXPORT = "Events exported successfully!";
    public static final String SUCCESS_SORT = "Events sorted successfully";
    
    // Default Values
    public static final String DEFAULT_EVENT_TIME = "";
    public static final String DEFAULT_CATEGORY_NAME = "General";
    public static final String DEFAULT_CATEGORY_COLOR = "#2196F3";
    
    // AI Constants
    public static final int AI_SUGGESTION_TIMEOUT_MS = 10000;
    public static final String AI_FALLBACK_SUGGESTION = "New Event";
    
    // File Provider
    public static final String FILE_PROVIDER_AUTHORITY = "com.example.eventtrackerapp_javaneharianamikhak.fileprovider";
    
    // Prevent instantiation
    private EventConstants() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }
} 