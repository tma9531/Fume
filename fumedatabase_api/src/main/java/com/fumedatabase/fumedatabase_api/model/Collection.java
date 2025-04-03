package com.fumedatabase.fumedatabase_api.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a collection of video games owned by a user.
 */
public class Collection {
    private int cnr;
    private final String name;
    private final String username;
    // Dynamically updated values
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

    public int getCnr() {
        return cnr;
    }

    public String getName() {
        return name;
    }

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
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public void saveToDatabase(Connection conn) {
        String sql = "insert into collection (name, username) values (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                cnr = rs.getInt("cnr");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves a list of collections for a given user from the database, including the number of games and total play time for each collection.
     * @param conn the Connection object representing the database connection
     * @param username the username of the user whose collections are to be retrieved
     * @return a list of Collection objects representing the user's collections, or an empty list if none are found
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public static List<Collection> getCollectionsByUser(Connection conn, String username) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * Renames the collection in the database.
     * @param conn the Connection object representing the database connection
     * @param newName the new name for the collection
     */
    @SuppressWarnings("CallToPrintStackTrace")
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

    /**
     * Deletes the collection from the database.
     * @param conn the Connection object representing the database connection
     */
    @SuppressWarnings("CallToPrintStackTrace")
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
     */
    @SuppressWarnings("CallToPrintStackTrace")
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
     */
    @SuppressWarnings("CallToPrintStackTrace")
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

    /**
     * Get all video games in the collection
     * @param conn the Connection object representing the database connection
     * @return list of video games in the collection
     */
    @SuppressWarnings("CallToPrintStackTrace")
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
    }
}
