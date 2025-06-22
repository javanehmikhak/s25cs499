package com.example.eventtrackerapp_javaneharianamikhak;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

import java.util.Calendar;
import android.app.DatePickerDialog;

/**
 * Provides a user interface for adding a new event or editing an existing one.
 * This activity operates in two modes: "add" and "edit". In "edit" mode, it
 * populates the fields with existing event data and updates the record upon saving.
 * In "add" mode, it creates a new event record.
 */
public class AddEventActivity extends AppCompatActivity {

    // --- UI and Data Members ---
    private EditText editTextEventName, editTextEventDate;
    private Button buttonSaveEvent, buttonCancel;
    private TextView textFormHeader; // Displays "Create Event" or "Update Event"
    private DatabaseHelper db;
    private boolean editMode = false; // Flag to determine if in "edit" or "add" mode
    private int eventId = -1; // ID of the event being edited, -1 if in "add" mode
    private int userId; // ID of the user who owns the event

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        db = new DatabaseHelper(this);
        userId = getIntent().getIntExtra("userId", -1);
        editTextEventName = findViewById(R.id.editTextEventName);
        editTextEventDate = findViewById(R.id.editTextEventDate);
        buttonSaveEvent = findViewById(R.id.buttonSaveEvent);
        buttonCancel = findViewById(R.id.buttonCancel);
        textFormHeader = findViewById(R.id.textFormHeader);

        // Set an OnClickListener on the date field to show a DatePickerDialog.
        // This provides a user-friendly way to select a date.
        editTextEventDate.setOnClickListener(v -> showDatePickerDialog());

        // --- Mode Detection (Add vs. Edit) ---
        // Check the intent for an "editMode" flag. This determines the activity's behavior.
        editMode = getIntent().getBooleanExtra("editMode", false);
        if (getSupportActionBar() != null) {
            // Set the title of the ActionBar based on the current mode.
            getSupportActionBar().setTitle("EventTrackerApp -- JavanehAianaMikhak");
        }

        // If in edit mode, retrieve the existing event's details from the intent
        // and populate the input fields. Otherwise, set the header for creating.
        eventId = getIntent().getIntExtra("eventId", -1);
        if (editMode && eventId != -1) {
            textFormHeader.setText("Update Event"); // Set header for updating
            String eventName = getIntent().getStringExtra("eventName");
            String eventDate = getIntent().getStringExtra("eventDate");
            editTextEventName.setText(eventName);
            editTextEventDate.setText(eventDate);
            buttonSaveEvent.setText("Update Event"); // Change button text for clarity
        } else {
            textFormHeader.setText("Create Event"); // Set header for creating
        }

        // --- Button Click Listeners ---

        // Set up the listener for the "Save" or "Update" button.
        buttonSaveEvent.setOnClickListener(v -> {
            String eventName = editTextEventName.getText().toString().trim();
            String eventDate = editTextEventDate.getText().toString().trim();

            // Validate that both fields are filled before proceeding.
            if (eventName.isEmpty() || eventDate.isEmpty()) {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success;
            // If in edit mode, update the existing event. Otherwise, add a new one.
            if (editMode && eventId != -1) {
                success = db.updateEvent(eventId, eventName, eventDate);
            } else {
                success = db.addEvent(eventName, eventDate, userId);
            }

            if (success) {
                // If the database operation was successful, check for SMS permission.
                if (hasSmsPermission()) {
                    String phoneNumber = db.getUserPhoneNumber(userId);
                    String message;
                    if (editMode) {
                        message = "Your event '" + eventName + "' has been updated.";
                    } else {
                        message = "A new event '" + eventName + "' has been added to your calendar.";
                    }
                    // Send the SMS and show a confirmation toast.
                    SmsSender.sendSms(phoneNumber, message);
                    Toast.makeText(this, "Event saved! SMS notification sent.", Toast.LENGTH_SHORT).show();
                } else {
                    // If no SMS permission, just show a simple confirmation.
                    Toast.makeText(this, "Event saved successfully.", Toast.LENGTH_SHORT).show();
                }
                setResult(RESULT_OK); // Signal that the data was changed.
                finish(); // Return to the previous screen.
            } else {
                Toast.makeText(this, "Error saving event", Toast.LENGTH_SHORT).show();
            }
        });

        // The cancel button simply closes the activity without saving changes.
        buttonCancel.setOnClickListener(v -> finish());
    }

    /**
     * Creates and displays a DatePickerDialog, allowing the user to pick a date easily.
     * The selected date is then formatted and set into the event date EditText.
     */
    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    // Format the selected date and update the EditText field.
                    String selectedDate = (monthOfYear + 1) + "/" + dayOfMonth + "/" + year1;
                    editTextEventDate.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
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
