package com.utscapstone.chatbot.entities.response;

import com.utscapstone.chatbot.entities.request.OutputContexts;

public class Response {

    private ResponseText[] fulfillmentMessages;
    private OutputContexts[] outputContexts;

    public void setFulfillmentMessages(ResponseText[] fulfillmentMessages) {
        this.fulfillmentMessages = fulfillmentMessages;
    }

    public ResponseText[] getFulfillmentMessages() {
        return fulfillmentMessages;
    }

    public OutputContexts[] getOutputContexts() {
        return outputContexts;
    }

    public void setOutputContexts(OutputContexts[] outputContexts) {
        this.outputContexts = outputContexts;
    }
}

