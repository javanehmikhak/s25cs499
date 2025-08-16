package com.example.eventtrackerapp_javaneharianamikhak;

import android.util.Log;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Validates event data and business rules.
 * Single Responsibility: Only handles validation logic.
 */
public final class EventValidator {
    
    private static final String TAG = "EventValidator";
    
    private EventValidator() {
        // Prevent instantiation
    }
    
    /**
     * Validates event request data.
     * @param request The event request to validate
     * @return ValidationResult with success status and error message
     */
    public static ValidationResult validateEventRequest(EventRequest request) {
        try {
            request.validate();
            return ValidationResult.success();
        } catch (IllegalArgumentException e) {
            return ValidationResult.failure(e.getMessage());
        }
    }
    
    /**
     * Validates event name.
     * @param eventName The event name to validate
     * @return ValidationResult with success status and error message
     */
    public static ValidationResult validateEventName(String eventName) {
        if (eventName == null || eventName.trim().isEmpty()) {
            return ValidationResult.failure(EventConstants.ERROR_EMPTY_EVENT_NAME);
        }
        if (eventName.length() > EventConstants.MAX_EVENT_NAME_LENGTH) {
            return ValidationResult.failure("Event name too long (max " + EventConstants.MAX_EVENT_NAME_LENGTH + " characters)");
        }
        return ValidationResult.success();
    }
    
    /**
     * Validates event date.
     * @param eventDate The event date to validate
     * @return ValidationResult with success status and error message
     */
    public static ValidationResult validateEventDate(String eventDate) {
        if (eventDate == null || eventDate.trim().isEmpty()) {
            return ValidationResult.failure(EventConstants.ERROR_EMPTY_EVENT_DATE);
        }
        if (!DateTimeUtils.isValidDate(eventDate)) {
            return ValidationResult.failure(EventConstants.ERROR_INVALID_DATE_FORMAT);
        }
        return ValidationResult.success();
    }
    
    /**
     * Validates event time.
     * @param eventTime The event time to validate
     * @return ValidationResult with success status and error message
     */
    public static ValidationResult validateEventTime(String eventTime) {
        if (eventTime == null || eventTime.trim().isEmpty()) {
            return ValidationResult.success(); // Time is optional
        }
        if (!DateTimeUtils.isValidTime(eventTime)) {
            return ValidationResult.failure("Invalid time format (use h:mm a format)");
        }
        return ValidationResult.success();
    }
    
    /**
     * Validates user ID.
     * @param userId The user ID to validate
     * @return ValidationResult with success status and error message
     */
    public static ValidationResult validateUserId(int userId) {
        if (userId == EventConstants.INVALID_USER_ID) {
            return ValidationResult.failure("Invalid user ID");
        }
        return ValidationResult.success();
    }
    
    /**
     * Checks for time conflicts between events.
     * @param newEvent The new event to check
     * @param existingEvents List of existing events
     * @param excludeEventId Event ID to exclude from conflict check
     * @return ValidationResult with success status and error message
     */
    public static ValidationResult checkTimeConflicts(Event newEvent, List<Event> existingEvents, int excludeEventId) {
        if (newEvent == null || newEvent.getDate() == null) {
            return ValidationResult.success();
        }
        
        List<Event> eventsOnSameDate = existingEvents.stream()
            .filter(event -> event.getDate().equals(newEvent.getDate()) && event.getId() != excludeEventId)
            .toList();
        
        for (Event existingEvent : eventsOnSameDate) {
            if (DateTimeUtils.eventsOverlap(newEvent, existingEvent)) {
                return ValidationResult.failure(EventConstants.ERROR_TIME_CONFLICT);
            }
        }
        
        return ValidationResult.success();
    }
    

    
    /**
     * Immutable result of validation operations.
     */
    public static final class ValidationResult {
        private final boolean isValid;
        private final String errorMessage;
        
        private ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }
        
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult failure(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }
        
        public boolean isValid() {
            return isValid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
} 