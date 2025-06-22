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
 * MainActivity serves as the primary entry point for the application, handling user
 * authentication through a login and registration screen. It validates user input and
 * interacts with the DatabaseHelper to verify credentials or create new user accounts.
 * Upon successful authentication, it navigates the user to their event overview screen.
 */
public class MainActivity extends AppCompatActivity {

    // UI Components for the login and registration form
    private EditText usernameText, passwordText;
    private Button buttonLogin, buttonRegister;
    private TextView textFeedback;

    /**
     * Initializes the activity, sets up the user interface, and wires up event listeners.
     * This method is called when the activity is first created. It's responsible for
     * finding UI elements by their ID, setting the title, and defining the behavior
     * for user interactions like text input and button clicks.
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down, this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     *                           Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("EventTrackerApp -- JavanehAianaMikhak");
        }

        // Initialize UI components
        usernameText = findViewById(R.id.editTextUsername);
        passwordText = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);
        textFeedback = findViewById(R.id.textFeedback);

        // A TextWatcher is used to monitor the username and password fields.
        // It enables the login button only when both fields contain text, preventing
        // the user from attempting to log in with empty credentials.
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

        // Attach the watcher to both input fields to monitor them for changes.
        usernameText.addTextChangedListener(loginWatcher);
        passwordText.addTextChangedListener(loginWatcher);

        // Handles the login process when the "LOG IN" button is clicked.
        // It retrieves the username and password, validates them against the database,
        // and navigates to the EventsOverviewActivity upon success.
        buttonLogin.setOnClickListener(v -> {
            String username = usernameText.getText().toString().trim();
            String password = passwordText.getText().toString().trim();

            // Ensure fields are not empty before querying the database.
            if (username.isEmpty() || password.isEmpty()) {
                textFeedback.setText("Please enter both username and password.");
                return;
            }

            DatabaseHelper db = new DatabaseHelper(this);
            boolean isValidUser = db.checkUser(username, password);

            if (isValidUser) {
                // If the user is valid, retrieve their ID and pass it to the next activity.
                int userId = db.getUserId(username);
                Intent intent = new Intent(MainActivity.this, EventsOverviewActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("username", username);
                startActivity(intent);
            } else {
                // Provide feedback if credentials do not match.
                textFeedback.setText("Incorrect credentials.");
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
        });
    }
}
