package git.aatufutaa.lobby.master.session;

import git.aatufutaa.server.communication.packet.MasterOutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PlayerOnlineMasterOutgoingPacket implements MasterOutgoingPacket {

    private int playerId;
    private boolean connected;

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeBoolean(this.connected);
    }
}
