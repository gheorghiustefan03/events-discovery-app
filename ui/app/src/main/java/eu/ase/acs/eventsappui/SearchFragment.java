package eu.ase.acs.eventsappui;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

import eu.ase.acs.eventsappui.adapters.VerticalEventAdapter;
import eu.ase.acs.eventsappui.entities.Event;

public class SearchFragment extends Fragment {
    private SearchView svSearch;
    private MainActivity mainActivity;
    private RecyclerView rvFiltered;
    VerticalEventAdapter adapter;
    private List<Event> filteredList = new ArrayList<>();

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        initComponents(view);
        mainActivity = (MainActivity) requireActivity();
        svSearch.clearFocus();
        adapter = new VerticalEventAdapter(filteredList, requireContext());
        adapter.setOnClickListener((position, event) -> {
            Intent intent = new Intent(requireActivity(), EventActivity.class);
            intent.putExtra(HomeFragment.EVENT_KEY, event);
            startActivity(intent);
        });
        rvFiltered.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvFiltered.setAdapter(adapter);
        svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                if(s.isEmpty()){
                    filteredList.clear();
                    adapter.notifyDataSetChanged();
                }
                else filterList(s);
                return false;
            }
        });

        return view;
    }

    private void filterList(String s) {
        filteredList.clear();
        for(Event e : mainActivity.allEvents){
            if(e.getName().toLowerCase().contains(s.toLowerCase()))
                filteredList.add(e);
        }
        adapter.notifyDataSetChanged();
    }

    private void initComponents(View view){
        svSearch = view.findViewById(R.id.sv_search);
        rvFiltered = view.findViewById(R.id.rv_filtered);
    }
}