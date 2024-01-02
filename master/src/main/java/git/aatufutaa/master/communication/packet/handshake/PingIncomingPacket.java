package git.aatufutaa.master.communication.packet.handshake;

import git.aatufutaa.master.communication.packet.IncomingPacket;
import git.aatufutaa.master.server.Server;
import io.netty.buffer.ByteBuf;

public class PingIncomingPacket implements IncomingPacket<Server> {
    @Override
    public void read(ByteBuf buf) throws Exception {
    }

    @Override
    public void handle(Server server) {
    }
}
