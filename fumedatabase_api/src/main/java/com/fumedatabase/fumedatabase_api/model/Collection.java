package com.fumedatabase.fumedatabase_api.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Collection {
    private int cnr;
    private String name;
    private String username;

    public Collection(String name, String username){
        this.name = name;
        this.username = username;
    }

    public int getCnr() {
        return cnr;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public void saveToDatabase(Connection conn) throws SQLException {
        String sql = "insert into collection (name, username) values (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                cnr = rs.getInt(1); // Get the generated collection number
            }
        }
    }
}
