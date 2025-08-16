package com.example.eventtrackerapp_javaneharianamikhak;

import java.util.List;
import java.util.Map;

/**
 * Service layer interface for event business logic.
 * Separates business rules from data access and UI concerns.
 */
public interface EventService {
    
    /**
     * Loads events for a user with preprocessing for efficient operations.
     * @param userId The user ID to load events for
     * @return List of events for the user
     */
    List<Event> loadEventsForUser(int userId);
    
    /**
     * Adds a new event with business rule validation.
     * @param eventName The name of the event
     * @param eventDate The date of the event
     * @param eventTime The time of the event (optional)
     * @param userId The user ID
     * @return true if event was added successfully
     */
    boolean addEvent(String eventName, String eventDate, String eventTime, int userId);
    
    /**
     * Updates an existing event with validation.
     * @param eventId The ID of the event to update
     * @param eventName The new name
     * @param eventDate The new date
     * @param eventTime The new time
     * @param userId The user ID
     * @return true if event was updated successfully
     */
    boolean updateEvent(int eventId, String eventName, String eventDate, String eventTime, int userId);
    
    /**
     * Deletes an event with proper cleanup.
     * @param eventId The ID of the event to delete
     * @param userId The user ID
     * @return true if event was deleted successfully
     */
    boolean deleteEvent(int eventId, int userId);
    
    /**
     * Checks for time-based conflicts between events.
     * @param newEvent The event to check for conflicts
     * @param excludeEventId Event ID to exclude from conflict check
     * @return true if conflicts exist
     */
    boolean hasTimeConflicts(Event newEvent, int excludeEventId);
    
    /**
     * Gets the next scheduled event for a user.
     * @param userId The user ID
     * @return The next event, or null if none found
     */
    Event getNextScheduledEvent(int userId);
    
    /**
     * Gets upcoming events for the next 7 days.
     * @param userId The user ID
     * @return List of upcoming events
     */
    List<Event> getUpcomingEvents(int userId);
    
    /**
     * Finds an event by name with O(1) lookup.
     * @param eventName The name to search for
     * @param userId The user ID
     * @return The event if found, null otherwise
     */
    Event findEventByName(String eventName, int userId);
    
    /**
     * Gets events for a specific date.
     * @param date The date to search for
     * @param userId The user ID
     * @return List of events on that date
     */
    List<Event> getEventsByDate(String date, int userId);
    
    /**
     * Gets event count by category for analytics.
     * @param userId The user ID
     * @return Map of category names to event counts
     */
    Map<String, Integer> getEventCountByCategory(int userId);
} 