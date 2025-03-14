package eu.ase.acs.eventsappui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.ase.acs.eventsappui.adapters.EventAdapter;
import eu.ase.acs.eventsappui.adapters.VerticalEventAdapter;
import eu.ase.acs.eventsappui.entities.CategoryEnum;
import eu.ase.acs.eventsappui.entities.Event;
import eu.ase.acs.eventsappui.entities.Location;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventListFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FloatingActionButton fab_recommended;
    private RecyclerView rv_all_events;

    public EventListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EventListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EventListFragment newInstance(String param1, String param2) {
        EventListFragment fragment = new EventListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        initComponents(view);
        fab_recommended.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HomeFragment fragment = new HomeFragment();
                ((MainActivity)(requireActivity())).setCurrentFragment(fragment, true);
            }
        });

        List<Event> events = getEvents();
        VerticalEventAdapter adapter = new VerticalEventAdapter(events, requireContext());
        adapter.setOnClickListener(new EventAdapter.OnClickListener() {
            @Override
            public void onClick(int position, Event event) {
                Intent intent = new Intent(requireActivity(), EventActivity.class);
                intent.putExtra(HomeFragment.EVENT_KEY, event);
                startActivity(intent);
            }
        });
        rv_all_events.setAdapter(adapter);
        rv_all_events.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        RecyclerView.ItemDecoration divider = new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.top = 5;
            }
        };
        rv_all_events.addItemDecoration(divider);
        return view;
    }

    private void initComponents(View view){
        fab_recommended = view.findViewById(R.id.fab_recommended);
        rv_all_events = view.findViewById(R.id.rv_all_events);
    }

    private List<Event> getEvents(){
        List<Event> events = new ArrayList<>();
        for(int i = 0; i < 30; i++){
            @SuppressLint("NewApi") Event event = new Event( "event " + i, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque sit amet maximus purus, id sodales lorem. Sed velit ipsum, viverra vitae convallis fringilla, accumsan ac leo. Nulla aliquam at nulla sit amet ultricies. In et libero fringilla, gravida mi vel, tempus mauris. Vivamus ultrices, leo quis eleifend placerat, libero turpis mattis orci, vel auctor quam lacus id dui. Donec non ligula enim. Aliquam eget felis purus. Curabitur eget ex nisl. ", new Location("location" + i, 44.456315051913876, 26.056915854070496), Arrays.asList(CategoryEnum.COMEDY), Arrays.asList("https://picsum.photos/1920/1080", "https://picsum.photos/1920/1080"), "https://www.google.com", LocalDateTime.of(2025, 3, 3, 00, 00), LocalDateTime.now());
            events.add(event);
        }
        return events;
    }
}