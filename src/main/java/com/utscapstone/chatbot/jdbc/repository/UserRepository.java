package com.utscapstone.chatbot.jdbc.repository;

import com.utscapstone.chatbot.jdbc.model.User;
import com.utscapstone.chatbot.jdbc.rowMapper.UserRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String getEmailFromFacebookId(String id){

        String query = "SELECT * FROM USER WHERE FB_ID = ?";
        User user = jdbcTemplate.queryForObject(query, new Object[]{id}, new UserRowMapper());
        return Objects.requireNonNull(user).getEmail();
    }

}
