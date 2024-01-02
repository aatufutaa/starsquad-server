package git.aatufutaa.server.play.confirm;

import git.aatufutaa.server.Server;
import git.aatufutaa.server.net.client.ClientBase;
import git.aatufutaa.server.net.packet.IncomingPacket;
import git.aatufutaa.server.play.PlayServer;
import git.aatufutaa.server.play.session.SessionBase;
import io.netty.buffer.ByteBuf;

public class ConfirmPingIncomingPacket implements IncomingPacket {

    private int latestAcceptedId;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.latestAcceptedId = buf.readIntLE();
    }

    @Override
    public void handle(ClientBase client) throws Exception {
        PlayServer.getServer().runOnMainThread(() -> {
            if (!client.isDataLoaded()) return;

            SessionBase session = PlayServer.getServer().getSessionManager().getSession(client.getPlayerId());
            if (session == null) {
                Server.warn("cant find session in ping for " + client.getPlayerId());
                return;
            }

            session.getPacketConfirmManager().onClientConfirm(this.latestAcceptedId);
        });
    }
}
