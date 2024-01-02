package git.aatufutaa.server.communication.packet.packets;

import git.aatufutaa.server.communication.packet.MasterIncomingPacket;
import io.netty.buffer.ByteBuf;

public class HelloMasterIncomingPacket implements MasterIncomingPacket {
    @Override
    public void read(ByteBuf buf) throws Exception {
    }

    @Override
    public void handle() {
    }
}
