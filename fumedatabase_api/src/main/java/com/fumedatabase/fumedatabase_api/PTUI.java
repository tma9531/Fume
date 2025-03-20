package com.fumedatabase.fumedatabase_api;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.PreparedStatement;

import com.jcraft.jsch.JSchException;

public class PTUI {

    final static String LOGIN_DETAILS_PATH = "./fumedatabase_api/docs/login.txt";
    private static User currentUser = null; // Placeholder for the current user
    static Scanner scan = new Scanner(System.in);

    /**
     * Main method to establish SSH and database connections, and display the main menu.
     * @param args command line arguments (not used)
     */
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
                displayLoginMenu(conn);
            } else {
                System.err.println("Failed to establish database connection.");
            }
            scan.close();
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

    /**
     * Displays a main menu for users to do specific task
     * @param conn the Connection object representing the database connection
     */
    private static void displayLoginMenu(Connection conn){
        while (currentUser == null) {
            System.out.println("Login Menu: ");
            System.out.println("0 - Create an account");
            System.out.println("1 - Login");
            System.out.println("9 - Exit");
            int choice = Integer.parseInt(scan.nextLine().trim());
            switch (choice) {
                case 0:
                    createUser(conn);
                    break;
                case 1:
                    login(conn);
                    break;
                case 9:
                    System.out.println("Exiting...");;
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        displayMainMenu(conn);
    }

    private static void displayMainMenu(Connection conn){
        while (true) {
            System.out.println("Main Menu: ");
            System.out.println("0 - Create a collection");
            System.out.println("9 - Logout");
            int choice = Integer.parseInt(scan.nextLine().trim());
            switch (choice) {
                case 0:
                    createCollection(conn);
                    break;
                case 1:
                    break;
                case 9:
                    System.out.println("Logging out...");
                    currentUser = null;
                    displayLoginMenu(conn);
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // USER METHODS FOR LOGIN SCREEN

    /**
     * Creates a new user by prompting for username, password, and email.
     * @param conn the Connection object representing the database connection
     * @throws SQLException if an error occurs while saving the user to the database
     */
    private static void createUser(Connection conn){
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

    private static void login(Connection conn){
        System.out.print("Enter username: ");
        String username = scan.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scan.nextLine().trim();
        try {
            currentUser = User.verifyCredentials(conn, username, password); 
            if (currentUser != null) {
                System.out.println("Login successful. Welcome " + currentUser.getUsername() + "!");
                currentUser.updateLastAccessDate(conn); // update last access date
            } else {
                System.out.println("Invalid username or password. Please try again.");
            }
        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
        }
    }

    /**
     * Create a new collection by prompting for the collection name.
     * @param conn the Connection object representing the database connection
     * @throws SQLException if an error occurs while saving the collection to the database
     */
    private static void createCollection(Connection conn){
        System.out.print("Enter collection name: ");
        String collectionName = scan.nextLine().trim();
        Collection collection = new Collection(collectionName, currentUser.getUsername());
        try{
            collection.saveToDatabase(conn);
            System.out.println("Collection created successfully with CN: " + collection.getCnr() + ".");
        }
        catch (SQLException e){
            System.err.println("Error creating collection: " + e.getMessage());
        }
    }
}
