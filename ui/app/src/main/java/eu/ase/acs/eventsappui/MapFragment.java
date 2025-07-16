package eu.ase.acs.eventsappui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.threeten.bp.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import eu.ase.acs.eventsappui.adapters.MapPinDropdownAdapter;
import eu.ase.acs.eventsappui.entities.Event;
import eu.ase.acs.eventsappui.entities.Location;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    GoogleMap gMap;
    private static CameraPosition position = null;
    private MainActivity mainActivity;
    private boolean isSpinnerInitialized;

    public MapFragment() {
    }

    @SuppressLint("MissingPermission")
    private void tryLoad(){

            for (Location location : mainActivity.allLocations) {
                gMap.addMarker(new MarkerOptions().position(
                        new LatLng(location.getLatitude(), location.getLongitude())
                ).title(location.getName())).setTag(location);
            }
            gMap.setOnMarkerClickListener(marker -> {
                if (marker.getTag() != null) {
                    showEventDialog(marker);
                    return false;
                }
                return true;
            });
            if (position == null) {
                position = CameraPosition.fromLatLngZoom(mainActivity.userLocation, 15);
            }
            gMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
            gMap.addCircle(new CircleOptions().center(mainActivity.userLocation).strokeColor(R.color.black).strokeWidth(4).radius(mainActivity.radius));
            gMap.setMyLocationEnabled(true);

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mainActivity = (MainActivity) requireActivity();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
        gMap.clear();
        gMap.setInfoWindowAdapter(new MapPinDropdownAdapter(requireContext()));
        tryLoad();
    }

    private void showEventDialog(Marker marker) {
        Location location = (Location) marker.getTag();
        List<Event> eventsAtLocation = mainActivity.allEvents.stream()
                .filter(e -> e.getLocation().equals(location))
                .collect(Collectors.toList());
        eventsAtLocation.add(0, new Event(-1, "Select an event:", "dummy event", location, new ArrayList<>(), new ArrayList<>(), "", LocalDateTime.now(), LocalDateTime.now()));

        View dialogView = getLayoutInflater().inflate(R.layout.event_dialog_layout, null);
        Spinner spinner = dialogView.findViewById(R.id.dialog_spinner_events);
        ArrayAdapter<Event> adapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_item, eventsAtLocation);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();
        isSpinnerInitialized = false;

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (!isSpinnerInitialized) {
                    isSpinnerInitialized = true;
                    return;
                }
                Event event = (Event) parentView.getItemAtPosition(position);
                Intent intent = new Intent(requireContext(), EventActivity.class);
                intent.putExtra(HomeFragment.EVENT_KEY, event);
                startActivity(intent);
                dialog.dismiss();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if(gMap != null)
            position = gMap.getCameraPosition();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (gMap != null) {
            gMap.clear();
            gMap.setOnMarkerClickListener(null);
            gMap.setInfoWindowAdapter(null);
            gMap = null;
        }
    }
}
