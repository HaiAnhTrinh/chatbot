package com.utscapstone.chatbot.googleCalendarAPI;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import com.utscapstone.chatbot.Configs;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CalendarServices {
    Calendar service;

    public CalendarServices(String userEmail) throws GeneralSecurityException, IOException {
        service = new Authorization().getService(userEmail);
    }

    //add an event to GG calendar
    public void addEvent(String startTime, String endTime, String[] attendeeEmails) throws IOException {
        System.out.println(startTime);
        System.out.println(endTime);
        Event event = new Event()
                .setSummary("This is the summary")
                .setLocation("Room test")
                .setDescription("This is a description");

        DateTime startDateTime = new DateTime(startTime);
        event.setStart(new EventDateTime().setDateTime(startDateTime));

        DateTime endDateTime = new DateTime(endTime);
        event.setEnd(new EventDateTime().setDateTime(endDateTime));

        EventAttendee[] attendees = new EventAttendee[attendeeEmails.length];
        for(int i=0; i < attendeeEmails.length; i++){
            attendees[i] = new EventAttendee().setEmail(attendeeEmails[i]);
        }
        event.setAttendees(Arrays.asList(attendees));

        String calendarId = "primary";
        service.events().insert(calendarId, event).execute();
    }

    //checks all attendees' availability
    //returns a map with true value if all is available within the time frame
    //otherwise, returns a map with false value and the suggestion time string
    public Map<String, Object> isAllAvailable(String rawDate,
                                              String rawStartTime,
                                              String rawEndTime,
                                              String hostEmail,
                                              String[] attendeeEmails)
            throws IOException {

        boolean available = true;
        String startTime = Utils.convertToRFC5322(rawDate, rawStartTime);
        String endTime = Utils.convertToRFC5322(rawDate, rawEndTime);
        Map<String, Object> freeInfoMap = new HashMap<>();

        //checks if all attendees are available within the time frame
        //each entry refers to an attendee
        FreeBusyResponse response = getFreeBusyResponse(startTime, endTime, hostEmail, attendeeEmails);
        for (Map.Entry<String, FreeBusyCalendar> entry : response.getCalendars().entrySet()) {
            if(!entry.getValue().getBusy().isEmpty()){
                available = false;
                break;
            }
        }

        if(available){
            freeInfoMap.put(Configs.IS_ALL_AVAILABLE, true);
            System.out.println("ALL IS AVAILABLE");
        }
        else{
            int[] timeSlot = getDayFreeBusy(startTime, rawDate, hostEmail, attendeeEmails);

            int duration = Utils.getMeetingDuration(startTime, endTime);
            int count = 0, startSlot = 0;
            //(timeSlot.length - duration) is the last available timeSlot within a single day
            for(int i=0; i < timeSlot.length - duration; i++){
                if(timeSlot[i] == 1 && count < duration){
                    count++;
                }
                else if(timeSlot[i] != 1 && count < duration){
                    startSlot = i+1;
                    count = 0;
                }
                else if(count == duration){
                    break;
                }
            }

            freeInfoMap.put(Configs.IS_ALL_AVAILABLE, false);
//            freeInfoMap.put(Configs.SUGGESTED_START_TIME, "Sorry, the closest available time is from "
//                    + Utils.convertTimeSlotToRFC5322(startSlot)
//                    + " to " + Utils.convertTimeSlotToRFC5322(startSlot + duration) + ".");
            freeInfoMap.put(Configs.SUGGESTED_START_TIME, Utils.convertTimeSlotToRFC5322(startSlot));
            freeInfoMap.put(Configs.SUGGESTED_END_TIME, Utils.convertTimeSlotToRFC5322(startSlot + duration));

            System.out.println("NOT ALL IS AVAILABLE");
        }

        return freeInfoMap;
    }

    //get the busy calendar from the specified startTime
    //endTime is 23:59:59 pm
    private int[] getDayFreeBusy(String startTime,
                                 String rawDate,
                                 String hostEmail,
                                 String[] attendeeEmails) throws IOException {

        //time slot array
        int[] timeSlot = new int[Configs.TIME_SLOT_NUMBER];
        Arrays.fill(timeSlot,Utils.convertRFC5322ToTimeSlot(startTime), timeSlot.length, 1);

        String endTime = Utils.convertToRFC5322(rawDate, "23:59:59+10:00");
        FreeBusyResponse response = getFreeBusyResponse(startTime, endTime, hostEmail, attendeeEmails);

        for (Map.Entry<String, FreeBusyCalendar> entry : response.getCalendars().entrySet()) {
            //iterate the busy array
            for (TimePeriod busyPeriod : entry.getValue().getBusy()){
                int startIndex = Utils.convertRFC5322ToTimeSlot(busyPeriod.getStart().toString());
                int endIndex = Utils.convertRFC5322ToTimeSlot(busyPeriod.getEnd().toString());
                int[] tempTimeSlot = new int[Configs.TIME_SLOT_NUMBER];
                Arrays.fill(tempTimeSlot, 1);
                Arrays.fill(tempTimeSlot, startIndex, endIndex, 0);
                for(int i=0; i< timeSlot.length; i++){
                    timeSlot[i] = timeSlot[i] * tempTimeSlot[i];
                }
            }
        }

        return timeSlot;
    }

    //build and execute the freeBusy request
    private FreeBusyResponse getFreeBusyResponse(String startTime,
                                                 String endTime,
                                                 String hostEmail,
                                                 String[] attendeeEmails) throws IOException {
        FreeBusyRequest freeBusyRequest = new FreeBusyRequest();
        freeBusyRequest
                .setTimeMin(new DateTime(startTime))
                .setTimeMax(new DateTime(endTime))
                .setTimeZone("+10:00");
        FreeBusyRequestItem[] freeBusyRequestItems = new FreeBusyRequestItem[attendeeEmails.length + 1];

        for(int i=0; i < freeBusyRequestItems.length; i++){
            if(i == freeBusyRequestItems.length - 1){
                //the host
                freeBusyRequestItems[i] = new FreeBusyRequestItem().setId(hostEmail);
                break;
            }
            freeBusyRequestItems[i] = new FreeBusyRequestItem().setId(attendeeEmails[i]);
        }
        freeBusyRequest.setItems(Arrays.asList(freeBusyRequestItems));

        return service.freebusy().query(freeBusyRequest).execute();
    }

}
