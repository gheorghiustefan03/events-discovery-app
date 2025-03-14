package eu.ase.acs.eventsappui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import eu.ase.acs.eventsappui.R;
import eu.ase.acs.eventsappui.entities.Event;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<Event> eventList;
    private Context context;
    private OnClickListener onClickListener;
    public EventAdapter(List<Event> eventList, Context context){
        this.eventList = eventList;
        this.context = context;
    }

    @NonNull
    @Override
    public EventAdapter.EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_layout, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventAdapter.EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.setName(event.getName());
        holder.setLocation(event.getLocation().getName());
        Glide.with(context).load(event.getImageUrls().get(0)).override(80, 143).centerCrop().into(holder.ivHeader);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onClickListener != null){
                    onClickListener.onClick(holder.getAdapterPosition(), event);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    // Interface for the click listener
    public interface OnClickListener {
        void onClick(int position, Event event);
    }

    public Context getContext(){
        return context;
    }

    public List<Event> getEventList() {
        return eventList;
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder{
        protected TextView tvName;
        protected TextView tvLocation;
        protected ImageView ivHeader;
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            ivHeader = itemView.findViewById(R.id.ivHeader);
        }
        public void setName(String name){
            tvName.setText(name);
        }
        public void setLocation(String location){
            tvLocation.setText("@" + location);
        }
    }
}
