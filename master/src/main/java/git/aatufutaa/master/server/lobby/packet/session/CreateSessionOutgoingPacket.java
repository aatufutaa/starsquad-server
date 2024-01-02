package git.aatufutaa.master.server.lobby.packet.session;

import git.aatufutaa.master.communication.packet.OutgoingPacket;
import git.aatufutaa.master.server.lobby.LobbyServer;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CreateSessionOutgoingPacket implements OutgoingPacket {

    private LobbyServer.SessionData sessionData;

    @Override
    public void write(ByteBuf buf) throws Exception {
        this.sessionData.write(buf);
    }
}
