package com.naza;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AESTest {
    private AES aes;

    @Before
    public void setUp() throws Exception {
        aes = new AES();
        aes.initFromStrings("hIQBfpto5UpJEHfYfSR8WA==", "fCvI1wG0JOYMX2YR");
    }

    @Test
    public void testEncryptionAndDecryption() throws Exception {
        String message = "Hello, World!";
        String encryptedMessage = aes.encrypt(message);
        String decryptedMessage = aes.decrypt(encryptedMessage);

        assertEquals(message, decryptedMessage);
    }
}