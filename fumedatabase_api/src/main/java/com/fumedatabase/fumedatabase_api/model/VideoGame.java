package com.fumedatabase.fumedatabase_api.model;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VideoGame {
    private int vgnr;
    private String title;
    private String esrbRating;

    public VideoGame(String title, String esrbRating) {
        this.title = title;
        this.esrbRating = esrbRating;
    }

    public VideoGame(int vgnr, String title, String esrbRating) {
        this.vgnr = vgnr;
        this.title = title;
        this.esrbRating = esrbRating;
    }

    public int getVgnr() {
        return vgnr;
    }

    public String getTitle() {
        return title;
    }

    public String getEsrbRating() {
        return esrbRating;
    }

    public static VideoGame getVideoGameByName(Connection conn, String gameName) throws SQLException {
        String sql = "select * from video_game where title = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, gameName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new VideoGame(rs.getInt("vgnr"), rs.getString("title"), rs.getString("esrbRating"));
            } else {
                return null; // No game found with the given name
            }
        }
    }

    public static List<VideoGame> searchVideoGames(Connection conn, String title) throws SQLException{
        String sql = "SELECT * FROM VideoGames WHERE title LIKE ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + title + "%");
            ResultSet rs = pstmt.executeQuery();
            List<VideoGame> videoGames = new ArrayList<>();
            while (rs.next()) {
                VideoGame game = new VideoGame(rs.getString("title"), rs.getString("esrbRating"));
                videoGames.add(game);
            }
            return videoGames;
        }
    }

    
    /**
     * Searches for video games in the database based on various criteria.
     * @param conn the database connection
     * @param title video game title to match
     * @param platform platform name to match
     * @param lowerReleaseDate lower bound of release date
     * @param upperReleaseDate upper bound of release date
     * @param developerName developer name to match
     * @param lowerPrice lower bound of price
     * @param upperPrice upper bound of price
     * @param genre genre name to match
     * @return
     * @throws SQLException
     */
    public static List<VideoGame> searchVideoGames(Connection conn, String title, String platform, String lowerReleaseDate, String upperReleaseDate, String developerName, float lowerPrice, float upperPrice, String genre, String sortBy, boolean ascending) throws SQLException{
        String sql = "SELECT DISTINCT vg.vgnr, vg.title, vg.esrbrating, ao.game_price, g.type, ao.game_release_date FROM video_game vg " + 
                        "LEFT JOIN available_on ao ON vg.vgnr = ao.vgnr LEFT JOIN platform p ON ao.pfnr = p.pfnr " +
                        "LEFT JOIN developed_by db ON vg.vgnr = db.vgnr LEFT JOIN developer d ON db.dnr = d.dnr " +
                        "LEFT JOIN is_genre ig ON vg.vgnr = ig.vgnr LEFT JOIN genre g ON ig.gnr = g.gnr " +
                        "WHERE 1 = 1 ";
        List<Object> params = new ArrayList<>();
        
        if (title != null && !title.isEmpty()) {
            sql += "AND vg.title LIKE ? ";
            params.add("%" + title + "%");
        }
        if (platform != null && !platform.isEmpty()) {
            sql += "AND p.name LIKE ? ";
            params.add("%" + platform + "%");
        }
        if (lowerReleaseDate != null && !lowerReleaseDate.isEmpty()) {
            sql += "AND ao.game_release_date >= ? ";
            params.add(Date.valueOf(lowerReleaseDate));
        }
        if (upperReleaseDate != null && !upperReleaseDate.isEmpty()) {
            sql += "AND ao.game_release_date <= ? ";
            params.add(Date.valueOf(upperReleaseDate));
        }
        if (developerName != null && !developerName.isEmpty()) {
            sql += "AND d.name LIKE ? ";
            params.add("%" + developerName + "%");
        }
        if (lowerPrice >= 0) {
            sql += "AND ao.game_price >= ? ";
            params.add(lowerPrice);
        }
        if (upperPrice >= 0) {
            sql += "AND ao.game_price <= ? ";
            params.add(upperPrice);
        }
        if (genre != null && !genre.isEmpty()) {
            sql += "AND g.type LIKE ? ";
            params.add("%" + genre + "%");
        }
        sql += "GROUP BY vg.vgnr, vg.title, vg.esrbrating, ao.game_price, g.type, ao.game_release_date ";
        if (sortBy == "title") {
            sql += "ORDER BY vg.title ";
        } else if (sortBy == "price") {
            sql += "ORDER BY ao.game_price ";
        } else if (sortBy == "genre") {
            sql += "ORDER BY g.type ";
        } else if (sortBy == "date") {
            sql += "ORDER BY ao.game_release_date ";
        } else {
            sql += "ORDER BY vg.title, ao.game_release_date ";
        }
        if (ascending) sql += "ASC";
        else sql += "DESC";

        PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i = 0; i < params.size(); i++) {
            if (params.get(i) instanceof String string) {
                pstmt.setString(i + 1, string);
            } else if (params.get(i) instanceof Float aFloat) {
                pstmt.setFloat(i + 1, aFloat);
            } else if (params.get(i) instanceof Date aDate) {
                pstmt.setDate(i + 1, aDate);
            }
        }
        ResultSet rs = pstmt.executeQuery();
        List<VideoGame> videoGames = new ArrayList<>();
        while (rs.next()) {
            VideoGame game = new VideoGame(rs.getInt("vgnr"), rs.getString("title"), rs.getString("esrbrating"));
            videoGames.add(game);
        }
        return videoGames;
    }

    public boolean checkPlatformOwnership(Connection conn, User user) {
        String sql = "SELECT count(*) FROM (SELECT o.pfnr FROM owns o WHERE o.username = ? INTERSECT SELECT ao.pfnr FROM available_on ao WHERE ao.vgnr = ?)"; // counts the amount of roles that match the pfnr and username
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setInt(2, vgnr);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // if the count is greater than 0, the user owns the platform
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
}
