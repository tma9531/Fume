package com.fumedatabase.fumedatabase_api.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Represents a rating given by a user to a video game.
 */
public class Rating {
    private final String username;
    private final int vgnr;
    private final int rating;

    /**
     * Constructor to initialize a Rating object with the provided username, video game number (vgnr), and rating.
     * @param username username of the user who rated the game
     * @param vgnr video game number (unique identifier)
     * @param rating rating given by user (1 to 5)
     */
    public Rating(String username, int vgnr, int rating) {
        this.username = username;
        this.vgnr = vgnr;
        this.rating = rating;
    }
    
    public String getUsername(){
        return username;
    }

    public int getVgnr(){
        return vgnr;
    }

    public int getRating(){
        return rating;  
    }

    /**
     * Saves the rating to the database.
     * @param conn the Connection object representing the database connection
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public void saveToDatabase(Connection conn) {
        String sql = "insert into rates (username, vgnr, rating) values (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, vgnr);
            pstmt.setInt(3, rating);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
