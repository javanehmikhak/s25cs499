package com.example.eventtrackerapp_javaneharianamikhak;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;


import androidx.appcompat.app.AppCompatActivity;

/**
 * Main authentication screen for EventTrackerApp.
 * 
 * This activity serves as the entry point for the application, providing user authentication
 * functionality including login and registration. It implements a clean separation of concerns
 * by delegating database operations to DatabaseHelper and validation to ValidationUtils.
 * 
 * Features:
 * - User login with username/password validation
 * - New user registration with duplicate username prevention
 * - Real-time input validation with visual feedback
 * - Secure navigation to EventsOverviewActivity upon successful authentication
 * 
 * Architecture: Follows MVVM principles with clear separation between UI and business logic.
 * Security: Implements proper input validation and error handling for user credentials.
 * 
 * @author Ariana Mikhak
 * @version 1.0
 * @since 2025
 */
public class MainActivity extends AppCompatActivity {

    // UI Components for the login and registration form
    private EditText usernameText, passwordText;
    private Button buttonLogin, buttonRegister;
    private TextView textFeedback;

    /**
     * Initialize UI and event listeners.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        usernameText = findViewById(R.id.editTextUsername);
        passwordText = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);
        textFeedback = findViewById(R.id.textFeedback);
        


        TextWatcher loginWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Trim whitespace and check if both fields are non-empty
                boolean bothFieldsAreFilled = !usernameText.getText().toString().trim().isEmpty() &&
                        !passwordText.getText().toString().trim().isEmpty();
                buttonLogin.setEnabled(bothFieldsAreFilled);
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        usernameText.addTextChangedListener(loginWatcher);
        passwordText.addTextChangedListener(loginWatcher);

        buttonLogin.setOnClickListener(v -> {
            String username = usernameText.getText().toString().trim();
            String password = passwordText.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                textFeedback.setText("Please enter both username and password.");
                return;
            }

            try {
                DatabaseHelper db = new DatabaseHelper(this);
                boolean isValidUser = db.checkUser(username, password);

                if (isValidUser) {
                    int userId = db.getUserId(username);
                    Intent intent = new Intent(MainActivity.this, EventsOverviewActivity.class);
                    intent.putExtra("userId", userId);
                    intent.putExtra("username", username);
                    startActivity(intent);
                } else {
                    textFeedback.setText("Incorrect credentials.");
                }
            } catch (IllegalArgumentException e) {
                textFeedback.setText("Invalid credentials format.");
            } catch (RuntimeException e) {
                textFeedback.setText("Database error occurred. Please try again.");
            }
        });

        // Handles the registration process when the "REGISTER NEW ACCOUNT" button is clicked.
        // It attempts to create a new user with the provided credentials and, if successful,
        // logs them in and navigates to the EventsOverviewActivity.
        buttonRegister.setOnClickListener(v -> {
            String username = usernameText.getText().toString().trim();
            String password = passwordText.getText().toString().trim();

            // Ensure fields are not empty before attempting to register.
            if (username.isEmpty() || password.isEmpty()) {
                textFeedback.setText("Please enter both username and password.");
                return;
            }

            try {
                DatabaseHelper db = new DatabaseHelper(this);
                boolean wasUserAdded = db.addUser(username, password);

                if (wasUserAdded) {
                    // On successful registration, immediately log the user in.
                    int userId = db.getUserId(username);
                    textFeedback.setText("Account created for: " + username);
                    Intent intent = new Intent(MainActivity.this, EventsOverviewActivity.class);
                    intent.putExtra("userId", userId);
                    intent.putExtra("username", username);
                    startActivity(intent);
                } else {
                    // Provide feedback if the username is already taken.
                    textFeedback.setText("Registration failed. Username may already exist.");
                }
            } catch (IllegalArgumentException e) {
                textFeedback.setText("Invalid username or password format.");
            } catch (RuntimeException e) {
                textFeedback.setText("Database error occurred. Please try again.");
            }
        });

        // Set up accessibility features after all other UI setup is complete
        setupAccessibility();


    }

    /**
     * Sets up basic accessibility features.
     */
    private void setupAccessibility() {
        // Set initial focus to username field for better accessibility flow
        usernameText.requestFocus();
    }


}
