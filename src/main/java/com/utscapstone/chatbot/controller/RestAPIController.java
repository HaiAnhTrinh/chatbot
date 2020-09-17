package com.utscapstone.chatbot.controller;

import com.utscapstone.chatbot.Configs;
import com.utscapstone.chatbot.Utils;
import com.utscapstone.chatbot.dialogflowAPI.DialogflowServices;
import com.utscapstone.chatbot.dialogflowAPI.entities.request.OutputContexts;
import com.utscapstone.chatbot.dialogflowAPI.entities.request.Request;
import com.utscapstone.chatbot.dialogflowAPI.entities.response.Response;
import com.utscapstone.chatbot.dialogflowAPI.entities.response.ResponseObject;
import com.utscapstone.chatbot.googleCalendarAPI.CalendarServices;
import com.utscapstone.chatbot.jdbc.repository.RoomAvailabilityRepository;
import com.utscapstone.chatbot.jdbc.repository.RoomRepository;
import com.utscapstone.chatbot.jdbc.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;


@RestController
public class RestAPIController {

    final UserRepository userRepository;
    final RoomRepository roomRepository;
    final RoomAvailabilityRepository roomAvailabilityRepository;

    //explicitly instantiate repositories instead of @Autowire
    //to avoid strange behaviours from not being able to auto configure jdbc
    public RestAPIController(JdbcTemplate jdbcTemplate) {
        this.userRepository = new UserRepository(jdbcTemplate);
        this.roomRepository = new RoomRepository(jdbcTemplate);
        this.roomAvailabilityRepository = new RoomAvailabilityRepository(jdbcTemplate);
    }


    @CrossOrigin(origins = "*")
    @GetMapping("/test")
    public void test(){
        String[] array = new String[]{"hai anh", "hoang le"};
        System.out.println("array: " + Arrays.toString(array).substring(1, Arrays.toString(array).length()-1));

    }

