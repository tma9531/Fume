package com.fumedatabase.fumedatabase_api;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import com.fumedatabase.fumedatabase_api.connection.DatabaseConnectionManager;
import com.fumedatabase.fumedatabase_api.connection.SSHConnectionManager;
import com.fumedatabase.fumedatabase_api.model.Collection;
import com.fumedatabase.fumedatabase_api.model.User;
import com.fumedatabase.fumedatabase_api.model.VideoGame;
import com.jcraft.jsch.JSchException;

public class PTUI {

    final static String LOGIN_DETAILS_PATH = "./fumedatabase_api/docs/login.txt";
    private static User currentUser = null; // Placeholder for the current user
    private static Connection connection = null;
    static Scanner scan = new Scanner(System.in);

    /**
     * Main method to establish SSH and database connections, and display the main menu.
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        SSHConnectionManager sshManager = new SSHConnectionManager();
        DatabaseConnectionManager dbManager = new DatabaseConnectionManager();
        try (FileReader read = new FileReader(LOGIN_DETAILS_PATH); BufferedReader br = new BufferedReader(read)) {
            String user = br.readLine().trim();
            String password = br.readLine().trim();    
            sshManager.connect(user, password);
            dbManager.connect(user, password);
            var conn = dbManager.getConnection();
            if (conn != null) {
                System.out.println("Database connection established successfully.");
                connection = conn;
                displayLoginMenu();
            } else {
                System.err.println("Failed to establish database connection.");
            }
            scan.close();
        } catch (JSchException e) {
            System.out.println("Bad Credentials");
        } catch (FileNotFoundException e) {
            System.out.println("Missing login info at " + LOGIN_DETAILS_PATH);
        } catch (IOException e1) {
            System.out.println("Error reading login info file.");
        } catch (Exception e) {
            System.out.println("SSH connection error.");
        }
        finally {
            dbManager.disconnect();
            sshManager.disconnect();
        }
    }

    /**
     * Displays a login menu for users to create an account or log in
     * @param conn the Connection object representing the database connection
     */
    private static void displayLoginMenu() {
        while (currentUser == null) {
            System.out.println("\nLogin Menu");
            System.out.println("[0] - Create an account");
            System.out.println("[1] - Login");
            System.out.println("[9] - Exit");
            int choice = 10;
            try {
                choice = Integer.parseInt(scan.nextLine().trim());
            } catch (NumberFormatException e) {
            }
            switch (choice) {
                case 0 -> createUser();
                case 1 -> login();
                case 9 -> {
                    System.out.println("Exiting...");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
        if (currentUser != null) {
            displayMainMenu();
        }
    }

    /**
     * Creates a new user by prompting for username, password, and email.
     * @param conn the Connection object representing the database connection
     */
    private static void createUser() {
        System.out.print("Enter username: ");
        String username = scan.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scan.nextLine().trim();
        User user = new User(username, password);
        user.saveToDatabase(connection);
        System.out.println("User created successfully.");
    }

    /**
     * Logs in as an existing user with a username and password.
     * @param conn
     */
    private static void login() {
        System.out.print("Enter username: ");
        String username = scan.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scan.nextLine().trim();
        currentUser = User.verifyCredentials(connection, username, password); 
        if (currentUser != null) {
            System.out.println("Login successful. Welcome " + currentUser.getUsername() + "!");
            currentUser.updateLastAccessDate(connection);
        } else {
            System.out.println("Invalid username or password. Please try again.");
        }
    }

    /**
     * Displays a main menu for users to do various actions
     * @param conn the Connection object representing the database connection
     */
    private static void displayMainMenu() {
        while (true) {
            System.out.println("\nWelcome, " + (currentUser != null ? currentUser.getUsername() : "___") + "!");
            System.out.println("Press the number corresponding to your choice: ");
            System.out.println("Main Menu: ");
            System.out.println("[0] - My collections");
            System.out.println("[1] - My ratings");
            System.out.println("[2] - My platforms");
            System.out.println("[3] - My followers");
            System.out.println("[4] - Following");
            System.out.println("[5] - Search video games");
            System.out.println("...");
            System.out.println("[9] - Logout");
            int choice = 10;
            try {
                choice = Integer.parseInt(scan.nextLine().trim());
            } catch (NumberFormatException e) {
            }
            switch (choice) {
                case 0 -> displayCollectionMenu();
                case 1 -> {
                }
                case 2 -> {
                }
                case 3 -> displayFollowers();
                case 4 -> displayFollowing();
                case 5 -> searchAllVideoGames();
                case 9 -> {
                    System.out.println("Logging out...");
                    currentUser = null;
                    displayLoginMenu();
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Displays a collection menu for users to do various collection-related actions
     * @param conn the Connection object representing the database connection
     */
    private static void displayCollectionMenu() {
        System.out.println("\nMy collections:");
        Collection collection = pickCollection();
        if (collection == null) {
            System.out.println("Returning to main menu...");
            return;
        }
        int page = 0;
        int numPages;
        gamesInCollectionLoop:
        while (true) {
            List<VideoGame> videoGames = collection.getVideoGames(connection);
            numPages = (videoGames.size() / 4) + (videoGames.size() % 4 == 0 ? 0 : 1);
            int maxVideoGameTitleLength = 0;
            for (VideoGame videoGame : videoGames) {
                if (videoGame.getTitle().length() > maxVideoGameTitleLength) {
                    maxVideoGameTitleLength = videoGame.getTitle().length();
                }
            }
            maxVideoGameTitleLength += 2;
            System.out.println("\nTo remove a game from this collection, enter the corresponding number.");
            System.out.println("Page (" + (page + 1) + "/" + numPages + ")");
            if (videoGames.isEmpty()) {
                System.out.println("This collection is empty.");
            }
            for (int i = 4 * page; i < 4 * page + 4; i++) {
                if (i >= videoGames.size()) {
                    break;
                }
                VideoGame videoGame = videoGames.get(i);
                System.out.println("[" + (i % 4) + "] - " + String.format("%-" + maxVideoGameTitleLength + "s", videoGame.getTitle()) + 
                                   "ESRB Rating: " + videoGame.getEsrbRating());
            }
            System.out.println("...");
            if (page > 0) {
                System.out.println("[4] - Previous page");
            }
            if (page < numPages - 1) {
                System.out.println("[5] - Next page");
            }
            System.out.println("[6] - Play a random game!");
            System.out.println("[7] - Rename this collection");
            System.out.println("[8] - Delete this collection");
            System.out.println("[9] - Return to main menu");
            int choice = 10;
            try {
                choice = Integer.parseInt(scan.nextLine().trim());
            } catch (NumberFormatException e) {
            }
            switch (choice) {
                case 0, 1, 2, 3 -> {
                    if (choice + page * 4 < videoGames.size()) collection.deleteVideoGame(connection, videoGames.get(choice + page * 4).getVgnr());
                }
                case 4 -> {
                    if (page > 0) {
                        page--;
                    }
                }
                case 5 -> {
                    if (page < numPages - 1) {
                        page++;
                    }
                }
                case 6 -> {
                    Random rand = new Random();
                    VideoGame randomVideoGame = videoGames.get(rand.nextInt(videoGames.size()));
                    System.out.print("Your random game is '" + randomVideoGame.getTitle() + "''.");
                    playVideoGame(randomVideoGame);
                }
                case 7 -> {
                    System.out.print("Enter a new name for this collection: ");
                    String newName = scan.nextLine().trim();
                    collection.rename(connection, newName);
                    System.out.println("Renamed collection!");
                }
                case 8 -> {
                    collection.delete(connection);
                    System.out.println("Deleted collection!");
                    break gamesInCollectionLoop;
                }
                case 9 -> {
                    System.out.println("Returning to main menu...");
                    break gamesInCollectionLoop;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Displays a throwaway collection selection menu
     * @param conn the Connection object representing the database connection
     */
    private static Collection pickCollection() {
        int page = 0, numPages;
        while (true) {
            List<Collection> collections = Collection.getCollectionsByUser(connection, currentUser.getUsername());
            numPages = (collections.size() / 6) + (collections.size() % 6 == 0 ? 0 : 1);
            int maxCollectionNameLength = 0;
            int maxCollectionNumGamesLength = 0;
            for (Collection collection : collections) {
                if (collection.getName().length() > maxCollectionNameLength) {
                    maxCollectionNameLength = collection.getName().length();
                }
                if (String.valueOf(collection.getNumGames()).length() > maxCollectionNumGamesLength) {
                    maxCollectionNumGamesLength = String.valueOf(collection.getNumGames()).length();
                }
            }
            maxCollectionNameLength += 2;
            maxCollectionNumGamesLength += 2 + 6;
            System.out.println("Page (" + (page + 1) + "/" + numPages + ")");
            if (collections.isEmpty()) {
                System.out.println("You have no collections.");
            }
            for (int i = 6 * page; i < 6 * page + 6; i++) {
                if (i >= collections.size()) {
                    break;
                }
                Collection collection = collections.get(i);
                System.out.println("[" + (i % 6) + "] - " + String.format("%-" + maxCollectionNameLength + "s", collection.getName()) + 
                                   String.format("%-" + maxCollectionNumGamesLength + "s", collection.getNumGames() + " games") +
                                   (collection.getTotalPlayTime() / 60) + ":" + (collection.getTotalPlayTime() % 60) + " play time");
            }
            System.out.println("...");
            if (page > 0) {
                System.out.println("[6] - Previous page");
            }
            if (page < numPages - 1) {
                System.out.println("[7] - Next page");
            }
            System.out.println("[8] - Create new collection");
            System.out.println("[9] - Cancel");
            int choice = 10;
            try {
                choice = Integer.parseInt(scan.nextLine().trim());
            } catch (NumberFormatException e) {
            }
            switch (choice) {
                case 0, 1, 2, 3, 4, 5 -> {
                    if (choice + page * 6 < collections.size()) return collections.get(choice + page * 6);
                }
                case 6 -> {
                    if (page > 0) page--;
                }
                case 7 -> {
                    if (page < numPages - 1) page++;
                }
                case 8 -> createCollection();
                case 9 -> {
                    System.out.println("Cancelling...");
                    return null;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Create a new collection by prompting for the collection name.
     * @param conn the Connection object representing the database connection
     */
    private static void createCollection() {
        System.out.print("Enter collection name: ");
        String collectionName = scan.nextLine().trim();
        Collection collection = new Collection(collectionName, currentUser.getUsername());
        collection.saveToDatabase(connection);
        System.out.println("Collection created successfully with CN: " + collection.getCnr() + ".");
    }

    /**
     * Displays a followers menu for users to see who is following them
     * @param conn the Connection object representing the database connection
     */
    public static void displayFollowers() {
        menuLoop:
        while (true) {
            List <User> followers = currentUser.getFollowers(connection);
            System.out.println("People following " + currentUser.getUsername() + ":");
            int page = 0;
            int numPages;
            numPages = (followers.size() / 7) + (followers.size() % 7 == 0 ? 0 : 1);
            System.out.println("Page (" + (page + 1) + "/" + numPages + ")");
            if (followers.isEmpty()) {
                System.out.println("No one is following you.");
            }
            for (int i = 7 * page; i < 7 * page + 7; i++) {
                if (i >= followers.size()) {
                    break;
                }
                User f = followers.get(i);
                System.out.println("[" + i + "] - " + f.getUsername());
            }
            System.out.println("...");
            if (page > 0) {
                System.out.println("[7] - Previous page");
            }
            if (page < numPages - 1) {
                System.out.println("[8] - Next page");
            }
            System.out.println("[9] - Return to main menu");
            int choice = 10;
            try {
                choice = Integer.parseInt(scan.nextLine().trim());
            } catch (NumberFormatException e) {
            }
            switch (choice) {
                case 7 -> {
                    if (page > 0) {
                        page--;
                    }
                }
                case 8 -> {
                    if (page < numPages - 1) {
                        page++;
                    }
                }
                case 9 -> {
                    System.out.println("Returning to main menu...");
                    break menuLoop;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Displays a following menu for users to follow and unfollow other users
     * @param conn the Connection object representing the database connection
     */
    public static void displayFollowing() {
        menuLoop:
        while (true) {
            List <User> following = currentUser.getFollowing(connection);
            System.out.println("Following for user " + currentUser.getUsername() + ":");
            int page = 0;
            int numPages;
            System.out.println("Select the follower you wish to unfollow.");
            numPages = (following.size() / 6) + (following.size() % 6 == 0 ? 0 : 1);
            System.out.println("Page (" + (page + 1) + "/" + numPages + ")");
            if (following.isEmpty()) {
                System.out.println("You have no one you're following.");
            }
            for (int i = 6 * page; i < 6 * page + 6; i++) {
                if (i >= following.size()) {
                    break;
                }
                User f = following.get(i);
                System.out.println("[" + i + "] - " + f.getUsername());
            }
            System.out.println("...");
            if (page > 0) {
                System.out.println("[6] - Previous page");
            }
            if (page < numPages - 1) {
                System.out.println("[7] - Next page");
            }
            System.out.println("[8] - Follow user");
            System.out.println("[9] - Return to main menu");
            int choice = 10;
            try {
                choice = Integer.parseInt(scan.nextLine().trim());
            } catch (NumberFormatException e) {
            }
            switch (choice) {
                case 0, 1, 2, 3, 4, 5 -> {
                    if (choice + page * 6 < following.size()) currentUser.unfollow(connection, following.get(choice + page * 6).getUsername());
                }
                case 6 -> {
                    if (page > 0) {
                        page--;
                    }
                }
                case 7 -> {
                    if (page < numPages - 1) {
                        page++;
                    }
                }
                case 8 -> {
                    System.out.print("Enter the email of the user you wish to follow: ");
                    String email = scan.nextLine().trim();
                
                    currentUser.follow(connection, email);
                }
                case 9 -> {
                    System.out.println("Returning to main menu...");
                    break menuLoop;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Displays list of video games based on user input criteria.
     * @param conn
     */
    private static void searchAllVideoGames() {
        System.out.print("\nEnter a video game title, a part of a title, or press ENTER to skip: ");
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
        System.out.print("Enter sort method ('title', 'price', 'genre', or 'date'): ");
        String sortBy = scan.nextLine().trim().toLowerCase();
        System.out.print("Enter 0 for ascending order or 1 for descending order: ");
        String ascString = scan.nextLine().trim();
        boolean ascending = ascString.equals("0");

        int page = 0;
        int numPages;
        searchLoop:
        while (true) {
            List<VideoGame> videoGames = VideoGame.searchVideoGames(connection, title, platform, lowerReleaseDate, upperReleaseDate, developerName, lowerPrice, upperPrice, genre, sortBy, ascending);
            numPages = (videoGames.size() / 7) + (videoGames.size() % 7 == 0 ? 0 : 1);
            int maxVideoGameTitleLength = 0;
            for (VideoGame videoGame : videoGames) {
                if (videoGame.getTitle().length() > maxVideoGameTitleLength) {
                    maxVideoGameTitleLength = videoGame.getTitle().length();
                }
            }
            maxVideoGameTitleLength += 2;
            System.out.println("\nSelect the game you wish to interact with.");
            System.out.println("Video games that matched your search:");
            System.out.println("Page (" + (page + 1) + "/" + numPages + ")");
            if (videoGames.isEmpty()) {
                System.out.println("No video games matched your search.");
            }
            for (int i = 7 * page; i < 7 * page + 7; i++) {
                if (i >= videoGames.size()) {
                    break;
                }
                VideoGame videoGame = videoGames.get(i);
                System.out.println("[" + (i % 7) + "] - " + String.format("%-" + maxVideoGameTitleLength + "s", videoGame.getTitle()) + 
                                   "ESRB Rating: " + videoGame.getEsrbRating());
            }
            System.out.println("...");
            if (page > 0) {
                System.out.println("[7] - Previous page");
            }
            if (page < numPages - 1) {
                System.out.println("[8] - Next page");
            }
            System.out.println("[9] - Return to main menu");
            int choice = 10;
            try {
                choice = Integer.parseInt(scan.nextLine().trim());
            } catch (NumberFormatException e) {
            }
            switch (choice) {
                case 0, 1, 2, 3, 4, 5, 6 -> {
                    if (choice + page * 7 < videoGames.size()) interactVideoGame(videoGames.get(choice + page * 7));
                }
                case 7 -> {
                    if (page > 0) {
                        page--;
                    }
                }
                case 8 -> {
                    if (page < numPages - 1) {
                        page++;
                    }
                }
                case 9 -> {
                    System.out.println("Returning to main menu...");
                    break searchLoop;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Displays interaction options after a user selects a video game.
     * @param conn the Connection object representing the database connection
     * @param videoGame the selected video game
     */
    private static void interactVideoGame(VideoGame videoGame) {
        System.out.println("\nYou selected '" + videoGame.getTitle() + "'.");
        System.out.println("Please select one of the following options:");
        System.out.println("[0] - Rate this video game");
        System.out.println("[1] - Play this video game");
        System.out.println("[2] - Add this video game to a collection");
        System.out.println("[9] - Cancel");
        int choice = 10;
        try {
            choice = Integer.parseInt(scan.nextLine().trim());
        } catch (NumberFormatException e) {
        }
        switch (choice) {
            case 0 -> rateVideoGame(videoGame);
            case 1 -> playVideoGame(videoGame);
            case 2 -> {
                System.out.println("\nChoose the collection to add '" + videoGame.getTitle() + "' to.");
                Collection collection = pickCollection();
                if (collection == null) {
                    break;
                }
                addVideoGameToCollection(videoGame, collection);
            }
            case 9 -> System.out.println("Cancelling selection...");
            default -> System.out.println("Invalid choice. Please try again.");
        }
    }

    /**
     * Rating selection for a user to rate a video game.
     * @param conn the Connection object representing the database connection
     * @param videoGame the selected video game
     */
    private static void rateVideoGame(VideoGame videoGame) {
        System.out.println("\nEnter your rating for '" + videoGame.getTitle() + "': ");
        String ratingStr = scan.nextLine().trim();
        int rating = 0;
        try {
            rating = ratingStr.isEmpty() ? -1 : Integer.parseInt(ratingStr);
        } catch (NumberFormatException e) {
        }
        if (rating < 1 || rating > 5) {
            System.out.println("Please enter a valid rating between 1 and 5.");
            return;
        }
        currentUser.rateVideoGame(connection, videoGame, rating);
        System.out.println("Succesfully added rating.");
    }

    /**
     * Play menu for a user to "play" a video game (logs time played)
     * @param conn the Connection object representing the database connection
     * @param videoGame the selected video game
     */
    private static void playVideoGame(VideoGame videoGame) {
        long currentTimeMillis = System.currentTimeMillis();
        Timestamp startTimestamp = new Timestamp(currentTimeMillis);
        System.out.println("\nStarting playing '" + videoGame.getTitle() + "' at " + startTimestamp.toString() + "!");
        System.out.println("Press ENTER when you're done playing.");
        scan.nextLine();
        currentTimeMillis = System.currentTimeMillis();
        Timestamp endTimestamp = new Timestamp(currentTimeMillis);
        currentUser.playVideoGame(connection, startTimestamp, endTimestamp, videoGame);
        System.out.println("Finished playing '" + videoGame.getTitle() + "' at " + endTimestamp.toString() + ".");
    }

    /**
     * Menu to add a video game to a user's existing collection
     * @param conn the Connection object representing the database connection
     * @param videoGame the selected video game
     * @param collection the collection the user wants to add it to
     */
    private static void addVideoGameToCollection(VideoGame videoGame, Collection collection) {
        List<VideoGame> videoGames = collection.getVideoGames(connection);
        for (VideoGame vg : videoGames) {
            if (vg.getVgnr() == videoGame.getVgnr()) {
                System.out.println("You already have this video game in your collection!");
                return;
            }
        }
        collection.addVideoGame(connection, videoGame.getVgnr());
        if (!videoGame.checkPlatformOwnership(connection, currentUser)) {
            System.out.println("WARNING: You do not own any platforms this video game is on.");
        }
        System.out.println("Succesfully added '" + videoGame.getTitle() + "'' to collection " + collection.getName() + ".");
    }
}
