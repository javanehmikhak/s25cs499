package com.example.eventtrackerapp_javaneharianamikhak;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages all database operations for the Event Tracker application. This includes
 * creating the database and tables, as well as handling all CRUD (Create, Read,
 * Update, Delete) operations for both users and their events. It uses SQLite,
 * Android's built-in database engine.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    // --- Database Constants ---
    private static final String DATABASE_NAME = "eventtracker.db";
    private static final int DATABASE_VERSION = 2; // Incremented to trigger onUpgrade

    // --- Table and Column Definitions ---

    // Users table: Stores user credentials and contact info.
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id"; // Primary Key
    public static final String COLUMN_USERNAME = "username"; // Must be unique
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_USER_PHONE = "phone"; // User's phone number for SMS

    // Events table: Stores event details, linked to a user.
    public static final String TABLE_EVENTS = "events";
    public static final String COLUMN_EVENT_ID = "id"; // Primary Key
    public static final String COLUMN_EVENT_NAME = "name";
    public static final String COLUMN_EVENT_DATE = "date";
    public static final String COLUMN_EVENT_USER_ID = "user_id"; // Foreign Key to users table

    /**
     * Constructs a new DatabaseHelper.
     * @param context The context of the application.
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of tables should happen.
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create the users table with a unique constraint on username
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "(" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_USERNAME + " TEXT UNIQUE," +
                COLUMN_PASSWORD + " TEXT," +
                COLUMN_USER_PHONE + " TEXT" + ")"; // Phone number is optional (TEXT)
        db.execSQL(CREATE_USERS_TABLE);

        // SQL statement to create the events table
        String CREATE_EVENTS_TABLE = "CREATE TABLE " + TABLE_EVENTS + "(" +
                COLUMN_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_EVENT_NAME + " TEXT," +
                COLUMN_EVENT_DATE + " TEXT," +
                COLUMN_EVENT_USER_ID + " INTEGER" + ")"; // Foreign key relation
        db.execSQL(CREATE_EVENTS_TABLE);
    }

    /**
     * Called when the database needs to be upgraded. This method will drop the old tables
     * and recreate them. This is a simple approach for this app; a real-world app
     * would migrate data instead of dropping it.
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if they exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        // Create tables again
        onCreate(db);
    }

    // --- User Management Methods ---

    /**
     * Adds a new user to the database.
     * @param username The user's desired username.
     * @param password The user's chosen password.
     * @return true if the user was added successfully, false otherwise (e.g., username exists).
     */
    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    /**
     * Checks if a user with the given credentials exists in the database.
     * @param username The username to check.
     * @param password The password to verify.
     * @return true if a matching user is found, false otherwise.
     */
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /**
     * Retrieves a user's ID based on their username.
     * @param username The username to look up.
     * @return The integer ID of the user, or -1 if not found.
     */
    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_USER_ID + " FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + "=?",
                new String[]{username});
        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
        }
        cursor.close();
        return userId;
    }

    /**
     * Retrieves a user's phone number based on their user ID.
     * @param userId The user's ID.
     * @return The phone number as a String, or null if not found or not set.
     */
    public String getUserPhoneNumber(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_USER_PHONE + " FROM " + TABLE_USERS + " WHERE " + COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)});
        String phoneNumber = null;
        if (cursor.moveToFirst()) {
            phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PHONE));
        }
        cursor.close();
        return phoneNumber;
    }

    /**
     * Updates the phone number for a specific user.
     * @param userId The ID of the user to update.
     * @param phoneNumber The new phone number.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateUserPhoneNumber(int userId, String phoneNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_PHONE, phoneNumber);
        int result = db.update(TABLE_USERS, values, COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)});
        return result > 0;
    }

    // --- Event Management Methods ---

    /**
     * Adds a new event for a specific user.
     * @param name The name or description of the event.
     * @param date The date of the event.
     * @param userId The ID of the user who owns this event.
     * @return true if the event was added successfully, false otherwise.
     */
    public boolean addEvent(String name, String date, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_NAME, name);
        values.put(COLUMN_EVENT_DATE, date);
        values.put(COLUMN_EVENT_USER_ID, userId);
        long result = db.insert(TABLE_EVENTS, null, values);
        return result != -1;
    }

    /**
     * Retrieves all events associated with a specific user ID.
     * @param userId The ID of the user whose events are to be retrieved.
     * @return A List of Event objects. The list will be empty if the user has no events.
     */
    public List<Event> getAllEvents(int userId) {
        List<Event> events = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_EVENTS + " WHERE " + COLUMN_EVENT_USER_ID + "=?",
                new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EVENT_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_NAME));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_DATE));
                events.add(new Event(id, name, date));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return events;
    }

    /**
     * Updates an existing event in the database.
     * @param id The ID of the event to update.
     * @param name The new name for the event.
     * @param date The new date for the event.
     * @return true if the record was updated successfully, false otherwise.
     */
    public boolean updateEvent(int id, String name, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_NAME, name);
        values.put(COLUMN_EVENT_DATE, date);
        int result = db.update(TABLE_EVENTS, values, COLUMN_EVENT_ID + "=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    /**
     * Deletes an event from the database.
     * @param id The ID of the event to delete.
     * @return true if the record was deleted successfully, false otherwise.
     */
    public boolean deleteEvent(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_EVENTS, COLUMN_EVENT_ID + "=?", new String[]{String.valueOf(id)});
        return result > 0;
    }
} 