package com.fumedatabase.fumedatabase_api.model;

import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


// get rid of statics when put into user class.
public class PasswordManager {
    /**
     * verifies the password of the user
     * @param username
     * @param password
     * @param hashedPassword
     * @return
     */
    public static boolean verifyPassword(String username, String password, String hashedPassword) {
        String salt = generateSalt(username);
        String hashedInputPassword = hashPassword(password, salt);
        return hashedInputPassword.equals(hashedPassword);
    }

    /**
     * Generates a salt by taking every other character of the username.
     * @param username the username to generate a salt from
     * @return the generated salt
     */
    // use this one when verifying credentials
    public static String generateSalt(String username) {
        StringBuilder salt = new StringBuilder();
        for (int i = 0; i < username.length(); i+=2){
            salt.append(username.charAt(i));
        }
        return salt.toString();
    }
    
    /**
     * Hashes a password using PBKDF2 with HmacSHA256.
     * @param password the password to hash
     * @param salt the salt to use for hashing
     * @return the hashed password
     */
    public static String hashPassword(String password, String salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}
