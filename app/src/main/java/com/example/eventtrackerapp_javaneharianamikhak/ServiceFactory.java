package com.example.eventtrackerapp_javaneharianamikhak;

import android.content.Context;

/**
 * Factory for creating service instances.
 * Implements dependency injection to reduce coupling.
 */
public class ServiceFactory {
    
    private static ServiceFactory instance;
    private final Context applicationContext;
    
    private ServiceFactory(Context context) {
        this.applicationContext = context.getApplicationContext();
    }
    
    public static synchronized ServiceFactory getInstance(Context context) {
        if (instance == null) {
            instance = new ServiceFactory(context);
        }
        return instance;
    }
    
    /**
     * Creates an EventService instance.
     * @return EventService implementation
     */
    public EventService createEventService() {
        return new EventServiceImpl(applicationContext);
    }
    
    /**
     * Creates a DatabaseHelper instance.
     * @return DatabaseHelper instance
     */
    public DatabaseHelper createDatabaseHelper() {
        return new DatabaseHelper(applicationContext);
    }
    
    /**
     * Creates a GeminiAIService instance.
     * @return GeminiAIService instance
     */
    public GeminiAIService createGeminiAIService() {
        return new GeminiAIService(applicationContext);
    }
} 