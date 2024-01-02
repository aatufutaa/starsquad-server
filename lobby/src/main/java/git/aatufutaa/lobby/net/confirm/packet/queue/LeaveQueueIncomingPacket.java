package git.aatufutaa.lobby.net.confirm.packet.queue;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.master.queue.LeaveQueueMasterOutgoingPacket;
import git.aatufutaa.lobby.net.packet.LobbyPacket;
import git.aatufutaa.lobby.session.Session;
import io.netty.buffer.ByteBuf;

public class LeaveQueueIncomingPacket extends LobbyPacket {

    @Override
    public void read(ByteBuf buf) throws Exception {
    }

    @Override
    protected void handle0(Session session) {
        if (!session.isInQueue()) {
            LobbyServer.warn("player leave queue while not in queue " + session);
            session.sendConfirmPacket(new LeaveQueueOutgoingPacket(true));
            return;
        }

        LobbyServer.getInstance().getMasterConnection().sendPacket(new LeaveQueueMasterOutgoingPacket(session.getPlayerId()));
    }
}
