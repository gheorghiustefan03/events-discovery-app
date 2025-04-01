package eu.ase.acs.eventsappui;

import android.content.Intent;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.ase.acs.eventsappui.adapters.EventAdapter;
import eu.ase.acs.eventsappui.entities.Category;
import eu.ase.acs.eventsappui.entities.Event;

public class HomeFragment extends Fragment {
    public static final String EVENT_KEY = "eventkey";
    private RecyclerView rvCategorized1, rvCategorized2, rvCategorized3, rvCategorized4;
    private TextView tvCategory1, tvCategory2, tvCategory3, tvCategory4;
    private FloatingActionButton fabAllEvents;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initComponents(view);

        List<TextView> tvList = Arrays.asList(tvCategory1, tvCategory2, tvCategory3, tvCategory4);
        MainActivity mainActivity = (MainActivity) requireActivity();

        for (int i = 0; i < tvList.size(); i++) {
            String category = mainActivity.recommendedCategories.get(i).toString().toLowerCase();
            String capitalized = category.substring(0, 1).toUpperCase() + category.substring(1);
            String displayable = capitalized.replace("_", ", ");
            tvList.get(i).setText(displayable);
        }

        List<RecyclerView> rvList = Arrays.asList(rvCategorized1, rvCategorized2, rvCategorized3, rvCategorized4);

        List<List<Event>> categoriesEventsLists = new ArrayList<>();
        for (Category category : mainActivity.recommendedCategories) {
            List<Event> eventsList = mainActivity.getRecommendedEventsForCategory(category);
            categoriesEventsLists.add(eventsList);
        }

        initLists(rvList, categoriesEventsLists, view);

        fabAllEvents.setOnClickListener(view1 -> {
            Fragment eventListFragment = new EventListFragment();
            ((MainActivity) requireActivity()).setCurrentFragment(eventListFragment, true);
        });

        return view;
    }



    private void initLists(List<RecyclerView> rvList, List<List<Event>> events, View view) {
        for (int i = 0; i < rvList.size(); i++) {
            RecyclerView rv = rvList.get(i);
            EventAdapter adapter = new EventAdapter(events.get(i), requireContext());
            adapter.setOnClickListener((position, event) -> {
                Intent intent = new Intent(requireActivity(), EventActivity.class);
                intent.putExtra(EVENT_KEY, event);
                startActivity(intent);
            });
            rv.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false));
            rv.setAdapter(adapter);
            DividerItemDecoration divider = new DividerItemDecoration(view.getContext(), LinearLayoutManager.HORIZONTAL);
            divider.setDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.divider));
            rv.addItemDecoration(divider);
        }
    }

    private void initComponents(View view) {
        rvCategorized1 = view.findViewById(R.id.rv_categorized_1);
        rvCategorized2 = view.findViewById(R.id.rv_categorized_2);
        rvCategorized3 = view.findViewById(R.id.rv_categorized_3);
        rvCategorized4 = view.findViewById(R.id.rv_categorized_4);

        tvCategory1 = view.findViewById(R.id.tv_category_1);
        tvCategory2 = view.findViewById(R.id.tv_category_2);
        tvCategory3 = view.findViewById(R.id.tv_category_3);
        tvCategory4 = view.findViewById(R.id.tv_category_4);

        fabAllEvents = view.findViewById(R.id.fab_all_events);
    }
}