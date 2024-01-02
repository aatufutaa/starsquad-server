package git.aatufutaa.lobby.net.confirm.packet.quests;

import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class QuestOutgoingPacket implements OutgoingPacket {

    private final int id;
    private final int amount;
    private final int step;

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(this.id);
        buf.writeShortLE(this.amount);
        buf.writeByte(this.step);
    }
}
