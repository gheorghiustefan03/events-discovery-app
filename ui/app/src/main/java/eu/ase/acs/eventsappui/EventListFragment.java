package eu.ase.acs.eventsappui;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import eu.ase.acs.eventsappui.adapters.EventAdapter;
import eu.ase.acs.eventsappui.adapters.VerticalEventAdapter;
import eu.ase.acs.eventsappui.entities.Event;

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
    private TextView tv_all_events;
    private FloatingActionButton fab_recommended, fab_view_saved;
    private RecyclerView rv_all_events;
    private boolean viewSaved = false;
    private MainActivity mainActivity;

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
    public void onResume() {
        if(viewSaved){
            mainActivity.getSavedEvents();
            loadEvents(mainActivity.savedEvents);
        }
        super.onResume();
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
                mainActivity.setCurrentFragment(fragment, true);
            }
        });
        fab_view_saved.setOnClickListener((v) -> {
            if(viewSaved){
                fab_view_saved.setImageResource(R.drawable.unsaved_icon);
                viewSaved = false;
                tv_all_events.setText(R.string.all_events);
                loadEvents(mainActivity.allEvents);
            }
            else{
                fab_view_saved.setImageResource(R.drawable.saved_icon);
                viewSaved = true;
                tv_all_events.setText(R.string.saved_events);
                mainActivity.getSavedEvents();
                loadEvents(mainActivity.savedEvents);
            }

        });
        mainActivity = (MainActivity)requireActivity();
        loadEvents(mainActivity.allEvents);

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

    private void loadEvents(List<Event> events){
        VerticalEventAdapter adapter = new VerticalEventAdapter(events, requireContext());
        adapter.setOnClickListener((position, event) -> {
            Intent intent = new Intent(requireActivity(), EventActivity.class);
            intent.putExtra(HomeFragment.EVENT_KEY, event);
            startActivity(intent);
        });
        rv_all_events.setAdapter(adapter);
    }
    private void initComponents(View view){
        fab_recommended = view.findViewById(R.id.fab_recommended);
        rv_all_events = view.findViewById(R.id.rv_all_events);
        fab_view_saved = view.findViewById(R.id.fab_view_saved);
        tv_all_events = view.findViewById(R.id.tv_all_events);
    }



}