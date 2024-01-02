package git.aatufutaa.master.party;

import git.aatufutaa.master.MasterServer;
import git.aatufutaa.master.communication.packet.IncomingPacket;
import git.aatufutaa.master.server.lobby.LobbyServer;
import git.aatufutaa.master.session.Session;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public class JoinPartyIncomingPacket implements IncomingPacket<LobbyServer> {

    private int playerId;
    private int partyId;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = buf.readInt();
        this.partyId = buf.readInt();
    }

    @Override
    public void handle(LobbyServer server) {
        MasterServer.getInstance().getSessionManager().getSession(this.playerId, (session) -> {

            if (session == null) {
                MasterServer.warn("cant find session for " + this.playerId + " in party join");
                server.sendPacket(new JoinPartyOutgoingPacket(this.playerId, JoinPartyOutgoingPacket.PartyJoinResponse.FAILED));
                return;
            }

            Party oldParty = session.getParty();

            if (oldParty != null) {
                MasterServer.warn(session + " tried to join party while already in party");
                server.sendPacket(new JoinPartyOutgoingPacket(this.playerId, JoinPartyOutgoingPacket.PartyJoinResponse.FAILED));
                return;
            }

            if (session.getQueueData() != null) {
                MasterServer.warn(session + " tried to join party in queue");
                server.sendPacket(new JoinPartyOutgoingPacket(this.playerId, JoinPartyOutgoingPacket.PartyJoinResponse.FAILED));
                return;
            }

            // make sure server didnt change for example game started
            if (session.getServer() != server) {
                MasterServer.warn(session + " server changed when join party");
                server.sendPacket(new JoinPartyOutgoingPacket(this.playerId, JoinPartyOutgoingPacket.PartyJoinResponse.FAILED));
                return;
            }

            // find party id
            MasterServer.getInstance().getPartyManager().getParty(this.partyId, party -> {

                if (party == null) {
                    MasterServer.warn("cant find party when join " + this.partyId + " " + session);
                    server.sendPacket(new JoinPartyOutgoingPacket(this.playerId, JoinPartyOutgoingPacket.PartyJoinResponse.PARTY_NOT_FOUND));
                    return;
                }

                MasterServer.getInstance().getSessionManager().runOnSessionThread(this.playerId, (callback) -> {

                    if (session.getParty() != null || session.getQueueData() != null || session.getServer() != server) {
                        MasterServer.warn("went BREAK " + this.partyId + " " + session);
                        server.sendPacket(new JoinPartyOutgoingPacket(this.playerId, JoinPartyOutgoingPacket.PartyJoinResponse.FAILED));
                        return;
                    }

                    party.lock(() -> {
                        if (party.join(session)) {
                            List<JoinPartyOutgoingPacket.PartyMember> members = new ArrayList<>(party.getMembers().size());
                            for (Session member : party.getMembers()) {
                                if (member.getPlayerId() == session.getPlayerId()) continue; // dont add the player
                                members.add(new JoinPartyOutgoingPacket.PartyMember(member));
                            }
                            server.sendPacket(new JoinPartyOutgoingPacket(this.playerId,
                                    JoinPartyOutgoingPacket.PartyJoinResponse.OK,
                                    party.getPartyId(),
                                    party.getLeader().getPlayerId(),
                                    members
                            ));
                            session.setParty(party);
                        } else {
                            server.sendPacket(new JoinPartyOutgoingPacket(this.playerId, JoinPartyOutgoingPacket.PartyJoinResponse.FAILED));
                        }
                    });
                });
            });
        });
    }
}
