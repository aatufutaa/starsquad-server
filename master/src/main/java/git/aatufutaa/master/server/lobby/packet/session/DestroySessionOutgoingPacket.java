package git.aatufutaa.master.server.lobby.packet.session;

import git.aatufutaa.master.communication.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DestroySessionOutgoingPacket implements OutgoingPacket {

    private int playerId;

    @Override
    public void write(ByteBuf buf) throws Exception {
        buf.writeInt(this.playerId);
    }
}
