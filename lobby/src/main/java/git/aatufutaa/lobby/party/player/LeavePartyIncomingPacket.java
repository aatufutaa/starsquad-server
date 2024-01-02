package git.aatufutaa.lobby.party.player;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.net.packet.LobbyPacket;
import git.aatufutaa.lobby.party.master.LeavePartyMasterOutgoingPacket;
import git.aatufutaa.lobby.session.Session;
import io.netty.buffer.ByteBuf;

public class LeavePartyIncomingPacket extends LobbyPacket {

    @Override
    public void read(ByteBuf buf) throws Exception {
    }

    @Override
    protected void handle0(Session session) {
        // TODO: check if in queue
        if (false) {
            LobbyServer.warn(session + " tried to leave party while in queue");
            session.sendConfirmPacket(new LeavePartyOutgoingPacket(LeavePartyOutgoingPacket.PartyLeaveResponse.CANT_LEAVE));
            return;
        }

        if (session.getParty() == null) {
            LobbyServer.warn(session + " tried to leave party while not in party");
            session.sendConfirmPacket(new LeavePartyOutgoingPacket(LeavePartyOutgoingPacket.PartyLeaveResponse.NOT_IN_PARTY));
            return;
        }

        // TODO: make sure player doesnt spam master

        LobbyServer.getInstance().getMasterConnection().sendPacket(new LeavePartyMasterOutgoingPacket(session.getPlayerId()));
    }
}
