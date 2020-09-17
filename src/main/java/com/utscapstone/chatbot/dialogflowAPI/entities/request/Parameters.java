package com.utscapstone.chatbot.dialogflowAPI.entities.request;

public class Parameters {

    private String[] date;
    private String[] startTime;
    private String[] endTime;
    private String[] attendeeNames;
    private String[] confirmBoolean;
    private String eventId;
    private String location;
    private String title;

    public String[] getDate() {
        return date;
    }

    public String[] getStartTime() {
        return startTime;
    }

    public void setStartTime(String[] startTime) {
        this.startTime = startTime;
    }

    public String[] getEndTime() {
        return endTime;
    }

    public void setEndTime(String[] endTime) {
        this.endTime = endTime;
    }

    public String[] getAttendeeNames() {
        return attendeeNames;
    }

    public String[] getConfirmBoolean() {
        return confirmBoolean;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
