package git.aatufutaa.master.server.lobby.packet.game;

import git.aatufutaa.master.communication.ByteBufUtil;
import git.aatufutaa.master.communication.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SendToGameOutgoingPacket implements OutgoingPacket {

    public enum SendType {
        OK,
        CANT_FIND_SERVER,
        FAILED
    }

    private final SendType sendType;
    private final int playerId;
    private final String host;
    private final int port;

    public SendToGameOutgoingPacket(SendType sendType, int playerId) {
        this(sendType, playerId, null, 0);
    }

    @Override
    public void write(ByteBuf buf) throws Exception {
        buf.writeByte(this.sendType.ordinal());
        buf.writeInt(this.playerId);
        if (this.sendType == SendType.OK) {
            ByteBufUtil.writeString(this.host, buf);
            buf.writeInt(this.port);
        }
    }
}
