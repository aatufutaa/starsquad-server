package git.aatufutaa.lobby.party.master;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.party.Party;
import git.aatufutaa.lobby.party.player.PlayerJoinPartyOutgoingPacket;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.communication.packet.MasterIncomingPacket;
import io.netty.buffer.ByteBuf;

public class PlayerJoinPartyMasterIncomingPacket implements MasterIncomingPacket {

    private int playerId;

    private JoinPartyMasterIncomingPacket.MasterPartyMember partyMember;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = buf.readInt();

        this.partyMember = new JoinPartyMasterIncomingPacket.MasterPartyMember();
        this.partyMember.read(buf);
    }

    @Override
    public void handle() {
        LobbyServer.getInstance().runOnMainThread(() -> {
            Session session = LobbyServer.getInstance().getSessionManager().getSession(this.playerId);

            if (session == null) {
                LobbyServer.warn("cant find session on party player join response for " + this.playerId);
                return;
            }

            Party party = session.getParty();

            if (party == null) {
                LobbyServer.warn("player joined party but party was not found " + session);
                return;
            }

            party.join(this.partyMember);

            session.sendConfirmPacket(new PlayerJoinPartyOutgoingPacket(this.partyMember.toPartyMember()));
        });
    }
}
