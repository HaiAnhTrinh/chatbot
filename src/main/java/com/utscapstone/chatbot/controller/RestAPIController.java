package com.utscapstone.chatbot.controller;

import com.utscapstone.chatbot.Configs;
import com.utscapstone.chatbot.Utils;
import com.utscapstone.chatbot.dialogflowAPI.AddResponse;
import com.utscapstone.chatbot.dialogflowAPI.DialogflowServices;
import com.utscapstone.chatbot.dialogflowAPI.ValidateInput;
import com.utscapstone.chatbot.dialogflowAPI.entities.request.OutputContexts;
import com.utscapstone.chatbot.dialogflowAPI.entities.request.Request;
import com.utscapstone.chatbot.dialogflowAPI.entities.response.Response;
import com.utscapstone.chatbot.dialogflowAPI.entities.response.ResponseObject;
import com.utscapstone.chatbot.googleCalendarAPI.CalendarServices;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;


@RestController
public class RestAPIController {

    @CrossOrigin(origins = "*")
    @PostMapping("/")
    public ResponseEntity<Response> processRequest(@RequestBody Request request) throws GeneralSecurityException, IOException {

        System.out.println("INTENT: " + request.getQueryResult().getIntent().getDisplayName());
//        System.out.println("originalDetectIntentRequest" + request.getOriginalDetectIntentRequest().getPayload().getData().getSender().getId());

        //follows the hierachy of Dialogflow response structure
        ResponseEntity<Response> response = new ResponseEntity<>(new Response(), HttpStatus.OK);
        LinkedList<ResponseObject> responseObjects = new LinkedList<>();

        switch (request.getQueryResult().getIntent().getDisplayName()) {
            case "Book a meeting - Retrieve info": {

                String rawDate = Utils.getDateFromRequest(request);
                String rawStartTime = Utils.getStartTimeFromRequest(request);
                String rawEndTime = Utils.getEndTimeFromRequest(request);
                String[] attendeeEmails = Utils.getAttendeeEmailsFromRequest(request);

                //check if all params are present
                //TODO: delete this???
                String paramsCheck = ValidateInput.missingParams(rawDate, rawStartTime, rawEndTime, attendeeEmails);
                if(paramsCheck != null ){
                    AddResponse.addTextResponse(responseObjects, paramsCheck);
                    break;
                }

                CalendarServices services = new CalendarServices(Configs.USER_EMAIL);
                Map<String, Object> freeMap = services.isAllAvailable(rawDate,
                        rawStartTime, rawEndTime,Configs.USER_EMAIL, attendeeEmails);

                //check the availability of all attendees (primary calendar)
                //if invalid, look for the most recent available time slot
                if(Boolean.parseBoolean(freeMap.get(Configs.IS_ALL_AVAILABLE).toString())){
                    AddResponse.addTextResponse(responseObjects, "Ok all attendees are available for the meeting");
                    AddResponse.addTextResponse(responseObjects, "Do you want me to process the booking?");
                }
                else{
                    LinkedList<OutputContexts> outputContexts = new LinkedList<>();
                    OutputContexts outputContext = request.getQueryResult().getOutputContexts().get(0);
                    outputContext.getParameters().setStartTime(new String[]{Utils.convertToRFC3339(rawDate, freeMap.get(Configs.SUGGESTED_START_TIME).toString())});
                    outputContext.getParameters().setEndTime(new String[]{Utils.convertToRFC3339(rawDate, freeMap.get(Configs.SUGGESTED_END_TIME).toString())});
                    outputContexts.add(outputContext);
                    Objects.requireNonNull(response.getBody()).setOutputContexts(outputContexts);

                    AddResponse.addTextResponse( responseObjects,
                            "Sorry, the closest available time is from "
                            + freeMap.get(Configs.SUGGESTED_START_TIME) + " to "
                            + freeMap.get(Configs.SUGGESTED_END_TIME)
                            + ".");

                    AddResponse.addTextResponse( responseObjects, "Can I process the booking at the suggested time?");
                }
                break;
            }
            case "Book a meeting - Confirm booking": {
                //if user confirms
                if(request.getQueryResult().getParameters().getConfirmBoolean()[0].equals("True")){
                    //add the event to the calendar
                    String rawDate = Utils.getDateFromOutputContexts(request);

                    String startTime = Utils.convertToRFC3339(rawDate, Utils.getStartTimeFromOutputContexts(request));
                    String endTime = Utils.convertToRFC3339(rawDate, Utils.getEndTimeFromOutputContexts(request));
                    String[] attendeeEmails = Utils.getAttendeeEmailsFromOutputContexts(request);

                    CalendarServices services = new CalendarServices(Configs.USER_EMAIL);
                    services.addEvent(startTime, endTime, attendeeEmails);
                    AddResponse.addTextResponse(responseObjects,"Ok, the meeting has been successfully booked.");
                    //TODO: update room database
                }
                else {
                    AddResponse.addTextResponse(responseObjects, "OK, I will look for another time.");
                    //TODO: find the next suitable time slot
                }
                break;

            }
            case "Cancel a meeting": {

                DialogflowServices.showMeetings(responseObjects,
                        "There is no meeting that you can cancel!!!",
                        "Meetings you can cancel");

                break;
            }
            case "Cancel a meeting - userChoice": {
                CalendarServices services = new CalendarServices(Configs.USER_EMAIL);
                //the queryText is the event id
                services.cancelMeeting(request.getQueryResult().getParameters().getEventId());
                AddResponse.addTextResponse(responseObjects, "Ok I have deleted that meeting for you");
                break;
            }
            case "View meetings": {
                //TODO: may want to display the meetings in some other forms?
                //show event date and time
                DialogflowServices.showMeetings(responseObjects,
                        "You have no incoming schedule!!!",
                        "Your schedule");
                break;
            }
            case "Update a meeting": {
                //shows incoming meetings
                DialogflowServices.showMeetings(responseObjects,
                        "You have no incoming schedule!!!",
                        "Choose which one to update");
                break;
            }
            case "Update a meeting - DateTime - GetInfo": {
                CalendarServices services = new CalendarServices(Configs.USER_EMAIL);
                String rawDate = Utils.getDateFromRequest(request);
                String rawStartTime = Utils.getStartTimeFromRequest(request);
                String rawEndTime = Utils.getEndTimeFromRequest(request);
                String eventId = "";

                for(OutputContexts o : request.getQueryResult().getOutputContexts()){
                    if(o.getName().equals(Configs.CONTEXT_NAME_PREFIX + "updateameeting-eventchosen-followup")){
                        eventId = o.getParameters().getEventId();
                    }
                }

                if(services.updateMeetingTime(eventId, rawDate, rawStartTime, rawEndTime)){
                    AddResponse.addTextResponse(responseObjects, "The meeting time has been updated");
                }
                break;
            }
            case "Update a meeting - Title - GetInfo": {
                CalendarServices services = new CalendarServices(Configs.USER_EMAIL);
                String title = request.getQueryResult().getQueryText();
                String eventId = "";
                for(OutputContexts o : request.getQueryResult().getOutputContexts()){
                    if(o.getName().equals(Configs.CONTEXT_NAME_PREFIX + "updateameeting-eventchosen-followup")){
                        eventId = o.getParameters().getEventId();
                    }
                }

                services.updateMeetingTitle(eventId, title);
                AddResponse.addTextResponse(responseObjects, "The meeting title has been updated");

                break;
            }
            default:
                AddResponse.addTextResponse(responseObjects, "NO MATCHED INTENT!!!");
                break;
        }

        Objects.requireNonNull(response.getBody()).setFulfillmentMessages(responseObjects);

        return response;
    }
}
