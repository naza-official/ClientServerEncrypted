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

public class MongoConnect {
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> users;
    private MongoCollection<Document> records;

    public void init() {
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

    public void insertUser(User user) {
        Document doc = new Document("username", user.getUsername())
                .append("salt", user.getSalt())
                .append("password", user.getPassword())
                .append("records", new ArrayList<ObjectId>());
        users.insertOne(doc);
    }

    User getUser(String username) throws AuthError {
        Document doc = users.find(new Document("username", username)).first();
        if (doc == null) {
            throw new AuthError("User not found");
        }
        return new User(doc.getString("username"), doc.getString("password"), doc.getString("salt"),
                (List<ObjectId>) doc.get("records"));
    }

    public ObjectId insertRecord(String username, String title, String record) {
        Document doc = new Document("record", record).append("title", title);
        InsertOneResult res = records.insertOne(doc);
        return res.getInsertedId().asObjectId().getValue();
    }

    public void updateUserRecords(String username, ObjectId recordId) {
        users.updateOne(new Document("username", username), new Document("$push", new Document("records", recordId)));
    }

    public List<String> getUserRecordTitles(User user) {
        List<ObjectId> recordIds = user.getRecords();

        List<String> titles = new ArrayList<>();
        for (ObjectId recordId : recordIds) {
            Document recordDoc = records.find(new Document("_id", recordId)).first();
            if (recordDoc != null) {
                titles.add(recordDoc.getString("title"));
            }
        }

        return titles;
    }

    public String getRecord(ObjectId recordId) {
        Document recordDoc = records.find(new Document("_id", recordId)).first();
        if (recordDoc != null) {
            return recordDoc.getString("record");
        }
        return null;
    }
}