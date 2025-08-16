# EventTrackerApp Enhancement Planning Document

This document summarizes all enhancement pseudocode blocks and TODOs inserted into the codebase, organized by enhancement category and file. I will use this as a master plan for future development and code review.

---

## 1. Software Engineering and Design (MVVM, AI Suggestions, UI Overhaul) COMPLETED

File: AddEventActivity.java
```
/*
 * Enhancement Plan: Software Engineering and Design (MVVM, AI Suggestions, UI Overhaul)
 *
 * --- MVVM Refactor: Replace direct DB/UI logic with ViewModel and LiveData ---
 * COMPLETED: Refactored AddEventActivity to use EventViewModel for all event operations
 * COMPLETED: Created EventViewModel to manage event data and business logic
 * COMPLETED: Implemented LiveData observers for UI updates
 *
 * --- AI-Driven Event Title Suggestions ---
 * COMPLETED: On AddEventActivity load, fetch recent events and context (time, location)
 * COMPLETED: Call generateSmartTitle(recentEvents, context) to get suggestion
 * COMPLETED: Prefill event title field if suggestion exists
 *
 * Pseudocode for AI Suggestion Feature
 * function onAddEventScreenLoad():
 *     recentEvents = fetchRecentEventHistory(userId)
 *     contextInfo = {
 *         time: getCurrentTimeOfDay(),
 *         location: getUserLocation()
 *     }
 *     suggestedTitle = generateSmartTitle(recentEvents, contextInfo)
 *     if suggestedTitle:
 *         prefillTitleField(suggestedTitle)
 *
 * COMPLETED: Integrated Gemini AI for intelligent event title suggestions
 *
 * function generateSmartTitle(events, context):
 *     keywords = extractFrequentKeywords(events)
 *     if context.time == "morning" and "meeting" in keywords:
 *         return "Morning Check-In"
 *     if context.location == "gym" and "workout" in keywords:
 *         return "Workout Session"
 *     return "Untitled Event"
 *
 * --- UI Overhaul ---
 * COMPLETED: Added AI suggestion button to layout
 * COMPLETED: Updated layouts to use Material Design 3 and ConstraintLayout
 * COMPLETED: Improved accessibility and responsiveness
 */
```

Files: All layout XMLs
```
<!--
    Enhancement Plan: Material Design 3 & ConstraintLayout
    COMPLETED: Changed root layout to ConstraintLayout.
    COMPLETED: Replaced all Buttons with MaterialButton.
    COMPLETED: Replaced EditText with TextInputLayout/TextInputEditText.
    COMPLETED: Applied Material styles and spacing.
    COMPLETED: Added accessibility improvements.
-->
```
```

---

## 2. Algorithms and Data Structures (PriorityQueue, HashMap, Conflict Detection) COMPLETED

Files: EventAdapter.java, EventsOverviewActivity.java, EventViewModel.java
```
/*
 * Enhancement Plan: Algorithms and Data Structures (PriorityQueue, HashMap, Conflict Detection)
 *
 * --- Priority Queue for Scheduling ---
 * COMPLETED: Implemented PriorityQueue<Event> for efficient event scheduling
 * COMPLETED: Used event date/time as priority for next-event logic
 * COMPLETED: Added getNextEvent() and getUpcomingEvents() methods
 *
 * Pseudocode for Reminder Scheduling
 * initialize PriorityQueue<Event> eventQueue ordered by event.date/time
 *
 * for each event in currentEventList:
 *     eventQueue.add(event)
 *
 * while (eventQueue is not empty):
 *     Event nextEvent = eventQueue.peek()
 *     currentTime = getCurrentTime()
 *     if (nextEvent.time > currentTime):
 *         wait until nextEvent.time
 *     else:
 *         eventQueue.poll()
 *         sendReminder(nextEvent)
 *
 * --- HashMap for Fast Lookup ---
 * COMPLETED: Added HashMap<String, Event> eventsByName for O(1) event lookup
 * COMPLETED: Added HashMap<String, List<Event>> dateToEventsMap for date-based lookups
 *
 * --- Conflict Detection Algorithm ---
 * COMPLETED: Implemented checkForConflicts() method for overlapping event detection
 * COMPLETED: Added hasConflictsOnDate() and getConflictingEvents() methods
 *
 * function checkForConflicts(newEvent, eventList):
 *     for event in eventList:
 *         if eventsOverlap(newEvent, event):
 *             return true
 *     return false
 *
 * --- Advanced Data Structures ---
 * COMPLETED: PriorityQueue for automatic event prioritization
 * COMPLETED: HashMap for efficient event lookups by name and date
 * COMPLETED: Conflict detection for scheduling overlaps
 * COMPLETED: Upcoming events summary using PriorityQueue data
 */
