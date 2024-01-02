package git.aatufutaa.lobby.party.master;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.party.Party;
import git.aatufutaa.lobby.party.player.CreatePartyOutgoingPacket;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.communication.packet.MasterIncomingPacket;
import io.netty.buffer.ByteBuf;

public class CreatePartyMasterIncomingPacket implements MasterIncomingPacket {

    private int playerId;
    private CreatePartyOutgoingPacket.CreatePartyResponse response;
    private int partyId;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = buf.readInt();
        this.response = CreatePartyOutgoingPacket.CreatePartyResponse.values()[buf.readByte()];
        if (this.response == CreatePartyOutgoingPacket.CreatePartyResponse.OK) {
            this.partyId = buf.readInt();
        }
    }

    @Override
    public void handle() {
        LobbyServer.getInstance().runOnMainThread(() -> {

            Session session = LobbyServer.getInstance().getSessionManager().getSession(this.playerId);

            if (session == null) {
                LobbyServer.warn("cant find session on party create response " + this.playerId + " response " + this.response);
                return;
            }

            String partyCode = null;

            if (this.response != CreatePartyOutgoingPacket.CreatePartyResponse.OK) {
                LobbyServer.warn("failed to create party for " + this.playerId + " " + this.response);
            } else {

                Party party = new Party(this.partyId, session.getPlayerId());
                session.setParty(party);

                partyCode = party.getPartyCode();

                LobbyServer.log("party created successfully for " + session);
            }

            session.sendConfirmPacket(new CreatePartyOutgoingPacket(this.response, partyCode));
        });
    }

}
