package git.aatufutaa.lobby.master.misc;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.mongo.PlayerId;
import git.aatufutaa.lobby.net.confirm.packet.friend.RemoveFriendInviteOutgoingPacket;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.communication.packet.MasterIncomingPacket;
import io.netty.buffer.ByteBuf;

public class RemoveInviteMasterIncomingPacket implements MasterIncomingPacket {

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

            String playerIdHash = PlayerId.convertIdToHash(this.friendId);

            if (session.getLobbyData().getIncomingInvites().containsKey(this.friendId)) {
                session.getLobbyData().getIncomingInvites().remove(this.friendId);
                session.sendConfirmPacket(new RemoveFriendInviteOutgoingPacket(playerIdHash, true));
            }
            if (session.getLobbyData().getOutgoingInvites().containsKey(this.friendId)) {
                session.getLobbyData().getOutgoingInvites().remove(this.friendId);
                session.sendConfirmPacket(new RemoveFriendInviteOutgoingPacket(playerIdHash, false));
            }
        });
    }
}
