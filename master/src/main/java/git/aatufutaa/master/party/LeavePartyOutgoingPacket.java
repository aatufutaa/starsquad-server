package git.aatufutaa.master.party;

import git.aatufutaa.master.communication.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LeavePartyOutgoingPacket implements OutgoingPacket {

    public enum PartyLeaveResponse {
        OK,
        DISBAND,
        NOT_IN_PARTY,
        CANT_LEAVE,
        FAILED
    }

    private final int playerId;
    private final PartyLeaveResponse response;

    @Override
    public void write(ByteBuf buf) throws Exception {
        buf.writeInt(this.playerId);
        buf.writeByte(this.response.ordinal());
    }
}
