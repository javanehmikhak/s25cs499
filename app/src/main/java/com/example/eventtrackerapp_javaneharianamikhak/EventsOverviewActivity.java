/*
 * Events overview screen.
 * Displays user's events with MVVM architecture.
 * Implements efficient data structures for event management.
 */
package com.example.eventtrackerapp_javaneharianamikhak;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;

import java.util.List;
import java.util.ArrayList;

/**
 * Main events display screen for EventTrackerApp.
 * 
 * This activity serves as the primary interface after user authentication, providing comprehensive
 * event management functionality. It implements MVVM architecture with EventViewModel and LiveData
 * for reactive UI updates and efficient data handling.
 * 
 * Features:
 * - Display user events with sorting and filtering capabilities
 * - Add new events with AI-powered title suggestions
 * - Edit and delete existing events with confirmation
 * - SMS notifications for event changes (with permission)
 * - CSV export functionality for data backup
 * - User profile and settings management
 * 
 * Architecture: 
 * - MVVM pattern with EventViewModel for business logic
 * - LiveData for reactive UI updates
 * - Repository pattern with DatabaseHelper
 * - Event-driven design with callback interfaces
 * 
 * Performance: Uses RecyclerView with efficient adapters and optimized data structures
 * including PriorityQueue for event scheduling and HashMap for fast lookups.
 * 
 * @author Ariana Mikhak
 * @version 1.0
 * @since 2025
 */
public class EventsOverviewActivity extends AppCompatActivity {

    // --- UI Components and Core Data ---
    private RecyclerView recyclerViewEvents; // The list view for events
    private EventAdapter eventAdapter; // The adapter to bind event data to the list
    private EventViewModel eventViewModel; // ViewModel for managing event data
    private DatabaseHelper db; // Helper class for database interactions (for SMS)
    private int userId; // The ID of the currently logged-in user
    private String username; // The username of the currently logged-in user
    private TextView textUpcomingEvents; // ENHANCEMENT: Shows upcoming events summary

