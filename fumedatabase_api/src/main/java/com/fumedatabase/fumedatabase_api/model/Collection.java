package com.fumedatabase.fumedatabase_api.model;

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

    /**
     * Retrieves a list of collections for a given user from the database, including the number of games and total play time for each collection.
     * @param conn the Connection object representing the database connection
     * @param username the username of the user whose collections are to be retrieved
     * @return a list of Collection objects representing the user's collections, or an empty list if none are found
     * @throws SQLException if an error occurs while retrieving collections from the database
     */
    public static List<Collection> getCollectionByUser(Connection conn, String username) throws SQLException{
        String sql = "select c.name, count(v.vgnr) as num_games, " +
                        " coalesce(sum(extract(epoch from (p.end_timestamp - p.start_timestamp)) / 60), 0) as total_play_time " +
                        "from collection c " +
                        "left join contained_in ci on c.cnr = ci.cnr " +
                        "left join video_game v on ci.vgnr = v.vgnr " +
                        "left join plays p on v.vgnr = p.vgnr and p.username = c.username " +
                        "where c.username = ? group by c.name order by c.name asc"; 
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
