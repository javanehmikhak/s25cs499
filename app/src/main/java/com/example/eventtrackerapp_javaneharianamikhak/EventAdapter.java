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
 * Manages the data and views for the list of events displayed in a RecyclerView.
 * This adapter is responsible for creating view holders for each event item, binding
 * event data (name and date) to the views, and handling user interactions like
 * clicks on the "Edit" and "Delete" buttons via callback listeners.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<Event> events;
    private final OnEventDeleteListener deleteListener;
    private final OnEventEditListener editListener;

    /**
     * Functional interface for handling the deletion of an event.
     */
    public interface OnEventDeleteListener {
        void onDelete(Event event);
    }

    /**
     * Functional interface for handling the editing of an event.
     */
    public interface OnEventEditListener {
        void onEdit(Event event);
    }

    /**
     * Constructs the EventAdapter.
     * @param events The initial list of events to display.
     * @param deleteListener The callback listener for delete actions.
     * @param editListener The callback listener for edit actions.
     */
    public EventAdapter(List<Event> events, OnEventDeleteListener deleteListener, OnEventEditListener editListener) {
        this.events = events;
        this.deleteListener = deleteListener;
        this.editListener = editListener;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     * This new ViewHolder will be used to display items of the adapter using onBindViewHolder.
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new EventViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method updates the contents of the ViewHolder to reflect the item at the given position.
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.eventText.setText(String.format("%s – %s", event.getName(), event.getDate()));
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
     * Returns the total number of items in the data set held by the adapter.
     * @return The total number of items in this adapter.
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