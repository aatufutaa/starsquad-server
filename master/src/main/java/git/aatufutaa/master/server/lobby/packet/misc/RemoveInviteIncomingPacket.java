package git.aatufutaa.master.server.lobby.packet.misc;

import git.aatufutaa.master.MasterServer;
import git.aatufutaa.master.communication.packet.IncomingPacket;
import git.aatufutaa.master.server.lobby.LobbyServer;
import io.netty.buffer.ByteBuf;

public class RemoveInviteIncomingPacket implements IncomingPacket<LobbyServer> {

    private int playerId;
    private int friendId;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = buf.readInt();
        this.friendId = buf.readInt();
    }

    @Override
    public void handle(LobbyServer server) {
        MasterServer.getInstance().getSessionManager().getSession(this.playerId, session -> {
            if (session == null) return;

            session.sendPacket(new RemoveInviteOutgoingPacket(this.playerId, this.friendId));
        });
    }
}
