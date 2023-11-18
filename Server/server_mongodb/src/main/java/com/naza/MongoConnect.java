package com.naza;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;

public class MongoConnect {
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> users;
    private MongoCollection<Document> records;

    /**
     * Initializes the MongoDB connection.
     */
    public void init() {
        // ...

        String connectionString = "mongodb://localhost:27017"; // Provide the connection string directly
        try {
            this.mongoClient = MongoClients.create(connectionString);
            this.database = mongoClient.getDatabase("serverDB");
            records = database.getCollection("records");
            users = database.getCollection("users");
        } catch (Exception e) {
            // Handle any exceptions
        }
    }

    /**
     * Inserts a new user into the database.
     *
     * @param user The user object to be inserted.
     * @throws AuthError If a user with the same username already exists.
     */
    public void insertUser(User user) throws AuthError {

        String username = user.getUsername();
        Document existingUser = users.find(new Document("username", username)).first();
        if (existingUser != null) {
            throw new AuthError("User with the same username already exists");
        }

        Document doc = new Document("username", username)
                .append("salt", user.getSalt())
                .append("password", user.getPassword())
                .append("records", new ArrayList<ObjectId>());
        users.insertOne(doc);
    }

    /*
     * Deletes a record from a user's records list.
     *
     * @param username The username of the user.
     * 
     * @param recordId The ID of the record to be deleted.
     * 
     * @return The number of records modified.
     */
    public int deleteRecord(String username, ObjectId recordId) {

        UpdateResult res = users.updateOne(new Document("username", username),
                new Document("$pull", new Document("records", recordId)));
        return (int) res.getModifiedCount();
    }

    /**
     * Retrieves a user by username.
     *
     * @param username The username of the user.
     * @return The User object.
     * @throws AuthError If the user is not found.
     */
    User getUser(String username) throws AuthError {

        Document doc = users.find(new Document("username", username)).first();
        if (doc == null) {
            throw new AuthError("User not found");
        }
        return new User(doc.getString("username"), doc.getString("password"), doc.getString("salt"),
                (List<ObjectId>) doc.get("records"));
    }

    /**
     * Inserts a new record into the database.
     *
     * @param username The username of the user.
     * @param title    The title of the record.
     * @param record   The record content.
     * @return The ID of the inserted record.
     */
    public ObjectId insertRecord(String username, String title, String record) {

        Document doc = new Document("record", record).append("title", title);
        InsertOneResult res = records.insertOne(doc);
        return res.getInsertedId().asObjectId().getValue();
    }

    /**
     * Updates a user's records list with a new record.
     *
     * @param username The username of the user.
     * @param recordId The ID of the record to be added.
     * @return The number of records modified.
     */
    public int updateUserRecords(String username, ObjectId recordId) {

        UpdateResult res = users.updateOne(new Document("username", username),
                new Document("$push", new Document("records", recordId)));
        return (int) res.getModifiedCount();
    }

    /**
     * Retrieves the titles of a user's records.
     *
     * 
     * @param user The User object.
     * @return The titles of the user's records.
     */
    public String getUserRecordTitles(User user) {

        List<ObjectId> recordIds = user.getRecords();

        StringBuilder titles = new StringBuilder();
        for (int i = 0; i < recordIds.size(); i++) {
            Document recordDoc = records.find(new Document("_id", recordIds.get(i))).first();
            if (recordDoc != null) {
                titles.append(String.format("%d %s\n", i, recordDoc.getString("title")));
            }
        }

        return titles.toString();
    }

    /**
     * Retrieves the content of a record by its ID.
     *
     * @param recordId The ID of the record.
     * @return The content of the record.
     */
    public String getRecord(ObjectId recordId) {

        Document recordDoc = records.find(new Document("_id", recordId)).first();
        if (recordDoc != null) {
            return recordDoc.getString("record");
        }
        return null;
    }

    public void close() {
        mongoClient.close();
    }

}