package eu.ase.acs.eventsappui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.threeten.bp.LocalDateTime;

import java.util.List;
import java.util.Locale;

import eu.ase.acs.eventsappui.R;
import eu.ase.acs.eventsappui.entities.Event;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private final List<Event> eventList;
    private final Context context;
    private OnClickListener onClickListener;

    public EventAdapter(List<Event> eventList, Context context) {
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
        LocalDateTime startDate = event.getStartDate();
        String dateToDisplay = String.format(String.format(Locale.ENGLISH, "%02d.%02d.%02d",
                startDate.getMonthValue(),
                startDate.getDayOfMonth(),
                startDate.getYear() - 2000));
        holder.setStartDate(dateToDisplay);
        Glide.with(context).load(event.getImageUrls().get(0).replace('\\', '/').replace("https://localhost:7295", "http://10.0.2.2:5073")).override(80, 143).centerCrop().into(holder.ivHeader);
        holder.itemView.setOnClickListener(view -> {
            if (onClickListener != null) {
                onClickListener.onClick(holder.getAdapterPosition(), event);
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

    public interface OnClickListener {
        void onClick(int position, Event event);
    }

    public Context getContext() {
        return context;
    }

    public List<Event> getEventList() {
        return eventList;
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        protected TextView tvName;
        protected TextView tvLocation;
        protected ImageView ivHeader;
        protected TextView tvStartDate;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvLocation = itemView.findViewById(R.id.tv_location);
            ivHeader = itemView.findViewById(R.id.iv_header);
            tvStartDate = itemView.findViewById(R.id.tv_start_date);
        }

        public void setName(String name) {
            tvName.setText(name);
        }

        public void setLocation(String location) {
            tvLocation.setText("@" + location);
        }

        public void setStartDate(String startDate) {
            tvStartDate.setText(startDate);
        }
    }
}
