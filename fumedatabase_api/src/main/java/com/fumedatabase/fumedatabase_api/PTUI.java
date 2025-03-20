package com.fumedatabase.fumedatabase_api;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.Connection;

import com.jcraft.jsch.JSchException;

public class PTUI {

    final static String LOGIN_DETAILS_PATH = "./docs/login.txt";

    public static void main(String[] args) throws SQLException {
        SSHConnectionManager sshManager = new SSHConnectionManager();
        DatabaseConnectionManager dbManager = new DatabaseConnectionManager();
        try {
            FileReader read = new FileReader(LOGIN_DETAILS_PATH);
            BufferedReader br = new BufferedReader(read);
            String user = br.readLine().trim();
            String password = br.readLine().trim();

            sshManager.connect(user, password);
            dbManager.connect(user, password);
            
            var conn = dbManager.getConnection();
            if (conn != null) {
                System.out.println("Database connection established successfully.");
                displayMainMenu(conn);
            } else {
                System.err.println("Failed to establish database connection.");
            }
            
            br.close();
            read.close();
        } catch (JSchException e) {
            System.out.println("Bad Credentials");
        } 
        catch (Exception e) {
            System.out.println("SSH Connection failed.");
            e.printStackTrace();
        }
        finally {
            dbManager.disconnect();
            sshManager.disconnect();
        }
    }

    private static void displayMainMenu(Connection conn){
        Scanner scan = new Scanner(System.in);
        while (true) {
            System.out.println("Main Menu: ");
            System.out.println("0 - Create an account");
            System.out.println("9 - Exit");
            int choice = Integer.parseInt(scan.nextLine().trim());
            switch (choice) {
                case 0:
                    // Implement account creation logic here
                    createUser(conn);
                    break;
                case 9:
                    System.out.println("Exiting...");
                    scan.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void createUser(Connection conn){
        Scanner scan = new Scanner(System.in);
        System.out.print("Enter username: ");
        String username = scan.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scan.nextLine().trim();
        User user = new User(username, password);
        try {
            user.saveToDatabase(conn);
            System.out.println("User created successfully.");
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
        }
    }
}
