package com.fumedatabase.fumedatabase_api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public class User {
    private String username;
    private String password;
    private LocalDate creationDate;
    private LocalDate lastAccessDate;
    private String email;

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.creationDate = LocalDate.now();
        this.lastAccessDate = LocalDate.now();
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public LocalDate getLastAccessDate() {
        return lastAccessDate;
    }

    public String getEmail(){
        return email;
    }

    public void saveToDatabase(Connection conn) throws SQLException {
        String sql = "insert into users (username, password, creationdate, lastaccessdate, email) values (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setDate(3, java.sql.Date.valueOf(creationDate));
            pstmt.setDate(4, java.sql.Date.valueOf(lastAccessDate));
            pstmt.setString(5, email);
            pstmt.executeUpdate();
        }
    }
}
