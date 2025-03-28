package eu.ase.acs.eventsappui.entities;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Event implements Serializable {
    private String name;
    private String description;
    private Location location;
    private List<CategoryEnum> categories;
    private List<String> imageUrls;
    private String link;
    private LocalDateTime startDate, endDate;

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Event(String name, String description, Location location, List<CategoryEnum> categories, List<String> imageUrls, String link, LocalDateTime startDate, LocalDateTime endDate) {
        this.setName(name);
        this.setDescription(description);
        this.setLocation(location);
        this.setCategories(categories);
        this.setImageUrls(imageUrls);
        this.setLink(link);
        this.setStartDate(startDate);
        this.setEndDate(endDate);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Location getLocation() {
        try {
            return location.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public void setLocation(Location location) {
        this.location = new Location(location.getName(), location.getLatitude(), location.getLongitude());
    }

    public List<CategoryEnum> getCategories() {
        return new ArrayList<>(categories);
    }

    public void setCategories(List<CategoryEnum> categories) {
        this.categories = new ArrayList<>(categories);
    }

    public List<String> getImageUrls() {
        return new ArrayList<>(imageUrls);
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = new ArrayList<>(imageUrls);
    }

    @NonNull
    @Override
    public String toString() {
        return getName();
    }
}
