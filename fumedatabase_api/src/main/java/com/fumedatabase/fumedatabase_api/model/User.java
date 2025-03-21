package com.fumedatabase.fumedatabase_api.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class User {
    private String username;
    private String password;
    private LocalDate creationDate;
    private LocalDate lastAccessDate;

    /**
     * Constructor to initialize a User object with the provided username, password, and email.
     * @param username the username of the user
     * @param password the password of the user
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.creationDate = LocalDate.now();
        this.lastAccessDate = LocalDate.now();
    }

    public User (String username){
        this.username = username;
        this.password = "";
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
     * Saves the user information to the database.
     * @param conn the Connection object representing the database connection
     * @throws SQLException
     */
    public void saveToDatabase(Connection conn) throws SQLException {
        // sql command to insert a new user into the database
        String sql = "insert into users (username, password, creationdate, lastaccessdate) values (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setDate(3, java.sql.Date.valueOf(creationDate));
            pstmt.setDate(4, java.sql.Date.valueOf(lastAccessDate));
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
                    rs.getString("password")
                );
            } else {
                return null; // Invalid credentials
            }
        }
    }

    /**
     * Updates the last access date of the user in the database to the current date
     * @param conn the Connection object representing the database connection
     * @throws SQLException
     */
    public void updateLastAccessDate(Connection conn) throws SQLException {
        String sql = "update users set lastaccessdate = current_date where username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        }
    }

    public void follow(Connection conn, String em) throws SQLException{
        String sql = "insert into follows (userfollowing, userbeingfollowed) values (?, (select username from email where email = ?))";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, this.username);
            pstmt.setString(2, em);
            pstmt.executeUpdate();

        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void unfollow(Connection conn, String unfollow){
        String sql = "delete from follows where userfollowing = ? and userbeingfollowed = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, this.username);
            pstmt.setString(2, unfollow);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<User> getFollowers(Connection conn){
        String sql = "select userfollowing from follows where userbeingfollowed = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, this.username);
            ResultSet rs = pstmt.executeQuery();
            ArrayList<User> followers = new ArrayList<User>();
            while (rs.next()) {
                User follower = new User(rs.getString("userfollowing"));
                followers.add(follower);
            }
            return followers;
        } catch (SQLException e) {
            e.printStackTrace();
        }
                return null;
    }

    public ArrayList<User> getFollowing(Connection conn){
        String sql = "select userbeingfollowed from follows where userfollowing = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, this.username);
            ResultSet rs = pstmt.executeQuery();
            ArrayList<User> following = new ArrayList<User>();
            while (rs.next()) {
                User follow = new User(rs.getString("userbeingfollowed"));
                following.add(follow);
            }
            return following;
        } catch (SQLException e) {
            e.printStackTrace();
        }
                return null;
    }
}