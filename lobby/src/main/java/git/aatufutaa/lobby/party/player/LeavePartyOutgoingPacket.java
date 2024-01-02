package git.aatufutaa.lobby.party.player;

import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LeavePartyOutgoingPacket implements OutgoingPacket {

    public enum PartyLeaveResponse {
        OK,
        DISBAND,
        NOT_IN_PARTY,
        CANT_LEAVE
    }

    private final PartyLeaveResponse response;

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(this.response.ordinal());
    }
}
