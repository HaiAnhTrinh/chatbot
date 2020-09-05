package com.utscapstone.chatbot.dialogflowAPI.entities.request;

public class Parameters {

    private String[] date;
    private String[] startTime;
    private String[] endTime;
    private String[] attendeeEmails;
    private String[] confirmBoolean;
    private String eventId;

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

    public String[] getAttendeeEmails() {
        return attendeeEmails;
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
}
