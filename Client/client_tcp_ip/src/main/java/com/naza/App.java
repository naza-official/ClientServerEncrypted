package com.naza;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class App {
    private static String SERVER_HOST = "localhost";
    private static int SERVER_PORT = 8888;
    private static final String KEYSTORE_PATH = System.getProperty("user.dir") + "/trusted_certs/keystore.jks";
    private static final String KEYSTORE_PASSWORD = "password";

    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Server host: ");
        SERVER_HOST = scanner.nextLine();
        System.out.println("Server port: ");
        SERVER_PORT = Integer.parseInt(scanner.nextLine());

        SSLSocket socket = createSSLSocket();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            String input;
            String query;
            String[] response;
            String responseCode;
            String responseBody;
            AES aes = new AES();

            System.out.println("Type '1' for log in, '2' for sign up");
            input = scanner.nextLine();

            System.out.println("Enter username");
            String username = scanner.nextLine();
            System.out.println("Enter password");
            String password = scanner.nextLine();

            if (input.equals("1")) {
                query = String.format("login %s %s", username, password);
            } else if (input.equals("2")) {
                query = String.format("signup %s %s", username, password);
            } else {
                System.err.println("Invalid input");
                return;
            }
            writer.println(encode(query));
            response = decode(reader.readLine()).split(" ", 2);
            responseCode = response[0];
            responseBody = response[1];

            if (responseCode.equals("200")) {
                System.out.println(responseBody);
            } else if (responseCode.startsWith("4") || responseCode.startsWith("5")) {
                System.err.println("Error: " + responseCode);
                System.err.println(responseBody);
                return;
            } else {
                System.err.println("Unknown error");
                return;
            }
            while (true) {
                System.out.println("Type '1' to read a document, '2' to create a document, '3' to delete a document");
                input = scanner.nextLine();

                if (input.equals("1")) {
                    writer.println(encode("read"));
                    responseBody = decode(reader.readLine());
                    if (responseBody.length() < 2) {
                        System.out.println("You have no saved documents");
                        return;
                    }
                    System.out.println("You have next saved documents: ");
                    System.out.println(responseBody);

                    System.out.println("Choose document number[print number]: ");
                    input = scanner.nextLine();
                    query = String.format("%s", input);
                    writer.println(encode(query));
                    response = decode(reader.readLine()).split(" ", 2);
                    responseCode = response[0];
                    responseBody = response[1];
                    if (responseCode.equals("200")) {
                        System.out.println("Got encrypted document");
                        System.out.println(responseBody);
                    } else if (responseCode.startsWith("4") || responseCode.startsWith("5")) {
                        System.err.println("Error: " + responseCode);
                        System.err.println(responseBody);
                        return;
                    } else {
                        System.err.println("Unknown error");
                        return;
                    }

                    System.out.println("Enter private key: ");
                    String privateKey = scanner.nextLine();
                    System.out.println("Enter IV: ");
                    String iv = scanner.nextLine();
                    aes.initFromStrings(privateKey, iv);
                    try {
                        String decryptedDocument = aes.decrypt(responseBody);
                        System.out.println("Decrypted document: ");
                        System.out.println(decryptedDocument);
                    } catch (Exception e) {
                        System.err.println("Decryption Error");
                        System.err.println(e.getMessage());
                        traceback: for (StackTraceElement ste : e.getStackTrace()) {
                            System.err.println(ste);
                        }
                        return;
                    }
                } else if (input.equals("2")) {

                    System.out.println("Enter your title[WILL NOT BE ENCRYPTED]: ");
                    String title = scanner.nextLine();
                    title = title.replace(" ", "_");
                    System.out.println("Enter your text[double press ENTER to stop]: ");
                    String line;
                    StringBuilder sb = new StringBuilder();
                    while (!(line = scanner.nextLine()).isEmpty()) {
                        sb.append(line).append("\n");
                    }
                    String text = sb.toString();
                    try {
                        aes.init();
                        text = aes.encrypt(text);
                        writer.println(encode("create " + title + " " + text));

                        response = decode(reader.readLine()).split(" ", 2);
                        responseCode = response[0];
                        responseBody = response[1];

                        if (responseCode.equals("200")) {
                            System.out.println(responseBody);
                            System.out.println(
                                    "Keep these keys safe!!! You will not be able to decrypt your document without them.");
                            aes.exportKeys();
                        } else if (responseCode.startsWith("4") || responseCode.startsWith("5")) {
                            System.err.println("Error: " + responseCode);
                            System.err.println(responseBody);
                            return;
                        } else {
                            System.err.println("Unknown error");
                            return;
                        }
                    } catch (Exception ignored) {

                    }

                    // realize encryption

                } else if (input.equals("3")) {
                    writer.println(encode("delete"));
                    responseBody = decode(reader.readLine());
                    if (responseBody.length() < 2) {
                        System.out.println("You have no saved documents");
                        return;
                    }
                    System.out.println("You have next saved documents: ");
                    System.out.println(responseBody);

                    System.out.println("Choose document number[print number]: ");
                    input = scanner.nextLine();
                    query = String.format("%s", input);
                    writer.println(encode(query));
                    response = decode(reader.readLine()).split(" ", 2);
                    responseCode = response[0];
                    responseBody = response[1];
                    if (responseCode.equals("200")) {
                        System.out.println("Success!");
                        System.out.println(responseBody);
                    } else if (responseCode.startsWith("4") || responseCode.startsWith("5")) {
                        System.err.println("Error: " + responseCode);
                        System.err.println(responseBody);
                        return;
                    } else {
                        System.err.println("Unknown error");
                        return;
                    }

                } else {
                    System.out.println("Invalid input");
                    return;
                }
            }
        } finally {
            scanner.close();
            socket.close();
        }
    }

    private static SSLSocket createSSLSocket() throws IOException {
        try {
            // Load the keystore with trusted certificates
            KeyStore trustedKeyStore = KeyStore.getInstance("JKS");
            trustedKeyStore.load(new FileInputStream(KEYSTORE_PATH), KEYSTORE_PASSWORD.toCharArray());

            // Create SSL context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(trustedKeyStore);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

            // Create SSL socket
            SSLSocketFactory socketFactory = sslContext.getSocketFactory();
            return (SSLSocket) socketFactory.createSocket(SERVER_HOST, SERVER_PORT);
        } catch (Exception e) {
            throw new IOException("Error creating SSL socket", e);
        }
    }

    public static String generateString(int length, char fillChar) {
        if (length < 0) {
            throw new IllegalArgumentException("Length must be non-negative");
        }

        char[] charArray = new char[length];
        Arrays.fill(charArray, fillChar);
        return new String(charArray);
    }

    public static String encode(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes());
    }

    public static String decode(String str) {
        byte[] decodedBytes = Base64.getDecoder().decode(str);
        return new String(decodedBytes);
    }
}
