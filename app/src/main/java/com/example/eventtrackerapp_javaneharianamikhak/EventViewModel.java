package com.example.eventtrackerapp_javaneharianamikhak;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Calendar;
import java.util.List;
import java.util.PriorityQueue;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

/**
 * ViewModel for managing event data and business logic using MVVM pattern.
 * 
 * This ViewModel implements the MVVM architecture pattern and provides a clean
 * separation between the UI layer and business logic. It manages event data using
 * LiveData for reactive UI updates and implements efficient data structures for
 * optimal performance.
 * 
 * Key Features:
 * - LiveData management for reactive UI updates
 * - PriorityQueue for efficient event scheduling (O(log n) operations)
 * - HashMap-based caching for O(1) event lookups
 * - Composite key optimization for collision-free event identification
 * - AI-powered event title suggestions
 * - Conflict detection and validation
 * 
 * Data Structures:
 * - PriorityQueue<Event>: Maintains events in priority order
 * - HashMap<String, Event>: Fast event lookup by name
 * - HashMap<String, List<Event>>: Events grouped by date
 * - HashMap<String, Event>: Composite key lookup for name+date combinations
 * 
 * Performance: Optimized for large datasets with efficient caching and lookup
 * mechanisms. Uses O(1) average case for most operations.
 * 
 * @author Ariana Mikhak
 * @version 1.0
 * @since 2025
 */
public class EventViewModel extends AndroidViewModel {
    
    private static final int MORNING_START = 5;
    private static final int MORNING_END = 12;
    private static final int AFTERNOON_END = 17;
    private static final int EVENING_END = 21;
    
    private final EventService eventService;
    private final GeminiAIService geminiAIService;
    
    private final MutableLiveData<List<Event>> eventsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> suggestedTitleLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    
    private final PriorityQueue<Event> eventQueue;
    private final HashMap<String, Event> eventsByName;
    private final HashMap<String, List<Event>> dateToEventsMap;
    
    // ENHANCEMENT: Composite key for better collision avoidance
    private final HashMap<String, Event> eventsByCompositeKey;
    public EventViewModel(Application application) {
        super(application);
        ServiceFactory serviceFactory = getServiceFactory();
        eventService = serviceFactory.createEventService();
        geminiAIService = serviceFactory.createGeminiAIService();
        
        eventQueue = new PriorityQueue<>();
        eventsByName = new HashMap<>();
        dateToEventsMap = new HashMap<>();
        eventsByCompositeKey = new HashMap<>();
    }
    
    protected ServiceFactory getServiceFactory() {
        return ServiceFactory.getInstance(getApplication());
    }
    
    /**
     * Loads events for a user and preprocesses them for efficient operations.
     * @param userId The user ID to load events for
     */
    public void loadUserEvents(final int userId) {
        setLoading(true);
        try {
            List<Event> events = eventService.loadEventsForUser(userId);
            preprocessEvents(events);
            
            eventsLiveData.setValue(events);
            errorMessageLiveData.setValue(null);
        } catch (Exception e) {
            android.util.Log.e("EventViewModel", "Error loading events", e);
            setError("Error loading events: " + e.getMessage());
        } finally {
            setLoading(false);
        }
    }
    
    /**
     * Adds a new event for the specified user.
     * @param eventName The name of the event
     * @param eventDate The date of the event
     * @param eventTime The time of the event (optional)
     * @param userId The user ID who owns the event
     */
    public void addEvent(final String eventName, final String eventDate, 
                        final String eventTime, final int userId) {
        setLoading(true);
        try {
            EventRequest request = new EventRequest.Builder()
                .eventName(eventName)
                .eventDate(eventDate)
                .eventTime(eventTime)
                .userId(userId)
                .build();
            
            EventValidator.ValidationResult validation = EventValidator.validateEventRequest(request);
            if (!validation.isValid()) {
                setError(validation.getErrorMessage());
                return;
            }
            
            boolean success = eventService.addEvent(eventName, eventDate, eventTime, userId);
            if (success) {
                loadUserEvents(userId);
            } else {
                setError("Failed to add event");
            }
        } catch (Exception e) {
            android.util.Log.e("EventViewModel", "Error adding event", e);
            setError("Error adding event: " + e.getMessage());
        } finally {
            setLoading(false);
        }
    }
    
