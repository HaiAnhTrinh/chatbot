package com.utscapstone.chatbot;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.CalendarScopes;
import java.util.Collections;
import java.util.List;

public class Configs {

    //CALENDAR API
    public static final String APPLICATION_NAME = "Chatbot";
    public static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    public static final String TOKENS_DIRECTORY_PATH = "tokens";
    public static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
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
    public static final String CONTEXT_NAME_PREFIX = "projects/schedulingchatbot-axjpyf/agent/sessions/cc430bfa-e14a-3c7e-83e3-1845850dda7b/contexts/";
    public static final String ERROR_MESSAGE = "There is something wrong with the system";

    //DB
    public static final String UPDATE_DETELE = "delete";
    public static final String UPDATE_INSERT = "insert";
}
