package git.aatufutaa.lobby.net.packet.hello;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.master.session.PlayerOnlineMasterOutgoingPacket;
import git.aatufutaa.lobby.net.LobbyClient;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.net.client.ClientBase;
import git.aatufutaa.server.net.packet.IncomingPacket;
import io.netty.buffer.ByteBuf;

public class HelloIncomingPacket implements IncomingPacket {

    private String secret;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.secret = ByteBufUtil.readString(buf, 32);
    }

    @Override
    public void handle(ClientBase client) throws Exception {
        LobbyServer.getInstance().runOnMainThread(() -> {

            Session session = LobbyServer.getInstance().getSessionManager().getSessionBySecret(this.secret);
            if (session == null) {
                LobbyServer.warn("cant find session for " + this.secret + " in login");
                client.sendPacketAndClose(new HelloOutgoingPacket(HelloOutgoingPacket.HelloResponse.FAIL));
                return;
            }

            boolean connected = session.isConnected();
            if (connected) {
                session.kick(null); // should already be kicked
            } else {
                LobbyServer.getInstance().getMasterConnection().sendPacket(new PlayerOnlineMasterOutgoingPacket(session.getPlayerId(), true));
            }

            client.confirmSession(session.getPlayerId());
            session.onConnect((LobbyClient) client);

            client.sendPacket(new HelloOutgoingPacket(HelloOutgoingPacket.HelloResponse.OK),
                    f -> client.enableEncryption(session.getKey())
            );
        });
    }
}
