package com.fumedatabase.fumedatabase_api;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class Queries {
    private final Connection conn;

    /**
     * Constructor to initialize the Queries class with a database connection.
     * @param conn the Connection object representing the database connection
     */
    public Queries(Connection conn) {
        this.conn = conn;
    }

    /**
     * Executes SQL queries entered by the user in a loop until 'exit' is typed.
     */
    public void executeQueries() {
        Scanner scanner = new Scanner(System.in);
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
        scanner.close();
    }
}
