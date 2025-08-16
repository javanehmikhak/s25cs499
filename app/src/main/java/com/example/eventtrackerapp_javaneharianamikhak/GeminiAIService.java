package com.example.eventtrackerapp_javaneharianamikhak;

import android.content.Context;
import android.util.Log;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.Part;
import com.google.ai.client.generativeai.type.TextPart;


import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CompletableFuture;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * Handles event title suggestions using external AI service.
 * Falls back to local pattern matching if AI is unavailable.
 * 
 * This service runs AI calls on background threads to keep UI responsive.
 * If the external service fails, we use a simple local algorithm for suggestions.
 */
public class GeminiAIService {
    
    private static final String TAG = "GeminiAIService";
    private static final int HTTP_OK = 200;
    private static final int MAX_LOG_LENGTH = 200;
    private static final int AI_TIMEOUT_MS = 3000; // Realistic timeout - 3 seconds for free tier
    
    private final GenerativeModel generativeModel;
    private final ExecutorService executorService;
    private final Context context;
    
    /**
     * Initialize the service with app context.
     * Sets up the AI model if API key is configured.
     */
    public GeminiAIService(Context context) {
        this.context = context;
        this.executorService = Executors.newSingleThreadExecutor();
        
        // Initialize Gemini model
        if (!AIConfig.AI_ENABLED) {
            Log.i(TAG, "Suggestions disabled. Using local fallback.");
            this.generativeModel = null;
        } else if (AIConfig.GEMINI_API_KEY == null || AIConfig.GEMINI_API_KEY.trim().isEmpty()) {
            Log.w(TAG, "API key missing. Using local fallback.");
            this.generativeModel = null;
        } else {
            Log.i(TAG, "External service enabled.");
            this.generativeModel = new GenerativeModel(AIConfig.GEMINI_MODEL, AIConfig.GEMINI_API_KEY);
        }
    }
    
