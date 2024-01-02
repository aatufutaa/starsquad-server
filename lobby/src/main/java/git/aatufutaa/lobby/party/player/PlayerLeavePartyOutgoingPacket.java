package git.aatufutaa.lobby.party.player;

import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PlayerLeavePartyOutgoingPacket implements OutgoingPacket {

    private final String playerId;

    @Override
    public void write(ByteBuf buf) {
        ByteBufUtil.writeString(this.playerId, buf);
    }
}
