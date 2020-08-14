package com.utscapstone.chatbot.entities.response;

public class ResponseText {

    private Text text;

    public ResponseText() {
        this.text = new Text();
    }

    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }

}
