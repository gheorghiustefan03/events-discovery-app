package eu.ase.acs.eventsappui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.ase.acs.eventsappui.adapters.EventAdapter;
import eu.ase.acs.eventsappui.entities.CategoryEnum;
import eu.ase.acs.eventsappui.entities.Event;
import eu.ase.acs.eventsappui.entities.Location;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final String EVENT_KEY = "eventkey";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private RecyclerView rv_categorized_1, rv_categorized_2, rv_categorized_3, rv_categorized_4;
    private TextView tv_category_1, tv_category_2, tv_category_3, tv_category_4;
    private FloatingActionButton fab_all_events;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initComponents(view);

        List<TextView> tvList = Arrays.asList(tv_category_1, tv_category_2, tv_category_3, tv_category_4);
        List<CategoryEnum> recommendedCategories = getRecommendedCategories();
        for(int i = 0; i < tvList.size(); i++){
            String category = recommendedCategories.get(i).toString().toLowerCase();
            String capitalized = category.substring(0, 1).toUpperCase() + category.substring(1);
            tvList.get(i).setText(capitalized);
        }

        List<RecyclerView> rvList = Arrays.asList(rv_categorized_1, rv_categorized_2, rv_categorized_3, rv_categorized_4);

        List<List<Event>> categoriesEventsLists = new ArrayList<>();
        for(CategoryEnum category : recommendedCategories){
            List<Event> eventsList = getRecommendedEventsInCategory(category);
            categoriesEventsLists.add(eventsList);
        }

        initLists(rvList, categoriesEventsLists, view);

        fab_all_events.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment eventListFragment = new EventListFragment();
                ((MainActivity)requireActivity()).setCurrentFragment(eventListFragment, true);
            }
        });

        return view;
    }
    private void initLists(List<RecyclerView> rvList, List<List<Event>> events, View view){
        for(int i = 0; i < rvList.size(); i++){
            RecyclerView rv = rvList.get(i);
            EventAdapter adapter = new EventAdapter(events.get(i), requireContext());
            adapter.setOnClickListener(new EventAdapter.OnClickListener() {
                @Override
                public void onClick(int position, Event event) {
                    Intent intent = new Intent(requireActivity(), EventActivity.class);
                    intent.putExtra(EVENT_KEY, event);
                    startActivity(intent);
                }
            });
            rv.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false));
            rv.setAdapter(adapter);
            DividerItemDecoration divider = new DividerItemDecoration(view.getContext(), LinearLayoutManager.HORIZONTAL);
            divider.setDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.divider));
            rv.addItemDecoration(divider);
        }
    }
    private void initComponents(View view){
        rv_categorized_1= view.findViewById(R.id.rv_categorized_1);
        rv_categorized_2 = view.findViewById(R.id.rv_categorized_2);
        rv_categorized_3 = view.findViewById(R.id.rv_categorized_3);
        rv_categorized_4 = view.findViewById(R.id.rv_categorized_4);

        tv_category_1 = view.findViewById(R.id.tv_category_1);
        tv_category_2 = view.findViewById(R.id.tv_category_2);
        tv_category_3 = view.findViewById(R.id.tv_category_3);
        tv_category_4 = view.findViewById(R.id.tv_category_4);

        fab_all_events = view.findViewById(R.id.fab_all_events);
    }
    private List<CategoryEnum> getRecommendedCategories(){
        return Arrays.asList(CategoryEnum.ARTS, CategoryEnum.COMEDY, CategoryEnum.EDUCATION, CategoryEnum.MUSIC);
    }
    private List<Event> getRecommendedEventsInCategory(CategoryEnum category){
        List<Event> result = new ArrayList<>();
        for(int i = 0; i < 4; i++){
            @SuppressLint("NewApi") Event event = new Event(category.toString() + " event" + i,
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque sit amet maximus purus, id sodales lorem. Sed velit ipsum, viverra vitae convallis fringilla, accumsan ac leo. Nulla aliquam at nulla sit amet ultricies. In et libero fringilla, gravida mi vel, tempus mauris. Vivamus ultrices, leo quis eleifend placerat, libero turpis mattis orci, vel auctor quam lacus id dui. Donec non ligula enim. Aliquam eget felis purus. Curabitur eget ex nisl. Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, making it over 2000 years old. Richard McClintock, a Latin professor at Hampden-Sydney College in Virginia, looked up one of the more obscure Latin words, consectetur, from a Lorem Ipsum passage, and going through the cites of the word in classical literature, discovered the undoubtable source. Lorem Ipsum comes from sections 1.10.32 and 1.10.33 of \"de Finibus Bonorum et Malorum\" (The Extremes of Good and Evil) by Cicero, written in 45 BC. This book is a treatise on the theory of ethics, very popular during the Renaissance. The first line of Lorem Ipsum, \"Lorem ipsum dolor sit amet..\", comes from a line in section 1.10.32."
                    , new Location("location" + i, 44.456315051913876, 26.056915854070496), Arrays.asList(CategoryEnum.COMEDY), Arrays.asList("https://picsum.photos/1920/1080"), "https://www.google.com", LocalDateTime.of(2025, 3, 3, 00, 00), LocalDateTime.now());
            result.add(event);
        }
        return result;
    }
}