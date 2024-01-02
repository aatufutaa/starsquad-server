package git.aatufutaa.lobby.net.packet.hello;

import git.aatufutaa.lobby.party.player.PlayerJoinPartyOutgoingPacket;
import git.aatufutaa.lobby.session.LobbyData;
import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.Setter;

import java.util.List;

@Setter
public class LobbyDataOutgoingPacket implements OutgoingPacket {

    private LobbyData lobbyData;

    private boolean inQueue;

    private boolean inParty;
    private String partyCode;
    private String partyLeaderId;
    private List<PlayerJoinPartyOutgoingPacket.PartyMember> members;

    private int location;

    @Override
    public void write(ByteBuf buf) {
        this.lobbyData.write(buf);

        buf.writeBoolean(this.inQueue);

        buf.writeBoolean(this.inParty);
        if (this.inParty) {
            ByteBufUtil.writeString(this.partyCode, buf);
            ByteBufUtil.writeString(this.partyLeaderId, buf);
            buf.writeByte(this.members.size());
            for (PlayerJoinPartyOutgoingPacket.PartyMember member : this.members) {
                member.write(buf);
            }
        }

        buf.writeByte(this.location);
    }
}
