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

    public Collection(int cnr, String name, String username) {
        this.cnr = cnr;
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
                cnr = rs.getInt("cnr"); // Get the generated collection number
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
    public static List<Collection> getCollectionsByUser(Connection conn, String username) throws SQLException{
        String sql = "select c.cnr, c.name, count(v.vgnr) as num_games, " +
                        "coalesce(sum(extract(epoch from (p.end_timestamp - p.start_timestamp)) / 60), 0) as total_play_time " +
                        "from collection c " +
                        "left join contained_in ci on c.cnr = ci.cnr " +
                        "left join video_game v on ci.vgnr = v.vgnr " +
                        "left join plays p on v.vgnr = p.vgnr and p.username = c.username " +
                        "where c.username = ? group by c.cnr, c.name order by c.name asc"; 
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            List<Collection> collections = new ArrayList<>();
            while (rs.next()) {
                Collection collection = new Collection(rs.getInt("cnr"), rs.getString("name"), username);
                collection.setNumGames(rs.getInt("num_games"));
                collection.setTotalPlayTime(rs.getInt("total_play_time"));
                collections.add(collection);
            }
            return collections;
        }
    }

    public void rename(Connection conn, String newName) {
        String sql = "UPDATE collection SET name = ? WHERE cnr = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newName);
            pstmt.setInt(2, cnr);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(Connection conn) {
        String clearRowsSql = "DELETE FROM contained_in WHERE cnr = ?";
        String sql = "DELETE FROM collection WHERE cnr = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(clearRowsSql)) {
            pstmt.setInt(1, cnr);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cnr);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a video to the collection in the database.
     * @param conn the Connection object representing the database connection
     * @param vgnr the video game to be added
     * @throws SQLException if an error occurs while adding the video game to the collection
     */
    public void addVideoGame(Connection conn, int vgnr) {
        String sql = "insert into contained_in (cnr, vgnr) values (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cnr);
            pstmt.setInt(2, vgnr);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete a video game in the collection in the database
     * @param conn the Connection object representing the database connection
     * @param vgnr the video game to be deleted
     * @throws SQLException if an error occurs while deleting the video game from the collection
     */
    public void deleteVideoGame(Connection conn, int vgnr) {
        String sql = "delete from contained_in where cnr = ? and vgnr = ?"; // delete the video game from the collection
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cnr);
            pstmt.setInt(2, vgnr);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<VideoGame> getVideoGames(Connection conn) {
        String sql = "SELECT * FROM video_game vg LEFT JOIN contained_in ci ON vg.vgnr = ci.vgnr LEFT JOIN collection c ON ci.cnr = c.cnr WHERE c.cnr = ?";
        List<VideoGame> videoGames = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cnr);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                VideoGame game = new VideoGame(rs.getInt("vgnr"), rs.getString("title"), rs.getString("esrbrating"));
                videoGames.add(game);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return videoGames;
/*
 * SELECT DISTINCT vg.title, vg.esrbrating FROM video_game vg " + 
                        "LEFT JOIN available_on ao ON vg.vgnr = ao.vgnr LEFT JOIN platform p ON ao.pfnr = p.pfnr " +
                        "LEFT JOIN developed_by db ON vg.vgnr = db.vgnr LEFT JOIN developer d ON db.dnr = d.dnr " +
                        "LEFT JOIN is_genre ig ON vg.vgnr = ig.vgnr LEFT JOIN genre g ON ig.gnr = g.gnr " +
                        "WHERE 1 = 1 
 */
    }
}
