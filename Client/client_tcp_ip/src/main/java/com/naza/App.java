package com.naza;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class App {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;
    private static final String KEYSTORE_PATH = System.getProperty("user.dir") + "/trusted_certs/keystore.jks";
    private static final String KEYSTORE_PASSWORD = "password";

    public static void main(String[] args) throws IOException {
        SSLSocket socket = createSSLSocket();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            // Send data to the server
            writer.println("Hello from the client!");

            // Receive response from the server
            String serverResponse = reader.readLine();
            System.out.println("Received from server: " + serverResponse);

        } finally {
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
}
