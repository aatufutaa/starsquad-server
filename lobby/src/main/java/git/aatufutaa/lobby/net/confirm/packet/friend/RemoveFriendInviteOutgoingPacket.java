package git.aatufutaa.lobby.net.confirm.packet.friend;

import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RemoveFriendInviteOutgoingPacket implements OutgoingPacket {

    private final String playerId;
    private final boolean incoming;

    @Override
    public void write(ByteBuf buf) {
        ByteBufUtil.writeString(this.playerId, buf);
        buf.writeBoolean(this.incoming);
    }
}
