package com.example.eventtrackerapp_javaneharianamikhak;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private EditText editTextPhoneNumber;
    private Button buttonSavePhoneNumber;
    private Button buttonCancelProfile;
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

        loadPhoneNumber();

        buttonSavePhoneNumber.setOnClickListener(v -> savePhoneNumber());
        buttonCancelProfile.setOnClickListener(v -> finish());
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
} 