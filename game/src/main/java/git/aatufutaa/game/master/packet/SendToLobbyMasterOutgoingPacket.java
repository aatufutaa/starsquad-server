package git.aatufutaa.game.master.packet;

import git.aatufutaa.server.communication.packet.MasterOutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SendToLobbyMasterOutgoingPacket implements MasterOutgoingPacket {

    private int playerId;

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(this.playerId);
    }
}
