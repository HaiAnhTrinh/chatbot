package com.utscapstone.chatbot.googleCalendarAPI;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import com.utscapstone.chatbot.Configs;
import com.utscapstone.chatbot.Utils;
import com.utscapstone.chatbot.dialogflowAPI.entities.response.CardResponseObject;
import com.utscapstone.chatbot.jdbc.repository.RoomAvailabilityRepository;
import com.utscapstone.chatbot.jdbc.repository.RoomRepository;
import com.utscapstone.chatbot.jdbc.repository.UserRepository;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

public class CalendarServices {
    Calendar service;
    String userEmail;

    public CalendarServices(String userEmail) throws GeneralSecurityException, IOException {
        service = new Authorization().getService(userEmail);
        this.userEmail = userEmail;
    }

    //add an event to Google calendar
    public void addEvent(String startTime, String endTime, String[] attendeeEmails, String location, String title) throws IOException {
        Event event = new Event()
                .setSummary(title)
                .setLocation(location)
                .setDescription("Default description");
        DateTime startDateTime = new DateTime(startTime);
        DateTime endDateTime = new DateTime(endTime);
        EventAttendee[] attendees = new EventAttendee[attendeeEmails.length];
        for(int i=0; i < attendeeEmails.length; i++){
            attendees[i] = new EventAttendee().setEmail(attendeeEmails[i]);
        }

        event.setStart(new EventDateTime().setDateTime(startDateTime));
        event.setEnd(new EventDateTime().setDateTime(endDateTime));
        event.setAttendees(Arrays.asList(attendees));
        service.events().insert(Configs.PRIMARY_CALENDAR, event).execute();
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
    //endTime is 23:59:59 pm to restrict looking for
    private int[] getDayFreeBusy(String startTime,
                                 String rawDate,
                                 String hostEmail,
                                 String[] attendeeEmails) throws IOException {

        //time slot array
        int[] timeSlot = new int[Configs.TIME_SLOT_NUMBER];
        Arrays.fill(timeSlot,Utils.convertRFC3339ToTimeSlot(startTime), timeSlot.length, 1);

        String endTime = Utils.convertToRFC3339(rawDate, "23:59:59+11:00");
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
                .setTimeZone("+11:00");
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
    public CardResponseObject.Card getMeetingInfo(String viewType) throws IOException {

        CardResponseObject cardResponseObject = new CardResponseObject();
        CardResponseObject.Card card = cardResponseObject.new Card();
        LinkedList<CardResponseObject.Card.Button> buttons = new LinkedList<>();

        Events events = service.events()
                .list(Configs.PRIMARY_CALENDAR)
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
            if(viewType.equals(Configs.VIEW_AUTHORIZED_MEETING)){
                if(event.getOrganizer().getEmail().equals(userEmail)){
                    buttons.add(button);
                }
            }
            else {
                buttons.add(button);
            }
        }

        card.setButtons(buttons);
        return card;
    }

    //cancel a meeting
    public void cancelMeeting(String eventId, RoomAvailabilityRepository repository) throws IOException {
        service.events().delete(Configs.PRIMARY_CALENDAR, eventId).execute();
        Event event = service.events().get(Configs.PRIMARY_CALENDAR, eventId).execute();

        repository.updateAvailability(event.getLocation(),
                Utils.getTimeFromRFC3339(event.getStart().getDateTime().toString()),
                Utils.getTimeFromRFC3339(event.getEnd().getDateTime().toString()),
                Utils.getDateFromRFC3339(event.getStart().getDateTime().toString()),
                Configs.AVAILABILITY_DETELE);
    }

    //update a meeting
    public boolean updateMeetingTime(String eventId, String rawDate, String rawStartTime, String rawEndTime) throws IOException {

        Event event = service.events().get(Configs.PRIMARY_CALENDAR, eventId).execute();

        //temporary delete the meeting for availability checking
        service.events().delete(Configs.PRIMARY_CALENDAR, eventId).execute();

        List<EventAttendee> eventAttendees = event.getAttendees();
        String[] attendeeEmails = new String[eventAttendees != null ? eventAttendees.size() : 0];
        int index = 0;
        String newRawDate = (rawDate != null) ? rawDate : Utils.getDateFromRFC3339(event.getStart().getDateTime().toStringRfc3339());
        String newRawStartTime = (rawStartTime != null) ? rawStartTime : Utils.getTimeFromRFC3339(event.getStart().getDateTime().toStringRfc3339());
        String newRawEndTime = (rawEndTime != null) ? rawEndTime : Utils.getTimeFromRFC3339(event.getEnd().getDateTime().toStringRfc3339());
        String organizerEmail = event.getOrganizer().getEmail();

        if(eventAttendees != null){
            for(EventAttendee a : eventAttendees){
                attendeeEmails[index] = a.getEmail();
                index++;
            }
        }

        Map<String, Object> freeMap = isAllAvailable(newRawDate, newRawStartTime, newRawEndTime, organizerEmail, attendeeEmails);
        boolean check = Boolean.parseBoolean(freeMap.get(Configs.IS_ALL_AVAILABLE).toString());
        //reimport the deleted event
        service.events().calendarImport(Configs.PRIMARY_CALENDAR, event).execute();

        if(check){
            DateTime startDateTime = new DateTime(Utils.convertToRFC3339(newRawDate, newRawStartTime));
            DateTime endDateTime = new DateTime(Utils.convertToRFC3339(newRawDate, newRawEndTime));
            event.setStart(new EventDateTime().setDateTime(startDateTime));
            event.setEnd(new EventDateTime().setDateTime(endDateTime));

            service.events().update(Configs.PRIMARY_CALENDAR, eventId, event).execute();
            System.out.println("EVENT UPDATED");
            return true;
        }

        return false;
    }

    //get the title for a meeting
    public String getMeetingTitle(String eventId) throws IOException {
        return service.events().get(Configs.PRIMARY_CALENDAR, eventId).execute().getSummary();
    }

    //update the title for a meeting
    public void updateMeetingTitle(String eventId, String newTitle) throws IOException {
        Event event = service.events().get(Configs.PRIMARY_CALENDAR, eventId).execute();
        event.setSummary(newTitle);
        service.events().update(Configs.PRIMARY_CALENDAR, eventId, event).execute();
    }

    //get the location for a meeting
    public String getMeetingLocation(String eventId) throws IOException {
        return service.events().get(Configs.PRIMARY_CALENDAR, eventId).execute().getLocation();
    }

    //update the location for a meeting
    //return 0 if the new room is valid, 1 if not enough capacity, 2 if is not available
    public int updateMeetingLocation(String eventId, String newLocation, RoomRepository roomRepository, RoomAvailabilityRepository roomAvailabilityRepository) throws IOException {
        Event event = service.events().get(Configs.PRIMARY_CALENDAR, eventId).execute();
        String date = Utils.getDateFromRFC3339(event.getStart().getDateTime().toString());
        String startTime = Utils.getTimeFromRFC3339(event.getStart().getDateTime().toString());
        String endTime = Utils.getTimeFromRFC3339(event.getEnd().getDateTime().toString());
        int numberOfParticipants = event.getAttendees() != null ? event.getAttendees().size(): 0;
        boolean canFit = roomRepository.canRoomFit(newLocation, numberOfParticipants);
        boolean isAvailable = roomAvailabilityRepository.isRoomAvailable(newLocation, startTime, endTime, date);

        if(canFit && isAvailable){
            roomAvailabilityRepository.updateAvailability(event.getLocation(), startTime, endTime, date, Configs.AVAILABILITY_DETELE);
            roomAvailabilityRepository.updateAvailability(newLocation, startTime, endTime, date, Configs.AVAILABILITY_INSERT);
            event.setLocation(newLocation);
            service.events().update(Configs.PRIMARY_CALENDAR, eventId, event).execute();
        }

        if(canFit && isAvailable){
            return 0;
        }
        else if(!canFit) {
            return 1;
        }
        else {
            return 2;
        }
    }

    //get the current participants of a meeting
    //return a string containing all participants' names
    public String getParticipants(String eventId, UserRepository userRepository) throws IOException {
        Event event = service.events().get(Configs.PRIMARY_CALENDAR, eventId).execute();
        List<EventAttendee> attendees = event.getAttendees();
        LinkedList<String> attendeeNames = new LinkedList<>();

        if(attendees == null){
            return "";
        }
        else{
            for(EventAttendee attendee : attendees){
                if(!attendee.getEmail().equals(event.getOrganizer().getEmail())){
                    attendeeNames.add(userRepository.getNameFromEmail(attendee.getEmail()));
                }
            }
            return attendeeNames.toString().substring(1, attendeeNames.toString().length()-1);
        }
    }

    //add participants to a meeting
    public String addParticipants(String eventId, String[] addNames, UserRepository userRepository, RoomRepository roomRepository) throws IOException {
        Event event = service.events().get(Configs.PRIMARY_CALENDAR, eventId).execute();
        List<EventAttendee> attendees = event.getAttendees() != null ? event.getAttendees() : new LinkedList<>();
        System.out.println("ATTENDEE SIZE: " + attendees.size());
        String[] addEmails = userRepository.getEmailsFromNames(addNames).get("resultArray");
        String[] attendeeEmails = new String[addEmails.length + attendees.size()];
        int index=0;

        for(EventAttendee attendee : attendees){
            attendeeEmails[index] = attendee.getEmail();
            index++;
        }

        for(int i=index; i < attendeeEmails.length; i++){
            attendeeEmails[i] = addEmails[i-index];
        }

        //temporary delete the meeting for availability checking
        service.events().delete(Configs.PRIMARY_CALENDAR, eventId).execute();
        Map<String, Object> availableMap =
                isAllAvailable(Utils.getDateFromRFC3339(event.getStart().getDateTime().toString()),
                        Utils.getTimeFromRFC3339(event.getStart().getDateTime().toString()),
                        Utils.getTimeFromRFC3339(event.getEnd().getDateTime().toString()),
                        event.getOrganizer().getEmail(),
                        attendeeEmails);
        boolean isAllAvailable = Boolean.parseBoolean(availableMap.get(Configs.IS_ALL_AVAILABLE).toString());
        boolean canRoomFit = roomRepository.canRoomFit(event.getLocation(), attendeeEmails.length);
        //reimport the deleted event
        service.events().calendarImport(Configs.PRIMARY_CALENDAR, event).execute();

        if(isAllAvailable){
            if(canRoomFit){
                for(String addEmail : addEmails){
                    attendees.add(new EventAttendee().setEmail(addEmail));
                }
                event.setAttendees(attendees);
                service.events().update(Configs.PRIMARY_CALENDAR, event.getId(), event).execute();
                return "New participants added";
            }
            else {
                String suggestedRoom = roomRepository.lookForEnoughCapacityAndAvailableRoom(Utils.getTimeFromRFC3339(event.getStart().getDateTime().toString()),
                        Utils.getTimeFromRFC3339(event.getEnd().getDateTime().toString()),
                        Utils.getDateFromRFC3339(event.getStart().getDateTime().toString()),
                        attendeeEmails.length+1);
                return "The current room does not have enough capacity." + (!suggestedRoom.isEmpty() ? "I suggest moving the location to " + suggestedRoom : "But I cannot find any room");
            }
        }
        else {
            return "Someone is unvailable, I suggest you consider changing the meeting time to be from "
                    + availableMap.get(Configs.SUGGESTED_START_TIME).toString() +
                    " to " + availableMap.get(Configs.SUGGESTED_END_TIME).toString() + ".";
        }
    }

    //remove participants from a meeting
    public void removeParticipants(String eventId, String[] removeNames, UserRepository userRepository) throws IOException {
        Event event = service.events().get(Configs.PRIMARY_CALENDAR, eventId).execute();
        List<EventAttendee> attendees = event.getAttendees();
        String[] removeEmails = userRepository.getEmailsFromNames(removeNames).get("resultArray");

        for (EventAttendee a : attendees){
            System.out.println("event attendees: " + a.getEmail());
        }

        for(String email : removeEmails){
            System.out.println("remove email: " + email);
            attendees.removeIf(attendee -> Objects.equals(email, attendee.getEmail()));
        }
        for (EventAttendee a : attendees){
            System.out.println("remaining attendees: " + a.getEmail());
        }

        event.setAttendees(attendees);
        service.events().update(Configs.PRIMARY_CALENDAR, event.getId(), event).execute();
    }

}