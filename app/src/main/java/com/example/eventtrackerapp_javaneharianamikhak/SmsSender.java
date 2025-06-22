package com.example.eventtrackerapp_javaneharianamikhak;

import android.telephony.SmsManager;
import android.util.Log;

/**
 * A utility class for sending SMS messages.
 */
public class SmsSender {

    private static final String TAG = "SmsSender";

    /**
     * Sends an SMS message to the specified phone number.
     *
     * @param phoneNumber The destination phone number.
     * @param message     The message to send.
     */
    public static void sendSms(String phoneNumber, String message) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            Log.e(TAG, "Phone number is null or empty. Cannot send SMS.");
            return;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Log.i(TAG, "SMS sent successfully to " + phoneNumber);
        } catch (Exception e) {
            Log.e(TAG, "Failed to send SMS to " + phoneNumber, e);
        }
    }
} 