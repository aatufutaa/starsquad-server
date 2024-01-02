package git.aatufutaa.lobby.party.player;

import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class JoinPartyOutgoingPacket implements OutgoingPacket {

    public enum PartyJoinResponse {
        OK,
        PARTY_NOT_FOUND,
        PARTY_FULL,
        NO_INVITE,
        FAILED
    }

    private final PartyJoinResponse response;
    private final String partyCode;
    private final String leaderId;
    private final List<PlayerJoinPartyOutgoingPacket.PartyMember> members;

    public JoinPartyOutgoingPacket(PartyJoinResponse response) {
        this(response, null, null, null);
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(this.response.ordinal());
        if (this.response == PartyJoinResponse.OK) {
            ByteBufUtil.writeString(this.partyCode, buf);
            ByteBufUtil.writeString(this.leaderId, buf);
            buf.writeByte(this.members.size());
            for (PlayerJoinPartyOutgoingPacket.PartyMember member : this.members) {
                member.write(buf);
            }
        }
    }
}
