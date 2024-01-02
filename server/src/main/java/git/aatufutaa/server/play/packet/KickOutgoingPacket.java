package git.aatufutaa.server.play.packet;

import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class KickOutgoingPacket implements OutgoingPacket {

    private String msg;

    @Override
    public void write(ByteBuf buf) {
        ByteBufUtil.writeString(this.msg, buf);
    }
}
