package com.utscapstone.chatbot.dialogflowAPI.entities.response;

import com.utscapstone.chatbot.Configs;

import java.util.LinkedList;

public class CardResponseObject implements ResponseObject{

    private Card card;
    private String platform;

    public CardResponseObject(){
        this.card = new Card();
        this.platform = Configs.PLATFORM_FACEBOOK;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    //INNER CLASSES
    public class Card {
        private String title;
        private LinkedList<Button> buttons;

        public Card(){
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public LinkedList<Button> getButtons() {
            return buttons;
        }

        public void setButtons(LinkedList<Button> buttons) {
            this.buttons = buttons;
        }

        public class Button {
            private String text;
            private String postback;
            public Button(){
            }

            public String getText() {
                return text;
            }

            public void setText(String text) {
                this.text = text;
            }

            public String getPostback() {
                return postback;
            }

            public void setPostback(String postback) {
                this.postback = postback;
            }
        }
    }
}
