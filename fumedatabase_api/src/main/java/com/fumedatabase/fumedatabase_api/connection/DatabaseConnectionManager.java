package com.fumedatabase.fumedatabase_api.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnectionManager {
    private Connection conn;

    /**
     * Establishes a connection to the PostgreSQL database using the provided credentials and port.
     * @param user the username of the database user
     * @param password the password of the database user
     * @param lport the local port to forward to the remote PostgreSQL server
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public void connect(String user, String password) {
        System.out.println("Connecting to database...");
        int assigned_port = 5432; // Assuming lport is the port forwarded by SSH
        String driverName = "org.postgresql.Driver";
        String url = "jdbc:postgresql://127.0.0.1:"+ assigned_port + "/" + "p32001_11";
        Properties props = new Properties();
        props.put("user", user);
        props.put("password", password);
        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            conn = DriverManager.getConnection(url, props);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the current database connection.
     * @return the current Connection object, or null if not connected
     */
    public Connection getConnection() {
        return conn;
    }

    /**
     * Closes the current database connection if it is open.
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public void disconnect() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
