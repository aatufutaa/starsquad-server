package git.aatufutaa.lobby.master.session;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.communication.packet.MasterIncomingPacket;
import io.netty.buffer.ByteBuf;

public class UpdateSessionMasterIncomingPacket implements MasterIncomingPacket {

    private int playerId;
    private byte[] key;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = buf.readInt();
        this.key = new byte[16];
        buf.readBytes(this.key);
    }

    @Override
    public void handle() {
        // update key when player relogin with new key (first kick old player then update key)
        Session session = LobbyServer.getInstance().getSessionManager().getSession(this.playerId);

        if (session == null) {
            LobbyServer.warn("attempt to update non existent session " + this.playerId);
            return;
        }

        session.kick(null); // kick old player
        session.update(this.key);
    }
}
