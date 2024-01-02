package git.aatufutaa.lobby.net.confirm.packet.misc;

import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RewardOutgoingPacket implements OutgoingPacket {

    private int id;
    private int target;
    private int collector;
    private int amount;

    @Override
    public void write(ByteBuf buf) {
        buf.writeShortLE(this.id);
        buf.writeByte(this.target);
        buf.writeByte(this.collector);
        buf.writeIntLE(this.amount);
    }
}
