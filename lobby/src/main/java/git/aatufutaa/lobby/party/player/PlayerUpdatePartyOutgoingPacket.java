package git.aatufutaa.lobby.party.player;

import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PlayerUpdatePartyOutgoingPacket implements OutgoingPacket {

    private PlayerJoinPartyOutgoingPacket.PartyMember partyMember;

    @Override
    public void write(ByteBuf buf) {
        this.partyMember.write(buf);
    }
}
