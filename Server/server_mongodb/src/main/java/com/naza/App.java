package com.naza;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class App {
    private static final int PORT = 8888;
    private static final String KEYSTORE_PATH = System.getProperty("user.dir") + "/certs/keystore.jks";
    private static final String KEYSTORE_PASSWORD = "password";

    public static void main(String[] args) throws IOException {
        SSLServerSocket serverSocket = createSSLServerSocket();

        System.out.println("Server started. Listening on port " + PORT);

        try {
            while (true) {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();

                // Handle client communication in a separate thread
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } finally {
            serverSocket.close();
        }
    }

    private static class ClientHandler implements Runnable {
        private final SSLSocket clientSocket;

        public ClientHandler(SSLSocket clientSocket) {
            this.clientSocket = clientSocket;
            this.run();
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

                // Read data from the client
                String clientData = reader.readLine();
                System.out.println("Received from client: " + clientData);

                // Send a response to the client
                writer.println("Hello from the server!");

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
}
