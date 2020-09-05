package com.utscapstone.chatbot.dialogflowAPI;

import com.utscapstone.chatbot.Configs;
import com.utscapstone.chatbot.dialogflowAPI.entities.response.CardResponseObject;
import com.utscapstone.chatbot.dialogflowAPI.entities.response.ResponseObject;
import com.utscapstone.chatbot.googleCalendarAPI.CalendarServices;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.LinkedList;

public class DialogflowServices {

    public static void showMeetings(LinkedList<ResponseObject> responseObjects, String noMeetingString, String cardTitle)
            throws GeneralSecurityException, IOException {
        CalendarServices services = new CalendarServices(Configs.USER_EMAIL);
        CardResponseObject.Card card = services.getMeetingInfo();

        if(card.getButtons().isEmpty()){
            AddResponse.addTextResponse(responseObjects, noMeetingString);
        }
        else {
            card.setTitle(cardTitle);
            AddResponse.addCardResponse(responseObjects, card);
        }
    }

}
