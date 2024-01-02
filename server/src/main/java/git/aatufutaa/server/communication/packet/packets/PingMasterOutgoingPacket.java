package git.aatufutaa.server.communication.packet.packets;

import git.aatufutaa.server.communication.packet.MasterOutgoingPacket;
import io.netty.buffer.ByteBuf;

public class PingMasterOutgoingPacket implements MasterOutgoingPacket {
    @Override
    public void write(ByteBuf buf) {
    }
}
