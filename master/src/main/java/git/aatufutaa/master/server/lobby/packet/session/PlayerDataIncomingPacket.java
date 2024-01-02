package git.aatufutaa.master.server.lobby.packet.session;

import git.aatufutaa.master.MasterServer;
import git.aatufutaa.master.communication.packet.IncomingPacket;
import git.aatufutaa.master.server.lobby.LobbyServer;
import git.aatufutaa.master.server.lobby.packet.misc.UpdateFriendStatusOutgoingPacket;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public class PlayerDataIncomingPacket implements IncomingPacket<LobbyServer> {

    private int playerId;

    private List<Integer> friends;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = buf.readInt();

        int count = buf.readByte();
        this.friends = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            this.friends.add(buf.readInt());
        }
    }

    @Override
    public void handle(LobbyServer server) {
        System.out.println("finding friends for " + this.playerId);

        MasterServer.getInstance().getSessionManager().getSession(this.playerId, (session) -> {
            if (session == null) return;

            session.setFriendsRequested(true);

            for (int friendId : this.friends) {
                System.out.println("finding " + friendId);

                MasterServer.getInstance().getSessionManager().getSession(friendId, friend -> {
                    if (friend == null) return;

                    System.out.println("found " + friendId);

                    friend.getOnlineFriends().put(session.getPlayerId(), session);
                    friend.sendIfLobby(new UpdateFriendStatusOutgoingPacket(friend.getPlayerId(), session.getPlayerId(), UpdateFriendStatusOutgoingPacket.FriendStatus.LOBBY));

                    MasterServer.getInstance().getSessionManager().runOnSessionThread(session.getPlayerId(), (callback) -> {
                        session.getOnlineFriends().put(friend.getPlayerId(), friend);
                        session.sendIfLobby(new UpdateFriendStatusOutgoingPacket(session.getPlayerId(), friend.getPlayerId(), friend.getStatus()));
                    });
                });
            }
        });
    }
}
