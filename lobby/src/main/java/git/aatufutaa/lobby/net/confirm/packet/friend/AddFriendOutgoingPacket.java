package git.aatufutaa.lobby.net.confirm.packet.friend;

import git.aatufutaa.lobby.master.misc.AddFriendMasterIncomingPacket;
import git.aatufutaa.lobby.master.misc.UpdateFriendStatusMasterIncomingPacket;
import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AddFriendOutgoingPacket implements OutgoingPacket {

    private final AddFriendMasterIncomingPacket.Friend friend;
    private final UpdateFriendStatusMasterIncomingPacket.FriendStatus status;

    @Override
    public void write(ByteBuf buf) {
        this.friend.write(buf);
        buf.writeByte(this.status.ordinal());
    }
}
