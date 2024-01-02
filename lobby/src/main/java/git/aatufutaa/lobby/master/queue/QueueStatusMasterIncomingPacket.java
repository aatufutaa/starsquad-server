package git.aatufutaa.lobby.master.queue;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.net.packet.queue.QueueStatusOutgoingPacket;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.communication.packet.MasterIncomingPacket;
import io.netty.buffer.ByteBuf;

public class QueueStatusMasterIncomingPacket implements MasterIncomingPacket {

    private int playerId;

    private int players;
    private int maxPlayers;
    private boolean party;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = buf.readInt();

        this.players = buf.readByte();
        this.maxPlayers = buf.readByte();
        this.party = buf.readBoolean();
    }

    @Override
    public void handle() {

        LobbyServer.getInstance().runOnMainThread(() -> {

            Session session = LobbyServer.getInstance().getSessionManager().getSession(this.playerId);

            if (session == null) {
                LobbyServer.warn("cant find session status " + this.playerId);
                return;
            }

            session.setInQueue(true);

            session.sendPacketSafe(new QueueStatusOutgoingPacket(this.players, this.maxPlayers, this.party));
        });
    }
}
