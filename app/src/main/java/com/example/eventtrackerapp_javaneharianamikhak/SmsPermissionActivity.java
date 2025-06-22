package com.example.eventtrackerapp_javaneharianamikhak;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Manages the entire lifecycle of the SMS permission for the application.
 * It provides a user interface to explain why the permission is needed, allows the
 * user to grant the permission, and provides a shortcut to system settings to
 * manage or revoke the permission later. The UI dynamically updates based on the
 * current permission status.
 */
public class SmsPermissionActivity extends AppCompatActivity {

    // Permission request code constant
    private static final int SMS_PERMISSION_CODE = 101;
    private TextView textPermissionResult, explanationText;
    private Button buttonRequest, buttonManage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_permission);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("SMS Permissions");
        }

        // Initialize UI components
        explanationText = findViewById(R.id.textSmsExplanation);
        buttonRequest = findViewById(R.id.buttonRequestPermission);
        textPermissionResult = findViewById(R.id.textPermissionResult);
        buttonManage = findViewById(R.id.buttonManagePermission);
        Button buttonDone = findViewById(R.id.buttonDone);

        // Immediately update the UI to reflect the current permission status on load.
        updatePermissionUI();

        // Set up the listener for the "Request Permission" button.
        // This initiates the standard Android permission request dialog.
        buttonRequest.setOnClickListener(v -> {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        });

        // Set up the listener for the "Manage Permission" button.
        // This opens the app's specific settings page in the Android system settings,
        // allowing the user to revoke the permission if they choose.
        buttonManage.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        });

        // The "Done" button simply closes this activity and returns to the previous screen.
        buttonDone.setOnClickListener(v -> finish());
    }

    /**
     * Callback for the result from requesting permissions. This method is invoked for every
     * call on requestPermissions(android.app.Activity, String[], int).
     * @param requestCode The request code passed in requestPermissions(...).
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            // After the user responds to the dialog, update the UI to reflect their choice.
            updatePermissionUI();
        }
    }

    /**
     * Checks the current SEND_SMS permission status and updates the UI accordingly.
     * This method centralizes all UI logic related to permission state. It shows or hides
     * the relevant text and buttons based on whether the permission is granted.
     */
    private void updatePermissionUI() {
        boolean isPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;

        if (isPermissionGranted) {
            // If permission is granted, hide the explanation and request button.
            // Show the "granted" status and the button to manage settings.
            explanationText.setVisibility(View.GONE);
            buttonRequest.setVisibility(View.GONE);
            buttonManage.setVisibility(View.VISIBLE);
            textPermissionResult.setText("Permission granted!");
            textPermissionResult.setVisibility(View.VISIBLE);
        } else {
            // If permission is not granted, show the explanation and request button.
            // Hide the manage button and show the "not granted" status.
            explanationText.setVisibility(View.VISIBLE);
            buttonRequest.setVisibility(View.VISIBLE);
            buttonManage.setVisibility(View.GONE);
            textPermissionResult.setText("Permission not granted. SMS notifications will be disabled.");
            textPermissionResult.setVisibility(View.VISIBLE);
        }
    }
}
