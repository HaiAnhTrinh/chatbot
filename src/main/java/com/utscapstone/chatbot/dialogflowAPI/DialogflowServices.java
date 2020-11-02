package com.utscapstone.chatbot.dialogflowAPI;

import com.utscapstone.chatbot.Utils;
import com.utscapstone.chatbot.dialogflowAPI.entities.request.OutputContexts;
import com.utscapstone.chatbot.dialogflowAPI.entities.request.Request;
import com.utscapstone.chatbot.dialogflowAPI.entities.response.CardResponseObject;
import com.utscapstone.chatbot.dialogflowAPI.entities.response.QuickRepliesResponseObject;
import com.utscapstone.chatbot.dialogflowAPI.entities.response.ResponseObject;
import com.utscapstone.chatbot.dialogflowAPI.entities.response.TextResponseObject;
import com.utscapstone.chatbot.googleCalendarAPI.CalendarServices;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.LinkedList;

public class DialogflowServices {

    static public void addTextResponse(LinkedList<ResponseObject> responseObjects, String newText){
        responseObjects.add(new TextResponseObject(newText));
    }

    static public void addCardResponse(LinkedList<ResponseObject> responseObjects, CardResponseObject.Card newCard){
        CardResponseObject cardResponseObject = new CardResponseObject();
        cardResponseObject.setCard(newCard);
        responseObjects.add(cardResponseObject);
    }

    static public void addQuickRepliesResponse(LinkedList<ResponseObject> responseObjects, QuickRepliesResponseObject.QuickReplies newQuickReplies){
        QuickRepliesResponseObject quickRepliesResponseObject = new QuickRepliesResponseObject();
        quickRepliesResponseObject.setQuickReplies(newQuickReplies);
        responseObjects.add(quickRepliesResponseObject);
    }

    public static boolean showMeetings(LinkedList<ResponseObject> responseObjects, String email, String viewType, String noMeetingString, String cardTitle)
            throws GeneralSecurityException, IOException {
        CalendarServices services = new CalendarServices(email);
        CardResponseObject.Card card = services.getMeetingInfo(viewType);

        if(card.getButtons().isEmpty()){
            addTextResponse(responseObjects, noMeetingString);
            return true;
        }
        else {
            card.setTitle(cardTitle);
            addCardResponse(responseObjects, card);
            return false;
        }
    }

    public static LinkedList<OutputContexts> resetContext(Request request){
        LinkedList<OutputContexts> outputContexts = new LinkedList<>();
        OutputContexts outputContext = request.getQueryResult().getOutputContexts().get(0);
        outputContext.setLifespanCount(0);
        outputContexts.add(outputContext);
        return outputContexts;
    }

    public static OutputContexts updateTime(Request request, String rawDate, String rawStartTime, String rawEndTime){
        OutputContexts outputContext = request.getQueryResult().getOutputContexts().getFirst();
        outputContext.getParameters().setStartTime(new String[]{Utils.convertToRFC3339(rawDate, rawStartTime)});
        outputContext.getParameters().setEndTime(new String[]{Utils.convertToRFC3339(rawDate, rawEndTime)});
        outputContext.getParameters().setRemoveNames(new String[]{});
        outputContext.getParameters().setAddNames(new String[]{});
        return outputContext;
    }

    public static OutputContexts updateLocation(Request request, String suggestedRoom){
        OutputContexts outputContext = request.getQueryResult().getOutputContexts().getFirst();
        outputContext.getParameters().setLocation(suggestedRoom);
        outputContext.getParameters().setRemoveNames(new String[]{});
        outputContext.getParameters().setAddNames(new String[]{});
        return outputContext;
    }

}
