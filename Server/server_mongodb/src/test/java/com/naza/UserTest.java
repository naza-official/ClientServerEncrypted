package com.naza;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class UserTest {

    private User user;

    @Before
    public void setUp() {
        user = new User("testuser", "password");
    }

    @Test
    public void testGetUsername() {
        assertEquals("testuser", user.getUsername());
    }

    @Test
    public void testGetSalt() {
        assertNotNull(user.getSalt());
    }

    @Test
    public void testGetPassword() {
        assertNotNull(user.getPassword());
    }

    @Test
    public void testGetRecords() {
        List<ObjectId> records = user.getRecords();
        assertNotNull(records);
        assertTrue(records.isEmpty());
    }

    @Test
    public void testEquals() {
        User otherUser = new User("testuser", "password", user.getSalt());
        assertTrue(user.equals(otherUser));
    }

    @Test
    public void testConstructorWithUsernameAndPassword() {
        assertEquals("testuser", user.getUsername());
        assertNotNull(user.getSalt());
        assertNotNull(user.getPassword());
        assertTrue(user.getRecords().isEmpty());
    }

    @Test
    public void testConstructorWithUsernamePasswordAndSalt() {
        User userWithSalt = new User("testuser", "password", "salt");
        assertEquals("testuser", userWithSalt.getUsername());
        assertEquals("salt", userWithSalt.getSalt());
        assertNotNull(userWithSalt.getPassword());
        assertTrue(userWithSalt.getRecords().isEmpty());
    }

    @Test
    public void testConstructorWithAllParameters() {
        List<ObjectId> records = new ArrayList<>();
        records.add(new ObjectId());
        User userWithAllParameters = new User("testuser", "password", "salt", records);
        assertEquals("testuser", userWithAllParameters.getUsername());
        assertEquals("salt", userWithAllParameters.getSalt());
        assertNotNull(userWithAllParameters.getPassword());
        assertEquals(records, userWithAllParameters.getRecords());
    }
}