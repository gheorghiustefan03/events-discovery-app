package eu.ase.acs.eventsappui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import eu.ase.acs.eventsappui.R;
import eu.ase.acs.eventsappui.entities.Event;
import eu.ase.acs.eventsappui.entities.Location;

public class MapPinDropdownAdapter implements GoogleMap.InfoWindowAdapter {
    private final Context context;

    public MapPinDropdownAdapter(Context context) {
        super();
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        View view = LayoutInflater.from(context).inflate(R.layout.map_pin_info_window, null);
        TextView locationName = view.findViewById(R.id.locationName);
        Location location = (Location)marker.getTag();
        locationName.setText(location.getName()); //display location name, or other relevant info.
        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}