package com.example.eventtrackerapp_javaneharianamikhak;

import android.content.Context;
import android.util.Log;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

/**
 * Concrete implementation of EventService.
 * Encapsulates business logic and data access patterns.
 */
public class EventServiceImpl implements EventService {
    
    private static final String TAG = "EventServiceImpl";
    private static final int CONFLICT_THRESHOLD_HOURS = 2;
    private static final int UPCOMING_DAYS = 7;
    
    private final DatabaseHelper databaseHelper;
    private final GeminiAIService geminiAIService;
    
    // Cached data structures for performance
    private final Map<Integer, PriorityQueue<Event>> userEventQueues = new HashMap<>();
    private final Map<Integer, Map<String, Event>> userEventMaps = new HashMap<>();
    private final Map<Integer, Map<String, List<Event>>> userDateToEventsMaps = new HashMap<>();
    
    public EventServiceImpl(Context context) {
        this.databaseHelper = new DatabaseHelper(context);
        this.geminiAIService = new GeminiAIService(context);
    }
    
    @Override
    public List<Event> loadEventsForUser(int userId) {
        try {
            List<Event> events = databaseHelper.getAllEvents(userId);
            preprocessEventsForUser(userId, events);
            return events;
        } catch (Exception e) {
            Log.e(TAG, "Error loading events for user " + userId, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public boolean addEvent(String eventName, String eventDate, String eventTime, int userId) {
        try {
            validateEventData(eventName, eventDate);
            
            Event newEvent = new Event(-1, eventName, eventDate, eventTime, userId);
            if (hasTimeConflicts(newEvent, -1)) {
                Log.w(TAG, "Time conflict detected for event: " + eventName);
                return false;
            }
            
            boolean success = databaseHelper.addEvent(eventName, eventDate, eventTime, userId);
            if (success) {
                refreshUserData(userId);
            }
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Error adding event", e);
            return false;
        }
    }
    
    @Override
    public boolean updateEvent(int eventId, String eventName, String eventDate, String eventTime, int userId) {
        try {
            validateEventData(eventName, eventDate);
            
            Event updatedEvent = new Event(eventId, eventName, eventDate, eventTime, userId);
            if (hasTimeConflicts(updatedEvent, eventId)) {
                Log.w(TAG, "Time conflict detected for updated event: " + eventName);
                return false;
            }
            
            boolean success = databaseHelper.updateEvent(eventId, eventName, eventDate, eventTime);
            if (success) {
                refreshUserData(userId);
            }
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Error updating event", e);
            return false;
        }
    }
    
    @Override
    public boolean deleteEvent(int eventId, int userId) {
        try {
            boolean success = databaseHelper.deleteEvent(eventId);
            if (success) {
                refreshUserData(userId);
            }
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting event", e);
            return false;
        }
    }
    
    @Override
    public boolean hasTimeConflicts(Event newEvent, int excludeEventId) {
        if (newEvent == null || newEvent.getDate() == null) {
            return false;
        }
        
        List<Event> eventsOnDate = userDateToEventsMaps.get(newEvent.getUserId())
            .get(newEvent.getDate());
        
        if (eventsOnDate == null) {
            return false;
        }
        
        return eventsOnDate.stream()
            .anyMatch(existingEvent -> 
                existingEvent.getId() != excludeEventId && 
                eventsOverlap(newEvent, existingEvent));
    }
    
    @Override
    public Event getNextScheduledEvent(int userId) {
        PriorityQueue<Event> eventQueue = userEventQueues.get(userId);
        return eventQueue != null ? eventQueue.peek() : null;
    }
    
    @Override
    public List<Event> getUpcomingEvents(int userId) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(UPCOMING_DAYS);
        
        return userEventQueues.get(userId).stream()
            .filter(event -> {
                LocalDate eventDate = DateTimeUtils.parseDate(event.getDate());
                return eventDate != null && 
                       !eventDate.isBefore(today) && 
                       !eventDate.isAfter(endDate);
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public Event findEventByName(String eventName, int userId) {
        Map<String, Event> eventMap = userEventMaps.get(userId);
        return eventMap != null ? eventMap.get(eventName) : null;
    }
    
    @Override
    public List<Event> getEventsByDate(String date, int userId) {
        Map<String, List<Event>> dateToEventsMap = userDateToEventsMaps.get(userId);
        return dateToEventsMap != null ? dateToEventsMap.get(date) : new ArrayList<>();
    }
    
    @Override
    public Map<String, Integer> getEventCountByCategory(int userId) {
        try {
            return databaseHelper.getEventCountByCategory(userId);
        } catch (Exception e) {
            Log.e(TAG, "Error getting event count by category", e);
            return new HashMap<>();
        }
    }
    
    private void preprocessEventsForUser(int userId, List<Event> events) {
        PriorityQueue<Event> eventQueue = new PriorityQueue<>();
        Map<String, Event> eventMap = new HashMap<>();
        Map<String, List<Event>> dateToEventsMap = new HashMap<>();
        
        events.forEach(event -> {
            eventQueue.offer(event);
            eventMap.put(event.getName(), event);
            dateToEventsMap.computeIfAbsent(event.getDate(), k -> new ArrayList<>()).add(event);
        });
        
        userEventQueues.put(userId, eventQueue);
        userEventMaps.put(userId, eventMap);
        userDateToEventsMaps.put(userId, dateToEventsMap);
    }
    
    private void refreshUserData(int userId) {
        loadEventsForUser(userId);
    }
    
    private void validateEventData(String eventName, String eventDate) {
        if (eventName == null || eventName.trim().isEmpty()) {
            throw new IllegalArgumentException("Event name cannot be empty");
        }
        if (eventDate == null || eventDate.trim().isEmpty()) {
            throw new IllegalArgumentException("Event date cannot be empty");
        }
        if (!DateTimeUtils.isValidDate(eventDate)) {
            throw new IllegalArgumentException("Invalid event date format");
        }
    }
    
    private boolean eventsOverlap(Event event1, Event event2) {
        return DateTimeUtils.eventsOverlap(event1, event2);
    }
    

} 