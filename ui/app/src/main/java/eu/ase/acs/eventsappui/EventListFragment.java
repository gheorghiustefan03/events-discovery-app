package eu.ase.acs.eventsappui;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import eu.ase.acs.eventsappui.adapters.VerticalEventAdapter;
import eu.ase.acs.eventsappui.entities.Event;

public class EventListFragment extends Fragment {
    private TextView tvAllEvents;
    private FloatingActionButton fabRecommended, fabViewSaved, fabSort;
    private RecyclerView rvAllEvents;
    private boolean viewSaved = false;

    private MainActivity mainActivity;

    public EventListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        sortEvents(mainActivity.allEventsSorting, mainActivity.allEvents, false);
        if (viewSaved) {
            mainActivity.getSavedEvents();
            sortEvents(mainActivity.savedEventsSorting, mainActivity.savedEvents, false);
            loadEvents(mainActivity.savedEvents);
            fabViewSaved.setImageResource(R.drawable.saved_icon);
        }
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        initComponents(view);
        fabRecommended.setOnClickListener(view1 -> {
                    HomeFragment fragment = new HomeFragment();
                    mainActivity.setCurrentFragment(fragment, true);
        });
        fabViewSaved.setOnClickListener((v) -> {
            if (viewSaved) {
                fabViewSaved.setImageResource(R.drawable.unsaved_icon);
                viewSaved = false;
                tvAllEvents.setText(R.string.all_events);
                loadEvents(mainActivity.allEvents);
            } else {
                fabViewSaved.setImageResource(R.drawable.saved_icon);
                viewSaved = true;
                tvAllEvents.setText(R.string.saved_events);
                mainActivity.getSavedEvents();
                sortEvents(mainActivity.savedEventsSorting, mainActivity.savedEvents, false);
                loadEvents(mainActivity.savedEvents);
            }

        });
        mainActivity = (MainActivity) requireActivity();
        loadEvents(mainActivity.allEvents);

        rvAllEvents.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        RecyclerView.ItemDecoration divider = new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.top = 10;
            }
        };
        rvAllEvents.addItemDecoration(divider);
        fabSort.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            MenuInflater menuInflater = popupMenu.getMenuInflater();
            menuInflater.inflate(R.menu.sort_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                sortEvents(menuItem.getTitle().toString(), viewSaved ? mainActivity.savedEvents : mainActivity.allEvents, false);
                return true;
            });
            popupMenu.show();
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        sortEvents("Recommended", mainActivity.allEvents, true);
    }

    private void sortEvents(String type, List<Event> events, boolean temporary){
        switch(type){
            case "Recommended":
                Map<Integer, Integer> indicesMap = mainActivity.recommendedIndices;
                events.sort(Comparator.comparingInt(e -> indicesMap.get(e.getId())));
                break;
            case "Alphabetical":
                events.sort(Comparator.comparing(Event::getName));
                break;
            case "Upcoming":
                events.sort((e1, e2) -> {
                    LocalDateTime startDate1 = e1.getStartDate();
                    LocalDateTime startDate2 = e2.getStartDate();
                    LocalDateTime now = LocalDateTime.now();
                    if(startDate1.isEqual(startDate2)) return 0;
                    if(startDate1.isBefore(now) && !startDate2.isBefore(now)){
                        return 1;
                    }
                    if(startDate2.isBefore(now) && !startDate1.isBefore(now)){
                        return -1;
                    }
                    return startDate1.compareTo(startDate2);
                });
                break;
        }
        if(!temporary){
            if(viewSaved) mainActivity.savedEventsSorting = type;
            else mainActivity.allEventsSorting = type;
        }
        loadEvents(events);
    }

    private void loadEvents(List<Event> events) {
        VerticalEventAdapter adapter = new VerticalEventAdapter(events, requireContext());
        adapter.setOnClickListener((position, event) -> {
            Intent intent = new Intent(requireActivity(), EventActivity.class);
            intent.putExtra(HomeFragment.EVENT_KEY, event);
            startActivity(intent);
        });
        rvAllEvents.setAdapter(adapter);
    }



    private void initComponents(View view) {
        fabRecommended = view.findViewById(R.id.fab_recommended);
        rvAllEvents = view.findViewById(R.id.rv_all_events);
        fabViewSaved = view.findViewById(R.id.fab_view_saved);
        tvAllEvents = view.findViewById(R.id.tv_all_events);
        fabSort = view.findViewById(R.id.fab_sort);
    }


}