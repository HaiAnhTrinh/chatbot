package com.utscapstone.chatbot.entities.request;

public class Parameters {

    private String[] date;
    private String[] startTime;
    private String[] endTime;
    private String[] attendeeEmails;
    private String[] confirmBoolean;

    public String[] getDate() {
        return date;
    }

    public void setDate(String[] date) {
        this.date = date;
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

    public void setAttendeeEmails(String[] attendeeEmails) {
        this.attendeeEmails = attendeeEmails;
    }

    public String[] getConfirmBoolean() {
        return confirmBoolean;
    }

    public void setConfirmBoolean(String[] confirmBoolean) {
        this.confirmBoolean = confirmBoolean;
    }
}
