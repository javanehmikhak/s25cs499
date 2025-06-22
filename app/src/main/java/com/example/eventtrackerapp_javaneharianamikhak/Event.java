package com.example.eventtrackerapp_javaneharianamikhak;

/**
 * Represents the data model for a single event. This is a plain old Java object (POJO)
 * that holds the properties of an event, including its unique ID, name, and date.
 * It is used to pass event data between different parts of the application, such as the
 * database helper and the UI adapters.
 */
public class Event {
    private final int id;
    private final String name;
    private final String date;

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
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDate() { return date; }
} 