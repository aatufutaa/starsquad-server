package git.aatufutaa.master.communication.packet;

import git.aatufutaa.master.server.Server;
import io.netty.buffer.ByteBuf;

public interface IncomingPacket<T extends Server> {

    void read(ByteBuf buf) throws Exception;
    void handle(T server);

}
