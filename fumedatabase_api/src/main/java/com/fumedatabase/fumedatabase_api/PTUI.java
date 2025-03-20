package com.fumedatabase.fumedatabase_api;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.SQLException;

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
                Queries queries = new Queries(conn);
                queries.executeQueries();
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
}
