package git.aatufutaa.master.server.lobby.packet.queue;

import git.aatufutaa.master.MasterServer;
import git.aatufutaa.master.communication.packet.IncomingPacket;
import git.aatufutaa.master.queue.QueueType;
import git.aatufutaa.master.server.lobby.LobbyServer;
import io.netty.buffer.ByteBuf;

public class JoinQueueIncomingPacket implements IncomingPacket<LobbyServer> {

    private int playerId;
    private QueueType queueType;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = buf.readInt();
        this.queueType = QueueType.values()[buf.readByte()];
    }

    @Override
    public void handle(LobbyServer server) {

        MasterServer.getInstance().getSessionManager().getSession(this.playerId, (session) -> {

            MasterServer.getInstance().getQueueManager().add(session, this.queueType);

        });
    }
}
