package git.aatufutaa.lobby.mongo;

import git.aatufutaa.lobby.LobbyServer;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class LobbyMongoManager {

    private MongoDatabase playDatabase;
    private MongoDatabase loginDatabase;

    public LobbyMongoManager() {
    }

    public void init() {
        this.playDatabase = LobbyServer.getInstance().getMongoManager().getClient().getDatabase("play");
        this.loginDatabase = LobbyServer.getInstance().getMongoManager().getClient().getDatabase("login");
    }

    public MongoCollection<Document> getPlayers() {
        return this.playDatabase.getCollection("players");
    }

    public MongoCollection<Document> getLoginPlayers() {
        return this.loginDatabase.getCollection("players");
    }
}
