package com.fumedatabase.fumedatabase_api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Collection {
    private int cnr;
    private String name;
    private String username;

    // num games and total play time
    private int numGames;
    private int totalPlayTime;

    /**
     * Constructor to initialize a Collection object with the provided name and username.
     * @param name the name of the collection
     * @param username the username of the user who owns this collection
     */
    public Collection(String name, String username){
        this.name = name;
        this.username = username;
    }

    /**
     * Returns the collection number (cnr) of this collection.
     * @return the collection number of this collection
     */
    public int getCnr() {
        return cnr;
    }

    /**
     * Returns the name of the collection.
     * @return the name of the collection
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the username associated with this collection.
     * @return the username of the user who owns this collection
     */
    public String getUsername() {
        return username;
    }

    public int getNumGames() {
        return numGames;
    }

    public void setNumGames(int numGames) {
        this.numGames = numGames;
    }

    public int getTotalPlayTime() {
        return totalPlayTime;
    }

    public void setTotalPlayTime(int totalPlayTime) {
        this.totalPlayTime = totalPlayTime;
    }

    /**
     * Saves the collection information to the database.
     * @param conn the Connection object representing the database connection
     * @throws SQLException if an error occurs while saving the collection to the database
     */
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

    public static List<Collection> getCollectionByUser(Connection conn, String username) throws SQLException{
        String sql = "select c.name, count(v.id) as num_games, sum(v.play_time) as total_play_time " +
                    "from collection c left join video_game v on c.cnr = v.collection_id " +
                    "where c.username = ? group by c.name order by c.name ASC"; 
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            List<Collection> collections = new ArrayList<>();
            while (rs.next()) {
                Collection collection = new Collection(rs.getString("name"), username);
                collection.setNumGames(rs.getInt("num_games"));
                collection.setTotalPlayTime(rs.getInt("total_play_time"));
                collections.add(collection);
            }
            return collections;
        }
    }
}
