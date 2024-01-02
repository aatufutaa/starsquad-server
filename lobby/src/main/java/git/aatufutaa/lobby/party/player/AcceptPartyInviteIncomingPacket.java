package git.aatufutaa.lobby.party.player;

import git.aatufutaa.lobby.mongo.PlayerId;
import git.aatufutaa.lobby.net.packet.LobbyPacket;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.communication.ByteBufUtil;
import io.netty.buffer.ByteBuf;

public class AcceptPartyInviteIncomingPacket extends LobbyPacket {

    private String playerId;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = ByteBufUtil.readString(buf, 9);
    }

    @Override
    protected void handle0(Session session) throws Exception {

        int playerId;
        try {
            playerId = PlayerId.parsePlayerId(this.playerId);
        } catch (Exception e) {
            return;
        }



    }


}
