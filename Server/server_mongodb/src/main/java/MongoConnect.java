import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCursor;

public class MongoConnect {

    public static void main(String[] args) {
        String connectionString = "mongodb://localhost:27017"; // Provide the connection string directly
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            List<Document> databases = new ArrayList<>();
            try (MongoCursor<Document> cursor = mongoClient.listDatabases().iterator()) {
                while (cursor.hasNext()) {
                    Document db = cursor.next();
                    System.out.println(db.toJson());
                    databases.add(db);
                }
            }
        }
    }
}
