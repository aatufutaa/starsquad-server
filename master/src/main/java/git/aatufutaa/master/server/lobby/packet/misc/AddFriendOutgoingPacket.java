package git.aatufutaa.master.server.lobby.packet.misc;

import git.aatufutaa.master.communication.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AddFriendOutgoingPacket implements OutgoingPacket {

    private final int playerId;
    private final int friendId;
    private final UpdateFriendStatusOutgoingPacket.FriendStatus status;

    @Override
    public void write(ByteBuf buf) throws Exception {
        buf.writeInt(this.playerId);
        buf.writeInt(this.friendId);
        buf.writeByte(this.status.ordinal());
    }
}
