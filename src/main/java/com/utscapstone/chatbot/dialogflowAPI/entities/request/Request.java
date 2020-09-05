package com.utscapstone.chatbot.dialogflowAPI.entities.request;

public class Request {
    private String responseId;
    private QueryResult queryResult;
    private OriginalDetectIntentRequest originalDetectIntentRequest;

    public String getResponseId() {
        return responseId;
    }

    public QueryResult getQueryResult() {
        return queryResult;
    }

    public OriginalDetectIntentRequest getOriginalDetectIntentRequest() {
        return originalDetectIntentRequest;
    }
}
