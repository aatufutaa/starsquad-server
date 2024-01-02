package git.aatufutaa.master.server.lobby.packet.session;

import git.aatufutaa.master.communication.packet.IncomingPacket;
import git.aatufutaa.master.server.lobby.LobbyServer;
import io.netty.buffer.ByteBuf;

public class CreateSessionIncomingPacket implements IncomingPacket<LobbyServer> {

    private int playerId;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = buf.readInt();
    }

    @Override
    public void handle(LobbyServer server) {
    }
}
