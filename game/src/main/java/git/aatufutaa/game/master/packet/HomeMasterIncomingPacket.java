package git.aatufutaa.game.master.packet;

import git.aatufutaa.game.GameServer;
import git.aatufutaa.game.net.packet.hello.SendToServerOutgoingPacket;
import git.aatufutaa.game.session.Session;
import git.aatufutaa.server.ServerType;
import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.communication.packet.MasterIncomingPacket;
import git.aatufutaa.server.net.client.ClientBase;
import io.netty.buffer.ByteBuf;

public class HomeMasterIncomingPacket implements MasterIncomingPacket {

    private int playerId;
    private String host;
    private int port;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = buf.readInt();
        this.host = ByteBufUtil.readString(buf, 20);
        this.port = buf.readInt();
    }

    @Override
    public void handle() {
        GameServer.getInstance().runOnMainThread(() -> {
            Session session = GameServer.getInstance().getSessionManager().getSession(this.playerId);

            if (session == null) {
                GameServer.warn("failed to send home session not exist " + this.playerId);
                return;
            }

            ClientBase client = session.getClient();
            if (client != null)
                client.sendPacket(new SendToServerOutgoingPacket(ServerType.LOBBY, this.host, this.port));
        });
    }
}
