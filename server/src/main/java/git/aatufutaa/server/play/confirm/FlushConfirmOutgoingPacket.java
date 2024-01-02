package git.aatufutaa.server.play.confirm;

import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FlushConfirmOutgoingPacket implements OutgoingPacket {

    private boolean send;
    private final int latestAcceptedId;

    @Override
    public void write(ByteBuf buf) {
        buf.writeBoolean(this.send);
        if (this.send)
            buf.writeIntLE(this.latestAcceptedId);
    }
}
