package git.aatufutaa.server.communication.packet;

import io.netty.buffer.ByteBuf;

public interface MasterOutgoingPacket {

    void write(ByteBuf buf);
}
