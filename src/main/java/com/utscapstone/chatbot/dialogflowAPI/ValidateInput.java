package com.utscapstone.chatbot.dialogflowAPI;

public class ValidateInput {

    public static String missingParams(String rawDate, String rawStartTime, String rawEndTime, String[] attendeeEmails){

        if(rawDate == null){
            return "Please let me know the date for the meeting";
        }
        else if (rawStartTime == null){
            return "Can you give me the starting time?";
        }
        else if (rawEndTime == null){
            return "When do you want to end it?";
        }
        else if (attendeeEmails.length == 0){
            return "Who do you want to do the meeting with?";
        }
        return null;
    }

}
