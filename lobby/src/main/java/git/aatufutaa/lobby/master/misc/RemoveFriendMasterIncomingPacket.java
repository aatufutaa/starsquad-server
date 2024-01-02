package git.aatufutaa.lobby.master.misc;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.mongo.PlayerId;
import git.aatufutaa.lobby.net.confirm.packet.friend.RemoveFriendOutgoingPacket;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.communication.packet.MasterIncomingPacket;
import io.netty.buffer.ByteBuf;

public class RemoveFriendMasterIncomingPacket implements MasterIncomingPacket {

    private int playerId;
    private int friendId;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = buf.readInt();
        this.friendId = buf.readInt();
    }

    @Override
    public void handle() {
        LobbyServer.getInstance().runOnMainThread(() -> {
            Session session = LobbyServer.getInstance().getSessionManager().getSession(this.playerId);

            if (session == null) return;

            if (session.getLobbyData().getFriends().remove(this.friendId) != null)
                session.sendConfirmPacket(new RemoveFriendOutgoingPacket(PlayerId.convertIdToHash(this.friendId)));
        });
    }
}
