package git.aatufutaa.lobby.master.queue;

import git.aatufutaa.server.communication.packet.MasterOutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LeaveQueueMasterOutgoingPacket implements MasterOutgoingPacket {

    private final int playerId;

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(this.playerId);
    }
}
