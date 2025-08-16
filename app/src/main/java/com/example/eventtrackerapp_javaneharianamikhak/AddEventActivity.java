/*
 * Add/Edit event screen.
 * Uses MVVM pattern with ViewModel for data management.
 * Includes event title suggestions and autocomplete.
 */
package com.example.eventtrackerapp_javaneharianamikhak;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

import java.util.Calendar;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;


/**
 * Add or edit events.
 * Two modes: add new event or edit existing one.
 * Uses MVVM pattern with ViewModel.
 */
public class AddEventActivity extends AppCompatActivity {

    private EditText editTextEventName, editTextEventDate, editTextEventTime;
    private Button buttonSaveEvent, buttonCancel;
    private TextView textFormHeader;
    private TextView textSuggestionText;
    private EventViewModel eventViewModel;
    private boolean editMode = false;
    private int eventId = -1;
    private int userId;
    private List<Event> existingEvents = new ArrayList<>();
    private String currentSuggestion = null;
    private boolean isAcceptingSuggestion = false;
    private final java.util.Stack<String> undoStack = new java.util.Stack<>();
    
    // UI timing constants
    private static final int SUGGESTION_RESET_DELAY_MS = 100;
    private static final int MIN_SUGGESTION_LENGTH = 2;
    private static final int LOADING_TIMEOUT_MS = 10000;
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("h:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
        userId = getIntent().getIntExtra("userId", -1);

        initializeUI();
        setupObservers();

        if (userId != -1) {
            eventViewModel.loadUserEvents(userId);
            // Delay AI suggestion to ensure UI is ready
            editTextEventName.post(() -> {
                if (!isFinishing()) {
            eventViewModel.generateEventTitleSuggestion(userId);
                }
            });
        }

        setupEventListeners();



        editMode = getIntent().getBooleanExtra("editMode", false);
        eventId = getIntent().getIntExtra("eventId", -1);
        
        if (editMode && eventId != -1) {
            textFormHeader.setText("Update Event");
            String eventName = getIntent().getStringExtra("eventName");
            String eventDate = getIntent().getStringExtra("eventDate");
            String eventTime = getIntent().getStringExtra("eventTime");
            editTextEventName.setText(eventName);
            editTextEventDate.setText(eventDate);
            if (eventTime != null && !eventTime.isEmpty()) {
                editTextEventTime.setText(eventTime);
            }
            buttonSaveEvent.setText("Update Event"); // Change button text for clarity
        } else {
            textFormHeader.setText("Create Event"); // Set header for creating
            // Force show AI suggestion for new events
            textSuggestionText.setText("💡 Loading AI suggestion...");
            textSuggestionText.setVisibility(android.view.View.VISIBLE);
        }
    }

    /**
     * Initializes all UI components by finding them by their IDs.
     */
    private void initializeUI() {
        editTextEventName = findViewById(R.id.editTextEventName);
        editTextEventDate = findViewById(R.id.editTextEventDate);
        editTextEventTime = findViewById(R.id.editTextEventTime);
        buttonSaveEvent = findViewById(R.id.buttonSaveEvent);
        buttonCancel = findViewById(R.id.buttonCancel);
        textFormHeader = findViewById(R.id.textFormHeader);
        textSuggestionText = findViewById(R.id.textSuggestionText);
        
        // Ensure time field starts completely empty
        editTextEventTime.setText(null);
    }