    @CrossOrigin(origins = "*")
    @PostMapping("/")
    public ResponseEntity<Response> processRequest(@RequestBody Request request) throws GeneralSecurityException, IOException {

        System.out.println("INTENT: " + request.getQueryResult().getIntent().getDisplayName());
//        String facebookId = request.getOriginalDetectIntentRequest().getPayload().getData().getSender().getId();
        String facebookId = "3771109326251927";
        String email = userRepository.getEmailFromFacebookId(facebookId);


        //follows the hierachy of Dialogflow response structure
        ResponseEntity<Response> response = new ResponseEntity<>(new Response(), HttpStatus.OK);
        LinkedList<ResponseObject> responseObjects = new LinkedList<>();

        switch (request.getQueryResult().getIntent().getDisplayName()) {
            case "Book a meeting - Retrieve info": {

                String rawDate = Utils.getDateFromRequest(request);
                String rawStartTime = Utils.getStartTimeFromRequest(request);
                String rawEndTime = Utils.getEndTimeFromRequest(request);
                String[] attendeeNames = Utils.getAttendeeNamesFromRequest(request);
                String location = Utils.getLocationFromRequest(request);
                LinkedList<OutputContexts> outputContexts = new LinkedList<>();

                assert attendeeNames != null;
                Map<String, String[]> attendeeMap = userRepository.getEmailsFromNames(attendeeNames);

                //not all users are known in the database
                if(Boolean.parseBoolean(attendeeMap.get("hasUnknown")[0])){

                    String[] unknowns = attendeeMap.get("resultArray");
                    DialogflowServices.addTextResponse(responseObjects,
                            "I don't have any records for " +
                                    Arrays.toString(unknowns).substring(1, Arrays.toString(unknowns).length()-1)
                            );
                    //dynamically reset the context
                    outputContexts = DialogflowServices.resetContext(request);
                    Objects.requireNonNull(response.getBody()).setOutputContexts(outputContexts);
                    break;
                }
                else {
                    String[] attendeeEmails = attendeeMap.get("resultArray");
                    CalendarServices services = new CalendarServices(email);
                    Map<String, Object> freeMap = services.isAllAvailable(rawDate,
                            rawStartTime, rawEndTime,email, attendeeEmails);

                    //check the availability of all attendees (primary calendar)
                    if(Boolean.parseBoolean(freeMap.get(Configs.IS_ALL_AVAILABLE).toString())){
                        DialogflowServices.addTextResponse(responseObjects, "All attendees are available for the meeting");
                    }
                    //if invalid, look for the most recent available time slot
                    else{
                        assert rawDate != null;
                        //set the new time for the context
                        OutputContexts outputContext = request.getQueryResult().getOutputContexts().get(0);
                        outputContext.getParameters().setStartTime(new String[]{Utils.convertToRFC3339(rawDate, freeMap.get(Configs.SUGGESTED_START_TIME).toString())});
                        outputContext.getParameters().setEndTime(new String[]{Utils.convertToRFC3339(rawDate, freeMap.get(Configs.SUGGESTED_END_TIME).toString())});
                        outputContexts.add(outputContext);
                        Objects.requireNonNull(response.getBody()).setOutputContexts(outputContexts);

                        rawStartTime = freeMap.get(Configs.SUGGESTED_START_TIME).toString();
                        rawEndTime= freeMap.get(Configs.SUGGESTED_END_TIME).toString();

                        DialogflowServices.addTextResponse( responseObjects,
                                "Sorry, the closest available time for all attendees is from "
                                        + rawStartTime + " to " + rawEndTime + ".");
                    }

                    //if there is a location, check the location
                    if(!location.isEmpty()){
                        if(roomRepository.canRoomFit(location, attendeeNames.length)){
                            if(roomAvailabilityRepository.isRoomAvailable(location, rawStartTime, rawEndTime, rawDate)){
                                DialogflowServices.addTextResponse(responseObjects, "The room is also available at the specified time");
                            }
                            else{
                                //if room can fit but not available, look for a location
                                LinkedList<String> rooms = roomAvailabilityRepository.lookForAvailableRooms(rawStartTime, rawEndTime, rawDate);
                                String suggestedRoom = ! rooms.isEmpty() ? rooms.getFirst() : "";

                                if(suggestedRoom.isEmpty()){
                                    DialogflowServices.addTextResponse(responseObjects,
                                            "Looks like I cannot find any room available at the meeting time for you");
                                    //dynamically reset the context
                                    outputContexts = DialogflowServices.resetContext(request);
                                    Objects.requireNonNull(response.getBody()).setOutputContexts(outputContexts);
                                    break;
                                }
                                else{
                                    //set the new location for the context
                                    OutputContexts outputContext = request.getQueryResult().getOutputContexts().get(0);
                                    outputContext.getParameters().setLocation(suggestedRoom);
                                    outputContexts.add(outputContext);
                                    Objects.requireNonNull(response.getBody()).setOutputContexts(outputContexts);

                                    DialogflowServices.addTextResponse(responseObjects,
                                            "The specified room is not available at the meeting time. I suggest "
                                                    + suggestedRoom + " instead");

                                }
                            }
                        }
                        else{
                            //if room cannot fit, look for a location
                            String suggestedRoom = roomRepository.lookForEnoughCapacityAndAvailableRoom(rawStartTime,rawEndTime,rawDate,attendeeNames.length);

                            if(suggestedRoom.isEmpty()){
                                DialogflowServices.addTextResponse(responseObjects,
                                        "There is no room with enough capacity at the meeting time.");
                                //dynamically reset the context
                                outputContexts = DialogflowServices.resetContext(request);
                                Objects.requireNonNull(response.getBody()).setOutputContexts(outputContexts);
                                break;
                            }
                            else {
                                //set the new location for the context
                                OutputContexts outputContext = request.getQueryResult().getOutputContexts().get(0);
                                outputContext.getParameters().setLocation(suggestedRoom);
                                outputContexts.add(outputContext);
                                Objects.requireNonNull(response.getBody()).setOutputContexts(outputContexts);

                                DialogflowServices.addTextResponse(responseObjects,
                                        "The specified room does not have enough capacity. I suggest "
                                                + suggestedRoom + " instead");
                            }
                        }
                    }
                    //if no location, look for a location
                    else{
                        String suggestedRoom = roomRepository.lookForEnoughCapacityAndAvailableRoom(rawStartTime,rawEndTime,rawDate,attendeeNames.length);

                        if(suggestedRoom.isEmpty()){
                            DialogflowServices.addTextResponse(responseObjects,
                                    "I cannot find any room that can hold "
                                            + (attendeeNames.length+1)
                                            + " people at the specified meeting time.");
                            //dynamically reset the context
                            outputContexts = DialogflowServices.resetContext(request);
                            Objects.requireNonNull(response.getBody()).setOutputContexts(outputContexts);
                            break;
                        }
                        else {
                            //set the new location for the context
                            OutputContexts outputContext = request.getQueryResult().getOutputContexts().get(0);
                            outputContext.getParameters().setLocation(suggestedRoom);
                            outputContexts.add(outputContext);
                            Objects.requireNonNull(response.getBody()).setOutputContexts(outputContexts);

                            DialogflowServices.addTextResponse(responseObjects,
                                    "I will allocate your meeting to " + suggestedRoom);
                        }
                    }
                }

                DialogflowServices.addTextResponse(responseObjects, "Do you want me to process the booking?");

                break;
            }
            case "Book a meeting - Confirm booking": {
                //if user confirms
                if(request.getQueryResult().getParameters().getConfirmBoolean()[0].equals("True")){

                    String rawDate = Utils.getDateFromOutputContexts(request);
                    String rawStartTime = Utils.getStartTimeFromOutputContexts(request);
                    String rawEndTime = Utils.getEndTimeFromOutputContexts(request);
                    String location = Utils.getLocationFromOutputContexts(request);
                    String title = Utils.getTitleFromOutputContexts(request);

                    String startTime = Utils.convertToRFC3339(rawDate, rawStartTime);
                    String endTime = Utils.convertToRFC3339(rawDate, rawEndTime);
                    String[] attendeeNames = Utils.getAttendeeNamesFromOutputContexts(request);
                    String[] attendeeEmails = userRepository.getEmailsFromNames(attendeeNames).get("resultArray");

                    //add the event to the calendar
                    CalendarServices services = new CalendarServices(email);
                    services.addEvent(startTime, endTime, attendeeEmails, location, title);

                    //update room database
                    roomAvailabilityRepository.updateAvailability(location, rawStartTime, rawEndTime, rawDate, Configs.UPDATE_INSERT);

                    DialogflowServices.addTextResponse(responseObjects,"Ok, the meeting has been successfully booked.");
                }
                else {
                    DialogflowServices.addTextResponse(responseObjects, "OK, I will look for another time.");
                    //TODO: find the next suitable time slot
                }
                break;

            }
            case "Cancel a meeting": {
                boolean noMeeting = DialogflowServices.showMeetings(responseObjects, email,
                        Configs.VIEW_AUTHORIZED_MEETING,
                        "There is no meeting that you can cancel!!!",
                        "Meetings you can cancel");
                if(noMeeting){
                    //dynamically reset the context
                    LinkedList<OutputContexts> outputContexts = DialogflowServices.resetContext(request);
                    Objects.requireNonNull(response.getBody()).setOutputContexts(outputContexts);
                }
                break;
            }
            case "Cancel a meeting - userChoice": {
                CalendarServices services = new CalendarServices(email);
                //the queryText is the event id
                services.cancelMeeting(request.getQueryResult().getParameters().getEventId(), roomAvailabilityRepository);
                DialogflowServices.addTextResponse(responseObjects, "Ok I have deleted that meeting for you");
                break;
            }
            case "View meetings": {
                //show event date and time
                DialogflowServices.showMeetings(responseObjects, email,
                        Configs.VIEW_ALL_MEETING,
                        "You have no incoming schedule!!!",
                        "Your schedule");
                break;
            }
            case "Update a meeting": {
                //shows incoming meetings
                boolean noMeeting = DialogflowServices.showMeetings(responseObjects, email,
                        Configs.VIEW_AUTHORIZED_MEETING,
                        "You have no incoming schedule!!!",
                        "Choose which one to update");
                if(noMeeting){
                    System.out.println("NO MEETING");
                    //dynamically reset the context
                    LinkedList<OutputContexts> outputContexts = DialogflowServices.resetContext(request);
                    Objects.requireNonNull(response.getBody()).setOutputContexts(outputContexts);
                }
                break;
            }
            case "Update a meeting - DateTime - GetInfo": {
                //TODO: check the room db
                CalendarServices services = new CalendarServices(email);
                String rawDate = Utils.getDateFromRequest(request);
                String rawStartTime = Utils.getStartTimeFromRequest(request);
                String rawEndTime = Utils.getEndTimeFromRequest(request);
                String eventId = DialogflowServices.getEventIdFromOutputContext(request);
                System.out.println("EVENT ID: " + eventId);

                if(eventId != null){
                    if(services.updateMeetingTime(eventId, rawDate, rawStartTime, rawEndTime)){
                        DialogflowServices.addTextResponse(responseObjects, "The meeting time has been updated");
                    }
                    else {
                        DialogflowServices.addTextResponse(responseObjects, "The new time is conflicting with others availibility");
                    }
                }
                else {
                    DialogflowServices.addTextResponse(responseObjects, Configs.ERROR_MESSAGE);
                }

                break;
            }
            case "Update a meeting - Title - GetInfo": {
                CalendarServices services = new CalendarServices(email);
                String title = request.getQueryResult().getQueryText();
                String eventId = DialogflowServices.getEventIdFromOutputContext(request);

                if(eventId != null){
                    services.updateMeetingTitle(eventId, title);
                    DialogflowServices.addTextResponse(responseObjects, "The meeting title has been updated");
                }
                else {
                    DialogflowServices.addTextResponse(responseObjects, Configs.ERROR_MESSAGE);
                }

                break;
            }
            case "View rooms": {
                DialogflowServices.addTextResponse(responseObjects, roomRepository.viewAllRooms());
                break;
            }
            default:
                DialogflowServices.addTextResponse(responseObjects, "NO MATCHED INTENT!!!");
                break;
        }

        Objects.requireNonNull(response.getBody()).setFulfillmentMessages(responseObjects);

        return response;
    }
}
