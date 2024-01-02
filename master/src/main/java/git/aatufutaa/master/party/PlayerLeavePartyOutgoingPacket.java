package git.aatufutaa.master.party;

import git.aatufutaa.master.communication.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PlayerLeavePartyOutgoingPacket implements OutgoingPacket {

    private final int playerId;

    private final int memberId;

    @Override
    public void write(ByteBuf buf) throws Exception {
        buf.writeInt(this.playerId);

        buf.writeInt(this.memberId);
    }
}
