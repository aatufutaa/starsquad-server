package git.aatufutaa.lobby.net.packet.hello;

import git.aatufutaa.server.ServerType;
import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SendToServerOutgoingPacket implements OutgoingPacket {

    private ServerType serverType;
    private String host;
    private int port;

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(this.serverType.ordinal());
        ByteBufUtil.writeString(this.host, buf);
        buf.writeShortLE(this.port);
    }
}
