package git.aatufutaa.lobby.party.player;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.net.packet.LobbyPacket;
import git.aatufutaa.lobby.party.master.CreatePartyMasterOutgoingPacket;
import git.aatufutaa.lobby.session.Session;
import io.netty.buffer.ByteBuf;

public class CreatePartyIncomingPacket extends LobbyPacket {

    @Override
    public void read(ByteBuf buf) throws Exception {
    }


    @Override
    protected void handle0(Session session) {
        if (session.getParty() != null) {
            LobbyServer.warn(session + " tried to create party while arleady in it");
            session.sendConfirmPacket(new CreatePartyOutgoingPacket(CreatePartyOutgoingPacket.CreatePartyResponse.FAILED, null));
            return;
        }

        // TODO: check if in queue
        if (false) {
            session.sendConfirmPacket(new CreatePartyOutgoingPacket(CreatePartyOutgoingPacket.CreatePartyResponse.FAILED, null));
        }

        // TODO: make sure client doesnt spam master

        LobbyServer.getInstance().getMasterConnection().sendPacket(new CreatePartyMasterOutgoingPacket(session.getPlayerId()));
    }
}
