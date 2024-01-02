package git.aatufutaa.master.party;

import git.aatufutaa.master.communication.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CreatePartyOutgoingPacket implements OutgoingPacket {

    private final int playerId;
    private final PartyCreateResponse response;
    private final int partyId;

    public enum PartyCreateResponse {
        OK,
        FAILED
    }

    @Override
    public void write(ByteBuf buf) throws Exception {
        buf.writeInt(this.playerId);
        buf.writeByte(this.response.ordinal());
        if (this.response == PartyCreateResponse.OK) {
            buf.writeInt(this.partyId);
        }
    }
}
