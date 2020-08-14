package com.utscapstone.chatbot.googleCalendarAPI;

import com.utscapstone.chatbot.entities.request.Request;

public class Utils {

    static public String getStartTimeFromRequest(Request request){
        return getTimeFromRFC5322(request.getQueryResult().getParameters().getStartTime()[0]);
    }

    static public String getEndTimeFromRequest(Request request){
        return getTimeFromRFC5322(request.getQueryResult().getParameters().getEndTime()[0]);
    }

    static public String getDateFromRequest(Request request){
        return getDateFromRFC5322(request.getQueryResult().getParameters().getDate()[0]);
    }

    static public String[] getAttendeeEmailsFromRequest(Request request){
        return request.getQueryResult().getParameters().getAttendeeEmails();
    }

    static public String convertToRFC5322(String date, String time){
        return date.concat("T").concat(time);
    }

    static public String getTimeFromRFC5322(String rfc5322){
        return rfc5322.substring(11);
    }

    static public String getDateFromRFC5322(String rfc5322){
        return rfc5322.substring(0, 10);
    }

    static public int convertRFC5322ToTimeSlot(String rfc5322){
        //00:00:00 -> 0
        //00:15:00 -> 1
        //00:30:00 -> 2
        //00:45:00 -> 3
        //01:00:00 -> 4
        //01:15:00 -> 5
        String time = getTimeFromRFC5322(rfc5322);
        int hour = Integer.parseInt(time.substring(0,2));
        int minute = Integer.parseInt(time.substring(3,5));

        return hour*4 + minute/15;
    }

    static public String convertTimeSlotToRFC5322(int timeSlot){
        int hour = timeSlot/4;
        int minute = Math.floorMod(timeSlot, 4) * 15;

        return String.format("%02d", hour) + ":" + String.format("%02d", minute) + ":00+10:00";
    }

    static public int getMeetingDuration(String startTime, String endTime){
        return convertRFC5322ToTimeSlot(endTime) - convertRFC5322ToTimeSlot(startTime);
    }
}
