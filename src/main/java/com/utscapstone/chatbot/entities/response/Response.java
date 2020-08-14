package com.utscapstone.chatbot.entities.response;

public class Response {

    public ResponseText[] getFulfillmentMessages() {
        return fulfillmentMessages;
    }

    public void setFulfillmentMessages(ResponseText[] fulfillmentMessages) {
        this.fulfillmentMessages = fulfillmentMessages;
    }

    private ResponseText[] fulfillmentMessages;
}

