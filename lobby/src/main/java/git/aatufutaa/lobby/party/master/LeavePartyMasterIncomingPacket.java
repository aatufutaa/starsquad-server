package git.aatufutaa.lobby.party.master;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.party.player.LeavePartyOutgoingPacket;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.communication.packet.MasterIncomingPacket;
import io.netty.buffer.ByteBuf;

public class LeavePartyMasterIncomingPacket implements MasterIncomingPacket {

    private int playerId;
    private LeavePartyOutgoingPacket.PartyLeaveResponse response;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = buf.readInt();
        this.response = LeavePartyOutgoingPacket.PartyLeaveResponse.values()[buf.readByte()];
    }

    @Override
    public void handle() {
        LobbyServer.getInstance().runOnMainThread(() -> {

            Session session = LobbyServer.getInstance().getSessionManager().getSession(this.playerId);

            if (session == null) {
                LobbyServer.warn("cant find session in party leave response " + this.playerId + " " + this.response);
                return;
            }

            session.sendConfirmPacket(new LeavePartyOutgoingPacket(this.response));

            if (session.getParty() == null) {
                LobbyServer.warn("cant find party when leave response " + this.playerId + " " + this.response);
                return;
            }

            session.setParty(null);
        });
    }
}