```
```

---

## 3. Databases (Normalization, Advanced Queries, Views, Export) COMPLETED

File: DatabaseHelper.java
```
/*
 * Enhancement Plan: Databases (Normalization, Advanced Queries, Views, Export)
 *
 * --- Normalize and Extend Schema ---
 * COMPLETED: Refactored database to have separate tables for users, categories, events
 * COMPLETED: Updated all CRUD operations to use new normalized schema
 * COMPLETED: Added foreign key relationships between tables
 *
 * --- Advanced Queries ---
 * COMPLETED: Implemented filtering by category/date, counting by type, and JOINs
 * COMPLETED: Added getEventsByCategoryAndDateRange() method
 * COMPLETED: Added getEventCountByCategory() method
 *
 * Pseudocode for Filtering Events by Category and Date Range
 * selectedCategory = getSelectedCategory()
 * startDate = getStartDateInput()
 * endDate = getEndDateInput()
 *
 * query = """
 * SELECT events.title, events.date, categories.name
 * FROM events
 * JOIN categories ON events.category_id = categories.id
 * WHERE categories.name = ? AND events.date BETWEEN ? AND ?
 * ORDER BY events.date ASC;
 * """
 * result = db.runQuery(query, [selectedCategory, startDate, endDate])
 * displayResults(result)
 *
 * --- SQLite Views ---
 * COMPLETED: Created event_summary_view for optimized UI queries
 * COMPLETED: Implemented LEFT JOIN for category data in views
 *
 * --- Data Export to CSV ---
 * COMPLETED: Implemented exportEventsToCSV() to allow users to save their events externally
 * COMPLETED: Added getEventsCSVSummary() for CSV content generation
 *
 * function exportEventsToCSV(userId):
 *     events = db.getEventsForUser(userId)
 *     csvData = convertToCSV(events)
 *     saveToFile(csvData)
 *
 * --- Database Schema Enhancement ---
 * COMPLETED: Three-table normalized design (users, categories, events)
 * COMPLETED: Foreign key constraints for data integrity
 * COMPLETED: Database version migration system
 * COMPLETED: Default categories for new users
 */
```
```

File: ProfileActivity.java (UI for export)
```
/*
 * Enhancement Plan: Databases (Normalization, Advanced Queries, Views, Export)
 *
 * --- Data Export to CSV ---
 * COMPLETED: Added UI element (button) to trigger exportEventsToCSV()
 * COMPLETED: Show confirmation or error message after export
 * COMPLETED: Integrated with Android file sharing system
 *
 * function onExportButtonClick():
 *     success = exportEventsToCSV(userId)
 *     if success:
 *         showToast("Export successful!")
 *     else:
 *         showToast("Export failed.")
 *
 * --- Export Features ---
 * COMPLETED: CSV export with event details and category information
 * COMPLETED: File sharing via Android's built-in sharing system
 * COMPLETED: Proper file provider configuration for secure file sharing
 * COMPLETED: User-friendly export button in ProfileActivity
 */
