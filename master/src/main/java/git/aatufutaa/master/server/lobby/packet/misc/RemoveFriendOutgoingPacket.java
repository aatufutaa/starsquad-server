package git.aatufutaa.master.server.lobby.packet.misc;

import git.aatufutaa.master.communication.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RemoveFriendOutgoingPacket implements OutgoingPacket {

    private final int playerId;
    private final int friendId;

    @Override
    public void write(ByteBuf buf) throws Exception {
        buf.writeInt(this.playerId);
        buf.writeInt(this.friendId);
    }
}
