package git.aatufutaa.master.party;

import git.aatufutaa.master.MasterServer;
import git.aatufutaa.master.communication.packet.IncomingPacket;
import git.aatufutaa.master.server.lobby.LobbyServer;
import io.netty.buffer.ByteBuf;

public class LeavePartyIncomingPacket implements IncomingPacket<LobbyServer> {

    private int playerId;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = buf.readInt();
    }

    @Override
    public void handle(LobbyServer server) {
        MasterServer.getInstance().getSessionManager().getSession(this.playerId, (session) -> {

            if (session == null) {
                MasterServer.warn("cant find session for " + this.playerId + " in party leave");
                server.sendPacket(new LeavePartyOutgoingPacket(this.playerId, LeavePartyOutgoingPacket.PartyLeaveResponse.FAILED));
                return;
            }

            Party oldParty = session.getParty();

            if (oldParty == null) {
                MasterServer.warn(session + " tried to leave party while not in party");
                server.sendPacket(new LeavePartyOutgoingPacket(this.playerId, LeavePartyOutgoingPacket.PartyLeaveResponse.NOT_IN_PARTY));
                return;
            }

            oldParty.lock(() -> {
                oldParty.leave(session, server, true);
            });
        });
    }
}
