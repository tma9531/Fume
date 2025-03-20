package com.fumedatabase.fumedatabase_api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class PostgresSSH {
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

        String driverName = "org.postgresql.Driver";
        Connection conn = null;
        Session session = null;
        
        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            session = jsch.getSession(user, rhost, 22);
            session.setPassword(password);
            session.setConfig(config);
            session.setConfig("PreferredAuthentications","publickey,keyboard-interactive,password");
            session.connect();
            System.out.println("Connected");
            int assigned_port = session.setPortForwardingL(lport, "127.0.0.1", rport);
            System.out.println("Port Forwarded");

            // Assigned port could be different from 5432 but rarely happens
            String url = "jdbc:postgresql://127.0.0.1:"+ assigned_port + "/" + databaseName;

            System.out.println("database Url: " + url);
            Properties props = new Properties();
            props.put("user", user);
            props.put("password", password);

            Class.forName(driverName);
            conn = DriverManager.getConnection(url, props);
            System.out.println("Database connection established");
            
            // Feel free to reformat this as yous see fit, but this is a simple way to get user input and execute SQL queries
            while (true){
                System.out.print("Enter SQL query (or 'exit' to quit): ");
                String sql = scanner.nextLine().trim();
                if (sql.equalsIgnoreCase("exit")) {
                    break;
                }
                if (!sql.isEmpty()) {
                    try (var stmt = conn.createStatement()) {
                        boolean hasResultSet = stmt.execute(sql);
                        if (hasResultSet) {
                            try (var rs = stmt.getResultSet()) {
                                while (rs.next()) {
                                    System.out.println(rs.getString(1)); // Print first column of each row
                                }
                            }
                        } else {
                            int updateCount = stmt.getUpdateCount();
                            System.out.println("Update count: " + updateCount);
                        }
                    } catch (SQLException e) {
                        System.err.println("SQL Error: " + e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Closing Database Connection");
                conn.close();
            }
            if (session != null && session.isConnected()) {
                System.out.println("Closing SSH Connection");
                session.disconnect();
            }
            scanner.close();
            System.out.println("Scanner closed");
        }
    }
}
