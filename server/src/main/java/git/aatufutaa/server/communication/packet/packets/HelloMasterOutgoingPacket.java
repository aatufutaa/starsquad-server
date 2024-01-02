package git.aatufutaa.server.communication.packet.packets;

import git.aatufutaa.server.ServerLocation;
import git.aatufutaa.server.ServerType;
import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.communication.packet.MasterOutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HelloMasterOutgoingPacket implements MasterOutgoingPacket {

    private ServerType serverType;
    private ServerLocation location;
    private String host;
    private int port;

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(this.serverType.ordinal());
        buf.writeByte(this.location.ordinal());
        if (this.serverType != ServerType.LOGIN) {
            ByteBufUtil.writeString(this.host, buf);
            buf.writeInt(this.port);
        }
    }
}
