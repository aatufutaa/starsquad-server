package git.aatufutaa.lobby.party.master;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.mongo.PlayerId;
import git.aatufutaa.lobby.party.Party;
import git.aatufutaa.lobby.party.player.PlayerLeavePartyOutgoingPacket;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.communication.packet.MasterIncomingPacket;
import io.netty.buffer.ByteBuf;

public class PlayerLeavePartyMasterIncomingPacket implements MasterIncomingPacket {

    private int playerId;

    private int memberId;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = buf.readInt();
        this.memberId = buf.readInt();
    }

    @Override
    public void handle() {
        LobbyServer.getInstance().runOnMainThread(() -> {
            Session session = LobbyServer.getInstance().getSessionManager().getSession(this.playerId);

            if (session == null) {
                LobbyServer.warn("cant find session on party player leave response for " + this.playerId);
                return;
            }

            Party party = session.getParty();

            if (party == null) {
                LobbyServer.warn("player left party but party was not found " + session);
                return;
            }

            if (party.leave(this.memberId)) {
                session.sendConfirmPacket(new PlayerLeavePartyOutgoingPacket(PlayerId.convertIdToHash(this.memberId)));
            }
        });
    }
}
