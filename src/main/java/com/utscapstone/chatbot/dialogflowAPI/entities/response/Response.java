package com.utscapstone.chatbot.dialogflowAPI.entities.response;

import com.utscapstone.chatbot.dialogflowAPI.entities.request.OutputContexts;

import java.util.LinkedList;

public class Response {

    private LinkedList<ResponseObject> fulfillmentMessages;
    private LinkedList<OutputContexts> outputContexts;

    public void setFulfillmentMessages(LinkedList<ResponseObject> fulfillmentMessages) {
        this.fulfillmentMessages = fulfillmentMessages;
    }

    public LinkedList<ResponseObject> getFulfillmentMessages() {
        return fulfillmentMessages;
    }

    public LinkedList<OutputContexts> getOutputContexts() {
        return outputContexts;
    }

    public void setOutputContexts(LinkedList<OutputContexts> outputContexts) {
        this.outputContexts = outputContexts;
    }
}