    /**
     * Updates an existing event and refreshes LiveData.
     * @param eventId The ID of the event to update
     * @param eventName The new name for the event
     * @param eventDate The new date for the event
     * @param eventTime The new time for the event
     * @param userId The ID of the user who owns the event
     */
    public void updateEvent(final int eventId, final String eventName, 
                           final String eventDate, final String eventTime, final int userId) {
        setLoading(true);
        try {
            EventRequest request = new EventRequest.Builder()
                .eventName(eventName)
                .eventDate(eventDate)
                .eventTime(eventTime)
                .userId(userId)
                .build();
            
            EventValidator.ValidationResult validation = EventValidator.validateEventRequest(request);
            if (!validation.isValid()) {
                setError(validation.getErrorMessage());
                return;
            }
            
            boolean success = eventService.updateEvent(eventId, eventName, eventDate, eventTime, userId);
            if (success) {
                loadUserEvents(userId);
            } else {
                setError("Failed to update event");
            }
        } catch (Exception e) {
            android.util.Log.e("EventViewModel", "Error updating event", e);
            setError("Error updating event: " + e.getMessage());
        } finally {
            setLoading(false);
        }
    }
    
    /**
     * Deletes an event and refreshes LiveData.
     * @param eventId The ID of the event to delete.
     * @param userId The ID of the user who owns the event.
     */
    /**
     * Deletes an event and refreshes the event list.
     * @param eventId The ID of the event to delete
     * @param userId The user ID who owns the event
     */
    public void deleteEvent(final int eventId, final int userId) {
        setLoading(true);
        try {
            boolean success = eventService.deleteEvent(eventId, userId);
            if (success) {
                loadUserEvents(userId);
            } else {
                setError("Failed to delete event");
            }
        } catch (Exception e) {
            android.util.Log.e("EventViewModel", "Error deleting event", e);
            setError("Error deleting event: " + e.getMessage());
        } finally {
            setLoading(false);
        }
    }
    
    /**
     * Generates an AI-powered event title suggestion based on user's event history.
     * @param userId The user ID to generate suggestions for
     */
    public void generateEventTitleSuggestion(final int userId) {
        setLoading(true);
        try {
            List<Event> recentEvents = eventService.loadEventsForUser(userId);
            String locationContext = getCurrentLocationContext();
            
            geminiAIService.generateEventTitleSuggestion(recentEvents, "anytime", locationContext, 
                createAISuggestionCallback());
        } catch (Exception e) {
            android.util.Log.e("EventViewModel", "Error generating AI suggestion", e);
            setError("Error generating suggestion: " + e.getMessage());
            setLoading(false);
        }
    }

    private GeminiAIService.AISuggestionCallback createAISuggestionCallback() {
        return new GeminiAIService.AISuggestionCallback() {
            @Override
            public void onSuccess(String suggestion) {
                try {
                    if (suggestedTitleLiveData != null) {
                        suggestedTitleLiveData.postValue(suggestion);
    }
                } catch (RuntimeException e) {
                    android.util.Log.e("EventViewModel", "Error posting AI suggestion: " + e.getMessage());
                } finally {
                    setLoading(false);
                }
            }
            
            @Override
            public void onError(String error) {
                try {
                    setError("AI suggestion error: " + error);
                } catch (RuntimeException e) {
                    android.util.Log.e("EventViewModel", "Error handling AI error: " + e.getMessage());
                } finally {
                    setLoading(false);
    }
            }
        };
    }
    

    
    /**
     * Preprocesses events into efficient data structures for O(1) operations.
     * @param events The list of events to preprocess
     */
        private void preprocessEvents(List<Event> events) {
        eventQueue.clear();
        eventsByName.clear();
        dateToEventsMap.clear();
        eventsByCompositeKey.clear();
        
        events.forEach(this::addEventToCache);
    }

    private void addEventToCache(Event event) {
        eventQueue.offer(event);
        eventsByName.put(event.getName(), event);
        dateToEventsMap.computeIfAbsent(event.getDate(), k -> new ArrayList<>()).add(event);
        
        // ENHANCEMENT: Add to composite key map for better collision avoidance
        String compositeKey = generateCompositeKey(event.getName(), event.getDate());
        eventsByCompositeKey.put(compositeKey, event);
    }
    
    /**
     * ENHANCEMENT: Generates a composite key combining event name and date.
     * This reduces collision probability in HashMap lookups.
     * 
     * @param eventName The name of the event
     * @param eventDate The date of the event
     * @return Composite key string
     */
    private String generateCompositeKey(String eventName, String eventDate) {
        return eventDate + "_" + eventName;
    }
    
    private void setLoading(boolean loading) {
        isLoadingLiveData.setValue(loading);
            }
    
    private void setError(String error) {
        errorMessageLiveData.setValue(error);
    }
    
    private String getCurrentTimeContext() {
        return DateTimeUtils.getCurrentTimeContext();
    }
    
    private String getCurrentLocationContext() {
        return "home"; // TODO: Add GPS integration
    }
    
    private Context getCurrentContext() {
        return getApplication().getApplicationContext();
    }
    
