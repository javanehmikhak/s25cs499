package com.example.eventtrackerapp_javaneharianamikhak;

/**
 * AI service configuration.
 * Put your API key here.
 */
public class AIConfig {
    
    /**
     * Your API key from Google.
     */
    public static final String GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY;
    
    /**
     * Which AI model to use.
     */
    public static final String GEMINI_MODEL = "gemini-1.5-flash";
    
    /**
     * How many past events to send to AI.
     */
    public static final int MAX_EVENTS_FOR_AI_CONTEXT = 10;
    
    /**
     * Max length for suggestions.
     */
    public static final int MAX_TITLE_LENGTH = 50;
    
    /**
     * API call timeout in ms.
     */
    public static final int AI_REQUEST_TIMEOUT_MS = 2500; // Realistic HTTP timeout - 2.5 seconds for free tier
    
    /**
     * Turn AI on/off.
     */
    public static final boolean AI_ENABLED = true;
    
    /**
     * Use local fallback if AI fails.
     */
    public static final boolean FALLBACK_ENABLED = true;
} 