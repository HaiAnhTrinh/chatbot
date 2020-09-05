package com.utscapstone.chatbot.dialogflowAPI.entities.response;

import com.utscapstone.chatbot.Configs;

import java.util.LinkedList;

public class QuickRepliesResponseObject implements ResponseObject {

    private QuickReplies quickReplies;
    private String platform;

    public QuickRepliesResponseObject() {
        this.platform = Configs.PLATFORM_FACEBOOK;
    }

    public QuickReplies getQuickReplies() {
        return quickReplies;
    }

    public void setQuickReplies(QuickReplies quickReplies) {
        this.quickReplies = quickReplies;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    //INNER CLASSES
    public class QuickReplies {

        private String title;
        private LinkedList<String> quickReplies;

        public QuickReplies() {
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public LinkedList<String> getQuickReplies() {
            return quickReplies;
        }

        public void setQuickReplies(LinkedList<String> quickReplies) {
            this.quickReplies = quickReplies;
        }
    }
}
