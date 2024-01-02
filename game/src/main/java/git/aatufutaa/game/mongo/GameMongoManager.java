package git.aatufutaa.game.mongo;

import git.aatufutaa.game.GameServer;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class GameMongoManager {

    private MongoDatabase database;

    public GameMongoManager() {
    }

    public void init() {
        this.database = GameServer.getInstance().getMongoManager().getClient().getDatabase("play");
    }

    public MongoCollection<Document> getPlayers() {
        return this.database.getCollection("players");
    }

}
