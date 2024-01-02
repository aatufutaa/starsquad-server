package git.aatufutaa.master.server.lobby.packet.misc;

import git.aatufutaa.master.MasterServer;
import git.aatufutaa.master.communication.packet.IncomingPacket;
import git.aatufutaa.master.server.ServerLocation;
import git.aatufutaa.master.server.lobby.LobbyServer;
import io.netty.buffer.ByteBuf;

public class UpdateLocationIncomingPacket implements IncomingPacket<LobbyServer> {

    private int playerId;
    private int location;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = buf.readInt();
        this.location = buf.readByte();
    }

    @Override
    public void handle(LobbyServer server) {
        ServerLocation location = ServerLocation.values()[this.location];

        MasterServer.getInstance().getSessionManager().getSession(this.playerId, session -> {
            if (session == null) return;

            session.setLocation(location); // TODO: if own lobby for location then kick player
        });
    }
}
