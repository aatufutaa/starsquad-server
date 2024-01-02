package git.aatufutaa.lobby.net.confirm.packet.queue;

import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LeaveQueueOutgoingPacket implements OutgoingPacket {

    private boolean failed;

    @Override
    public void write(ByteBuf buf) {
        buf.writeBoolean(this.failed);
    }
}
