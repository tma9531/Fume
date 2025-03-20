package com.fumedatabase.fumedatabase_api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DatabaseConnectionManager {
    private Connection conn;

    /**
     * Establishes a connection to the PostgreSQL database using the provided credentials and port.
     * @param user the username of the database user
     * @param password the password of the database user
     * @param databaseName the name of the database to connect to
     * @param lport the local port to forward to the remote PostgreSQL server
     * @throws Exception if an error occurs while connecting to the database
     */
    public void connect(String user, String password, String databaseName, int lport) throws Exception {
        // Assigned port could be different from 5432 but rarely happens
        int assigned_port = lport; // Assuming lport is the port forwarded by SSH
        String driverName = "org.postgresql.Driver";
        String url = "jdbc:postgresql://127.0.0.1:"+ assigned_port + "/" + databaseName;
        Properties props = new Properties();
        props.put("user", user);
        props.put("password", password);
        Class.forName(driverName);
        conn = DriverManager.getConnection(url, props);
        System.out.println("Database connection established");
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
    public void disconnect() throws Exception {
        if (conn != null && !conn.isClosed()) {
            conn.close();
            System.out.println("Database connection closed");
        }
    }
}