```

---

## 4. User Interface Enhancements (Sorting, Material Design, Accessibility) COMPLETED

Files: EventsOverviewActivity.java, activity_events_overview.xml
```
/*
 * Enhancement Plan: User Interface Enhancements (Sorting, Material Design, Accessibility)
 *
 * --- Event Sorting Functionality ---
 * COMPLETED: Added comprehensive sorting options for events
 * COMPLETED: Implemented sort by Date (earliest/latest), Name (A-Z/Z-A), Time (earliest/latest)
 * COMPLETED: Added sort button to main events page for easy access
 * COMPLETED: Integrated with existing Event comparators (BY_DATE, BY_NAME)
 *
 * --- Material Design Integration ---
 * COMPLETED: Updated layouts to use Material Design 3 components
 * COMPLETED: Added MaterialButton styling for sort button
 * COMPLETED: Implemented proper Material Design spacing and typography
 * COMPLETED: Added sort icon (ic_sort.xml) for visual consistency
 *
 * --- User Experience Improvements ---
 * COMPLETED: Moved sorting from hamburger menu to prominent button on main page
 * COMPLETED: Added sorting dialog with clear options and cancel functionality
 * COMPLETED: Implemented immediate visual feedback after sorting
 * COMPLETED: Fixed login crash issues related to toolbar menu setup
 *
 * --- Accessibility Features ---
 * COMPLETED: Added proper content descriptions for screen readers
 * COMPLETED: Implemented keyboard navigation support
 * COMPLETED: Added focus management for better accessibility
 *
 * function showSortOptionsDialog():
 *     displaySortOptions(["Date (Earliest)", "Date (Latest)", "Name (A-Z)", "Name (Z-A)", "Time (Earliest)", "Time (Latest)"])
 *     onOptionSelected(option):
 *         sortEvents(option)
 *         showFeedback("Events sorted successfully")
 */
```
```

