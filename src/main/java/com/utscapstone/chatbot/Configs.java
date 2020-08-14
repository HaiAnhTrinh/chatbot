package com.utscapstone.chatbot;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.CalendarScopes;
import java.util.Collections;
import java.util.List;

public class Configs {

    public static final String APPLICATION_NAME = "Chatbot";
    public static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    public static final String TOKENS_DIRECTORY_PATH = "tokens";
    public static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    public static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    public static final int TIME_SLOT_NUMBER = 96;

}
