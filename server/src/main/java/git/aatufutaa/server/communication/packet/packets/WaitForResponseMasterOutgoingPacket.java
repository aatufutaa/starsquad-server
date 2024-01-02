package git.aatufutaa.server.communication.packet.packets;

import git.aatufutaa.server.communication.packet.MasterOutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.Setter;

public class WaitForResponseMasterOutgoingPacket implements MasterOutgoingPacket {

    @Setter
    private int responseId;

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(this.responseId);
    }
}
