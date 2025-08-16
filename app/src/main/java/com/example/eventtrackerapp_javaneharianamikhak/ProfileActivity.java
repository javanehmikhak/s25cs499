/*
 * Profile management screen.
 * Allows users to update their phone number for SMS notifications.
 *
 * ENHANCEMENT: Added CSV export functionality for database enhancement.
 */
package com.example.eventtrackerapp_javaneharianamikhak;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private EditText editTextPhoneNumber;
    private Button buttonSavePhoneNumber;
    private Button buttonCancelProfile;
    private Button buttonExportEvents; // ENHANCEMENT: CSV export button
    private DatabaseHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);
        userId = getIntent().getIntExtra("USER_ID", -1);

        if (userId == -1) {
            Toast.makeText(this, "Error: User not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        buttonSavePhoneNumber = findViewById(R.id.buttonSavePhoneNumber);
        buttonCancelProfile = findViewById(R.id.buttonCancelProfile);
        buttonExportEvents = findViewById(R.id.buttonExportEvents); // ENHANCEMENT: CSV export

        loadPhoneNumber();

        buttonSavePhoneNumber.setOnClickListener(v -> savePhoneNumber());
        buttonCancelProfile.setOnClickListener(v -> finish());
        buttonExportEvents.setOnClickListener(v -> exportEventsToCSV()); // ENHANCEMENT: CSV export
    }

    private void loadPhoneNumber() {
        String phoneNumber = dbHelper.getUserPhoneNumber(userId);
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            editTextPhoneNumber.setText(phoneNumber);
        }
    }

    private void savePhoneNumber() {
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "Phone number cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.updateUserPhoneNumber(userId, phoneNumber)) {
            Toast.makeText(this, "Phone number saved successfully.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save phone number.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ENHANCEMENT: Exports user events to CSV file and shares it.
     */
    private void exportEventsToCSV() {
        try {
            // Create CSV content
            String csvContent = dbHelper.getEventsCSVSummary(userId);
            
            // Create file in app's external files directory
            File exportDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "exports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            
            File csvFile = new File(exportDir, "events_export.csv");
            FileWriter writer = new FileWriter(csvFile);
            writer.write(csvContent);
            writer.close();
            
            // Share the file
            Uri fileUri = FileProvider.getUriForFile(this, 
                getApplicationContext().getPackageName() + ".fileprovider", csvFile);
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Event Tracker Export");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Here's your event data export from Event Tracker App.");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(Intent.createChooser(shareIntent, "Share Events Export"));
            
            Toast.makeText(this, "Events exported successfully!", Toast.LENGTH_SHORT).show();
            
        } catch (IOException e) {
            Toast.makeText(this, "Failed to export events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
} 