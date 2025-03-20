package com.fumedatabase.fumedatabase_api;

import java.sql.SQLException;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;

import com.jcraft.jsch.JSchException;


public class PTUI {
    public static void main(String[] args) throws SQLException {
        try {
            FileReader read = new FileReader("/login.txt");
            BufferedReader br = new BufferedReader(read);
            Scanner scanner = new Scanner(System.in);

            String user = br.readLine().trim(); // Read username from file
            String password = br.readLine().trim(); // Read password from file
            SSHConnectionManager sshManager = new SSHConnectionManager();
            DatabaseConnectionManager dbManager = new DatabaseConnectionManager();

            // Establish SSH connection and port forwarding
            sshManager.connect(user, password);
            
            // Establish database connection
            dbManager.connect(user, password);
            
            // Get the database connection
            var conn = dbManager.getConnection();
            if (conn != null) {
                System.out.println("Database connection established successfully.");
                
                // Create and execute queries
                Queries queries = new Queries(conn);
                queries.executeQueries();
            } else {
                System.err.println("Failed to establish database connection.");
            }

        } catch (JSchException e) {
            System.out.println("Bad Credentials");
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try{
                // Disconnect from the database and SSH session
                dbManager.disconnect();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                sshManager.disconnect();
                scanner.close();
            }
        }
    }
}
