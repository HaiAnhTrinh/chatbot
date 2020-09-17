package com.utscapstone.chatbot.jdbc.repository;

import com.utscapstone.chatbot.Configs;
import com.utscapstone.chatbot.Utils;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.List;

@Repository
@Configuration
@EnableScheduling
public class RoomAvailabilityRepository {

    private final JdbcTemplate jdbcTemplate;

    public RoomAvailabilityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //creates a new available slot for the 7th day from today
    //delete the record for yesterday
    //called at 00:00:01 (hh:mm:ss) everyday
    @Scheduled(cron = "1 0 0 * * ?")
    public void updateRoomAvailabilityScheduler(){
        //allows booking 7 days in advance
        String addQuery = "insert into ROOM_AVAILABILITY(ROOM_NAME, DATE, AVAILABILITY) values ( ?, ?, ? )";
        String deleteQuery = "delete from ROOM_AVAILABILITY where DATE = ?";

        List<String> rooms = jdbcTemplate.queryForList("select NAME from ROOM", String.class);

        for(String room : rooms){
            jdbcTemplate.update(addQuery, room, java.time.LocalDate.now().plusDays(7), "0".repeat(Configs.TIME_SLOT_NUMBER));
        }
        jdbcTemplate.update(deleteQuery, java.time.LocalDate.now().minusDays(1));
    }


    //get the availability of a room at the specified date
    public String getAvailability(String roomName, String rawDate){
        String query = "select AVAILABILITY from ROOM_AVAILABILITY where ROOM_NAME = ? and DATE = ?";
        String availability = jdbcTemplate.queryForObject(query,
                new Object[]{roomName, rawDate},
                String.class);

        assert availability != null;
        return availability;
    }


    //check if the room is available within the specified timeframe
    public boolean isRoomAvailable(String roomName, String rawStartTime, String rawEndTime, String rawDate){

        boolean isAvailable = true;
        int startSlot = Utils.convertRFC3339ToTimeSlot(Utils.convertToRFC3339(rawDate, rawStartTime));
        int endSlot = Utils.convertRFC3339ToTimeSlot(Utils.convertToRFC3339(rawDate, rawEndTime));

        char[] availability = getAvailability(roomName, rawDate).toCharArray();
        for(int i=startSlot; i <= endSlot; i++){
            if(availability[i] == '1'){
                isAvailable = false;
                break;
            }
        }

        return isAvailable;
    }


    //process the booking for a room
    public void updateAvailability(String roomName, String rawStartTime, String rawEndTime, String rawDate, String updateType){
        String query = "update ROOM_AVAILABILITY set AVAILABILITY = ? where DATE = ? and ROOM_NAME = ?";
        int startSlot = Utils.convertRFC3339ToTimeSlot(Utils.convertToRFC3339(rawDate, rawStartTime));
        int endSlot = Utils.convertRFC3339ToTimeSlot(Utils.convertToRFC3339(rawDate, rawEndTime));
        StringBuilder availability = new StringBuilder();
        availability.append(getAvailability(roomName, rawDate));

        for(int i=startSlot; i < endSlot; i++){
            availability.setCharAt(i, updateType.equals(Configs.UPDATE_INSERT) ?'1' : '0');
        }

        jdbcTemplate.update(query, availability, rawDate, roomName);
    }

    //look for an available room at the specified time frame
    public LinkedList<String> lookForAvailableRooms(String rawStartTime, String rawEndTime, String rawDate){
        String query = "select ROOM_NAME from ROOM_AVAILABILITY where DATE = ?";
        LinkedList<String> availableRooms = new LinkedList<>();

        List<String> roomList = jdbcTemplate.queryForList(query, new Object[]{rawDate}, String.class);

        for(String roomName : roomList){
            if(isRoomAvailable(roomName, rawStartTime, rawEndTime, rawDate)){
               availableRooms.add(roomName);
            }
        }

        return availableRooms;
    }
}
