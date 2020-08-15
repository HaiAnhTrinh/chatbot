package com.utscapstone.chatbot.controller;

import com.utscapstone.chatbot.Configs;
import com.utscapstone.chatbot.entities.request.OutputContexts;
import com.utscapstone.chatbot.entities.request.Request;
import com.utscapstone.chatbot.entities.response.Response;
import com.utscapstone.chatbot.entities.response.ResponseText;
import com.utscapstone.chatbot.googleCalendarAPI.CalendarServices;
import com.utscapstone.chatbot.googleCalendarAPI.Utils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Objects;


@RestController
public class RestAPIController {

    @CrossOrigin(origins = "*")
    @PostMapping("/")
    public ResponseEntity<Response> processRequest(@RequestBody Request request) throws GeneralSecurityException, IOException {

        System.out.println("INTENT: " + request.getQueryResult().getIntent().getDisplayName());

        ResponseEntity<Response> response = new ResponseEntity<>(new Response(), HttpStatus.OK);
        ResponseText responseText = new ResponseText();
        String[] textResponse = new String[1];
        String rawDate;
        String rawStartTime;
        String rawEndTime;

        switch (request.getQueryResult().getIntent().getDisplayName()) {
            case "Book a meeting - Retrieve info": {
                //check the availability of all attendees (primary calendar)
                //if invalid, look for the most recent available time slot
                rawDate = Utils.getDateFromRequest(request);
                rawStartTime = Utils.getStartTimeFromRequest(request);
                rawEndTime = Utils.getEndTimeFromRequest(request);

                CalendarServices services = new CalendarServices("trinhhaianh38@gmail.com");
                Map<String, Object> freeMap = services.isAllAvailable(rawDate,
                        rawStartTime, rawEndTime,"trinhhaianh38@gmail.com",
                        Utils.getAttendeeEmailsFromRequest(request));

                if(Boolean.parseBoolean(freeMap.get(Configs.IS_ALL_AVAILABLE).toString())){
                    textResponse[0] = "Ok all attendees are available for the meeting, shall I process the booking?";
                }
                else{
                    OutputContexts outputContexts = request.getQueryResult().getOutputContexts()[0];
                    outputContexts.getParameters().setStartTime(new String[]{Utils.convertToRFC5322(Utils.getDateFromRequest(request), freeMap.get(Configs.SUGGESTED_START_TIME).toString())});
                    outputContexts.getParameters().setEndTime(new String[]{Utils.convertToRFC5322(Utils.getDateFromRequest(request), freeMap.get(Configs.SUGGESTED_END_TIME).toString())});
                    Objects.requireNonNull(response.getBody()).setOutputContexts(new OutputContexts[]{outputContexts});

                    textResponse[0] = "Sorry, the closest available time is from "
                            + freeMap.get(Configs.SUGGESTED_START_TIME)
                            + " to "
                            + freeMap.get(Configs.SUGGESTED_END_TIME)
                            + ". Do you want me to process the booking?";
                }
                break;
            }
            case "Book a meeting - Confirm booking": {
                //if user confirms
                if(request.getQueryResult().getParameters().getConfirmBoolean()[0].equals("True")){
                    //add the event to the calendar
                    rawDate = Utils.getDateFromOutputContexts(request);

                    String startTime = Utils.convertToRFC5322(rawDate, Utils.getStartTimeFromOutputContexts(request));
                    String endTime = Utils.convertToRFC5322(rawDate, Utils.getEndTimeFromOutputContexts(request));
                    String[] attendeeEmails = Utils.getAttendeeEmailsFromOutputContexts(request);

                    CalendarServices services = new CalendarServices("trinhhaianh38@gmail.com");
                    services.addEvent(startTime, endTime, attendeeEmails);
                    textResponse[0] = "Ok, the meeting has been successfully booked.";

                    //update room database
                }
                else {
                    textResponse[0] = "OK, I will look for another time.";
                }
                break;

            }
            case "Cancel a meeting": {
                //remove the event from the calendar
                //update room database
                break;
            }
            case "View all meetings": {
                //show event ID, other details
                break;
            }
            case "Update a meeting": {
                //idk ...
                break;
            }
            default:
                textResponse[0] = "NO MATCHED INTENT!!!";
                break;
        }

        responseText.getText().setText(textResponse);

        ResponseText[] responseTexts = {responseText};
        Objects.requireNonNull(response.getBody()).setFulfillmentMessages(responseTexts);

        return response;
    }
}
