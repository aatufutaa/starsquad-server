package git.aatufutaa.game.net.packet.hello;

import git.aatufutaa.game.GameServer;
import git.aatufutaa.game.net.cllient.GameClient;
import git.aatufutaa.game.net.cllient.SessionState;
import git.aatufutaa.game.session.Session;
import git.aatufutaa.game.session.SessionManager;
import git.aatufutaa.server.net.client.ClientBase;
import git.aatufutaa.server.net.packet.IncomingPacket;
import io.netty.buffer.ByteBuf;

public class DataLoadedIncomingPacket implements IncomingPacket {

    private boolean send;
    private int latestAcceptedId;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.send = buf.readBoolean();
        if (this.send)
            this.latestAcceptedId = buf.readIntLE();
    }

    @Override
    public void handle(ClientBase client) throws Exception {
        GameServer.getInstance().runOnMainThread(() -> {
            GameClient gameClient = (GameClient) client;
            if (gameClient.assertState(SessionState.STAGE)) return;

            Session session = GameServer.getInstance().getSessionManager().getSession(client.getPlayerId());

            if (session == null) {
                GameServer.warn("data loaded did not find session for " + client);
                client.getChannel().close();
                return;
            }

            if (this.send) {
                if (gameClient.isSentStage0()) {
                    // cant do this
                    client.getChannel().close();
                    return;
                }

                if (!session.isAllowReconnect()) {
                    GameServer.warn("client was disconnected for too long dont allow reconnect " + client);
                    client.getChannel().close();
                    return;
                }

                session.setSendConfirmPackets(true);
                session.setWaitingLatestAcceptedId(this.latestAcceptedId);
            } else {
                session.setSendConfirmPackets(false);
            }

            // generate udp id
            ((SessionManager) GameServer.getInstance().getSessionManager()).registerUdpId(session);

            client.sendPacket(new StartUdpOutgoingPacket(session.getUdpId()));

            gameClient.setState(SessionState.UDP_READY);
        });
    }
}
