package git.aatufutaa.master.session;

import git.aatufutaa.master.MasterServer;
import git.aatufutaa.master.communication.packet.OutgoingPacket;
import git.aatufutaa.master.party.Party;
import git.aatufutaa.master.queue.QueueData;
import git.aatufutaa.master.server.PlayServer;
import git.aatufutaa.master.server.ServerLocation;
import git.aatufutaa.master.server.game.GameServer;
import git.aatufutaa.master.server.lobby.LobbyServer;
import git.aatufutaa.master.server.lobby.packet.misc.UpdateFriendStatusOutgoingPacket;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class Session {

    @Getter
    private final int playerId;

    @Getter
    private String token;

    @Getter
    @Setter
    private byte[] key;

    @Getter
    @Setter
    private PlayServer server;

    @Getter
    @Setter
    private boolean connected;
    @Getter
    @Setter
    private int timeout;

    @Getter
    @Setter
    private boolean destroyed;

    @Getter
    @Setter
    private QueueData queueData;

    @Getter
    @Setter
    private Party party;

    //@Getter
    //@Setter
    //private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Getter@Setter
    private ServerLocation location;

    @Getter
    private final Map<Integer, Session> onlineFriends = new HashMap<>();

    @Getter@Setter
    private boolean friendsRequested;

    public Session(int playerId, ServerLocation location) {
        this.playerId = playerId;
        this.location = location;
    }

    public void generateToken() {
        this.token = UUID.randomUUID().toString().substring(0, 32);
    }

    public void sendPacket(OutgoingPacket packet) {
        PlayServer server = this.server;
        if (server != null) server.sendPacket(packet);
    }

    public void sendIfLobby(OutgoingPacket packet) {
        PlayServer server = this.server;
        if (server instanceof LobbyServer)
            server.sendPacket(packet);
    }

    public void updateFriendStatus(UpdateFriendStatusOutgoingPacket.FriendStatus status) {
        for (Session friend : this.onlineFriends.values()) {
            MasterServer.getInstance().getSessionManager().runOnSessionThread(friend.getPlayerId(), callback-> {
                friend.sendIfLobby(new UpdateFriendStatusOutgoingPacket(friend.getPlayerId(), this.playerId, status));
            });
        }
    }

    public UpdateFriendStatusOutgoingPacket.FriendStatus getStatus() {
        if (this.server instanceof GameServer) {
            return UpdateFriendStatusOutgoingPacket.FriendStatus.GAME;
        }
        return UpdateFriendStatusOutgoingPacket.FriendStatus.LOBBY;
    }
}
