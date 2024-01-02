package git.aatufutaa.game.net.packet.hello;

import git.aatufutaa.game.GameServer;
import git.aatufutaa.game.game.Game;
import git.aatufutaa.game.net.cllient.GameClient;
import git.aatufutaa.game.session.Session;
import git.aatufutaa.game.net.cllient.SessionState;
import git.aatufutaa.server.net.client.ClientBase;
import git.aatufutaa.server.net.packet.IncomingPacket;
import git.aatufutaa.server.play.confirm.FlushConfirmOutgoingPacket;
import io.netty.buffer.ByteBuf;

public class UdpReadyIncomingPacket implements IncomingPacket {

    public enum UdpStatus {
        OK,
        FALLBACK_TO_TCP,
        READY_TO_ACCEPT_PACKETS
    }

    private UdpStatus status;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.status = UdpStatus.values()[buf.readByte()];
    }

    @Override
    public void handle(ClientBase client) throws Exception {
        GameServer.getInstance().runOnMainThread(() -> {
            GameClient gameClient = (GameClient) client;

            if (this.status == UdpStatus.OK || this.status == UdpStatus.FALLBACK_TO_TCP) {
                if (gameClient.assertState(SessionState.UDP_READY)) return;

                Session session = GameServer.getInstance().getSessionManager().getSession(client.getPlayerId());

                if (session == null) {
                    GameServer.warn("cant find session in udp read for " + client);
                    return;
                }

                // if reconnect flush queue
                if (session.isSendConfirmPackets()) {
                    session.setSendConfirmPackets(false);
                    session.getPacketConfirmManager().onConnected(client, session.getWaitingLatestAcceptedId());
                    client.sendPacket(new FlushConfirmOutgoingPacket(true, session.getPacketConfirmManager().getLatestAcceptedId()));
                } else {
                    client.sendPacket(new FlushConfirmOutgoingPacket(false, 0));
                }

                // udp ready -> send data
                Game game = session.getGame();
                if (game == null) {
                    GameServer.warn("cant find game in udp ready for " + session);
                    client.getChannel().close();
                    return;
                }

                game.sendDynamicData(session);

                if (this.status == UdpStatus.FALLBACK_TO_TCP) {
                    GameServer.warn("cant start udp with " + session);
                    GameServer.warn("fall back to tcp->");
                    gameClient.setFallbackToTcp(true);
                    gameClient.setState(SessionState.DONE);
                    gameClient.setDataLoaded(true);
                } else {
                    gameClient.setState(SessionState.UDP_READY_SEND_PACKET);
                }

                return;
            }

            // client receive dynamic data, now start sending udp
            if (gameClient.assertState(SessionState.UDP_READY_SEND_PACKET)) return;

            Session session = GameServer.getInstance().getSessionManager().getSession(client.getPlayerId());

            if (session == null) {
                GameServer.warn("cant find session in udp read for " + client);
                return;
            }

            gameClient.setState(SessionState.DONE);
            gameClient.setDataLoaded(true);
        });
    }
}
