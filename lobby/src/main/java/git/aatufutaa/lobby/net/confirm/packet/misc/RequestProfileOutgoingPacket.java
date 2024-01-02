package git.aatufutaa.lobby.net.confirm.packet.misc;

import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RequestProfileOutgoingPacket implements OutgoingPacket {

    private final String name;

    private final int rating;

    @Override
    public void write(ByteBuf buf) {
        ByteBufUtil.writeString(this.name, buf);

        buf.writeIntLE(this.rating);
    }
}
