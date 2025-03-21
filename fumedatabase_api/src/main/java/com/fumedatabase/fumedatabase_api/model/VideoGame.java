package com.fumedatabase.fumedatabase_api.model;

import java.sql.Connection;
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
}
