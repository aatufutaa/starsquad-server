package git.aatufutaa.server.communication.packet;

import io.netty.buffer.ByteBuf;

public interface MasterIncomingPacket {

    void read(ByteBuf buf) throws Exception;
    void handle();

}
