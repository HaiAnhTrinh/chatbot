package com.utscapstone.chatbot.controller;

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


@RestController
public class RestAPIController {

    @CrossOrigin(origins = "*")
    @PostMapping("/")
    public ResponseEntity<Response> processRequest(@RequestBody Request request) throws GeneralSecurityException, IOException {
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

                if(Boolean.parseBoolean(freeMap.get("isAllAvailable").toString())){
                    textResponse[0] = "Ok all attendees are available for the meeting";
                }
                else{
                    textResponse[0] = freeMap.get("suggestedTime").toString();
                }

            }
            case "Book a meeting - Confirm booking": {
                //if user confirms
                //add the event to the calendar
//                rawDate = Utils.getDateFromRequest(request);
//                rawStartTime = Utils.getStartTimeFromRequest(request);
//                rawEndTime = Utils.getEndTimeFromRequest(request);
//
//                String startTime = rawDate.concat(rawStartTime);
//                String endTime = rawDate.concat(rawEndTime);
//
//                CalendarServices services = new CalendarServices("trinhhaianh37@gmail.com");
//                services.addEvent(startTime, endTime,
//                        request.getQueryResult().getParameters().getAttendeeEmails());
                //update room database
            }
            case "Cancel a meeting": {
                //remove the event from the calendar
                //update room database
            }
            case "View all meetings": {
                //show event ID, other details
            }
            case "Update a meeting": {
                //idk ...
            }
        }

        responseText.getText().setText(textResponse);

        ResponseText[] responseTexts = {responseText};
        response.getBody().setFulfillmentMessages(responseTexts);

        return response;
    }
}
