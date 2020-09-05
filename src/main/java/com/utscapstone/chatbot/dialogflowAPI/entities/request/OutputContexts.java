package com.utscapstone.chatbot.dialogflowAPI.entities.request;

public class OutputContexts {
    private Parameters parameters = new Parameters();
    private String name;
    private int lifespanCount;

    public Parameters getParameters() {
        return parameters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLifespanCount() {
        return lifespanCount;
    }

    public void setLifespanCount(int lifespanCount) {
        this.lifespanCount = lifespanCount;
    }
}