    // --- Activity Request Codes ---
    private static final int ADD_EVENT_REQUEST = 1;
    private static final int EDIT_EVENT_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_overview);

        // Initialize ViewModel and database helper
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
        db = new DatabaseHelper(this);
        userId = getIntent().getIntExtra("userId", -1);
        username = getIntent().getStringExtra("username");

        // Set up MaterialToolbar with personalized title
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (username != null && !username.isEmpty()) {
            toolbar.setTitle(username.toUpperCase() + "'S EVENTS");
        } else {
            toolbar.setTitle("Upcoming Events");
        }

        // Initialize RecyclerView
        recyclerViewEvents = findViewById(R.id.recyclerViewEvents);
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));
        
        // ENHANCEMENT: Initialize upcoming events summary
        textUpcomingEvents = findViewById(R.id.textUpcomingEvents);
        
        // ENHANCEMENT: Set up sort button
        findViewById(R.id.buttonSort).setOnClickListener(v -> showSortOptionsDialog());
        
        // ENHANCEMENT: Set up empty state
        setupEmptyState();

        // Initialize the EventAdapter with callbacks
        eventAdapter = new EventAdapter(null, // Will be set by LiveData observer
            // 1. Delete Callback: Invoked when the "Delete" button is clicked for an event.
            event -> {
                String eventName = event.getName(); // Get name before deleting
                eventViewModel.deleteEvent(event.getId(), userId);

                // Show a confirmation Toast and send SMS if permissions are enabled.
                if (hasSmsPermission()) {
                    String phoneNumber = db.getUserPhoneNumber(userId);
                    String message = "Your event '" + eventName + "' has been deleted.";
                    SmsSender.sendSms(phoneNumber, message);
                    Toast.makeText(EventsOverviewActivity.this, "Event deleted! SMS notification sent.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EventsOverviewActivity.this, "Event deleted successfully.", Toast.LENGTH_SHORT).show();
                }
            },
            // 2. Edit Callback: Invoked when the "Edit" button is clicked for an event.
            event -> {
                // Launch AddEventActivity in "edit mode", passing the event's details.
                Intent intent = new Intent(EventsOverviewActivity.this, AddEventActivity.class);
                intent.putExtra("editMode", true);
                intent.putExtra("eventId", event.getId());
                intent.putExtra("eventName", event.getName());
                intent.putExtra("eventDate", event.getDate());
                intent.putExtra("eventTime", event.getTime());
                intent.putExtra("userId", userId);
                startActivityForResult(intent, EDIT_EVENT_REQUEST);
            }
        );
        recyclerViewEvents.setAdapter(eventAdapter);

        // Set up LiveData observers
        setupObservers();

        // Load events using ViewModel
        if (userId != -1) {
            eventViewModel.loadUserEvents(userId);
        }



        // Set up the Floating Action Button for adding a new event.
        // Launches AddEventActivity in "add mode".
        findViewById(R.id.fabAddEvent).setOnClickListener(v -> {
            Intent intent = new Intent(EventsOverviewActivity.this, AddEventActivity.class);
            intent.putExtra("userId", userId);
            startActivityForResult(intent, ADD_EVENT_REQUEST);
        });

        // Set up the Floating Action Button for the settings menu.
        // It displays a PopupMenu with options for "Profile", "SMS Preferences", and "Logout".
        findViewById(R.id.fabMenu).setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(EventsOverviewActivity.this, v);
            popup.getMenuInflater().inflate(R.menu.settings_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_logout) {
                    // On logout, clear the activity stack and return to the login screen.
                    Intent intent = new Intent(EventsOverviewActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.menu_sms_prefs) {
                    // Navigate to the SMS permission management screen.
                    Intent intent = new Intent(EventsOverviewActivity.this, SmsPermissionActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.menu_profile) {
                    // Navigate to the Profile screen to manage phone number.
                    Intent intent = new Intent(EventsOverviewActivity.this, ProfileActivity.class);
                    intent.putExtra("USER_ID", userId);
                    startActivity(intent);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    /**
     * Sets up LiveData observers for reactive UI updates.
     */
    private void setupObservers() {
        // Observe events list changes
        eventViewModel.getEvents().observe(this, new Observer<List<Event>>() {
            @Override
            public void onChanged(List<Event> events) {
                if (events != null) {
                    eventAdapter.updateEvents(events);
                    // ENHANCEMENT: Update upcoming events summary using PriorityQueue
                    updateUpcomingEventsSummary();
                }
            }
        });

        // Observe loading state
        eventViewModel.getIsLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                // Could show/hide loading indicator here if needed
            }
        });

        // Observe error messages
        eventViewModel.getErrorMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String errorMessage) {
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    Toast.makeText(EventsOverviewActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Handles the result returned from other activities, specifically AddEventActivity.
     * If an event was successfully added or edited (indicated by RESULT_OK), this
     * method triggers a refresh of the event list to display the changes.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == ADD_EVENT_REQUEST || requestCode == EDIT_EVENT_REQUEST) && resultCode == RESULT_OK) {
            // Refresh events using ViewModel
            if (userId != -1) {
                eventViewModel.loadUserEvents(userId);
            }
        }
    }

    /**
     * Checks if the app has been granted the SEND_SMS permission.
     * @return true if permission is granted, false otherwise.
     */
    private boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * ENHANCEMENT: Updates the upcoming events summary using PriorityQueue data.
     */
    private void updateUpcomingEventsSummary() {
        if (textUpcomingEvents == null) return;
        
        List<Event> upcomingEvents = eventViewModel.getUpcomingEvents();
        Event nextEvent = eventViewModel.getNextScheduledEvent();
        
        StringBuilder summary = new StringBuilder();
        summary.append("📅 Upcoming Events (Next 7 Days): ").append(upcomingEvents.size()).append("\n");
        
        if (nextEvent != null) {
            summary.append("⏰ Next: ").append(nextEvent.getName()).append(" on ").append(nextEvent.getDate());
            if (nextEvent.getTime() != null && !nextEvent.getTime().isEmpty()) {
                summary.append(" at ").append(nextEvent.getTime());
            }
        } else {
            summary.append("No upcoming events");
        }
        
        textUpcomingEvents.setText(summary.toString());
        textUpcomingEvents.setVisibility(upcomingEvents.isEmpty() ? View.GONE : View.VISIBLE);
    }

    /**
     * ENHANCEMENT: Sets up empty state messaging for better UX.
     */
    private void setupEmptyState() {
        // This would typically involve setting up a TextView or other view
        // to show when the RecyclerView is empty
        // For now, we'll handle this in the adapter observer
    }



    /**
     * ENHANCEMENT: Shows a dialog with sorting options for events.
     */
    private void showSortOptionsDialog() {
        android.util.Log.d("EventsOverviewActivity", "showSortOptionsDialog called!");
        String[] sortOptions = {
            "Date (Earliest First)",
            "Date (Latest First)", 
            "Name (A-Z)",
            "Name (Z-A)",
            "Time (Earliest First)",
            "Time (Latest First)"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort Events")
               .setItems(sortOptions, (dialog, which) -> {
                   android.util.Log.d("EventsOverviewActivity", "Sort option selected: " + which);
                   sortEvents(which);
               })
               .setNegativeButton("Cancel", null)
               .show();
    }

    /**
     * ENHANCEMENT: Sorts events based on the selected option.
     * @param sortOption The index of the selected sorting option
     */
    private void sortEvents(int sortOption) {
        List<Event> currentEvents = eventViewModel.getEvents().getValue();
        if (currentEvents == null || currentEvents.isEmpty()) {
            Toast.makeText(this, "No events to sort", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Event> sortedEvents = new ArrayList<>(currentEvents);
        
        switch (sortOption) {
            case 0: // Date (Earliest First)
                sortedEvents.sort(Event.BY_DATE);
                break;
            case 1: // Date (Latest First)
                sortedEvents.sort(Event.BY_DATE.reversed());
                break;
            case 2: // Name (A-Z)
                sortedEvents.sort(Event.BY_NAME);
                break;
            case 3: // Name (Z-A)
                sortedEvents.sort(Event.BY_NAME.reversed());
                break;
            case 4: // Time (Earliest First)
                sortedEvents.sort((e1, e2) -> {
                    String time1 = e1.getTime() != null ? e1.getTime() : "";
                    String time2 = e2.getTime() != null ? e2.getTime() : "";
                    return time1.compareTo(time2);
                });
                break;
            case 5: // Time (Latest First)
                sortedEvents.sort((e1, e2) -> {
                    String time1 = e1.getTime() != null ? e1.getTime() : "";
                    String time2 = e2.getTime() != null ? e2.getTime() : "";
                    return time2.compareTo(time1);
                });
                break;
        }

        eventAdapter.updateEvents(sortedEvents);
        Toast.makeText(this, "Events sorted successfully", Toast.LENGTH_SHORT).show();
    }
}
