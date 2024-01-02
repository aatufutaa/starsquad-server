package git.aatufutaa.lobby.net.packet.hello;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.net.LobbyClient;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.lobby.session.SessionState;
import git.aatufutaa.server.net.client.ClientBase;
import git.aatufutaa.server.net.packet.IncomingPacket;
import git.aatufutaa.server.play.confirm.FlushConfirmOutgoingPacket;
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

        LobbyServer.getInstance().runOnMainThread(() -> {

            Session session = LobbyServer.getInstance().getSessionManager().getSession(client.getPlayerId());

            if (session == null) {
                LobbyServer.warn("cant find session in data loaded for " + client);
                client.getChannel().close();
                return;
            }

            if (session.getSessionState() != SessionState.LOADED) {
                // cant do this disconnect
                client.getChannel().close();
                return;
            }

            // if reconnect
            if (this.send) {
                if (((LobbyClient) client).isSentStage0()) {
                    // cant do this disconnect
                    client.getChannel().close();
                    return;
                }
            }

            session.getPacketConfirmManager().onConnected(client, this.latestAcceptedId); // send confirm packet
            client.sendPacket(new FlushConfirmOutgoingPacket(true, session.getPacketConfirmManager().getLatestAcceptedId())); // tell client to send all confirm packets

            client.setDataLoaded(true);
        });
    }
}
