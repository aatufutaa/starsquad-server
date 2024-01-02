package git.aatufutaa.master.server.login.packet;

import git.aatufutaa.master.communication.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RequestOutgoingPacket implements OutgoingPacket {

    private int requestId;

    @Override
    public void write(ByteBuf buf) throws Exception {
        buf.writeInt(this.requestId);
    }
}