    /**
     * Get a suggested event title based on user's past events.
     * Calls external AI service, falls back to local logic if needed.
     */
    public void generateEventTitleSuggestion(List<Event> userEvents, String timeContext, 
                                           String locationContext, AISuggestionCallback callback) {
        
        Log.i(TAG, "Starting AI suggestion generation...");
        
        if (generativeModel == null) {
            Log.i(TAG, "AI model is null, using immediate local fallback");
            // Use local fallback if AI is disabled
            String fallbackSuggestion = generateFallbackSuggestion(userEvents, timeContext, locationContext);
            callback.onSuccess(fallbackSuggestion);
            return;
        }
        
        // Use CompletableFuture with ultra-fast timeout for robust async handling
        CompletableFuture<String> aiFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Log.i(TAG, "Starting AI API call...");
                String result = generateAISuggestion(userEvents, timeContext, locationContext);
                Log.i(TAG, "AI API call completed, result: " + (result != null ? "success" : "null"));
                return result;
            } catch (RuntimeException e) {
                Log.e(TAG, "AI suggestion error: " + e.getMessage());
                return null;
            }
        }, executorService);
        
        // Ultra-fast timeout protection
        CompletableFuture<String> timeoutFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Log.i(TAG, "Starting timeout timer for " + AI_TIMEOUT_MS + "ms...");
                Thread.sleep(AI_TIMEOUT_MS);
                Log.i(TAG, "Timeout reached!");
                return "TIMEOUT";
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "TIMEOUT";
            }
        }, executorService);
        
        // Race between AI response and timeout
        CompletableFuture.anyOf(aiFuture, timeoutFuture)
            .thenAcceptAsync(result -> {
                try {
                    String suggestion = (String) result;
                    Log.i(TAG, "Race completed, result type: " + (suggestion != null ? suggestion : "null"));
                    
                    if ("TIMEOUT".equals(suggestion) || suggestion == null) {
                        // Use fallback on timeout or error
                        Log.w(TAG, "AI timeout or error, using fallback");
                        String fallbackSuggestion = generateFallbackSuggestion(userEvents, timeContext, locationContext);
                        Log.i(TAG, "Fallback suggestion generated: " + fallbackSuggestion);
                        // Use main thread for callback
                        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                            callback.onSuccess(fallbackSuggestion);
                        });
                    } else {
                        // AI succeeded
                        Log.i(TAG, "AI suggestion successful: " + suggestion);
                        // Use main thread for callback
                        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                            callback.onSuccess(suggestion);
                        });
                    }
                } catch (RuntimeException e) {
                    Log.e(TAG, "Callback error: " + e.getMessage());
                    // Final fallback
                    String fallbackSuggestion = generateFallbackSuggestion(userEvents, timeContext, locationContext);
                    // Use main thread for callback
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        callback.onSuccess(fallbackSuggestion);
                    });
                }
            }, executorService);
    }
    
    /**
     * Try to get suggestion from AI service.
     * Returns local fallback if AI call fails.
     */
    private String generateAISuggestion(List<Event> userEvents, String timeContext, String locationContext) {
        try {
            String eventHistory = buildEventHistory(userEvents);
            String prompt = buildAIPrompt(eventHistory, timeContext, locationContext);
            
            if (generativeModel != null) {
                String aiSuggestion = makeDirectGeminiAPICall(prompt);
                if (aiSuggestion != null && !aiSuggestion.trim().isEmpty()) {
                    return cleanSuggestion(aiSuggestion);
                }
            }
            
            return generateEnhancedFallbackSuggestion(userEvents, timeContext, locationContext);
            
        } catch (RuntimeException e) {
            Log.e(TAG, "Suggestion error: " + e.getMessage());
            return generateEnhancedFallbackSuggestion(userEvents, timeContext, locationContext);
        }
    }
    
    /**
     * Build event history string.
     */
    private String buildEventHistory(List<Event> userEvents) {
        if (userEvents == null || userEvents.isEmpty()) {
            return "";
        }
        
        StringBuilder history = new StringBuilder("Recent events:\n");
        int maxEvents = Math.min(userEvents.size(), AIConfig.MAX_EVENTS_FOR_AI_CONTEXT);
        for (int i = 0; i < maxEvents; i++) {
            Event event = userEvents.get(i);
            history.append("- ").append(event.getName()).append(" (").append(event.getDate()).append(")\n");
        }
        return history.toString();
    }
    
    /**
     * Build the text prompt to send to AI service.
     */
    private String buildAIPrompt(String eventHistory, String timeContext, String locationContext) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an AI assistant helping users create meaningful event titles. ");
        prompt.append("Based on the user's event history and current context, suggest a concise, professional event title. ");
        prompt.append("The title should be 1-4 words maximum and relevant to their patterns.\n\n");
        
        if (!eventHistory.isEmpty()) {
            prompt.append(eventHistory).append("\n");
        }
        
        prompt.append("Current context:\n");
        prompt.append("- Time: ").append(timeContext).append("\n");
        if (locationContext != null && !locationContext.isEmpty()) {
            prompt.append("- Location: ").append(locationContext).append("\n");
        }
        
        prompt.append("\nPlease suggest a single event title that fits the user's patterns and current context. ");
        prompt.append("Respond with only the title, no explanations or additional text.");
        
        return prompt.toString();
    }
    
    /**
     * Clean up the AI response text.
     */
    private String cleanSuggestion(String suggestion) {
        if (suggestion == null || suggestion.trim().isEmpty()) {
            return "New Event";
        }
        
        // Remove quotes, extra whitespace, and common AI prefixes
        String cleaned = suggestion.trim()
                .replaceAll("^[\"']+|[\"']+$", "") // Remove surrounding quotes
                .replaceAll("^Title:\\s*", "") // Remove "Title:" prefix
                .replaceAll("^Suggestion:\\s*", "") // Remove "Suggestion:" prefix
                .trim();
        
        // Ensure it's not too long
        if (cleaned.length() > AIConfig.MAX_TITLE_LENGTH) {
            cleaned = cleaned.substring(0, AIConfig.MAX_TITLE_LENGTH - 3) + "...";
        }
        
        return cleaned.isEmpty() ? "New Event" : cleaned;
    }
    
    /**
     * Local fallback when AI service is down.
     * Looks at user's past events to suggest similar titles.
     */
    private String generateEnhancedFallbackSuggestion(List<Event> userEvents, String timeContext, String locationContext) {
        if (userEvents == null || userEvents.isEmpty()) {
            return getTimeBasedSuggestion("Event", timeContext);
        }
        
        // Preprocess: Build frequency map of event types for O(1) lookup
        Map<String, Integer> eventTypeFrequency = new HashMap<>();
        String mostCommonType = null;
        int maxFrequency = 0;
        
        // Enhanced pattern recognition for free tier fallback
        for (Event event : userEvents) {
            String eventType = extractEventType(event.getName());
            int frequency = eventTypeFrequency.getOrDefault(eventType, 0) + 1;
            eventTypeFrequency.put(eventType, frequency);
            
            if (frequency > maxFrequency) {
                maxFrequency = frequency;
                mostCommonType = eventType;
            }
        }
        
        // Use most common event type if found
        if (mostCommonType != null && maxFrequency > 1) {
            return getTimeBasedSuggestion(mostCommonType, timeContext);
        }
        
        // Fallback to most recent event
        Event mostRecent = userEvents.get(0);
        String baseType = extractEventType(mostRecent.getName());
        return getTimeBasedSuggestion(baseType, timeContext);
    }
    
    private String extractEventType(String eventName) {
        String lowerName = eventName.toLowerCase();
        
        // O(1) pattern matching with early returns
        if (lowerName.contains("meeting") || lowerName.contains("call") || lowerName.contains("sync")) {
            return "Meeting";
        }
        if (lowerName.contains("workout") || lowerName.contains("gym") || lowerName.contains("exercise")) {
            return "Workout";
        }
        if (lowerName.contains("lunch") || lowerName.contains("dinner") || lowerName.contains("meal")) {
            return "Meal";
        }
        if (lowerName.contains("appointment") || lowerName.contains("doctor") || lowerName.contains("checkup")) {
            return "Appointment";
        }
        if (lowerName.contains("coffee") || lowerName.contains("break") || lowerName.contains("rest")) {
            return "Break";
        }
        if (lowerName.contains("review") || lowerName.contains("planning") || lowerName.contains("strategy")) {
            return "Planning";
        }
        if (lowerName.contains("presentation") || lowerName.contains("demo") || lowerName.contains("show")) {
            return "Presentation";
        }
        if (lowerName.contains("interview") || lowerName.contains("discussion")) {
            return "Discussion";
        }
        
        // Extract last word as base type
        String[] words = eventName.split("\\s+");
        return words.length > 0 ? words[words.length - 1] : "Event";
    }
    
    /**
     * Simple fallback - basic pattern matching.
     */
    private String generateFallbackSuggestion(List<Event> userEvents, String timeContext, String locationContext) {
        Log.i(TAG, "Generating local fallback suggestion...");
        long startTime = System.currentTimeMillis();
        
        // Extract patterns from user events
        if (userEvents != null && !userEvents.isEmpty()) {
            // Look for common patterns
            for (Event event : userEvents) {
                String eventName = event.getName().toLowerCase();
                
                if (eventName.contains("meeting") || eventName.contains("call")) {
                    return getTimeBasedSuggestion("Meeting", timeContext);
                }
                if (eventName.contains("workout") || eventName.contains("gym")) {
                    return getTimeBasedSuggestion("Workout", timeContext);
                }
                if (eventName.contains("lunch") || eventName.contains("dinner")) {
                    return getTimeBasedSuggestion("Meal", timeContext);
                }
                if (eventName.contains("appointment") || eventName.contains("doctor")) {
                    return getTimeBasedSuggestion("Appointment", timeContext);
                }
                if (eventName.contains("coffee") || eventName.contains("break")) {
                    return getTimeBasedSuggestion("Break", timeContext);
                }
                if (eventName.contains("review") || eventName.contains("planning")) {
                    return getTimeBasedSuggestion("Planning", timeContext);
                }
            }
            
            // If no specific patterns found, use the most recent event type
            Event mostRecent = userEvents.get(0);
            String[] words = mostRecent.getName().split("\\s+");
            if (words.length > 0) {
                String baseType = words[words.length - 1]; // Use last word as base type
                return getTimeBasedSuggestion(baseType, timeContext);
            }
        }
        
        // Default time-based suggestions
        String result = getTimeBasedSuggestion("Event", timeContext);
        Log.i(TAG, "Local fallback completed in " + (System.currentTimeMillis() - startTime) + "ms: " + result);
        return result;
    }
    
    /**
     * Add time prefix to event type.
     */
    private String getTimeBasedSuggestion(String baseType, String timeContext) {
        switch (timeContext.toLowerCase()) {
            case "morning":
                return "Morning " + baseType;
            case "afternoon":
                return "Afternoon " + baseType;
            case "evening":
                return "Evening " + baseType;
            case "night":
                return "Night " + baseType;
            default:
                return baseType;
        }
    }
    
    /**
     * Call the AI service via HTTP.
     * Returns null if call fails.
     */
    private String makeDirectGeminiAPICall(String prompt) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + AIConfig.GEMINI_API_KEY);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            
            // Set realistic timeouts for free tier
            connection.setConnectTimeout(1000); // 1 second connection timeout
            connection.setReadTimeout(AIConfig.AI_REQUEST_TIMEOUT_MS);
            
            // Create the request body
            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();
            part.put("text", prompt);
            parts.put(part);
            content.put("parts", parts);
            contents.put(content);
            requestBody.put("contents", contents);
            
            // Send the request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Read the response
            int responseCode = connection.getResponseCode();
            Log.i(TAG, "Gemini API response code: " + responseCode);
            
            if (responseCode == HTTP_OK) {
                java.io.BufferedReader in = new java.io.BufferedReader(
                    new java.io.InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                Log.i(TAG, "Gemini API raw response: " + response.toString().substring(0, Math.min(response.length(), MAX_LOG_LENGTH)) + "...");
                
                // Parse the JSON response
                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.has("candidates") && jsonResponse.getJSONArray("candidates").length() > 0) {
                    JSONObject candidate = jsonResponse.getJSONArray("candidates").getJSONObject(0);
                    if (candidate.has("content") && candidate.getJSONObject("content").has("parts")) {
                        JSONArray responseParts = candidate.getJSONObject("content").getJSONArray("parts");
                        if (responseParts.length() > 0 && responseParts.getJSONObject(0).has("text")) {
                            String aiText = responseParts.getJSONObject(0).getString("text");
                            Log.i(TAG, "Successfully extracted AI text: " + aiText);
                            return aiText;
                        }
                    }
                }
                Log.w(TAG, "No valid candidates found in Gemini response");
            } else {
                // Read error response
                java.io.BufferedReader errorReader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(connection.getErrorStream()));
                String errorLine;
                StringBuilder errorResponse = new StringBuilder();
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine);
                }
                errorReader.close();
                
                Log.e(TAG, "Gemini API error response: " + errorResponse.toString());
            }
            
        } catch (java.io.IOException e) {
            Log.e(TAG, "Network error making Gemini API call: " + e.getMessage());
        } catch (org.json.JSONException e) {
            Log.e(TAG, "JSON parsing error in Gemini API call: " + e.getMessage());
        } catch (RuntimeException e) {
            Log.e(TAG, "Error making direct Gemini API call: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        
        return null;
    }
    
    /**
     * Clean up resources.
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    /**
     * Callback for suggestion results.
     */
    public interface AISuggestionCallback {
        /**
         * Got a suggestion.
         */
        void onSuccess(String suggestion);
        
        /**
         * Something went wrong.
         */
        void onError(String error);
    }
} 