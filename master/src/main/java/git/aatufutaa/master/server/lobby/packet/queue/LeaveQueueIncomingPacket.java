package git.aatufutaa.master.server.lobby.packet.queue;

import git.aatufutaa.master.MasterServer;
import git.aatufutaa.master.communication.packet.IncomingPacket;
import git.aatufutaa.master.queue.QueueData;
import git.aatufutaa.master.server.lobby.LobbyServer;
import io.netty.buffer.ByteBuf;

public class LeaveQueueIncomingPacket implements IncomingPacket<LobbyServer> {

    private int playerId;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = buf.readInt();
    }

    @Override
    public void handle(LobbyServer server) {
        MasterServer.getInstance().getSessionManager().getSession(this.playerId, (session) -> {

            if (session == null) {
                MasterServer.warn("cant find session when leave queue for " + this.playerId);
                return;
            }

            QueueData queueData = session.getQueueData();

            if (queueData == null) {
                MasterServer.warn(session + " tried to leave queue while not in queue");
                server.sendPacket(new LeaveQueueOutgoingPacket(this.playerId, true));
                return;
            }

            queueData.leave(session);
        });
    }
}
