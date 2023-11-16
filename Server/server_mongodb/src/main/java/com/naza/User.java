package com.naza;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

import org.bson.types.ObjectId;

import java.util.ArrayList;

public class User {
    private String username;
    private String salt;
    private String password;
    private List<ObjectId> records;

    public User(String username, String password, String salt, List<ObjectId> records) {
        this.username = username;
        this.salt = salt;
        this.password = password;
        this.records = records;
    }

    public User(String username, String password) {
        this.username = username;
        this.salt = generateSalt();
        this.password = get_SHA_512_SecurePassword(password, salt);
        this.records = new ArrayList<ObjectId>();
    }

    public User(String username, String password, String salt) {
        this.username = username;
        this.salt = generateSalt();
        this.password = get_SHA_512_SecurePassword(password, salt);
        this.records = new ArrayList<ObjectId>();
    }

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

    public static String generateSalt() {
        SecureRandom RANDOM = new SecureRandom();
        Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
        byte[] salt = new byte[16]; // 128 bits
        RANDOM.nextBytes(salt);
        return ENCODER.encodeToString(salt);
    }

    public String getUsername() {
        return this.username;
    }

    public String getSalt() {
        return this.salt;
    }

    public String getPassword() {
        return this.password;
    }

    public List<ObjectId> getRecords() {
        return this.records;
    }

    public boolean equals(User other) {
        return this.username.equals(other.getUsername()) && this.password.equals(other.getPassword());
    }
}
