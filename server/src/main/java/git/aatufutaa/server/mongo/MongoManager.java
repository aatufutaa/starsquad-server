package git.aatufutaa.server.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import org.bson.Document;

public class MongoManager {

    @Getter
    private MongoClient client;
    private MongoDatabase internal;

    public void init() {
        this.client = MongoClients.create("mongodb://localhost:27017");

        this.internal = this.client.getDatabase("internal");
    }

    public MongoCollection<Document> getUpdates() {
        return this.internal.getCollection("updates");
    }

    public void close() {
        this.client.close();
    }
}
