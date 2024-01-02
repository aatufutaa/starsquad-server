package git.aatufutaa.lobby.net.confirm.packet.misc;

import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ClaimProgressionOutgoingPacket implements OutgoingPacket {

    private final int id;

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(this.id);
    }
}
