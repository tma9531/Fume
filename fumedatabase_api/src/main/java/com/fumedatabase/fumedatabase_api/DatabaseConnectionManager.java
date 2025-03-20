package com.fumedatabase.fumedatabase_api;

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
     * @throws Exception if an error occurs while connecting to the database
     */
    public void connect(String user, String password) throws Exception {
        System.out.println("Connecting to database...");
        int assigned_port = 5432; // Assuming lport is the port forwarded by SSH
        String driverName = "org.postgresql.Driver";
        String url = "jdbc:postgresql://127.0.0.1:"+ assigned_port + "/" + "p32001_11";
        Properties props = new Properties();
        props.put("user", user);
        props.put("password", password);
        Class.forName(driverName);
        conn = DriverManager.getConnection(url, props);
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
     * @throws Exception if an error occurs while closing the connection
     */
    public void disconnect() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
            System.out.println("Database connection closed");
        }
    }
}
