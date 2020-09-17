package com.utscapstone.chatbot.jdbc.repository;

import com.utscapstone.chatbot.Utils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String getEmailFromFacebookId(String id){
        String query = "select * from USER where FB_ID = ?";
        return jdbcTemplate.query(query, new Object[]{id}, resultSet -> {
            if(resultSet.next()){
                return resultSet.getString("EMAIL");
            }
            else {
                return null;
            }
        });
    }

    public String getEmailFromName(String name){

        String[] nameArray = name.split(" ");
        int indicator = Utils.hasDigit(name) ? Integer.parseInt(nameArray[nameArray.length-1]) : 0;
        String lastName = Utils.hasDigit(name) ? nameArray[nameArray.length-2] : nameArray[nameArray.length-1];
        String givenName = "";

        int i=0;
        while(!nameArray[i].equals(lastName)){
            givenName = givenName.concat(nameArray[i] + " ");
            i++;
        }
        givenName = givenName.trim();

        String query = "select * from USER where GIVEN_NAME = ? and LAST_NAME = ? and INDICATOR = ?";
        return jdbcTemplate.query(query, new Object[]{givenName, lastName, indicator}, resultSet -> {
            if(resultSet.next()){
                return resultSet.getString("EMAIL");
            }
            else {
                return null;
            }
        });
    }

    public Map<String, String[]> getEmailsFromNames(String[] names){

        Map<String, String[]> resultMap = new HashMap<>();
        boolean hasUnknown = false;
        LinkedList<String> emails = new LinkedList<>();
        LinkedList<String> unknowns = new LinkedList<>();

        for (String name : names) {
            String email = getEmailFromName(name);

            if (email != null) {
                emails.add(email);
            } else {
                unknowns.add(name);
                hasUnknown = true;
            }
        }


        resultMap.put("hasUnknown", new String[]{String.valueOf(hasUnknown)});
        resultMap.put("resultArray", hasUnknown ? unknowns.toArray(new String[0]) : emails.toArray(new String[0]));

        return resultMap;
    }

}
