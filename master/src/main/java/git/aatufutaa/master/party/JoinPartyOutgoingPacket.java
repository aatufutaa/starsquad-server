package git.aatufutaa.master.party;

import git.aatufutaa.master.communication.ByteBufUtil;
import git.aatufutaa.master.communication.packet.OutgoingPacket;
import git.aatufutaa.master.session.Session;
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

    @AllArgsConstructor
    public static class PartyMember {
        private int playerId;
        private String name;

        private int trophies;

        private int hero;
        private int heroTrophies;

        public PartyMember(Session session) {
            this.playerId = session.getPlayerId();
            this.name = "name"; // TODO

            this.trophies = 0; // TODO:

            this.hero = 0; // TODO:
            this.heroTrophies = 0; // TODO
        }

        public void write(ByteBuf buf) {
            buf.writeInt(this.playerId);
            ByteBufUtil.writeString(this.name, buf);

            buf.writeInt(this.trophies);

            buf.writeByte(this.hero);
            buf.writeShort(this.heroTrophies);
        }
    }

    private final int playerId;
    private final PartyJoinResponse response;
    private final int partyId;
    private final int leaderId;
    private List<PartyMember> members;

    public JoinPartyOutgoingPacket(int playerId, PartyJoinResponse response) {
        this(playerId, response, 0, 0, null);
    }

    @Override
    public void write(ByteBuf buf) throws Exception {
        buf.writeInt(this.playerId);
        buf.writeByte(this.response.ordinal());
        if (this.response == PartyJoinResponse.OK) {
            buf.writeInt(this.partyId);

            buf.writeInt(this.leaderId);

            buf.writeByte(this.members.size());
            for (PartyMember member : this.members) {
                member.write(buf);
            }
        }
    }
}
