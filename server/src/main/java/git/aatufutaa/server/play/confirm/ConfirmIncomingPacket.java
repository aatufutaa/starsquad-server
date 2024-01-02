package git.aatufutaa.server.play.confirm;

import git.aatufutaa.server.Server;
import git.aatufutaa.server.net.client.ClientBase;
import git.aatufutaa.server.net.packet.IncomingPacket;
import git.aatufutaa.server.play.PlayServer;
import git.aatufutaa.server.play.session.SessionBase;
import io.netty.buffer.ByteBuf;

public class ConfirmIncomingPacket implements IncomingPacket {

    private int id;
    private IncomingPacket packet;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.id = buf.readIntLE();

        int packetId = buf.readByte();
        Class<? extends IncomingPacket> clazz = ConfirmPacketRegistry.getClazz(packetId);

        this.packet = clazz.getConstructor().newInstance();
        this.packet.read(buf);
    }

    @Override
    public void handle(ClientBase client) throws Exception {
        PlayServer.getServer().runOnMainThread(() -> {
            if (!client.isDataLoaded()) return;

            SessionBase session = PlayServer.getServer().getSessionManager().getSession(client.getPlayerId());
            if (session == null) {
                Server.warn("cant find session in confirm for " + client.getPlayerId());
                return;
            }

            try {
                session.getPacketConfirmManager().handle(client, this.id, this.packet);
            } catch (Exception e) {
                e.printStackTrace();
                client.getChannel().close();
            }
        });
    }
}
