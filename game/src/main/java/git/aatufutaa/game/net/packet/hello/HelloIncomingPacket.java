package git.aatufutaa.game.net.packet.hello;

import git.aatufutaa.game.GameServer;
import git.aatufutaa.game.net.cllient.GameClient;
import git.aatufutaa.game.session.Session;
import git.aatufutaa.game.net.cllient.SessionState;
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
        GameServer.getInstance().runOnMainThread(() -> {

            GameClient gameClient = (GameClient) client;
            if (gameClient.assertState(SessionState.HELLO)) return;

            Session session = GameServer.getInstance().getSessionManager().getSessionBySecret(this.secret);

            if (session == null) {
                GameServer.warn("session doesnt exist for " + this.secret);

                client.sendPacketAndClose(new HelloOutgoingPacket(HelloOutgoingPacket.HelloResponse.FAIL));

                return;
            }

            GameServer.log("found session for " + session);

            session.kick(null);
            session.onConnect(gameClient);

            client.confirmSession(session.getPlayerId());
            client.sendPacket(new HelloOutgoingPacket(HelloOutgoingPacket.HelloResponse.OK), f -> client.enableEncryption(session.getKey()));

            gameClient.setState(SessionState.STAGE);
        });
    }
}
