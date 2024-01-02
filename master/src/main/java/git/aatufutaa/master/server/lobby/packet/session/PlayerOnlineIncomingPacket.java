package git.aatufutaa.master.server.lobby.packet.session;

import git.aatufutaa.master.MasterServer;
import git.aatufutaa.master.communication.packet.IncomingPacket;
import git.aatufutaa.master.server.lobby.LobbyServer;
import io.netty.buffer.ByteBuf;

public class PlayerOnlineIncomingPacket implements IncomingPacket<LobbyServer> {

    private int playerId;
    private boolean connected;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = buf.readInt();
        this.connected = buf.readBoolean();
    }

    @Override
    public void handle(LobbyServer server) {
        MasterServer.getInstance().getSessionManager().setConnected(this.playerId, this.connected);
    }
}
