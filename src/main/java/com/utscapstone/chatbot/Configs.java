package com.utscapstone.chatbot;

public class Configs {

    //CALENDAR API
    public static final String APPLICATION_NAME = "Chatbot";
    public static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    public static final String PRIMARY_CALENDAR = "primary";
    public static final int TIME_SLOT_NUMBER = 96;
    public static final String IS_ALL_AVAILABLE = "isAllAvailable";
    public static final String SUGGESTED_START_TIME = "suggestedStartTime";
    public static final String SUGGESTED_END_TIME = "suggestedEndTime";
    public static final String VIEW_ALL_MEETING = "reader";
    public static final String VIEW_AUTHORIZED_MEETING = "writer";

    //DIALOGFLOW API
    public static final String PLATFORM_FACEBOOK = "FACEBOOK";
    public static final String ERROR_MESSAGE = "There is something wrong with the system";

    //DB
    public static final String AVAILABILITY_DETELE = "delete";
    public static final String AVAILABILITY_INSERT = "insert";
}
