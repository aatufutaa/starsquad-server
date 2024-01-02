package git.aatufutaa.server.play.confirm;

import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ConfirmPingOutgoingPacket implements OutgoingPacket {

    private final int latestAcceptedId;

    @Override
    public void write(ByteBuf buf) {
        buf.writeIntLE(this.latestAcceptedId);
    }
}
