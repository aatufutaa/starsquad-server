package git.aatufutaa.game.net.packet.hello;

import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StartUdpOutgoingPacket implements OutgoingPacket {

    private short udpId;

    @Override
    public void write(ByteBuf buf) {
        buf.writeShortLE(this.udpId);
    }
}
