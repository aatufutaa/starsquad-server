package git.aatufutaa.game.net.packet.hello;

import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HelloOutgoingPacket implements OutgoingPacket {

    public enum HelloResponse {
        OK,
        FAIL
    }

    private HelloResponse response;

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(this.response.ordinal());
    }
}
