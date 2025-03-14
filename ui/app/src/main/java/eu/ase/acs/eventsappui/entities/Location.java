package eu.ase.acs.eventsappui.entities;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Location implements Serializable {
    private String name;
    private double latitude;
    private double longitude;

    public Location(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    @NonNull
    @Override
    public Location clone() throws CloneNotSupportedException {
        return new Location(this.name, this.latitude, this.longitude);
    }
}
