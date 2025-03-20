package com.fumedatabase.fumedatabase_api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class User {
    private String username;
    private String password;
    private LocalDate creationDate;
    private LocalDate lastAccessDate;
    private String email;

    /**
     * Constructor to initialize a User object with the provided username, password, and email.
     * @param username the username of the user
     * @param password the password of the user
     * @param email the email of the user
     */
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.creationDate = LocalDate.now();
        this.lastAccessDate = LocalDate.now();
        this.email = email;
    }

    /**
     * Returns the username of the user.
     * @return the username of the user
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the password of the user.
     * @return the password of the user
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the creation date of the user.
     * @return the creation date of the user
     */
    public LocalDate getCreationDate() {
        return creationDate;
    }

    /**
     * Returns the last access date of the user.
     * @return the last access date of the user
     */
    public LocalDate getLastAccessDate() {
        return lastAccessDate;
    }

    /**
     * Returns the email address of the user.
     * @return the email address of the user
     */
    public String getEmail(){
        return email;
    }

    /**
     * Saves the user information to the database.
     * @param conn the Connection object representing the database connection
     * @throws SQLException
     */
    public void saveToDatabase(Connection conn) throws SQLException {
        // sql command to insert a new user into the database
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

    /**
     * Verify the credentials of a user when they log in
     * @param conn the Connection object representing the database connection
     * @param username the username of the user trying to log in
     * @param password the password of the user trying to log in
     * @return the user that is currently logged in
     */
    public static User verifyCredentials(Connection conn, String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email")
                );
            } else {
                return null; // Invalid credentials
            }
        }
    }
}
