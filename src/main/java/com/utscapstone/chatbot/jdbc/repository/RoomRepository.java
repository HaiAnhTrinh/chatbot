package com.utscapstone.chatbot.jdbc.repository;

import com.utscapstone.chatbot.Utils;
import com.utscapstone.chatbot.jdbc.model.Room;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.List;

@Repository
public class RoomRepository {

    private final JdbcTemplate jdbcTemplate;

    public RoomRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //return all the rooms in a string
    public String viewAllRooms(){
        String roomList =
        jdbcTemplate.queryForList("select NAME from ROOM", String.class).toString();

        return roomList.substring(1, roomList.length()-1);
    }

    //check if a specified room has enough capacity
    public boolean canRoomFit(String roomName, int numberOfPartcipants){
        String query = "select CAPACITY from ROOM where NAME = ?";
        int capacity = jdbcTemplate.queryForObject(query, new Object[]{roomName}, Integer.class);
        return numberOfPartcipants + 1 <= capacity;
    }

    public String lookForEnoughCapacityAndAvailableRoom(String rawStartTime, String rawEndTime, String rawDate, int numberOfParticipants){
        String query = "select NAME from ROOM where CAPACITY >= ?";
        RoomAvailabilityRepository roomAvailabilityRepository = new RoomAvailabilityRepository(jdbcTemplate);
        String suggestedRoom = "";

        List<String> enoughRooms = jdbcTemplate.queryForList(query, new Object[]{numberOfParticipants+1}, String.class);
        LinkedList<String> availableRooms = roomAvailabilityRepository.lookForAvailableRooms(rawStartTime,rawEndTime,rawDate);

        for(String roomName : enoughRooms){
            for(String availableRoom : availableRooms){
                if(roomName.equals(availableRoom)){
                    suggestedRoom = availableRoom;
                }
            }
        }

        return suggestedRoom;
    }

}
