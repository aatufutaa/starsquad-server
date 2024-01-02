package git.aatufutaa.lobby.net.confirm.packet.misc;

import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RemoveCoinsOutgoingPacket implements OutgoingPacket {

    private final RemoveType type;
    private final int amount;

    public enum RemoveType {
        COINS,
        GEMS,
        EXP_COMMON,
        EXP_RARE,
        EXP_LEGENDARY,
        LEVEL
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(this.type.ordinal());
        buf.writeIntLE(this.amount);
    }
}
