package git.aatufutaa.lobby.master.session;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.communication.packet.MasterIncomingPacket;
import io.netty.buffer.ByteBuf;

public class DestroySessionIncomingPacket implements MasterIncomingPacket {

    private int playerId;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = buf.readInt();
    }

    @Override
    public void handle() {
        LobbyServer.log("Destroy received " + this.playerId);
        LobbyServer.getInstance().runOnMainThread(() -> {
            Session session = LobbyServer.getInstance().getSessionManager().removeSession(this.playerId);
            if (session != null) {
                LobbyServer.log("session " + session + " destroyed by master");
                session.kick("session destroyed");
            }
        });
    }
}
