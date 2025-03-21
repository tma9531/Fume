/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.fumedatabase.fumedatabase_api.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author emmettpeterson
 */
public class Rating {
    private String username;
    private int vgnr;
    private int rating;

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

    public void saveToDatabase(Connection conn) throws SQLException {
        String sql = "insert into rates (username, vgnr, rating) values (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, vgnr);
            pstmt.setInt(3, rating);
            pstmt.executeUpdate();
            
        }catch(SQLException e){
            System.out.println(e);
        }
    }



}
