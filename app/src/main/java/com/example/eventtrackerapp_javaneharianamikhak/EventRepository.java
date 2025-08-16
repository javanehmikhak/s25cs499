package com.example.eventtrackerapp_javaneharianamikhak;

import android.content.Context;
import java.util.List;

/**
 * Repository class that abstracts access to event data sources.
 * Acts as a single source of truth for event data for the ViewModel.
 */
public class EventRepository {
    private final DatabaseHelper dbHelper;

    public EventRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public List<Event> getAllEvents(int userId) {
        return dbHelper.getAllEvents(userId);
    }

    public boolean addEvent(String name, String date, String time, int userId) {
        return dbHelper.addEvent(name, date, time, userId);
    }

    public boolean updateEvent(int id, String name, String date, String time) {
        return dbHelper.updateEvent(id, name, date, time);
    }

    public boolean deleteEvent(int id) {
        return dbHelper.deleteEvent(id);
    }
} 