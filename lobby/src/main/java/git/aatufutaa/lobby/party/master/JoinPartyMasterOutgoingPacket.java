package git.aatufutaa.lobby.party.master;

import git.aatufutaa.server.communication.packet.MasterOutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JoinPartyMasterOutgoingPacket implements MasterOutgoingPacket {

    private final int playerId;
    private final int partyId;

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.partyId);
    }
}
