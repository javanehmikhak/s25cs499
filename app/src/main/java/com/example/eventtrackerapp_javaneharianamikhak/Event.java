package com.example.eventtrackerapp_javaneharianamikhak;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;

/**
 * Represents the data model for a single event. This is a plain old Java object (POJO)
 * that holds the properties of an event, including its unique ID, name, and date.
 * It is used to pass event data between different parts of the application, such as the
 * database helper and the UI adapters.
 * 
 * Enhanced to implement Comparable for PriorityQueue usage and better date handling.
 */
public class Event implements Comparable<Event> {
    private final int id;
    private final String name;
    private final String date;
    private final String time; // Event time (optional)
    private final int userId; // Added for better data management
    private Category category; // Added for database normalization
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy");
    
    // Comparator strategies for different sorting options
    public static final Comparator<Event> BY_NAME = Comparator.comparing(Event::getName);
    public static final Comparator<Event> BY_DATE = Comparator.comparing(Event::getDate);
    public static final Comparator<Event> BY_DAYS_UNTIL = Comparator.comparing(Event::getDaysUntil);
    
    private LocalDate getParsedDate() {
        try {
            return LocalDate.parse(this.date, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Constructs a new Event object.
     * @param id The unique identifier for the event.
     * @param name The name or description of the event.
     * @param date The date of the event as a string.
     */
    public Event(int id, String name, String date) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = null;
        this.userId = -1; // Default value for backward compatibility
    }

    /**
     * Constructs a new Event object with user ID.
     * @param id The unique identifier for the event.
     * @param name The name or description of the event.
     * @param date The date of the event as a string.
     * @param userId The ID of the user who owns this event.
     */
    public Event(int id, String name, String date, int userId) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = null;
        this.userId = userId;
    }

    /**
     * Constructs a new Event object with time.
     * @param id The unique identifier for the event.
     * @param name The name or description of the event.
     * @param date The date of the event as a string.
     * @param time The time of the event as a string (optional).
     * @param userId The ID of the user who owns this event.
     */
    public Event(int id, String name, String date, String time, int userId) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.userId = userId;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public int getUserId() { return userId; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    /**
     * Compares this event with another event based on date for PriorityQueue ordering.
     * Events are ordered by date (earliest first).
     * @param other The event to compare with.
     * @return Negative if this event is earlier, positive if later, 0 if same date.
     */
    @Override
    public int compareTo(Event other) {
        if (other == null) return 1;
        
        LocalDate thisDate = getParsedDate();
        LocalDate otherDate = other.getParsedDate();
        
        if (thisDate != null && otherDate != null) {
            return thisDate.compareTo(otherDate);
        }
        return this.date.compareTo(other.date);
    }

    /**
     * Checks if this event equals another event based on ID.
     * @param obj The object to compare with.
     * @return true if the events have the same ID, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Event event = (Event) obj;
        return id == event.id;
    }

    /**
     * Generates a hash code based on the event ID.
     * @return Hash code for this event.
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    /**
     * Returns a string representation of this event.
     * @return String representation showing event name and date.
     */
    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", userId=" + userId +
                ", category=" + (category != null ? category.getName() : "null") +
                '}';
    }

    /**
     * Checks if this event is in the past.
     * @return true if the event date is before today, false otherwise.
     */
    private LocalDate getTodayDate() {
        return LocalDate.now();
    }
    
    public boolean isPast() {
        LocalDate eventDate = getParsedDate();
        LocalDate today = getTodayDate();
        return eventDate != null && eventDate.isBefore(today);
    }
    
    public boolean isToday() {
        LocalDate eventDate = getParsedDate();
        LocalDate today = getTodayDate();
        return eventDate != null && eventDate.equals(today);
    }

    /**
     * Gets the number of days until this event.
     * @return Number of days until the event (negative if past).
     */
    public int getDaysUntil() {
        LocalDate eventDate = getParsedDate();
        LocalDate today = getTodayDate();
        if (eventDate == null) return Integer.MAX_VALUE;
        
        return (int) java.time.temporal.ChronoUnit.DAYS.between(today, eventDate);
    }
} 