    /**
     * Gets the next scheduled event (highest priority).
     * @return The next event or null if no events exist
     */
    public Event getNextEvent() {
        return eventQueue.peek();
    }
    
    /**
     * Checks if an event with the given name exists.
     * @param eventName The name to check
     * @return true if an event with this name exists
     */
    public boolean eventExists(final String eventName) {
        return eventsByName.containsKey(eventName);
    }
    
    /**
     * Checks if an event with the given name and date exists.
     * @param eventName The name to check
     * @param eventDate The date to check
     * @return true if an event with this name and date exists
     */
    public boolean eventExists(final String eventName, final String eventDate) {
        // ENHANCEMENT: Use composite key for O(1) lookup instead of O(n) stream
        String compositeKey = generateCompositeKey(eventName, eventDate);
        return eventsByCompositeKey.containsKey(compositeKey);
    }

    public boolean hasConflictsOnDate(final String eventDate, final int excludeEventId) {
        List<Event> eventsOnDate = dateToEventsMap.get(eventDate);
        return eventsOnDate != null && eventsOnDate.stream()
            .anyMatch(event -> event.getId() != excludeEventId);
    }

    /**
     * Checks if there are any scheduling conflicts for a given date.
     * @param eventDate The date to check for conflicts
     * @param excludeEventId Event ID to exclude from conflict check (for updates)
     * @return true if conflicts exist on this date
     */
    @Deprecated
    public boolean conflictsExist(final String eventDate, final int excludeEventId) {
        return hasConflictsOnDate(eventDate, excludeEventId);
    }

    /**
     * Gets all events that conflict with a given date.
     * @param eventDate The date to check for conflicts
     * @param excludeEventId Event ID to exclude from results (for updates)
     * @return List of conflicting events
     */
    public List<Event> getConflictingEvents(final String eventDate, final int excludeEventId) {
        List<Event> eventsOnDate = dateToEventsMap.get(eventDate);
        if (eventsOnDate == null) return new ArrayList<>();
        
        return eventsOnDate.stream()
            .filter(event -> event.getId() != excludeEventId)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * ENHANCEMENT: Advanced conflict detection algorithm that checks for overlapping events.
     * @param newEvent The event to check for conflicts
     * @param excludeEventId Event ID to exclude from conflict check (for updates)
     * @return true if there are overlapping events
     */
        public boolean checkForConflicts(final Event newEvent, final int excludeEventId) {
        return eventService.hasTimeConflicts(newEvent, excludeEventId);
    }

    /**
     * ENHANCEMENT: Checks if two events overlap in time.
     * @param event1 First event to compare
     * @param event2 Second event to compare
     * @return true if events overlap
     */
    private boolean eventsOverlap(final Event event1, final Event event2) {
        return DateTimeUtils.eventsOverlap(event1, event2);
    }



    /**
     * Gets the next scheduled event.
     * @return The next event or null if no events exist
     */
    public Event getNextScheduledEvent() {
        List<Event> events = eventsLiveData.getValue();
        if (events == null || events.isEmpty()) {
            return null;
                }
        return eventService.getNextScheduledEvent(events.get(0).getUserId());
    }

    /**
     * Gets upcoming events (next 7 days).
     * @return List of upcoming events
     */
    public List<Event> getUpcomingEvents() {
        List<Event> events = eventsLiveData.getValue();
        if (events == null || events.isEmpty()) {
            return new ArrayList<>();
        }
        return eventService.getUpcomingEvents(events.get(0).getUserId());
                }

    /**
     * Finds an event by name.
     * @param eventName The name of the event to find
     * @return The event or null if not found
     */
    public Event findEventByName(final String eventName) {
        return eventsByName.get(eventName);
    }
    
    /**
     * ENHANCEMENT: Finds an event by its name and date using composite key.
     * This provides O(1) lookup performance.
     * @param eventName The name to search for
     * @param eventDate The date to search for
     * @return The event or null if not found
     */
    public Event findEventByNameAndDate(final String eventName, final String eventDate) {
        String compositeKey = generateCompositeKey(eventName, eventDate);
        return eventsByCompositeKey.get(compositeKey);
    }
    
    /**
     * Gets events by date.
     * @param date The date to get events for
     * @return List of events on that date
     */
    public List<Event> getEventsByDate(final String date) {
        return dateToEventsMap.getOrDefault(date, new ArrayList<>());
    }
    
    public LiveData<List<Event>> getEvents() {
        return eventsLiveData;
    }
    
    public LiveData<String> getSuggestedTitle() {
        return suggestedTitleLiveData;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        if (geminiAIService != null) {
            geminiAIService.shutdown();
        }
    }
} 