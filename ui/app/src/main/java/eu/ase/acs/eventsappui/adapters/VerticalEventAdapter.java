package eu.ase.acs.eventsappui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

import java.util.List;

import eu.ase.acs.eventsappui.R;
import eu.ase.acs.eventsappui.entities.Event;

public class VerticalEventAdapter extends EventAdapter{

    public VerticalEventAdapter(List<Event> eventList, Context context) {
        super(eventList, context);
    }
    @NonNull
    @Override
    public EventAdapter.EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vertical_event_layout, parent, false);
        return new EventViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull EventAdapter.EventViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Event event = getEventList().get(position);
        Context context = getContext();
        Glide.with(context).load(event.getImageUrls().get(0)).override(120, 143).centerCrop().into(holder.ivHeader);
    }
}
