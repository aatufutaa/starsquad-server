package git.aatufutaa.master.server;

import git.aatufutaa.master.MasterServer;
import git.aatufutaa.master.session.Session;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public abstract class PlayServer extends Server {

    @Getter
    @Setter
    private String host;
    @Getter
    @Setter
    private int port;

    private final Map<Integer, Session> players = new HashMap<>();

    public PlayServer(int serverId, ServerLocation location, Channel channel) {
        super(serverId, location, channel);
    }

    public int getPlayerCount() {
        return this.players.size();
    }

    public boolean addPlayer(Session session) {
        if (this.players.containsKey(session.getPlayerId())) {
            MasterServer.warn("trying to add player to server " + this + " while already in it " + session);
            return false;
        }
        this.players.put(session.getPlayerId(), session);
        return true;
    }

    public boolean removePlayer(Session session) {
        boolean removed = this.players.remove(session.getPlayerId()) != null;
        if (!removed) {
            MasterServer.warn("trying to remove player from server " + this + " when not in it " + session);
        }
        return removed;
    }

    @Override
    public void crash() {
        for (Session session : this.players.values()) {
            MasterServer.getInstance().getSessionManager().runOnSessionThread(session.getPlayerId(), (p) -> {
                if (session.getServer() == this) {
                    session.setServer(null);
                    session.setConnected(false);
                }
            });
        }
        this.players.clear(); // clear if some task tries to access them? should not need
    }

    @Override
    public String toString() {
        return super.toString() + ", host=" + this.host + ", port=" + this.port + ", players=" + this.getPlayerCount();
    }
}
