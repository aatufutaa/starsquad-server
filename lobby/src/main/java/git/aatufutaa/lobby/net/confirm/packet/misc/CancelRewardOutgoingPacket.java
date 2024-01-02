package git.aatufutaa.lobby.net.confirm.packet.misc;

import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CancelRewardOutgoingPacket implements OutgoingPacket {

    private int id;

    @Override
    public void write(ByteBuf buf) {
        buf.writeShortLE(this.id);
    }
}
