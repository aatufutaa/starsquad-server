package git.aatufutaa.master.server.game.packet;

import git.aatufutaa.master.communication.ByteBufUtil;
import git.aatufutaa.master.communication.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HomeOutgoingPacket implements OutgoingPacket {

    private final int playerId;
    private final String host;
    private final int port;

    @Override
    public void write(ByteBuf buf) throws Exception {
        buf.writeInt(this.playerId);
        ByteBufUtil.writeString(this.host, buf);
        buf.writeInt(this.port);
    }
}
