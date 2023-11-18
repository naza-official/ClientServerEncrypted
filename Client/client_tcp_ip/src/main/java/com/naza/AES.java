package com.naza;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/*
    ENCRYPTION
 public static void main(String[] args) {
        try {
            AESEncryption server = new AESEncryption();
            server.initFromStrings("CHuO1Fjd8YgJqTyapibFBQ==", "e3IYYJC2hxe24/EO");
            String encryptedMessage = server.encrypt("TheXCoders_2");
            System.err.println("Encrypted Message : " + encryptedMessage);
        } catch (Exception ignored) {
        }
    }
public static void main(String[] args) {
        try {
            AESDecryption client = new AESDecryption();
            client.initFromStrings("CHuO1Fjd8YgJqTyapibFBQ==", "e3IYYJC2hxe24/EO");
            String decryptedMessage = client.decrypt("mqQQF6K2GEaR0JKTd1yN58Mbs7qeYamM0xgung==");
            System.err.println("Decrypted Message : " + decryptedMessage);
        } catch (Exception ignored) {
        }

    DECRYPTION
 */

/**
 * AES class represents an implementation of the Advanced Encryption Standard
 * (AES) algorithm.
 * It provides methods for key generation, encryption, and decryption using AES.
 */
public class AES {
    private SecretKey key;
    private int KEY_SIZE = 128;
    private int T_LEN = 128;
    private byte[] IV;

    /**
     * Initializes the AES object by generating a new random secret key.
     * 
     * @throws Exception if an error occurs during key generation.
     */
    public void init() throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(KEY_SIZE);
        key = generator.generateKey();
    }

    /**
     * Prints the secret key and initialization vector (IV) to the standard error
     * stream.
     */
    public void exportKeys() {
        System.err.println("Secret Key : " + encode(key.getEncoded()));
        System.err.println("IV : " + encode(IV));
    }

    /**
     * Sets the key size and tag length for AES encryption and decryption.
     * 
     * @param KEY_SIZE the key size in bits.
     * @param T_LEN    the tag length in bits.
     */
    public void setKeySizeAndTlen(int KEY_SIZE, int T_LEN) {
        this.KEY_SIZE = KEY_SIZE;
        this.T_LEN = T_LEN;
    }

    /**
     * Initializes the AES object using the provided secret key and IV.
     * 
     * @param secretKey the secret key as a Base64-encoded string.
     * @param IV        the initialization vector as a Base64-encoded string.
     */
    public void initFromStrings(String secretKey, String IV) {
        key = new SecretKeySpec(decode(secretKey), "AES");
        this.IV = decode(IV);
    }

    /**
     * Decrypts the given encrypted message using AES/GCM/NoPadding mode.
     * 
     * @param encryptedMessage the encrypted message as a Base64-encoded string.
     * @return the decrypted message.
     * @throws Exception if an error occurs during decryption.
     */
    public String decrypt(String encryptedMessage) throws Exception {
        byte[] messageInBytes = decode(encryptedMessage);
        Cipher decryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(T_LEN, IV);
        decryptionCipher.init(Cipher.DECRYPT_MODE, key, spec);
        byte[] decryptedBytes = decryptionCipher.doFinal(messageInBytes);
        return new String(decryptedBytes);
    }

    /**
     * Encrypts the given message using AES/GCM/NoPadding mode.
     * 
     * @param message the message to encrypt.
     * @return the encrypted message as a Base64-encoded string.
     * @throws Exception if an error occurs during encryption.
     */
    public String encrypt(String message) throws Exception {
        byte[] messageInBytes = message.getBytes();
        Cipher encryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
        encryptionCipher.init(Cipher.ENCRYPT_MODE, key);
        IV = encryptionCipher.getIV();
        byte[] encryptedBytes = encryptionCipher.doFinal(messageInBytes);
        return encode(encryptedBytes);
    }

    private String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    private byte[] decode(String data) {
        return Base64.getDecoder().decode(data);
    }

    public static void main(String[] args) {
        try {
            AES server = new AES();
            server.initFromStrings("hIQBfpto5UpJEHfYfSR8WA==", "fCvI1wG0JOYMX2YR");
            String encryptedMessage = "PgxymN2OaxJTXIFho8SCG98vXlTTL9vaWh9l/1LNTyoEzYurj6puCmg2SxxRAuwv4hGDHGpz7vEBvXhE8A==";
            String res = server.decrypt(encryptedMessage);
            System.err.println("Decrypted Message : " + res);
        } catch (Exception ignored) {
            // Handle exception
        }
    }
}
