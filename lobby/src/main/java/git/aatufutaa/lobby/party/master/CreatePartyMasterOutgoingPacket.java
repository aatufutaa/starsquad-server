package git.aatufutaa.lobby.party.master;

import git.aatufutaa.server.communication.packet.MasterOutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CreatePartyMasterOutgoingPacket implements MasterOutgoingPacket {

    private final int playerId;

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(this.playerId);
    }
}
