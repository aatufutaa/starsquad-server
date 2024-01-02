package git.aatufutaa.lobby.net.confirm.packet.friend;

import git.aatufutaa.lobby.master.misc.AddFriendMasterIncomingPacket;
import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AddFriendInviteOutgoingPacket implements OutgoingPacket {

    private final AddFriendMasterIncomingPacket.Friend friend;
    private final boolean incoming;

    @Override
    public void write(ByteBuf buf) {
        this.friend.write(buf);
        buf.writeBoolean(this.incoming);
    }
}
