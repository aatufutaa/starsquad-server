package git.aatufutaa.server.play.confirm;

import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ConfirmOutgoingPacket implements OutgoingPacket {

    private final int id;
    private final OutgoingPacket packet;

    @Override
    public void write(ByteBuf buf) {
        buf.writeIntLE(this.id);

        int packetId = ConfirmPacketRegistry.getId(this.packet.getClass());
        buf.writeByte(packetId);
        this.packet.write(buf);
    }
}
