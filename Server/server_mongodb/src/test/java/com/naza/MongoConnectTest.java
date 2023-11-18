package com.naza;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MongoConnectTest {

    private MongoConnect mongoConnect;

    @Before
    public void setUp() {
        mongoConnect = new MongoConnect();
        mongoConnect.init();
    }

    @After
    public void tearDown() {
        mongoConnect.close();
    }

    @Test
    public void testInsertUser() throws AuthError {
        User user = new User("testing", "pass", "salt", null);
        mongoConnect.insertUser(user);

        User retrievedUser = mongoConnect.getUser("testuser");
        assertNotNull(retrievedUser);
        assertEquals("testuser", retrievedUser.getUsername());
        assertEquals("password", retrievedUser.getPassword());
        assertEquals("salt", retrievedUser.getSalt());
    }

    @Test(expected = AuthError.class)
    public void testInsertUser_duplicateUsername() throws AuthError {
        User user1 = new User("testuser", "password1", "salt1", null);
        User user2 = new User("testuser", "password2", "salt2", null);

        mongoConnect.insertUser(user1);
        mongoConnect.insertUser(user2); // This should throw AuthError
    }

    @Test
    public void testDeleteRecord() {
        String username = "testuser";
        ObjectId recordId = new ObjectId();

        int modifiedCount = mongoConnect.deleteRecord(username, recordId);
        assertEquals(0, modifiedCount);
    }

    @Test
    public void testGetUser() throws AuthError {
        User user = new User("test2", "password", "salt", null);
        mongoConnect.insertUser(user);

        User retrievedUser = mongoConnect.getUser("testuser");
        assertNotNull(retrievedUser);
        assertEquals("testuser", retrievedUser.getUsername());
        assertEquals("password", retrievedUser.getPassword());
        assertEquals("salt", retrievedUser.getSalt());
    }

    @Test(expected = AuthError.class)
    public void testGetUser_userNotFound() throws AuthError {
        mongoConnect.getUser("nonexistentuser"); // This should throw AuthError
    }

    @Test
    public void testInsertRecord() {
        String username = "testuser";
        String title = "Test Record";
        String record = "This is a test record";

        ObjectId insertedId = mongoConnect.insertRecord(username, title, record);
        assertNotNull(insertedId);
    }

    @Test
    public void testUpdateUserRecords() {
        String username = "testuser";
        ObjectId recordId = new ObjectId();

        int modifiedCount = mongoConnect.updateUserRecords(username, recordId);
        assertEquals(1, modifiedCount);
    }

    @Test
    public void testGetUserRecordTitles() throws AuthError {
        User user = mongoConnect.getUser("testuser");

        String recordTitles = mongoConnect.getUserRecordTitles(user);
        assertNotNull(recordTitles);
        assertTrue(recordTitles.isEmpty());
    }

    @Test
    public void testGetRecord() {
        ObjectId recordId = new ObjectId();

        String record = mongoConnect.getRecord(recordId);
        assertNull(record);
    }
}