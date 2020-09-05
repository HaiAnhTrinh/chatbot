package com.utscapstone.chatbot.dialogflowAPI.entities.request;

import java.util.LinkedList;

public class QueryResult {

    private Intent intent;
    private String queryText;
    private Parameters parameters;
    private LinkedList<OutputContexts> outputContexts;

    public Intent getIntent() {
        return intent;
    }

    public String getQueryText() {
        return queryText;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public LinkedList<OutputContexts> getOutputContexts() {
        return outputContexts;
    }

    //INNER CLASSES
    public class Intent {
        private String displayName;

        public String getDisplayName() {
            return displayName;
        }
    }
}
