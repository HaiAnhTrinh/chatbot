package com.utscapstone.chatbot.entities.request;

public class QueryResult {

    private Intent intent;
    private String queryText;
    private Parameters parameters;

    public Intent getIntent() {
        return intent;
    }

    public String getQueryText() {
        return queryText;
    }

    public Parameters getParameters() {
        return parameters;
    }
}
