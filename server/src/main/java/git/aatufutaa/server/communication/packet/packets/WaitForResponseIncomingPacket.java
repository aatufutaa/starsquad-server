package git.aatufutaa.server.communication.packet.packets;

import git.aatufutaa.server.communication.MasterConnection;
import git.aatufutaa.server.communication.packet.MasterIncomingPacket;
import io.netty.buffer.ByteBuf;

public class WaitForResponseIncomingPacket implements MasterIncomingPacket {

    private int responseId;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.responseId = buf.readInt();
    }

    @Override
    public void handle() {
    }

    public void handle(MasterConnection connection) {
        connection.handleRequest(this.responseId, this);
    }
}
