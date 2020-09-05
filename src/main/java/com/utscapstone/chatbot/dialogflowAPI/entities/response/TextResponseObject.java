package com.utscapstone.chatbot.dialogflowAPI.entities.response;

import com.utscapstone.chatbot.Configs;

import java.util.LinkedList;

public class TextResponseObject implements ResponseObject{

    private Text text;
    private String platform;

    public TextResponseObject(String newText) {
        this.text = new Text(newText);
        this.platform = Configs.PLATFORM_FACEBOOK;
    }

    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    //INNER CLASSES
    public class Text {

        private LinkedList<String> text;

        public Text(String newText){
            this.text = new LinkedList<>();
            this.text.add(newText);
        }

        public LinkedList<String> getText() {
            return text;
        }

        public void setText(LinkedList<String> text) {
            this.text = text;
        }

    }
}
