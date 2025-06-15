package eu.ase.acs.eventsappui.entities;
import androidx.annotation.NonNull;

import org.threeten.bp.LocalDateTime;

public class EventInteraction {
    private String deviceId;
    private int eventId;
    private String interactionType;
    private String timestamp;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getInteractionType() {
        return interactionType;
    }

    public void setInteractionType(String interactionType) {
        this.interactionType = interactionType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    public EventInteraction(String deviceId, int eventId, String interactionType, String timestamp) {
        this.deviceId = deviceId;
        this.eventId = eventId;
        this.interactionType = interactionType;
        this.timestamp = timestamp;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }
}
