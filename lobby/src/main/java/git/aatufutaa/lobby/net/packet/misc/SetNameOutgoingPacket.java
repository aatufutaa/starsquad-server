package git.aatufutaa.lobby.net.packet.misc;

import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SetNameOutgoingPacket implements OutgoingPacket {

    public enum NameResponse {
        OK,
        NAME_CHANGED,
        NOT_ALLOWED,
        INVALID_CHARACTERS,
        INVALID_OTHER,
        INSERT_FAILED
    }

    private final NameResponse response;
    private final String name;

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(this.response.ordinal());
        if (this.response == NameResponse.OK || this.response == NameResponse.NAME_CHANGED) {
            ByteBufUtil.writeString(this.name, buf);
        }
    }
}
