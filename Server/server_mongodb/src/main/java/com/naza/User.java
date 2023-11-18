package com.naza;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

import org.bson.types.ObjectId;

import java.util.ArrayList;

/**
 * The User class represents a user in the system.
 * It contains information such as username, password, salt, and records.
 */
public class User {
    private String username;
    private String salt;
    private String password;
    private List<ObjectId> records;

    /**
     * Default constructor for User class.
     * Initializes the username, salt, password, and records.
     */
    public User() {
        this.username = "";
        this.salt = "";
        this.password = "";
        this.records = new ArrayList<ObjectId>();
    }

    /**
     * Constructor for User class with username, password, salt, and records.
     * 
     * @param username The username of the user.
     * @param password The password of the user.
     * @param salt     The salt used for password hashing.
     * @param records  The list of records associated with the user.
     */
    public User(String username, String password, String salt, List<ObjectId> records) {
        this.username = username;
        this.salt = salt;
        this.password = password;
        this.records = records;
    }

    /**
     * Constructor for User class with username and password.
     * Generates a salt and hashes the password using SHA-512 algorithm.
     * Initializes the username, salt, password, and records.
     * 
     * @param username The username of the user.
     * @param password The password of the user.
     */
    public User(String username, String password) {
        this.username = username;
        this.salt = generateSalt();
        this.password = get_SHA_512_SecurePassword(password, salt);
        this.records = new ArrayList<ObjectId>();
    }

    /**
     * Constructor for User class with username, password, and salt.
     * Generates a salt and hashes the password using SHA-512 algorithm.
     * Initializes the username, salt, password, and records.
     * 
     * @param username The username of the user.
     * @param password The password of the user.
     * @param salt     The salt used for password hashing.
     */
    public User(String username, String password, String salt) {
        this.username = username;
        this.salt = salt;
        this.password = get_SHA_512_SecurePassword(password, salt);
        this.records = new ArrayList<ObjectId>();
    }

    /**
     * Hashes the given password using SHA-512 algorithm and the provided salt.
     * 
     * @param passwordToHash The password to be hashed.
     * @param salt           The salt used for password hashing.
     * @return The hashed password.
     */
    private static String get_SHA_512_SecurePassword(String passwordToHash, String salt) {
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] bytes = md.digest(passwordToHash.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    /**
     * Generates a random salt for password hashing.
     * 
     * @return The generated salt.
     */
    public static String generateSalt() {
        SecureRandom RANDOM = new SecureRandom();
        Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
        byte[] salt = new byte[16]; // 128 bits
        RANDOM.nextBytes(salt);
        return ENCODER.encodeToString(salt);
    }

    /**
     * Returns the username of the user.
     * 
     * @return The username.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Returns the salt used for password hashing.
     * 
     * @return The salt.
     */
    public String getSalt() {
        return this.salt;
    }

    /**
     * Returns the hashed password of the user.
     * 
     * @return The hashed password.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Returns the list of records associated with the user.
     * 
     * @return The list of records.
     */
    public List<ObjectId> getRecords() {
        return this.records;
    }

    public boolean equals(User other) {
        return this.username.equals(other.getUsername()) && this.password.equals(other.getPassword());
    }
}
