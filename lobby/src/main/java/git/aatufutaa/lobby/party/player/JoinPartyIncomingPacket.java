package git.aatufutaa.lobby.party.player;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.net.packet.LobbyPacket;
import git.aatufutaa.lobby.party.Party;
import git.aatufutaa.lobby.party.master.JoinPartyMasterOutgoingPacket;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.communication.ByteBufUtil;
import io.netty.buffer.ByteBuf;

public class JoinPartyIncomingPacket extends LobbyPacket {

    private String partyCode;

    @Override
    public void read(ByteBuf buf) throws Exception {
        System.out.println("read join party");
        this.partyCode = ByteBufUtil.readString(buf, 9); // 1 + 8
    }

    @Override
    protected void handle0(Session session) {
        if (session.getParty() != null) {
            LobbyServer.warn(session + " tried to join party while in it");
            session.sendConfirmPacket(new JoinPartyOutgoingPacket(JoinPartyOutgoingPacket.PartyJoinResponse.FAILED));
            return;
        }

        // TODO: rate limit join request

        // get party id form hash
        String partyCode = this.partyCode;
        if (partyCode.startsWith("#"))
            partyCode = partyCode.substring(1); // if player added # in begin remove it

        int partyId;
        try {
            partyId = Party.getPartyId(partyCode);
        } catch (Exception e) {
            LobbyServer.warn("failed to parsae party code " + partyCode);
            session.sendConfirmPacket(new JoinPartyOutgoingPacket(JoinPartyOutgoingPacket.PartyJoinResponse.PARTY_NOT_FOUND));
            return;
        }

        // TODO: make sure player doesnt spam master

        LobbyServer.getInstance().getMasterConnection().sendPacket(new JoinPartyMasterOutgoingPacket(session.getPlayerId(), partyId));
    }
}
