package git.aatufutaa.game.net.packet.hello;

import git.aatufutaa.server.ServerType;
import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SendToServerOutgoingPacket implements OutgoingPacket {

    private final ServerType serverType;
    private final String host;
    private final int port;

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(this.serverType.ordinal());
        ByteBufUtil.writeString(this.host, buf);
        buf.writeShortLE(this.port);
    }
}
