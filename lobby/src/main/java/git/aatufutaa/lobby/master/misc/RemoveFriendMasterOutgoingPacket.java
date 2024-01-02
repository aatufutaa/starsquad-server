package git.aatufutaa.lobby.master.misc;

import git.aatufutaa.server.communication.packet.MasterOutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RemoveFriendMasterOutgoingPacket implements MasterOutgoingPacket {

    private final int playerId;
    private final int friendId;

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.friendId);
    }
}
