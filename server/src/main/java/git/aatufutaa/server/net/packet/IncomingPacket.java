package git.aatufutaa.server.net.packet;

import git.aatufutaa.server.net.client.ClientBase;
import io.netty.buffer.ByteBuf;

public interface IncomingPacket {

    void read(ByteBuf buf) throws Exception;

    void handle(ClientBase client) throws Exception;
}
