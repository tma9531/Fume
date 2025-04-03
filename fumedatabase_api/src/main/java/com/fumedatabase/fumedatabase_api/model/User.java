package com.fumedatabase.fumedatabase_api.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Represents a user in the system.
 */
public class User {
    private final String username;
    private final String password;
    private LocalDate creationDate;
    private LocalDate lastAccessDate;

    /**
     * Constructor to initialize a User object with the provided username and password.
     * @param username the username of the user
     * @param password the password of the user
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.creationDate = LocalDate.now();
        this.lastAccessDate = LocalDate.now();
    }

    /**
     * Constructor to initialize a User object with the provided username (password is set to null).
     * @param username the username of the user
     */
    public User(String username) {
        this.username = username;
        this.password = null;
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

    /**
     * Saves the user to the database.
     * @param conn the Connection object representing the database connection
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public void saveToDatabase(Connection conn) {
        String sql = "insert into users (username, password, creationdate, lastaccessdate) values (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setDate(3, java.sql.Date.valueOf(creationDate));
            pstmt.setDate(4, java.sql.Date.valueOf(lastAccessDate));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Verify the credentials of a user when they log in
     * @param conn the Connection object representing the database connection
     * @param username the username of the user trying to log in
     * @param password the password of the user trying to log in
     * @return the user that is currently logged in
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public static User verifyCredentials(Connection conn, String username, String password) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Error occurred
    }

    /**
     * Updates the last access date of the user in the database to the current date
     * @param conn the Connection object representing the database connection
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public void updateLastAccessDate(Connection conn) {
        String sql = "update users set lastaccessdate = current_date where username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Follow another user by inserting a record into the follows table
     * @param conn the Connection object representing the database connection
     * @param email the email of the user to be followed
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public void follow(Connection conn, String email) {
        String sql = "insert into follows (userfollowing, userbeingfollowed) values (?, (select username from email where email = ?))";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, this.username);
            pstmt.setString(2, email);
            pstmt.executeUpdate();

        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * Unfollow another user by deleting the record from the follows table
     * @param conn the Connection object representing the database connection
     * @param unfollow the email of the user to be unfollowed
     */
    @SuppressWarnings("CallToPrintStackTrace")
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

    /**
     * Gets the followers of the user by querying the follows table
     * @param conn the Connection object representing the database connection
     * @return list of followers
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public ArrayList<User> getFollowers(Connection conn){
        String sql = "select userfollowing from follows where userbeingfollowed = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, this.username);
            ResultSet rs = pstmt.executeQuery();
            ArrayList<User> followers = new ArrayList<>();
            while (rs.next()) {
                User follower = new User(rs.getString("userfollowing"));
                followers.add(follower);
            }
            return followers;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Error occurred
    }

    /**
     * Gets the users that the current user is following by querying the follows table
     * @param conn the Connection object representing the database connection
     * @return list of users the user is following
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public ArrayList<User> getFollowing(Connection conn){
        String sql = "select userbeingfollowed from follows where userfollowing = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, this.username);
            ResultSet rs = pstmt.executeQuery();
            ArrayList<User> following = new ArrayList<>();
            while (rs.next()) {
                User follow = new User(rs.getString("userbeingfollowed"));
                following.add(follow);
            }
            return following;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Error occurred
    }

    /**
     * Rate a video game by inserting a record into the rates table
     * @param conn the Connection object representing the database connection
     * @param game the video game to be rated
     * @param rating the rating given by the user (1 to 5)
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public void rateVideoGame(Connection conn, VideoGame game, int rating) {
        String sql = "INSERT INTO rates VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, game.getVgnr());
            pstmt.setInt(3, rating);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Play a video game by inserting a record into the plays table
     * @param conn the Connection object representing the database connection
     * @param start timestamp the user started playing
     * @param end timestamp the user finished playing
     * @param game the video game being played
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public void playVideoGame(Connection conn, Timestamp start, Timestamp end, VideoGame game) {
        String sql = "INSERT INTO plays VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, start);
            pstmt.setTimestamp(2, end);
            pstmt.setString(3, username);
            pstmt.setInt(4, game.getVgnr());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
