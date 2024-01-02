package git.aatufutaa.lobby.net.confirm.packet.friend;

import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RemoveFriendOutgoingPacket implements OutgoingPacket {

    private final String friendId;

    @Override
    public void write(ByteBuf buf) {
        ByteBufUtil.writeString(this.friendId, buf);
    }
}
