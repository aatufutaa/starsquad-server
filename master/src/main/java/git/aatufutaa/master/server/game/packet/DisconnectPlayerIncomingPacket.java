package git.aatufutaa.master.server.game.packet;

import git.aatufutaa.master.MasterServer;
import git.aatufutaa.master.communication.packet.IncomingPacket;
import git.aatufutaa.master.server.game.GameServer;
import io.netty.buffer.ByteBuf;

public class DisconnectPlayerIncomingPacket implements IncomingPacket<GameServer> {

    private int playerId;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = buf.readInt();
    }

    @Override
    public void handle(GameServer server) {
        MasterServer.getInstance().getSessionManager().tryToDisconnect(this.playerId, server);
    }
}
