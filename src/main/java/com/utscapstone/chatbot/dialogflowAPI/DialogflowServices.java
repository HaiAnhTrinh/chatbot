package com.utscapstone.chatbot.dialogflowAPI;

import com.utscapstone.chatbot.Configs;
import com.utscapstone.chatbot.dialogflowAPI.entities.request.OutputContexts;
import com.utscapstone.chatbot.dialogflowAPI.entities.request.Request;
import com.utscapstone.chatbot.dialogflowAPI.entities.response.CardResponseObject;
import com.utscapstone.chatbot.dialogflowAPI.entities.response.ResponseObject;
import com.utscapstone.chatbot.googleCalendarAPI.CalendarServices;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.LinkedList;

public class DialogflowServices {

    public static void showMeetings(LinkedList<ResponseObject> responseObjects, String email, String viewType, String noMeetingString, String cardTitle)
            throws GeneralSecurityException, IOException {
        CalendarServices services = new CalendarServices(email);
        CardResponseObject.Card card = services.getMeetingInfo(viewType);

        if(card.getButtons().isEmpty()){
            AddResponse.addTextResponse(responseObjects, noMeetingString);
        }
        else {
            card.setTitle(cardTitle);
            AddResponse.addCardResponse(responseObjects, card);
        }
    }

    public static String getEventIdFromOutputContext(Request request){
        for(OutputContexts o : request.getQueryResult().getOutputContexts()){
            if(o.getName().equals(Configs.CONTEXT_NAME_PREFIX + "updateameeting-eventchosen-followup")){
                return o.getParameters().getEventId();
            }
        }
        return null;
    }

}
