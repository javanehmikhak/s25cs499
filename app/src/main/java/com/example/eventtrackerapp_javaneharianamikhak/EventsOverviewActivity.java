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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import java.util.List;

/**
 * Displays a list of events for the currently logged-in user. This activity serves as the
 * main screen after login, providing core functionalities like viewing, adding, editing, and
 * deleting events. It also provides navigation to SMS permission settings and a logout option.
 */
public class EventsOverviewActivity extends AppCompatActivity {

    // --- UI Components and Core Data ---
    private RecyclerView recyclerViewEvents; // The list view for events
    private EventAdapter eventAdapter; // The adapter to bind event data to the list
    private List<Event> eventList; // The list of event objects for the current user
    private DatabaseHelper db; // Helper class for database interactions
    private int userId; // The ID of the currently logged-in user
    private String username; // The username of the currently logged-in user

    // --- Activity Request Codes ---
    private static final int ADD_EVENT_REQUEST = 1;
    private static final int EDIT_EVENT_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_overview);

        db = new DatabaseHelper(this);
        userId = getIntent().getIntExtra("userId", -1);
        username = getIntent().getStringExtra("username");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("EventTrackerApp -- JavanehAianaMikhak");
        }

        // Set a personalized header with the user's name
        TextView textHeader = findViewById(R.id.textHeader);
        if (username != null && !username.isEmpty()) {
            textHeader.setText(username.toUpperCase() + "'S EVENTS");
        }

        recyclerViewEvents = findViewById(R.id.recyclerViewEvents);
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));

        // Load all events for the current user from the database
        eventList = db.getAllEvents(userId);

        // Initialize the EventAdapter. The adapter is provided with two lambda functions
        // to handle callbacks for delete and edit actions from within the list items.
        eventAdapter = new EventAdapter(eventList,
            // 1. Delete Callback: Invoked when the "Delete" button is clicked for an event.
            event -> {
                db.deleteEvent(event.getId());
                refreshEvents(); // Refresh the list to show the event has been removed.
                // Show a confirmation Toast if SMS permissions are enabled.
                if (hasSmsPermission()) {
                    Toast.makeText(EventsOverviewActivity.this, "Event deleted! SMS sent.", Toast.LENGTH_SHORT).show();
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
                intent.putExtra("userId", userId);
                startActivityForResult(intent, EDIT_EVENT_REQUEST);
            }
        );
        recyclerViewEvents.setAdapter(eventAdapter);

        // Set up the Floating Action Button for adding a new event.
        // Launches AddEventActivity in "add mode".
        findViewById(R.id.fabAddEvent).setOnClickListener(v -> {
            Intent intent = new Intent(EventsOverviewActivity.this, AddEventActivity.class);
            intent.putExtra("userId", userId);
            startActivityForResult(intent, ADD_EVENT_REQUEST);
        });

        // Set up the Floating Action Button for the settings menu.
        // It displays a PopupMenu with options for "Logout" and "SMS Preferences".
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
                }
                return false;
            });
            popup.show();
        });
    }

    /**
     * Refreshes the event list by querying the database for the latest data
     * and notifying the adapter of the change.
     */
    private void refreshEvents() {
        eventList = db.getAllEvents(userId);
        eventAdapter.updateEvents(eventList);
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
            refreshEvents();
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
}
