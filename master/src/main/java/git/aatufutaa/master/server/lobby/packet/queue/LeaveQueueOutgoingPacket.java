package git.aatufutaa.master.server.lobby.packet.queue;

import git.aatufutaa.master.communication.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LeaveQueueOutgoingPacket implements OutgoingPacket {

    private final int playerId;
    private final boolean failed;

    @Override
    public void write(ByteBuf buf) throws Exception {
        buf.writeInt(this.playerId);
        buf.writeBoolean(this.failed);
    }
}
