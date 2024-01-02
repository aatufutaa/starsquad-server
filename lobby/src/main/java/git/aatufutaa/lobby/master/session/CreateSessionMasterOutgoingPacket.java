package git.aatufutaa.lobby.master.session;

import git.aatufutaa.server.communication.packet.MasterOutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CreateSessionMasterOutgoingPacket implements MasterOutgoingPacket {

    private int playerId;

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(this.playerId);
    }
}
