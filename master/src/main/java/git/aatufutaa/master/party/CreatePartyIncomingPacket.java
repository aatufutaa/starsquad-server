package git.aatufutaa.master.party;

import git.aatufutaa.master.MasterServer;
import git.aatufutaa.master.communication.packet.IncomingPacket;
import git.aatufutaa.master.server.lobby.LobbyServer;
import io.netty.buffer.ByteBuf;

public class CreatePartyIncomingPacket implements IncomingPacket<LobbyServer> {

    private int playerId;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = buf.readInt();
    }

    @Override
    public void handle(LobbyServer server) {
        MasterServer.getInstance().getSessionManager().getSession(this.playerId, (session) -> {

            if (session == null) {
                MasterServer.warn("cant find session for " + this.playerId + " in party create");
                server.sendPacket(new CreatePartyOutgoingPacket(this.playerId, CreatePartyOutgoingPacket.PartyCreateResponse.FAILED, 0));
                return;
            }

            Party oldParty = session.getParty();

            if (oldParty != null) {
                MasterServer.warn(session + " tried to create party while already in party");
                server.sendPacket(new CreatePartyOutgoingPacket(this.playerId, CreatePartyOutgoingPacket.PartyCreateResponse.FAILED, 0));
                return;
            }

            if (session.getQueueData() != null) {
                MasterServer.warn(session + " tried to create party in queue");
                server.sendPacket(new CreatePartyOutgoingPacket(this.playerId, CreatePartyOutgoingPacket.PartyCreateResponse.FAILED, 0));
                return;
            }

            // make sure server didnt change for example game started
            if (session.getServer() != server) {
                MasterServer.warn(session + " server changed when creating party");
                server.sendPacket(new CreatePartyOutgoingPacket(this.playerId, CreatePartyOutgoingPacket.PartyCreateResponse.FAILED, 0));
                return;
            }

            MasterServer.getInstance().getPartyManager().createParty(session, party -> {

                MasterServer.getInstance().getSessionManager().runOnSessionThread(this.playerId, (callback) -> {

                    if (session.getParty() != null || session.getQueueData() != null || session.getServer() != server) {
                        MasterServer.warn(session + " went in BREAK while creating one");
                        MasterServer.getInstance().getPartyManager().destroyParty(party);
                        server.sendPacket(new CreatePartyOutgoingPacket(this.playerId, CreatePartyOutgoingPacket.PartyCreateResponse.FAILED, 0));
                        return;
                    }

                    party.lock(() -> {
                        session.setParty(party);

                        server.sendPacket(new CreatePartyOutgoingPacket(this.playerId, CreatePartyOutgoingPacket.PartyCreateResponse.OK, party.getPartyId()));
                    });
                });
            });
        });
    }
}
