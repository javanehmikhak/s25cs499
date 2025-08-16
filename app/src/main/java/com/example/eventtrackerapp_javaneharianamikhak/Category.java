/*
 * Category class for EventTrackerApp.
 * Represents event categories for database normalization.
 */
package com.example.eventtrackerapp_javaneharianamikhak;

/**
 * Represents a category for events in the Event Tracker application.
 * This class supports database normalization by separating category
 * information from event data.
 */
public class Category {
    private int id;
    private String name;
    private String color;
    private int userId;

    /**
     * Constructs a new Category with all fields.
     * @param id The unique identifier for the category.
     * @param name The name of the category.
     * @param color The color code for the category (hex format).
     * @param userId The ID of the user who owns this category.
     */
    public Category(int id, String name, String color, int userId) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.userId = userId;
    }

    /**
     * Constructs a new Category without an ID (for new categories).
     * @param name The name of the category.
     * @param color The color code for the category (hex format).
     * @param userId The ID of the user who owns this category.
     */
    public Category(String name, String color, int userId) {
        this(0, name, color, userId);
    }

    /**
     * Gets the category ID.
     * @return The category ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the category ID.
     * @param id The category ID to set.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the category name.
     * @return The category name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the category name.
     * @param name The category name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the category color.
     * @return The category color in hex format.
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the category color.
     * @param color The category color to set (hex format).
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Gets the user ID who owns this category.
     * @return The user ID.
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Sets the user ID who owns this category.
     * @param userId The user ID to set.
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Returns a string representation of the category.
     * @return A string containing the category name.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Checks if this category equals another object.
     * @param obj The object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Category category = (Category) obj;
        return id == category.id && userId == category.userId &&
               java.util.Objects.equals(name, category.name) &&
               java.util.Objects.equals(color, category.color);
    }

    /**
     * Generates a hash code for this category.
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, name, color, userId);
    }
} 