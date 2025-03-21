package com.fumedatabase.fumedatabase_api;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.List;
import java.sql.ResultSet;

import com.fumedatabase.fumedatabase_api.connection.DatabaseConnectionManager;
import com.fumedatabase.fumedatabase_api.connection.SSHConnectionManager;
import com.fumedatabase.fumedatabase_api.model.Collection;
import com.fumedatabase.fumedatabase_api.model.User;
import com.fumedatabase.fumedatabase_api.model.VideoGame;
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
            System.out.println("1 - View collections");
            System.out.println("3 - Add a game to a collection");
            System.out.println("8 - Search video games");
            System.out.println("9 - Logout");
            int choice = Integer.parseInt(scan.nextLine().trim());
            switch (choice) {
                case 0:
                    createCollection(conn);
                    break;
                case 1:
                    viewCollections(conn);
                    break;
                case 3:
                    addGameToCollection(conn);
                case 8:
                    searchAllVideoGames(conn);
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

    // COLLECTION METHODS FOR MAIN MENU

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

    /**
     * Displays the list of collections for the current user.
     * @param conn the Connection object representing the database connection
     * @throws SQLException if an error occurs while retrieving collections from the database
     */
    private static void viewCollections(Connection conn){
        try{
            List <Collection> collections = Collection.getCollectionsByUser(conn, currentUser.getUsername());
            System.out.println("Collections for user " + currentUser.getUsername() + ":");
            for (Collection collection : collections) {
                System.out.println("Name: " + collection.getName() +
                        ", Number of Games: " + collection.getNumGames() +
                        ", Total Play Time: " + collection.getTotalPlayTime() / 60 + ":" + collection.getTotalPlayTime() % 60);
            }
        }
        catch (SQLException e){
            System.err.println("Error retrieving collections: " + e.getMessage());
        }
    }

    /**
     * Adds a video game to a collection
     * @param conn the Connection object representing the database connection
     * @throws SQLException if an error occurs while adding the game to the collection
     */
    private static void addGameToCollection(Connection conn) {
        try {
        // get collection name, game name, and platform name from user input
            System.out.print("Enter collection name: ");
            String collectionName = scan.nextLine().trim();
            // checks if collection exists
            List<Collection> collections = Collection.getCollectionsByUser(conn, currentUser.getUsername());
            Collection collection = collections.stream()
                .filter(c -> c.getName().equalsIgnoreCase(collectionName))
                .findFirst()
                .orElse(null);
            if (collection == null){
                System.out.println("Collection not found.");
                return;
            }

            int cnr = collection.getCnr(); // get collection number
            
            System.out.print("Enter game name: ");
            String gameName = scan.nextLine().trim();
            VideoGame videoGame = VideoGame.getVideoGameByName(conn, gameName);
            // checks if game exists
            if (videoGame == null) {
                System.out.println("Game not found.");
                return;
            }
            int vgnr = videoGame.getVgnr(); // get video game number

            System.out.print("Enter platform name: ");
            String platformName = scan.nextLine().trim();         
            int pfnr = getPfnrByPlatformName(conn, platformName);

            // checks if platform exists
            if (pfnr == -1) {
                System.out.println("Platform not found.");
                return;
            }

            // add the game to the collection
            if (!collection.checkPlatformOwnership(conn, pfnr)) {
                System.out.println("Warning: You do not own the platform for this game.");
            }
            collection.addVideoGame(conn, cnr, vgnr);
            System.out.println("Game added to collection successfully.");
        }
        catch (SQLException e) {
            System.err.println("Error adding game to collection: " + e.getMessage());
        }
    }

    // HELPER METHODS FOR GAME AND PLATFORM NUMBER RETRIEVAL 

    /**
     * Retrieves the platform number (pfnr) by platform name from the database.
     * @param conn the Connection object representing the database connection
     * @param platformName the name of the platform
     * @return the platform number (pfnr) if found, otherwise -1
     */
    private static int getPfnrByPlatformName(Connection conn, String platformName) throws SQLException {
        String sql = "select pfnr from platform where name = ?";
        try (var pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, platformName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("pfnr");
                }
            }
        }
        return -1; // Platform not found
    }
    // VIDEO GAME METHODS FOR MAIN MENU

    /**
     * Displays list of video games based on user input criteria.
     * @param conn
     * @throws SQLException if an error occurs while searching for video games in the database
     */
    private static void searchAllVideoGames(Connection conn){
        System.out.print("Enter a video game title, a part of a title, or press ENTER to skip: ");
        String title = scan.nextLine().trim();
        System.out.print("Enter a platform or press ENTER to skip: ");
        String platform = scan.nextLine().trim();
        System.out.print("Enter a lower bound for release date (YYYY-MM-DD) or press ENTER to skip: ");
        String lowerReleaseDate = scan.nextLine().trim();
        System.out.print("Enter an upper bound for release date (YYYY-MM-DD) or press ENTER to skip: ");
        String upperReleaseDate = scan.nextLine().trim();
        System.out.print("Enter a developer name or press ENTER to skip: ");
        String developerName = scan.nextLine().trim();
        System.out.print("Enter a lower bound for price or press ENTER to skip: ");
        String lowerPriceStr = scan.nextLine().trim();
        float lowerPrice = lowerPriceStr.isEmpty() ? -1 : Float.parseFloat(lowerPriceStr);
        System.out.print("Enter an upper bound for price or press ENTER to skip: ");
        String upperPriceStr = scan.nextLine().trim();
        float upperPrice = upperPriceStr.isEmpty() ? -1 : Float.parseFloat(upperPriceStr);
        System.out.print("Enter a genre or press ENTER to skip: ");
        String genre = scan.nextLine().trim();
        try {
            List<VideoGame> videoGames = VideoGame.searchVideoGames(conn, title, platform, lowerReleaseDate, upperReleaseDate, developerName, lowerPrice, upperPrice, genre);
            System.out.println("All video games with your search constraints:");
            for (VideoGame game : videoGames) {
                System.out.println("\tTitle: " + game.getTitle() + "\t\t\tESRB Rating: " + game.getEsrbRating());
            }
        } catch (SQLException e) {
            System.err.println("Error searching for video games: " + e.getMessage());
        }
    }
}
