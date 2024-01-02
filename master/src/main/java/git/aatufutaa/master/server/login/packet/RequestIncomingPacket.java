package git.aatufutaa.master.server.login.packet;

import git.aatufutaa.master.communication.packet.IncomingPacket;
import git.aatufutaa.master.server.Server;
import io.netty.buffer.ByteBuf;

public abstract class RequestIncomingPacket<T extends Server> implements IncomingPacket<T> {

    protected int requestId;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.requestId = buf.readInt();
    }
}
