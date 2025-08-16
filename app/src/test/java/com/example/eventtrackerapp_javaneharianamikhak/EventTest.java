package com.example.eventtrackerapp_javaneharianamikhak;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for Event class.
 * Tests all methods, edge cases, and data validation.
 */
public class EventTest {
    
    private Event validEvent;
    private Event futureEvent;
    private Event pastEvent;
    private Event todayEvent;
    
    @Before
    public void setUp() {
        validEvent = new Event(1, "Test Meeting", "12/25/2025", 1);
        futureEvent = new Event(2, "Future Event", "12/31/2025", 1);
        pastEvent = new Event(3, "Past Event", "1/1/2024", 1);
        todayEvent = new Event(4, "Today Event", java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("M/d/yyyy")), 1);
    }
    
    @Test
    public void testEventCreation() {
        assertEquals(1, validEvent.getId());
        assertEquals("Test Meeting", validEvent.getName());
        assertEquals("12/25/2025", validEvent.getDate());
        assertEquals(1, validEvent.getUserId());
    }
    
    @Test
    public void testEventCreationWithoutUserId() {
        Event event = new Event(1, "Test", "12/25/2025");
        assertEquals(-1, event.getUserId());
    }
    
    @Test
    public void testCompareTo() {
        // Same date should return 0
        Event sameDate = new Event(5, "Same Date", "12/25/2025", 1);
        assertEquals(0, validEvent.compareTo(sameDate));
        
        // Future date should return negative
        assertTrue(validEvent.compareTo(futureEvent) < 0);
        
        // Past date should return positive
        assertTrue(validEvent.compareTo(pastEvent) > 0);
        
        // Null comparison
        assertTrue(validEvent.compareTo(null) > 0);
    }
    
    @Test
    public void testEquals() {
        Event sameId = new Event(1, "Different Name", "Different Date", 2);
        Event differentId = new Event(2, "Test Meeting", "12/25/2025", 1);
        
        assertTrue(validEvent.equals(sameId));
        assertFalse(validEvent.equals(differentId));
        assertFalse(validEvent.equals(null));
        assertFalse(validEvent.equals("Not an Event"));
    }
    
    @Test
    public void testHashCode() {
        Event sameId = new Event(1, "Different Name", "Different Date", 2);
        assertEquals(validEvent.hashCode(), sameId.hashCode());
    }
    
    @Test
    public void testIsPast() {
        assertTrue(pastEvent.isPast());
        assertFalse(futureEvent.isPast());
        assertFalse(validEvent.isPast());
    }
    
    @Test
    public void testIsToday() {
        assertTrue(todayEvent.isToday());
        assertFalse(futureEvent.isToday());
        assertFalse(pastEvent.isToday());
    }
    
    @Test
    public void testGetDaysUntil() {
        assertTrue(futureEvent.getDaysUntil() > 0);
        assertTrue(pastEvent.getDaysUntil() < 0);
        assertEquals(0, todayEvent.getDaysUntil());
    }
    
    @Test
    public void testToString() {
        String expected = "Event{id=1, name='Test Meeting', date='12/25/2025', time='null', userId=1, category=null}";
        assertEquals(expected, validEvent.toString());
    }
    
    @Test
    public void testComparatorStrategies() {
        List<Event> events = Arrays.asList(
            new Event(1, "Zebra", "12/25/2025", 1),
            new Event(2, "Alpha", "12/25/2025", 1),
            new Event(3, "Beta", "12/26/2025", 1)
        );
        
        // Test BY_NAME comparator
        events.sort(Event.BY_NAME);
        assertEquals("Alpha", events.get(0).getName());
        assertEquals("Beta", events.get(1).getName());
        assertEquals("Zebra", events.get(2).getName());
        
        // Test BY_DATE comparator
        events.sort(Event.BY_DATE);
        assertEquals("12/25/2025", events.get(0).getDate());
        assertEquals("12/25/2025", events.get(1).getDate());
        assertEquals("12/26/2025", events.get(2).getDate());
    }
    
    @Test
    public void testInvalidDateHandling() {
        Event invalidDate = new Event(1, "Test", "invalid-date", 1);
        assertEquals(Integer.MAX_VALUE, invalidDate.getDaysUntil());
        assertFalse(invalidDate.isPast());
        assertFalse(invalidDate.isToday());
    }
} 