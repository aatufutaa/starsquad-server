package git.aatufutaa.server.net.packet;

import io.netty.buffer.ByteBuf;

public interface OutgoingPacket {

    void write(ByteBuf buf);
}
