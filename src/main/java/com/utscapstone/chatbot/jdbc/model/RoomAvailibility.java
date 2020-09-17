package com.utscapstone.chatbot.jdbc.model;

import java.sql.Date;

public class RoomAvailibility {
    private String roomName;
    private Date date;
    private String availability;

    public RoomAvailibility() {
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }
}
