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

public class AES {
    private SecretKey key;
    private int KEY_SIZE = 128;
    private int T_LEN = 128;
    private byte[] IV;

    public void init() throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(KEY_SIZE);
        key = generator.generateKey();
    }

    public void exportKeys() {
        System.err.println("Secret Key : " + encode(key.getEncoded()));
        System.err.println("IV : " + encode(IV));
    }

    public void setKeySizeAndTlen(int KEY_SIZE, int T_LEN) {
        this.KEY_SIZE = KEY_SIZE;
        this.T_LEN = T_LEN;
    }

    public void initFromStrings(String secretKey, String IV) {
        key = new SecretKeySpec(decode(secretKey), "AES");
        this.IV = decode(IV);
    }

    public String decrypt(String encryptedMessage) throws Exception {
        byte[] messageInBytes = decode(encryptedMessage);
        Cipher decryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(T_LEN, IV);
        decryptionCipher.init(Cipher.DECRYPT_MODE, key, spec);
        byte[] decryptedBytes = decryptionCipher.doFinal(messageInBytes);
        return new String(decryptedBytes);
    }

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

}