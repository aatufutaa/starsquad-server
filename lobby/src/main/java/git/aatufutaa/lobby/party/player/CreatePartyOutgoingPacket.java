package git.aatufutaa.lobby.party.player;

import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CreatePartyOutgoingPacket implements OutgoingPacket {

    public enum CreatePartyResponse {
        OK,
        FAILED
    }

    private final CreatePartyResponse response;
    private final String partyCode;

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(this.response.ordinal());
        if (this.response == CreatePartyResponse.OK) {
            ByteBufUtil.writeString(this.partyCode, buf);
        }
    }
}
