package eu.ase.acs.eventsappui.entities;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Objects;

public class Location implements Serializable {
    private int id;
    private String name;
    private double latitude;
    private double longitude;

    public Location(int id, String name, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public int getId(){return id;}
    public void setId(int id){this.id = id;}

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
        return new Location(this.id, this.name, this.latitude, this.longitude);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Double.compare(latitude, location.latitude) == 0 && Double.compare(longitude, location.longitude) == 0 && name.equals(location.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, latitude, longitude);
    }
}
