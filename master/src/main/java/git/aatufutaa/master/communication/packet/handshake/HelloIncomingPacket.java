package git.aatufutaa.master.communication.packet.handshake;

import git.aatufutaa.master.communication.ByteBufUtil;
import git.aatufutaa.master.communication.packet.IncomingPacket;
import git.aatufutaa.master.server.Server;
import git.aatufutaa.master.server.ServerLocation;
import git.aatufutaa.master.server.ServerType;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

@Getter
public class HelloIncomingPacket implements IncomingPacket<Server> {

    private ServerType serverType;
    private ServerLocation location;

    private String host;
    private int port;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.serverType = ServerType.values()[buf.readByte()];
        this.location = ServerLocation.values()[buf.readByte()];
        if (this.serverType != ServerType.LOGIN) {
            this.host = ByteBufUtil.readString(buf, 20);
            this.port = buf.readInt();
        }
    }

    @Override
    public void handle(Server server) {
    }
}
