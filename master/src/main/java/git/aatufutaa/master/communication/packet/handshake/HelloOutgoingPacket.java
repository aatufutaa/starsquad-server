package git.aatufutaa.master.communication.packet.handshake;

import git.aatufutaa.master.communication.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;

public class HelloOutgoingPacket implements OutgoingPacket {
    @Override
    public void write(ByteBuf buf) throws Exception {
    }
}
