package git.aatufutaa.lobby.master.queue;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.net.confirm.packet.queue.LeaveQueueOutgoingPacket;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.communication.packet.MasterIncomingPacket;
import io.netty.buffer.ByteBuf;

public class LeaveQueueMasterIncomingPacket implements MasterIncomingPacket {

    private int playerId;
    private boolean failed;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = buf.readInt();
        this.failed = buf.readBoolean();
    }

    @Override
    public void handle() {
        LobbyServer.getInstance().runOnMainThread(() -> {

            Session session = LobbyServer.getInstance().getSessionManager().getSession(this.playerId);

            if (session == null) {
                LobbyServer.warn("cant find session in leave queue " + this.playerId);
                return;
            }

            if (this.failed) {
                LobbyServer.warn("leave queue failed for " + session);
            } else {
                session.setInQueue(false);
            }
            session.sendConfirmPacket(new LeaveQueueOutgoingPacket(this.failed));
        });
    }
}
