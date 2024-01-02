package git.aatufutaa.lobby.net.confirm.packet.common;

import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LinkGameCenterOutgoingPacket implements OutgoingPacket {

    private final String name;
    private final String tag;
    private final String token;

    @Override
    public void write(ByteBuf buf) {
        ByteBufUtil.writeString(this.name, buf);
        ByteBufUtil.writeString(this.tag, buf);
        ByteBufUtil.writeString(this.token, buf);
    }
}
