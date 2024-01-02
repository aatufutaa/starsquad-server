package git.aatufutaa.master.party;

import git.aatufutaa.master.communication.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PlayerJoinPartyOutgoingPacket implements OutgoingPacket {

    private final int playerId;

    private JoinPartyOutgoingPacket.PartyMember partyMember;

    @Override
    public void write(ByteBuf buf) throws Exception {
        buf.writeInt(this.playerId);

        this.partyMember.write(buf);
    }
}
