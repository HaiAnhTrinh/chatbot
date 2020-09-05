package com.utscapstone.chatbot.dialogflowAPI.entities.request;

public class OriginalDetectIntentRequest {
    private Payload payload;
    public OriginalDetectIntentRequest() {
    }
    public Payload getPayload() {
        return payload;
    }

    //INNER CLASSES
    public class Payload {
        private Data data;
        public Payload() {
        }
        public Data getData() {
            return data;
        }

        public class Data {
            private Sender sender;
            public Data() {
            }
            public Sender getSender() {
                return sender;
            }
            public class Sender {
                private String id;
                public Sender() {
                }
                public String getId() {
                    return id;
                }
            }
        }
    }
}
