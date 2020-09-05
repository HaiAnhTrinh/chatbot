package com.utscapstone.chatbot.googleCalendarAPI;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import com.utscapstone.chatbot.Configs;
import com.utscapstone.chatbot.Utils;
import com.utscapstone.chatbot.dialogflowAPI.entities.response.CardResponseObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

public class CalendarServices {
    Calendar service;

    public CalendarServices(String userEmail) throws GeneralSecurityException, IOException {
        service = new Authorization().getService(userEmail);
    }

    //add an event to Google calendar
    public void addEvent(String startTime, String endTime, String[] attendeeEmails) throws IOException {
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
        String startTime = Utils.convertToRFC3339(rawDate, rawStartTime);
        String endTime = Utils.convertToRFC3339(rawDate, rawEndTime);
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
            freeInfoMap.put(Configs.SUGGESTED_START_TIME, Utils.convertTimeSlotToRFC3339(startSlot));
            freeInfoMap.put(Configs.SUGGESTED_END_TIME, Utils.convertTimeSlotToRFC3339(startSlot + duration));

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
        Arrays.fill(timeSlot,Utils.convertRFC3339ToTimeSlot(startTime), timeSlot.length, 1);

        String endTime = Utils.convertToRFC3339(rawDate, "23:59:59+10:00");
        FreeBusyResponse response = getFreeBusyResponse(startTime, endTime, hostEmail, attendeeEmails);

        for (Map.Entry<String, FreeBusyCalendar> entry : response.getCalendars().entrySet()) {
            //iterate the busy array
            for (TimePeriod busyPeriod : entry.getValue().getBusy()){
                int startIndex = Utils.convertRFC3339ToTimeSlot(busyPeriod.getStart().toString());
                int endIndex = Utils.convertRFC3339ToTimeSlot(busyPeriod.getEnd().toString());
                int[] tempTimeSlot = new int[Configs.TIME_SLOT_NUMBER];

                //the timeslot array will have all slots available by default, value 1 is available
                Arrays.fill(tempTimeSlot, 1);
                //fill in the unavailable slots
                Arrays.fill(tempTimeSlot, startIndex, endIndex, 0);
                //merge with the main array
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

    //retrieve meeting info
    public CardResponseObject.Card getMeetingInfo() throws IOException {

        CardResponseObject cardResponseObject = new CardResponseObject();
        CardResponseObject.Card card = cardResponseObject.new Card();
        LinkedList<CardResponseObject.Card.Button> buttons = new LinkedList<>();

        Events events = service.events()
                .list("primary")
                .setTimeMin(new DateTime(System.currentTimeMillis()))
                .setMaxResults(10)
                .setSingleEvents(true)
                .setOrderBy("startTime")
                .execute();
        List<Event> items = events.getItems();

        for(Event event : items){
            CardResponseObject.Card.Button button = card.new Button();
            button.setPostback(event.getId());
            button.setText(Utils.getDateFromRFC3339(event.getStart().getDateTime().toString()).substring(5)
                    + " (" + Utils.getTimeFromRFC3339(event.getStart().getDateTime().toString()).substring(0,5)
                    + " to " + Utils.getTimeFromRFC3339(event.getEnd().getDateTime().toString()).substring(0,5) + ")");
            buttons.add(button);
        }

        card.setButtons(buttons);
        return card;
    }

    //cancel a meeting
    public void cancelMeeting(String eventId) throws IOException {
        service.events().delete("primary", eventId).execute();
    }

    //update a meeting
    public boolean updateMeetingTime(String eventId, String rawDate, String rawStartTime, String rawEndTime) throws IOException {

        Event event = service.events().get("primary", eventId).execute();
        List<EventAttendee> eventAttendees = event.getAttendees();
        String[] attendeeEmails = new String[eventAttendees.size()];
        int index = 0;
        String newRawDate = rawDate != null ? rawDate : Utils.getDateFromRFC3339(event.getStart().getDateTime().toStringRfc3339());
        String newRawStartTime = rawStartTime != null ? rawStartTime : Utils.getTimeFromRFC3339(event.getStart().getDateTime().toStringRfc3339());
        String newRawEndTime = rawEndTime != null ? rawEndTime : Utils.getTimeFromRFC3339(event.getEnd().getDateTime().toStringRfc3339());
        String organizerEmail = event.getOrganizer().getEmail();

        for(EventAttendee a : eventAttendees){
            attendeeEmails[index] = a.getEmail();
        }

        Map<String, Object> freeMap = isAllAvailable(newRawDate, newRawStartTime, newRawEndTime, organizerEmail, attendeeEmails);

        if(Boolean.parseBoolean(freeMap.get(Configs.IS_ALL_AVAILABLE).toString())){
            DateTime startDateTime = new DateTime(Utils.convertToRFC3339(newRawDate, newRawStartTime));
            DateTime endDateTime = new DateTime(Utils.convertToRFC3339(newRawDate, newRawEndTime));
            event.setStart(new EventDateTime().setDateTime(startDateTime));
            event.setEnd(new EventDateTime().setDateTime(endDateTime));

            service.events().update("primary", eventId, event).execute();
            System.out.println("EVENT UPDATED");
            return true;
        }

        return false;
    }

    public void updateMeetingTitle(String eventId, String newTitle) throws IOException {
        Event event = service.events().get("primary", eventId).execute();
        event.setSummary(newTitle);
        service.events().update("primary", eventId, event).execute();
    }

}