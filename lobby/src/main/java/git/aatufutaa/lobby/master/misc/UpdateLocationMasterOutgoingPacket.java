package git.aatufutaa.lobby.master.misc;

import git.aatufutaa.server.communication.packet.MasterOutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UpdateLocationMasterOutgoingPacket implements MasterOutgoingPacket {

    private int playerId;
    private int location;

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeByte(this.location);
    }
}
