package com.utscapstone.chatbot.entities.request;

public class Parameters {

    private String[] date;
    private String[] startTime;
    private String[] endTime;
    private String[] attendeeEmails;

    public String[] getDate() {
        return date;
    }

    public String[] getStartTime() {
        return startTime;
    }

    public String[] getEndTime() {
        return endTime;
    }

    public String[] getAttendeeEmails() {
        return attendeeEmails;
    }

}