    /**
     * Sets up LiveData observers for reactive UI updates.
     */
    private void setupObservers() {
        // Observe events list for autocomplete functionality
        eventViewModel.getEvents().observe(this, new Observer<List<Event>>() {
            @Override
            public void onChanged(List<Event> events) {
                if (events != null) {
                    existingEvents = events;
                }
            }
        });

        // Observe suggested title changes
        eventViewModel.getSuggestedTitle().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String suggestedTitle) {
                try {
                    if (suggestedTitle != null && !editMode && !isFinishing()) {
                    // Set the suggestion as a hint/placeholder instead of pre-filling
                    editTextEventName.setHint("Event Name (suggests: " + suggestedTitle + ")");
                    
                    // Show the suggestion in the card when the field is empty
                    if (editTextEventName.getText().toString().trim().isEmpty()) {
                        textSuggestionText.setText("💡 AI suggests: " + suggestedTitle);
                        textSuggestionText.setVisibility(android.view.View.VISIBLE);
                    }
                    
                        // Don't show toast to avoid UI interference
                        // Toast.makeText(AddEventActivity.this, 
                        //     "💡 Suggests: " + suggestedTitle, Toast.LENGTH_SHORT).show();
                    }
                } catch (RuntimeException e) {
                    // Log error but don't crash the activity
                    android.util.Log.e("AddEventActivity", "Error handling AI suggestion: " + e.getMessage());
                }
            }
        });

        // Observe loading state
        eventViewModel.getIsLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                try {
                    if (isLoading != null && !isFinishing()) {
                        // In edit mode, don't disable the button for loading states
                        if (editMode) {
                            buttonSaveEvent.setEnabled(true);
                        } else {
                    buttonSaveEvent.setEnabled(!isLoading);
                            // Force enable button after a reasonable timeout
                            if (isLoading) {
                                buttonSaveEvent.postDelayed(() -> {
                                    if (!isFinishing()) {
                                        buttonSaveEvent.setEnabled(true);
                                    }
                                }, LOADING_TIMEOUT_MS);
                            }
                        }
                    }
                } catch (RuntimeException e) {
                    android.util.Log.e("AddEventActivity", "Error handling loading state: " + e.getMessage());
                    // Force enable button on error
                    if (!isFinishing()) {
                        buttonSaveEvent.setEnabled(true);
                    }
                }
            }
        });

        // Observe error messages
        eventViewModel.getErrorMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String errorMessage) {
                try {
                    if (errorMessage != null && !errorMessage.isEmpty() && !isFinishing()) {
                    Toast.makeText(AddEventActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (RuntimeException e) {
                    android.util.Log.e("AddEventActivity", "Error handling error message: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Sets up event listeners for user interactions.
     */
    private void setupEventListeners() {
        // Set an OnClickListener on the date field to show a DatePickerDialog.
        // This provides a user-friendly way to select a date.
        editTextEventDate.setOnClickListener(v -> showDatePickerDialog());

        // Set up time picker - users can type or use the picker
        editTextEventTime.setOnClickListener(v -> showTimePickerDialog());

        buttonSaveEvent.setOnClickListener(v -> saveEvent());

        // The cancel button simply closes the activity without saving changes.
        buttonCancel.setOnClickListener(v -> finish());

        // Text change listener for real-time autocomplete suggestions
        editTextEventName.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isAcceptingSuggestion) {
                    handleTextChange(s.toString());
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                // This method is no longer needed since we handle suggestions in handleTextChange
            }
        });

        // Accept suggestions with Tab or Enter
        editTextEventName.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == android.view.KeyEvent.ACTION_DOWN && 
                (keyCode == android.view.KeyEvent.KEYCODE_TAB || keyCode == android.view.KeyEvent.KEYCODE_ENTER)) {
                
                        if (currentSuggestion != null) {
                    acceptSuggestion();
                            if (keyCode == android.view.KeyEvent.KEYCODE_TAB) {
                                        editTextEventDate.requestFocus();
                                    }
                    return true;
                    }
                }
                return false;
        });
    }

    /**
     * Accept current suggestion.
     */
    private void acceptSuggestion() {
        isAcceptingSuggestion = true;
        editTextEventName.setText(currentSuggestion);
        editTextEventName.setSelection(currentSuggestion.length());
        currentSuggestion = null;
        editTextEventName.setHint("Event Name");
        
        editTextEventName.postDelayed(() -> isAcceptingSuggestion = false, SUGGESTION_RESET_DELAY_MS);
    }
    
    /**
     * Handle text changes for suggestions.
     */
    private void handleTextChange(String currentText) {
        String trimmedText = currentText.toLowerCase().trim();
        
        // Save state for undo (only if not empty)
        if (!trimmedText.isEmpty()) {
            undoStack.push(currentText);
        }
        
        if (trimmedText.length() >= MIN_SUGGESTION_LENGTH) {
            // Find matching existing events
            String suggestion = findMatchingEvent(trimmedText);
            
            if (suggestion != null && !suggestion.equalsIgnoreCase(trimmedText)) {
                // Store the current suggestion for autocomplete
                currentSuggestion = suggestion;
                
                // Show autocomplete suggestion in the suggestion MaterialCardView
                textSuggestionText.setText("💡 Press Tab or Enter to complete: " + suggestion);
                textSuggestionText.setVisibility(android.view.View.VISIBLE);
            } else {
                // No match found, clear suggestion and hide suggestion TextView
                currentSuggestion = null;
                textSuggestionText.setVisibility(android.view.View.GONE);
            }
        } else if (trimmedText.length() == 0) {
            // Show AI suggestion when field is empty
            currentSuggestion = null;
            String suggestion = eventViewModel.getSuggestedTitle().getValue();
            if (suggestion != null && !editMode) {
                textSuggestionText.setText("💡 AI suggests: " + suggestion);
                textSuggestionText.setVisibility(android.view.View.VISIBLE);
            } else {
                textSuggestionText.setVisibility(android.view.View.GONE);
            }
        } else {
            // Less than 2 characters, clear suggestion and hide suggestion TextView
            currentSuggestion = null;
            textSuggestionText.setVisibility(android.view.View.GONE);
        }
    }
    
    private void undoLastChange() {
        if (!undoStack.isEmpty()) {
            String previousText = undoStack.pop();
            editTextEventName.setText(previousText);
            editTextEventName.setSelection(previousText.length());
        }
    }

    /**
     * Finds a matching existing event name based on the current input.
     * @param currentText The text the user has typed.
     * @return A matching event name suggestion, or null if no match found.
     */
    private String findMatchingEvent(String currentText) {
        if (currentText.length() < MIN_SUGGESTION_LENGTH) return null;
        
        String lowerText = currentText.toLowerCase();
        
        // Look for exact prefix match first
        return existingEvents.stream()
            .filter(event -> event.getName().toLowerCase().startsWith(lowerText))
            .map(Event::getName)
            .findFirst()
            .orElseGet(() -> 
                // Fallback to partial match
                existingEvents.stream()
                    .filter(event -> event.getName().toLowerCase().contains(lowerText))
                    .map(Event::getName)
                    .findFirst()
                    .orElse(null)
            );
    }

    /**
     * Handles the event saving logic using the ViewModel.
     */
    private void saveEvent() {
        String eventName = editTextEventName.getText().toString().trim();
        String eventDate = editTextEventDate.getText().toString().trim();
        String eventTime = editTextEventTime.getText().toString().trim();

        if (!isValidInput(eventName, eventDate)) return;
        if (isDuplicateEvent(eventName, eventDate)) return;
        
        // ENHANCEMENT: Create temporary event for advanced conflict detection
        Event tempEvent = new Event(-1, eventName, eventDate, eventTime, userId);
        int excludeEventId = editMode ? eventId : -1;
        
        // ENHANCEMENT: Use advanced conflict detection algorithm
        if (eventViewModel.checkForConflicts(tempEvent, excludeEventId)) {
            showAdvancedConflictDialog(eventName, eventDate, eventTime, excludeEventId);
            return;
        }
        
        // Basic conflict check for events on same date
        if (eventViewModel.conflictsExist(eventDate, excludeEventId)) {
            showConflictDialog(eventName, eventDate, eventTime, excludeEventId);
            return;
        }

        saveEventToDatabase(eventName, eventDate, eventTime);
    }
    
    private boolean isValidInput(String eventName, String eventDate) {
        if (eventName.isEmpty() || eventDate.isEmpty()) {
            Toast.makeText(this, "Please fill in event name and date", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
        }

    private boolean isDuplicateEvent(String eventName, String eventDate) {
        if (!editMode && eventViewModel.eventExists(eventName, eventDate)) {
            Toast.makeText(this, "An event with this name and date already exists", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
        }

    private void showConflictDialog(String eventName, String eventDate, String eventTime, int excludeEventId) {
            List<Event> conflictingEvents = eventViewModel.getConflictingEvents(eventDate, excludeEventId);
        String conflictMessage = buildConflictMessage(conflictingEvents, eventDate);
            
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Scheduling Conflict")
            .setMessage(conflictMessage)
            .setPositiveButton("Proceed", (dialog, which) -> saveEventToDatabase(eventName, eventDate, eventTime))
            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
    
    private String buildConflictMessage(List<Event> conflictingEvents, String eventDate) {
        StringBuilder message = new StringBuilder("Scheduling conflict detected! You already have ");
        message.append(conflictingEvents.size()).append(" event(s) on ").append(eventDate).append(":\n");
        
        conflictingEvents.forEach(conflict -> 
            message.append("• ").append(conflict.getName()).append("\n"));
        
        message.append("\nDo you want to proceed anyway?");
        return message.toString();
        }

    /**
     * ENHANCEMENT: Shows advanced conflict dialog for overlapping events.
     */
    private void showAdvancedConflictDialog(String eventName, String eventDate, String eventTime, int excludeEventId) {
        Event tempEvent = new Event(-1, eventName, eventDate, eventTime, userId);
        List<Event> conflictingEvents = eventViewModel.getConflictingEvents(eventDate, excludeEventId);
        
        StringBuilder message = new StringBuilder("⚠️ Time Conflict Detected!\n\n");
        message.append("Your event '").append(eventName).append("' at ").append(eventTime).append(" may overlap with:\n\n");
        
        conflictingEvents.forEach(conflict -> {
            message.append("• ").append(conflict.getName());
            if (conflict.getTime() != null && !conflict.getTime().isEmpty()) {
                message.append(" at ").append(conflict.getTime());
            }
            message.append("\n");
        });
        
        message.append("\nThis could cause scheduling conflicts. Do you want to proceed anyway?");
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Time Overlap Warning")
            .setMessage(message.toString())
            .setPositiveButton("Proceed", (dialog, which) -> saveEventToDatabase(eventName, eventDate, eventTime))
            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    /**
     * Saves the event to the database after conflict checking.
     */
    private void saveEventToDatabase(String eventName, String eventDate, String eventTime) {
        // Use ViewModel to save the event
        if (editMode && eventId != -1) {
            eventViewModel.updateEvent(eventId, eventName, eventDate, eventTime, userId);
        } else {
            eventViewModel.addEvent(eventName, eventDate, eventTime, userId);
        }

        if (hasSmsPermission()) {
            DatabaseHelper db = new DatabaseHelper(this);
            String phoneNumber = db.getUserPhoneNumber(userId);
            String timeInfo = eventTime.isEmpty() ? "" : " at " + eventTime;
            String message = editMode ? 
                "Your event '" + eventName + "' has been updated." :
                "A new event '" + eventName + "' has been added to your calendar for " + eventDate + timeInfo + ".";
            
            SmsSender.sendSms(phoneNumber, message);
            Toast.makeText(this, "Event saved! SMS notification sent.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Event saved successfully.", Toast.LENGTH_SHORT).show();
        }
        
        setResult(RESULT_OK); // Signal that the data was changed.
        finish(); // Return to the previous screen.
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
     * Creates and displays a TimePickerDialog, allowing the user to pick a time easily.
     * The selected time is then formatted and set into the event time EditText.
     */
    private void showTimePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute1) -> {
                    // Format the selected time and update the EditText field.
                    Calendar timeCalendar = Calendar.getInstance();
                    timeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    timeCalendar.set(Calendar.MINUTE, minute1);
                    String selectedTime = TIME_FORMAT.format(timeCalendar.getTime());
                    editTextEventTime.setText(selectedTime);
                }, hour, minute, false); // false for 12-hour format
        timePickerDialog.show();
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

