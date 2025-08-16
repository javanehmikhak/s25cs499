/*
 * RecyclerView adapter for events list.
 * Handles event display and user interactions.
 */
package com.example.eventtrackerapp_javaneharianamikhak;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * RecyclerView adapter for displaying events list.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<Event> events;
    private final OnEventDeleteListener deleteListener;
    private final OnEventEditListener editListener;

    public interface OnEventDeleteListener {
        void onDelete(Event event);
    }

    public interface OnEventEditListener {
        void onEdit(Event event);
    }

    /**
     * Create adapter with event list and listeners.
     */
    public EventAdapter(List<Event> events, OnEventDeleteListener deleteListener, OnEventEditListener editListener) {
        this.events = events;
        this.deleteListener = deleteListener;
        this.editListener = editListener;
    }

    /**
     * Create new ViewHolder for RecyclerView.
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Bind event data to ViewHolder.
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        String eventDisplay = event.getName() + " – " + event.getDate();
        if (event.getTime() != null && !event.getTime().isEmpty()) {
            eventDisplay += " at " + event.getTime();
        }
        holder.eventText.setText(eventDisplay);
        
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(event);
            }
        });
        holder.editButton.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEdit(event);
            }
        });
    }

    /**
     * Get total number of events.
     */
    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * Updates the list of events displayed by the adapter and refreshes the RecyclerView.
     * @param newEvents The new list of events to display.
     */
    public void updateEvents(List<Event> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     * It holds cached references to the views (e.g., TextView, Buttons) within the item layout,
     * so that findViewById() is not called repeatedly.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        final TextView eventText;
        final Button deleteButton;
        final Button editButton;

        EventViewHolder(View view) {
            super(view);
            eventText = view.findViewById(R.id.eventText);
            deleteButton = view.findViewById(R.id.deleteButton);
            editButton = view.findViewById(R.id.editButton);
        }
    }
}