package com.utscapstone.chatbot.jdbc.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RoomRepository {

    private final JdbcTemplate jdbcTemplate;

    public RoomRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void count() {
        int result = jdbcTemplate.queryForObject("select count(*) from ROOM", Integer.class);

        System.out.println("Row count: " + result);
    }

}
