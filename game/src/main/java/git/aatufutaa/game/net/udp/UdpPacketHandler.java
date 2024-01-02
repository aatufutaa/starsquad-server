package git.aatufutaa.game.net.udp;

import git.aatufutaa.game.GameServer;
import git.aatufutaa.game.game.Game;
import git.aatufutaa.game.game.entity.EntityData;
import git.aatufutaa.game.net.cllient.GameClient;
import git.aatufutaa.game.session.Session;
import git.aatufutaa.game.session.SessionManager;
import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.AllArgsConstructor;

import java.net.InetSocketAddress;
import java.util.List;

public class UdpPacketHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @AllArgsConstructor
    public static class MoveOutgoingPacket implements OutgoingPacket {

        private short id;
        private int serverTick;
        private List<EntityData> entities;

        @Override
        public void write(ByteBuf buf) {
            buf.writeShortLE(this.id);
            buf.writeShortLE(this.serverTick);
            buf.writeByte(this.entities.size());
            for (EntityData entityData : this.entities) {
                entityData.write(buf);
            }
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        try {

            InetSocketAddress sender = msg.sender();
            ByteBuf buf = msg.content();

            short udpId = buf.readShortLE();
            boolean isPingPacket = buf.readableBytes() == 0;

            Game.PlayerInput packet;

            if (!isPingPacket) {
                packet = new Game.PlayerInput(false);
                packet.read(buf);
            } else {
                packet = null;
            }

            GameServer.getInstance().runOnMainThread(() -> {
                Session session = ((SessionManager) GameServer.getInstance().getSessionManager()).getUdpSession(udpId);

                if (session == null) {
                    GameServer.warn("cant find udp session for " + udpId);
                    return;
                }

                GameClient client = (GameClient) session.getClient();
                if (client == null || !client.isConnected()) return;

                Game game = session.getGame();

                if (game == null) {
                    GameServer.warn("cant find game udp for " + session);
                    return;
                }

                if (isPingPacket) {
                    GameServer.log("sending udp response");
                    client.setUdpAddress(sender);
                    // send ping response
                    GameServer.getInstance().getUdpNetworkManager().sendPacket(buf1 -> buf1.writeShortLE(udpId), sender);
                } else {
                    if (!client.canSendUdp()) return;

                    int clientTick = packet.getClientTick();

                    // udp packet out of order
                    // -> only accept latest one
                    if (clientTick < client.getNextUdpTick()) {
                        GameServer.warn("expired udp packet " + clientTick + " " + client.getNextUdpTick());
                        return;
                    }

                    client.setNextUdpTick(clientTick + 1);

                    client.setUdpAddress(sender);

                    game.handleInput(session, packet);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
