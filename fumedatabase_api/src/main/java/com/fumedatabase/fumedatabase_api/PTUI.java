package com.fumedatabase.fumedatabase_api;


import java.sql.SQLException;
import java.util.Scanner;


public class PTUI {
    public static void main(String[] args) throws SQLException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your CS username: ");
        String user = scanner.nextLine().trim();

        System.out.print("Enter your CS password: ");
        String password = scanner.nextLine().trim();

        SSHConnectionManager sshManager = new SSHConnectionManager();
        DatabaseConnectionManager dbManager = new DatabaseConnectionManager();
        
        try {
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

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try{
                // Disconnect from the database and SSH session
                dbManager.disconnect();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                sshManager.disconnect();
                scanner.close();
                System.out.println("Scanner closed");
            }
        }
    }
}