Files: Category.java, Event.java
```
/*
 * Enhancement Plan: Data Model Enhancements
 *
 * --- Category System ---
 * COMPLETED: Created Category class for event categorization
 * COMPLETED: Implemented color coding for categories
 * COMPLETED: Added user-specific category management
 * COMPLETED: Integrated categories with database normalization
 *
 * --- Event Model Enhancements ---
 * COMPLETED: Enhanced Event class with category support
 * COMPLETED: Added Comparable implementation for sorting
 * COMPLETED: Implemented utility methods for date/time handling
 * COMPLETED: Added support for optional time fields
 *
 * --- Database Integration ---
 * COMPLETED: Foreign key relationships between events and categories
 * COMPLETED: Default category creation for new users
 * COMPLETED: Category-aware event queries and filtering
 */
```
```

---

## Implementation Summary - All Enhancements COMPLETED

### 1. Software Engineering and Design (MVVM, AI Suggestions, UI Overhaul)
- MVVM Architecture: Implemented EventViewModel and LiveData for reactive UI
- AI Integration: Added Gemini AI integration with local fallback system
- UI Overhaul: Updated to Material Design 3 with accessibility features

### 2. Algorithms and Data Structures (PriorityQueue, HashMap, Conflict Detection)
- PriorityQueue: Added for event scheduling and prioritization
- HashMap: Implemented for fast event lookups by name and date
- Conflict Detection: Built scheduling conflict detection algorithms
- Data Structures: Optimized for better performance

### 3. Databases (Normalization, Advanced Queries, Views, Export)
- Database Normalization: Created three-table design (users, categories, events)
- Advanced Queries: Added JOIN operations, filtering, and aggregation
- SQLite Views: Built optimized queries for UI performance
- CSV Export: Implemented data export functionality with file sharing

### 4. User Interface Enhancements (Sorting, Material Design, Accessibility)
- Event Sorting: Added comprehensive sorting options with user-friendly interface
- Material Design: Updated to Material Design 3 implementation
- Accessibility: Included accessibility support with screen reader compatibility
- User Experience: Created intuitive interface with immediate feedback

### Technical Achievements:
- Modern Architecture: Built MVVM with LiveData and ViewModel
- AI Integration: Added real-time AI suggestions with fallback
- Database Design: Created normalized schema with advanced querying
- Performance: Optimized data structures and algorithms
- User Experience: Built intuitive interface with comprehensive functionality
- Accessibility: Included accessibility compliance
- Code Quality: Follows CS-499 Code Review Checklist standards

### Files Created/Enhanced:
- New Files: `Category.java`, `GeminiAIService.java`, `AIConfig.java`, `EventViewModel.java`
- Enhanced Files: `DatabaseHelper.java`, `EventsOverviewActivity.java`, `AddEventActivity.java`
- UI Files: Updated all layout XMLs to Material Design 3
- Resources: Added sort icons, menu files, and drawable resources

### Scope Completion Status: 100% COMPLETE
All planned enhancements have been implemented and tested. The EventTrackerApp now includes:
- Professional-grade architecture and design
- Advanced algorithms and data structures
- Robust database with export capabilities
- Modern, accessible user interface
- AI-powered intelligent features

## Notes
- Each pseudocode block has been implemented as working code.
- All TODOs have been completed and converted to functional features.
- This document serves as a comprehensive record of all enhancements completed for CS-499.
- The implementation follows industry best practices and CS-499 learning outcomes.

## Implementation Summary - Software Engineering and Design Enhancement COMPLETED

### What Was Implemented:
1. MVVM Architecture: Created `EventViewModel` class that separates UI logic from data operations
2. LiveData Integration: Added reactive UI updates using LiveData observers
3. AI-Driven Event Title Suggestions: Built intelligent event naming based on user history and time context
4. Enhanced Event Class: Made Event implement Comparable for PriorityQueue usage and added utility methods
5. Refactored Activities: Updated `AddEventActivity` and `EventsOverviewActivity` to use ViewModel pattern
6. UI Improvements: Added AI suggestion button to the event creation interface

### Key Features:
- Smart Title Suggestions: Analyzes user's event history to suggest relevant event names
- Time-Based Context: Considers time of day (morning/afternoon/evening) for suggestions
- Reactive UI: LiveData ensures UI updates automatically when data changes
- Efficient Data Structures: PriorityQueue for event scheduling, HashMap for fast lookups
- Error Handling: Added error handling with user-friendly messages

### Technical Achievements:
- Follows Android MVVM best practices
- Implements proper separation of concerns
- Uses modern Android architecture components
- Maintains backward compatibility
- Follows CS-499 Code Review Checklist standards 

## Implementation Summary - Gemini AI Integration COMPLETED

### What Was Implemented:
1. Gemini AI Service: Created `GeminiAIService` class for AI-powered event title suggestions
2. AI Configuration: Added `AIConfig` class for centralized AI settings management
3. Enhanced EventViewModel: Integrated Gemini AI with existing MVVM architecture
4. Fallback System: Added local suggestions when AI is unavailable
5. Async Processing: Built non-blocking AI calls with error handling
6. Setup Documentation: Created setup guide for Gemini integration
7. Real API Integration: Added direct HTTP calls to Google's Gemini API
8. Enhanced Local AI: Built pattern recognition and time-based suggestions as fallback

### Current Status:
- Framework: Complete and functional
- API Integration: Implemented with real Gemini API calls
- Fallback System: Working with local suggestions as backup
- User Experience: Functional with intelligent AI suggestion

### Key Features:
- Smart AI Suggestions: Gemini analyzes user event history and context for intelligent titles
- Time-Aware Context: Considers time of day (morning/afternoon/evening/night) for suggestions
- Privacy-Focused: Only sends event names and context, no personal data
- Robust Fallback: Local suggestions work when AI is unavailable or disabled
- Configurable: Easy to enable/disable AI features and customize behavior

### Technical Achievements:
- Modern AI Integration: Uses Google's latest Gemini AI API
- Proper Architecture: Follows MVVM pattern with clean separation of concerns
- Error Handling: Added error handling with user feedback
- Resource Management: Added cleanup of AI service resources
- Documentation: Created setup guide and technical documentation

### Files Created/Modified:
- New Files: `GeminiAIService.java`, `AIConfig.java`, `GEMINI_SETUP.md`
- Enhanced Files: `EventViewModel.java`, `build.gradle`, `AndroidManifest.xml`
- Dependencies: Added Gemini AI and networking libraries
- Permissions: Added internet and network state permissions

### AI Capabilities:
- Pattern Recognition: Learns from user's event naming patterns
- Contextual Suggestions: Considers time, location, and event history
- Professional Titles: Generates concise, professional event names
- Scalable: Handles varying amounts of user data efficiently