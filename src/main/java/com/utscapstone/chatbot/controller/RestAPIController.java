package com.utscapstone.chatbot.controller;

import com.utscapstone.chatbot.Configs;
import com.utscapstone.chatbot.Utils;
import com.utscapstone.chatbot.dialogflowAPI.AddResponse;
import com.utscapstone.chatbot.dialogflowAPI.ValidateInput;
import com.utscapstone.chatbot.dialogflowAPI.entities.request.OutputContexts;
import com.utscapstone.chatbot.dialogflowAPI.entities.request.Request;
import com.utscapstone.chatbot.dialogflowAPI.entities.response.CardResponseObject;
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
        System.out.println("originalDetectIntentRequest" + request.getOriginalDetectIntentRequest().getPayload().getData().getSender().getId());

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
                    outputContext.getParameters().setStartTime(new String[]{Utils.convertToRFC5322(rawDate, freeMap.get(Configs.SUGGESTED_START_TIME).toString())});
                    outputContext.getParameters().setEndTime(new String[]{Utils.convertToRFC5322(rawDate, freeMap.get(Configs.SUGGESTED_END_TIME).toString())});
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

                    String startTime = Utils.convertToRFC5322(rawDate, Utils.getStartTimeFromOutputContexts(request));
                    String endTime = Utils.convertToRFC5322(rawDate, Utils.getEndTimeFromOutputContexts(request));
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

                //list up to 10 meetings for user to choose
                CalendarServices services = new CalendarServices(Configs.USER_EMAIL);
                CardResponseObject.Card card = services.getMeetingInfo();

                if(card.getButtons().isEmpty()){
                    AddResponse.addTextResponse(responseObjects, "There is no meeting that you can cancel!!!");
                }
                else {
                    card.setTitle("Meetings you can cancel");
                    AddResponse.addTextResponse(responseObjects, "Which one do you want to cancel?");
                    AddResponse.addCardResponse(responseObjects, card);
                }

                break;
            }
            case "Cancel a meeting - userChoice": {
                CalendarServices services = new CalendarServices(Configs.USER_EMAIL);
                //the queryText is the event id
                services.cancelMeeting(request.getQueryResult().getQueryText());
                AddResponse.addTextResponse(responseObjects, "Ok I have deleted that meeting for you");
                break;
            }
            case "View meetings": {
                //TODO: may want to display the meetings in some other forms?
                //show event date and time
                CalendarServices services = new CalendarServices(Configs.USER_EMAIL);
                CardResponseObject.Card card = services.getMeetingInfo();

                if(card.getButtons().isEmpty()){
                    AddResponse.addTextResponse(responseObjects, "You have no incoming schedule!!!");
                }
                else {
                    card.setTitle("Your schedule");
                    AddResponse.addCardResponse(responseObjects, card);
                }
                break;
            }
            case "Update a meeting": {
                //idk ...
                CalendarServices services = new CalendarServices(Configs.USER_EMAIL);
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
