package git.aatufutaa.lobby.master.tutorial;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.net.packet.hello.SendToServerOutgoingPacket;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.ServerType;
import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.communication.packet.MasterIncomingPacket;
import io.netty.buffer.ByteBuf;

public class StartTutorialMasterIncomingPacket implements MasterIncomingPacket {

    public enum TutorialResponse {
        OK,
        CANT_FIND_SERVER,
        FAIL
    }

    private TutorialResponse response;
    private int playerId;

    private String host;
    private int port;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.response = TutorialResponse.values()[buf.readByte()];
        this.playerId = buf.readInt();

        if (this.response == TutorialResponse.OK) {
            this.host = ByteBufUtil.readString(buf, 20);
            this.port = buf.readInt();
        }
    }

    @Override
    public void handle() {
        LobbyServer.getInstance().runOnMainThread(() -> {
            Session session = LobbyServer.getInstance().getSessionManager().getSession(this.playerId);

            if (session == null) {
                LobbyServer.warn("cant find session on start tutorial for " + this.playerId);
                return;
            }

            if (this.response != TutorialResponse.OK) {
                LobbyServer.warn("Master failed to start tutorial for " + this.playerId);
                session.kick("Failed to start tutorial. Sorry!");
                return;
            }

            LobbyServer.log("Starting tutorial for " + this.playerId);
            LobbyServer.log("game server-> " + this.host);
            LobbyServer.log("port->" + this.port);

            session.sendPacketAndClose(new SendToServerOutgoingPacket(ServerType.GAME, this.host, this.port));
        });
    }
}
