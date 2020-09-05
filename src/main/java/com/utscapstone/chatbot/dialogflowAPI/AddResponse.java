package com.utscapstone.chatbot.dialogflowAPI;

import com.utscapstone.chatbot.dialogflowAPI.entities.response.CardResponseObject;
import com.utscapstone.chatbot.dialogflowAPI.entities.response.QuickRepliesResponseObject;
import com.utscapstone.chatbot.dialogflowAPI.entities.response.ResponseObject;
import com.utscapstone.chatbot.dialogflowAPI.entities.response.TextResponseObject;

import java.util.LinkedList;

public class AddResponse {

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

}
