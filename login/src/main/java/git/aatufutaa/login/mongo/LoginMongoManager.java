package git.aatufutaa.login.mongo;

import git.aatufutaa.login.LoginServer;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;

public class LoginMongoManager {

    private MongoDatabase database;

    public LoginMongoManager() {
    }

    public void init() {
        this.database = LoginServer.getInstance().getMongoManager().getClient().getDatabase("login");

        this.initPlayerId();

        this.getPlayers().createIndex(new Document("game_center", 1), new IndexOptions().unique(true).partialFilterExpression(new Document("game_center", new Document("$exists", true))));
        this.getPlayers().createIndex(new Document("google_play", 1), new IndexOptions().unique(true).partialFilterExpression(new Document("google_play", new Document("$exists", true))));
    }

    public MongoCollection<Document> getPlayers() {
        return this.database.getCollection("players");
    }

    private void initPlayerId() {
        this.database.getCollection("playerId").updateOne(
                new Document("_id", "playerId"),
                Updates.setOnInsert(new Document("_id", "playerId").append("counter", 1)),
                new UpdateOptions().upsert(true)
        );
    }

    public int generatePlayerId() {
        Document document = this.database.getCollection("playerId").findOneAndUpdate(
                new Document("_id", "playerId"),
                Updates.inc("counter", 1)
        );
        if (document == null) {
            LoginServer.getInstance().crash("cant increment playerId");
            return -1;
        }
        return document.getInteger("counter");
    }

    public boolean testConnection() {
        try {
            this.database.getCollection("playerId").find(new Document("_id", "playerId")).first();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
