package git.aatufutaa.lobby.party.master;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.mongo.PlayerId;
import git.aatufutaa.lobby.party.Party;
import git.aatufutaa.lobby.party.player.JoinPartyOutgoingPacket;
import git.aatufutaa.lobby.party.player.PlayerJoinPartyOutgoingPacket;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.communication.packet.MasterIncomingPacket;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class JoinPartyMasterIncomingPacket implements MasterIncomingPacket {

    private int playerId;
    private JoinPartyOutgoingPacket.PartyJoinResponse response;
    private int partyId;
    private int leaderId;
    private List<MasterPartyMember> members;
    
    @Getter
    public static class MasterPartyMember {
        private int playerId;
        private String name;

        private int trophies;

        private int hero;
        private int heroTrophies;

        public void read(ByteBuf buf) throws Exception {
            this.playerId = buf.readInt();
            this.name = ByteBufUtil.readString(buf, 16);

            this.trophies = buf.readInt();

            this.hero = buf.readByte();
            this.heroTrophies = buf.readShort();
        }

        public PlayerJoinPartyOutgoingPacket.PartyMember toPartyMember() {
            String playerId = PlayerId.convertIdToHash(this.playerId);
            PlayerJoinPartyOutgoingPacket.PartyMember member = new PlayerJoinPartyOutgoingPacket.PartyMember(
                    playerId,
                    this.name,

                    this.trophies,

                    this.hero,
                    this.heroTrophies
            );
            return member;
        }
    }

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = buf.readInt();
        this.response = JoinPartyOutgoingPacket.PartyJoinResponse.values()[buf.readByte()];
        if (this.response == JoinPartyOutgoingPacket.PartyJoinResponse.OK) {
            this.partyId = buf.readInt();

            this.leaderId = buf.readInt();

            int count = buf.readByte();
            this.members = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                MasterPartyMember member = new MasterPartyMember();
                member.read(buf);
                this.members.add(member);
            }
        }
    }

    @Override
    public void handle() {
        LobbyServer.getInstance().runOnMainThread(() -> {

            Session session = LobbyServer.getInstance().getSessionManager().getSession(this.playerId);

            if (session == null) {
                LobbyServer.warn("cant find session in party join response " + this.playerId + " " + this.response);
                return;
            }

            String partyCode = null;
            String leaderId = null;
            List<PlayerJoinPartyOutgoingPacket.PartyMember> members = null;

            if (this.response == JoinPartyOutgoingPacket.PartyJoinResponse.OK) {
                if (session.getParty() != null) {
                    LobbyServer.warn("player joined party while already in party ??? " + this.playerId + " " + this.response);
                }

                Party party = new Party(this.partyId, this.leaderId);

                for (MasterPartyMember member : this.members)
                    party.join(member);

                session.setParty(party);

                partyCode = party.getPartyCode();
                leaderId = PlayerId.convertIdToHash(party.getLeaderId());

                members = new ArrayList<>(this.members.size());
                for (MasterPartyMember member : this.members) {
                    members.add(member.toPartyMember());
                }
            }

            session.sendConfirmPacket(new JoinPartyOutgoingPacket(this.response, partyCode, leaderId, members));
        });
    }
}
