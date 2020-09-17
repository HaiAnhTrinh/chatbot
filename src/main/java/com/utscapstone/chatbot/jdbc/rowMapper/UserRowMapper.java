package com.utscapstone.chatbot.jdbc.rowMapper;

import com.utscapstone.chatbot.jdbc.model.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {

        User user = new User();

        user.setEmail(rs.getString("EMAIL"));
        user.setFacebookId(rs.getString("FB_ID"));
        user.setGivenName(rs.getString("GIVEN_NAME"));
        user.setGivenName(rs.getString("LAST_NAME"));
        user.setIndicator(rs.getInt("INDICATOR"));

        return user;
    }

}
