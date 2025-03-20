package com.fumedatabase.fumedatabase_api;


import java.sql.SQLException;
import java.util.Scanner;


public class PTUI {
    public static void main(String[] args) throws SQLException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter local port (default 5432): ");
        int lport = Integer.parseInt(scanner.nextLine().trim());
        if (lport == 0) lport = 5432;

        System.out.print("Enter remote host (default starbug.cs.rit.edu): ");
        String rhost = scanner.nextLine().trim();
        if (rhost.isEmpty()) rhost = "starbug.cs.rit.edu";

        System.out.print("Enter remote port (default 5432): ");
        int rport = Integer.parseInt(scanner.nextLine().trim());
        if (rport == 0) rport = 5432;

        System.out.print("Enter your CS username: ");
        String user = scanner.nextLine().trim();

        System.out.print("Enter your CS password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Enter your database name: ");
        String databaseName = scanner.nextLine().trim();

        SSHConnectionManager sshManager = new SSHConnectionManager();
        DatabaseConnectionManager dbManager = new DatabaseConnectionManager();
        
        try {
            // Establish SSH connection and port forwarding
            sshManager.connect(user, password, rport, lport);
            
            // Establish database connection
            dbManager.connect(user, password, databaseName, lport);
            
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
