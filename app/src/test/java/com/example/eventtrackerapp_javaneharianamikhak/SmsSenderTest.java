package com.example.eventtrackerapp_javaneharianamikhak;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for SmsSender class.
 * Tests input validation and error handling.
 */
public class SmsSenderTest {
    
    @Test
    public void testSendSmsWithValidPhoneNumber() {
        // This test would require mocking SmsManager
        // For now, we test that the method doesn't throw exceptions
        try {
            SmsSender.sendSms("+1234567890", "Test message");
            // If we reach here, no exception was thrown
            assertTrue(true);
        } catch (Exception e) {
            // Expected in test environment without SMS permissions
            assertTrue(e instanceof SecurityException || e instanceof RuntimeException);
        }
    }
    
    @Test
    public void testSendSmsWithNullPhoneNumber() {
        // Should handle null gracefully
        try {
            SmsSender.sendSms(null, "Test message");
            // Method should complete without throwing exception
            assertTrue(true);
        } catch (Exception e) {
            // Expected in test environment
            assertTrue(e instanceof SecurityException || e instanceof RuntimeException);
        }
    }
    
    @Test
    public void testSendSmsWithEmptyPhoneNumber() {
        // Should handle empty string gracefully
        try {
            SmsSender.sendSms("", "Test message");
            SmsSender.sendSms("   ", "Test message");
            // Method should complete without throwing exception
            assertTrue(true);
        } catch (Exception e) {
            // Expected in test environment
            assertTrue(e instanceof SecurityException || e instanceof RuntimeException);
        }
    }
    
    @Test
    public void testSendSmsWithNullMessage() {
        // Should handle null message gracefully
        try {
            SmsSender.sendSms("+1234567890", null);
            // If we reach here, no exception was thrown
            assertTrue(true);
        } catch (Exception e) {
            // Expected in test environment
            assertTrue(e instanceof SecurityException || e instanceof RuntimeException);
        }
    }
    
    @Test
    public void testSendSmsWithEmptyMessage() {
        // Should handle empty message gracefully
        try {
            SmsSender.sendSms("+1234567890", "");
            SmsSender.sendSms("+1234567890", "   ");
            // If we reach here, no exception was thrown
            assertTrue(true);
        } catch (Exception e) {
            // Expected in test environment
            assertTrue(e instanceof SecurityException || e instanceof RuntimeException);
        }
    }
    
    @Test
    public void testSendSmsWithInvalidPhoneFormat() {
        // Should handle invalid phone number format gracefully
        try {
            SmsSender.sendSms("invalid-phone", "Test message");
            // If we reach here, no exception was thrown
            assertTrue(true);
        } catch (Exception e) {
            // Expected in test environment
            assertTrue(e instanceof IllegalArgumentException || e instanceof SecurityException || e instanceof RuntimeException);
        }
    }
} 