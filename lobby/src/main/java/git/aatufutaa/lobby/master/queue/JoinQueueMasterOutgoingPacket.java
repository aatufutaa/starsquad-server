package git.aatufutaa.lobby.master.queue;

import git.aatufutaa.lobby.net.confirm.packet.queue.JoinQueueIncomingPacket;
import git.aatufutaa.server.communication.packet.MasterOutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JoinQueueMasterOutgoingPacket implements MasterOutgoingPacket {

    private final int playerId;
    private final JoinQueueIncomingPacket.QueueType queueType;

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeByte(this.queueType.ordinal());
    }
}
