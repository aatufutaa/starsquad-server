package git.aatufutaa.master.communication.packet;

import io.netty.buffer.ByteBuf;

public interface OutgoingPacket {

    void write(ByteBuf buf) throws Exception;

}
