package git.aatufutaa.lobby.master.game;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.net.packet.misc.AlertOutgoingPacket;
import git.aatufutaa.lobby.net.packet.hello.SendToServerOutgoingPacket;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.ServerType;
import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.communication.packet.MasterIncomingPacket;
import io.netty.buffer.ByteBuf;

public class SendToGameMasterIncomingPacket implements MasterIncomingPacket {

    public enum SendType {
        OK,
        CANT_FIND_SERVER,
        FAILED
    }

    private SendType sendType;
    private int playerId;
    private String host;
    private int port;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.sendType = SendType.values()[buf.readByte()];
        this.playerId = buf.readInt();
        if (this.sendType == SendType.OK) {
            this.host = ByteBufUtil.readString(buf, 20);
            this.port = buf.readInt();
        }
    }

    @Override
    public void handle() {
        LobbyServer.getInstance().runOnMainThread(() -> {
            Session session = LobbyServer.getInstance().getSessionManager().getSession(this.playerId);

            if (session == null) {
                LobbyServer.warn("cant find session on start game for " + this.playerId);
                return;
            }

            if (this.sendType != SendType.OK) {
                LobbyServer.warn("Master failed to start game for " + this.playerId);
                session.sendPacketSafe(new AlertOutgoingPacket(AlertOutgoingPacket.AlertType.FAILED_TO_START_GAME));
                return;
            }

            session.sendPacketAndClose(new SendToServerOutgoingPacket(ServerType.GAME, this.host, this.port));
        });
    }
}
