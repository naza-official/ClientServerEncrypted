package com.naza;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.util.Base64;
import java.util.Scanner;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

/**
 * The main class that represents the server application.
 */
public class App {
    private static int PORT = 8888;
    private static final String KEYSTORE_PATH = System.getProperty("user.dir") + "/certs/keystore.jks";
    private static final String KEYSTORE_PASSWORD = "password";

    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the server port: ");
        PORT = scanner.nextInt();
        scanner.close();

        SSLServerSocket serverSocket = createSSLServerSocket();
        MongoConnect mongoConnect = new MongoConnect();

        System.out.println("Server started. Listening on port " + PORT);

        try {
            while (true) {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();

                // Handle client communication in a separate thread
                new Thread(new ClientHandler(clientSocket, mongoConnect)).start();
            }
        } finally {
            mongoConnect.close();
            serverSocket.close();
        }
    }

    private static class ClientHandler implements Runnable {
        private final SSLSocket clientSocket;
        private final MongoConnect mongoConnect;

        public ClientHandler(SSLSocket clientSocket, MongoConnect mongoConnect) {
            this.clientSocket = clientSocket;
            this.mongoConnect = mongoConnect;
            this.run();
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String[] request;
                String requestBody;
                String requestMethod;
                User user = new User();
                mongoConnect.init();

                request = decode(reader.readLine()).split(" ", 2);

                requestMethod = request[0];
                requestBody = request[1];

                System.out.println("Received from client: " + requestMethod);

                if (requestMethod.equals("login")) {

                    try {
                        user = mongoConnect.getUser(requestBody.split(" ")[0]);
                        User findUser = new User(requestBody.split(" ")[0], requestBody.split(" ")[1], user.getSalt());
                        if (findUser.equals(user)) {
                            writer.println(encode("200 Login successful"));
                        } else {
                            writer.println(encode("401 Login failed. Password incorrect"));
                            System.err.println("401 Login failed. Password incorrect");
                            return;
                        }
                    } catch (AuthError e) {
                        writer.println(encode("401 Login failed. User not found"));
                        System.err.println("401 Login failed. User not found");
                        return;
                    }

                } else if (requestMethod.equals("signup")) {
                    try {
                        user = new User(requestBody.split(" ")[0], requestBody.split(" ")[1]);
                        mongoConnect.insertUser(user);
                        writer.println(encode("200 Signup successful"));
                    } catch (AuthError e) {
                        writer.println(encode("401 Signup failed. User already exists"));
                        System.err.println("401 Signup failed. User already exists");
                        return;
                    } catch (Exception e) {
                        writer.println(encode("500 Signup failed"));
                        System.err.println("500 Signup failed");
                        return;
                    }
                }
                request = decode(reader.readLine()).split(" ", 2);
                requestMethod = request[0];
                if (requestMethod.equals("read")) {
                    String titles = mongoConnect.getUserRecordTitles(user);
                    writer.println(encode(titles));
                    requestMethod = decode(reader.readLine());
                    try {
                        String doc = mongoConnect.getRecord(user.getRecords().get(Integer.parseInt(requestMethod)));
                        writer.println(encode("200 " + doc));
                    } catch (Exception e) {
                        writer.println(encode("400 Invalid input"));
                        System.err.println("400 Invalid input");
                    }
                } else if (requestMethod.equals("create")) {
                    requestBody = request[0];
                    int res = mongoConnect.updateUserRecords(user.getUsername(),
                            mongoConnect.insertRecord(user.getUsername(),
                                    requestBody.split(" ")[0], requestBody.split(" ")[1]));
                    if (res > 0) {
                        writer.println(encode("200 Records created: " + res));
                    } else {
                        writer.println(encode("500 Records created: " + res));
                    }
                } else if (requestMethod.equals("delete")) {
                    String titles = mongoConnect.getUserRecordTitles(user);
                    writer.println(encode(titles));
                    requestMethod = decode(reader.readLine());
                    try {
                        int res = mongoConnect.deleteRecord(user.getUsername(),
                                user.getRecords().get(Integer.parseInt(requestMethod)));
                        writer.println(encode("200 Records deleted: " + res));
                    } catch (Exception e) {
                        writer.println(encode("400 Invalid input"));
                        System.err.println("400 Invalid input");
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static SSLServerSocket createSSLServerSocket() throws IOException {
        try {
            // Load the keystore
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(KEYSTORE_PATH), KEYSTORE_PASSWORD.toCharArray());

            // Initialize key manager factory
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, KEYSTORE_PASSWORD.toCharArray());

            // Initialize SSL context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

            // Create SSL server socket
            SSLServerSocketFactory socketFactory = sslContext.getServerSocketFactory();
            return (SSLServerSocket) socketFactory.createServerSocket(PORT);
        } catch (Exception e) {
            throw new IOException("Error creating SSL server socket", e);
        }
    }

    public static String encode(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes());
    }

    public static String decode(String str) {
        byte[] decodedBytes = Base64.getDecoder().decode(str);
        return new String(decodedBytes);
    }
}
