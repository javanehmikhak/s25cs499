package com.example.eventtrackerapp_javaneharianamikhak;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

/**
 * Utility class for permission operations.
 * 
 * This class centralizes all Android permission-related logic throughout the
 * application, following the Single Responsibility Principle. It provides
 * comprehensive permission checking and management for various app features.
 * 
 * Permission Categories:
 * - SMS Permissions: For event notifications
 * - Storage Permissions: For CSV export functionality
 * - Internet Permissions: For AI-powered features
 * - Network Permissions: For connectivity features
 * 
 * Features:
 * - Permission status checking
 * - Permission rationale messaging
 * - User-friendly permission names
 * - Bulk permission validation
 * - Permission requirement analysis
 * 
 * Security: Implements proper permission checking patterns and provides
 * clear rationale messages to improve user experience and app store compliance.
 * 
 * @author Ariana Mikhak
 * @version 1.0
 * @since 2025
 */
public final class PermissionsUtil {
    
    // Prevent instantiation
    private PermissionsUtil() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }
    
    /**
     * Checks if SMS permission is granted.
     * @param context The application context
     * @return true if SMS permission is granted, false otherwise
     */
    public static boolean hasSmsPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) 
               == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Checks if storage permission is granted.
     * @param context The application context
     * @return true if storage permission is granted, false otherwise
     */
    public static boolean hasStoragePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
               == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Checks if internet permission is granted.
     * @param context The application context
     * @return true if internet permission is granted, false otherwise
     */
    public static boolean hasInternetPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET) 
               == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Checks if network state permission is granted.
     * @param context The application context
     * @return true if network state permission is granted, false otherwise
     */
    public static boolean hasNetworkStatePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE) 
               == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Checks if all required permissions for the app are granted.
     * @param context The application context
     * @return true if all permissions are granted, false otherwise
     */
    public static boolean hasAllRequiredPermissions(Context context) {
        return hasSmsPermission(context) && 
               hasStoragePermission(context) && 
               hasInternetPermission(context) && 
               hasNetworkStatePermission(context);
    }
    
    /**
     * Gets the SMS permission rationale message.
     * @return Rationale message for SMS permission
     */
    public static String getSmsPermissionRationale() {
        return "SMS permission is required to send event notifications. " +
               "This helps you stay informed about your upcoming events.";
    }
    
    /**
     * Gets the storage permission rationale message.
     * @return Rationale message for storage permission
     */
    public static String getStoragePermissionRationale() {
        return "Storage permission is required to export your events to CSV files. " +
               "This allows you to backup and share your event data.";
    }
    
    /**
     * Gets the internet permission rationale message.
     * @return Rationale message for internet permission
     */
    public static String getInternetPermissionRationale() {
        return "Internet permission is required for AI-powered event suggestions. " +
               "This enhances your event planning experience.";
    }
    
    /**
     * Checks if permission should show rationale.
     * This is a simplified version - in a real app, you'd track if the user
     * has previously denied the permission.
     * @param context The application context
     * @param permission The permission to check
     * @return true if rationale should be shown, false otherwise
     */
    public static boolean shouldShowPermissionRationale(Context context, String permission) {
        // In a real implementation, you'd check if the user has previously denied
        // the permission and should see a rationale
        return !hasPermission(context, permission);
    }
    
    /**
     * Generic method to check if a specific permission is granted.
     * @param context The application context
     * @param permission The permission to check
     * @return true if permission is granted, false otherwise
     */
    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) 
               == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Gets a user-friendly permission name.
     * @param permission The permission string
     * @return User-friendly permission name
     */
    public static String getPermissionDisplayName(String permission) {
        switch (permission) {
            case Manifest.permission.SEND_SMS:
                return "SMS";
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return "Storage";
            case Manifest.permission.INTERNET:
                return "Internet";
            case Manifest.permission.ACCESS_NETWORK_STATE:
                return "Network State";
            default:
                return "Unknown Permission";
        }
    }
} 