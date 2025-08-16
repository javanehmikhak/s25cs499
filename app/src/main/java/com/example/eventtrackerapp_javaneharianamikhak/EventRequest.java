package com.example.eventtrackerapp_javaneharianamikhak;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * Immutable value object for event creation/update requests.
 * Ensures data integrity and prevents side effects.
 */
public final class EventRequest {
    
    private final String eventName;
    private final String eventDate;
    private final String eventTime;
    private final int userId;
    private final Integer categoryId;
    
    private EventRequest(Builder builder) {
        this.eventName = builder.eventName;
        this.eventDate = builder.eventDate;
        this.eventTime = builder.eventTime;
        this.userId = builder.userId;
        this.categoryId = builder.categoryId;
    }
    
    public String getEventName() {
        return eventName;
    }
    
    public String getEventDate() {
        return eventDate;
    }
    
    public String getEventTime() {
        return eventTime;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public Integer getCategoryId() {
        return categoryId;
    }
    
    /**
     * Validates the event request data.
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (eventName == null || eventName.trim().isEmpty()) {
            throw new IllegalArgumentException(EventConstants.ERROR_EMPTY_EVENT_NAME);
        }
        if (eventDate == null || eventDate.trim().isEmpty()) {
            throw new IllegalArgumentException(EventConstants.ERROR_EMPTY_EVENT_DATE);
        }
        if (userId == EventConstants.INVALID_USER_ID) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        if (!isValidDate(eventDate)) {
            throw new IllegalArgumentException(EventConstants.ERROR_INVALID_DATE_FORMAT);
        }
    }
    
    private boolean isValidDate(String dateString) {
        try {
            LocalDate.parse(dateString, DateTimeFormatter.ofPattern("M/d/yyyy"));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EventRequest that = (EventRequest) obj;
        return userId == that.userId &&
               Objects.equals(eventName, that.eventName) &&
               Objects.equals(eventDate, that.eventDate) &&
               Objects.equals(eventTime, that.eventTime) &&
               Objects.equals(categoryId, that.categoryId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(eventName, eventDate, eventTime, userId, categoryId);
    }
    
    @Override
    public String toString() {
        return "EventRequest{" +
                "eventName='" + eventName + '\'' +
                ", eventDate='" + eventDate + '\'' +
                ", eventTime='" + eventTime + '\'' +
                ", userId=" + userId +
                ", categoryId=" + categoryId +
                '}';
    }
    
    /**
     * Builder pattern for creating EventRequest instances.
     */
    public static class Builder {
        private String eventName;
        private String eventDate;
        private String eventTime = EventConstants.DEFAULT_EVENT_TIME;
        private int userId = EventConstants.INVALID_USER_ID;
        private Integer categoryId;
        
        public Builder eventName(String eventName) {
            this.eventName = eventName;
            return this;
        }
        
        public Builder eventDate(String eventDate) {
            this.eventDate = eventDate;
            return this;
        }
        
        public Builder eventTime(String eventTime) {
            this.eventTime = eventTime != null ? eventTime : EventConstants.DEFAULT_EVENT_TIME;
            return this;
        }
        
        public Builder userId(int userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder categoryId(Integer categoryId) {
            this.categoryId = categoryId;
            return this;
        }
        
        public EventRequest build() {
            return new EventRequest(this);
        }
    }
} 